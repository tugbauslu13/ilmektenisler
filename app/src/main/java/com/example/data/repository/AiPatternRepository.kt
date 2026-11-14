package com.example.data.repository

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Base64
import android.util.Log
import com.example.data.model.AssemblyDiagramType
import com.example.data.model.AssemblyGuideStep
import com.example.data.model.GeneratedPattern
import com.example.data.model.PatternMaterials
import com.example.data.model.PatternPart
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.SerializationException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.ServiceDisabledException
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class NonKnittingImageException(message: String) : IllegalArgumentException(message)

data class InlineImagePayload(
    val bytes: ByteArray,
    val mimeType: String
)

class AiPatternRepository {

    private val materialsSchema = Schema.obj(
        properties = mapOf(
            "ip_turu" to Schema.string(description = "İpin dokusuna göre uygun ip türü ve kalınlığı (ör. Kadife ip, Pamuklu/koton, %100 Akrilik, Makrome)"),
            "tig_no" to Schema.string(description = "Milimetre cinsinden önerilen tığ veya şiş numarası (ör. 4.0 mm)"),
            "renkler" to Schema.array(
                items = Schema.string(),
                description = "Tahmini ana renk ve detay renk isimleri/kodları"
            ),
            "ek_malzemeler" to Schema.array(
                items = Schema.string(),
                description = "Elyaf, kilitli göz, etamin iğnesi, düğme gibi ek malzemeler"
            )
        ),
        optionalProperties = listOf("renkler", "ek_malzemeler")
    )

    private val partSchema = Schema.obj(
        properties = mapOf(
            "parca_adi" to Schema.string(description = "Parça adı (Örn: Kafa, Gövde, Kollar, Arka Panel, Motif Şeması)"),
            "adimlar" to Schema.array(
                items = Schema.string(),
                description = "Yalnızca sh, zn, x, v, a, cc, hdc, dc kısaltmalarıyla ve satır sonu toplam ilmek sayısıyla yazılmış sıra adımları"
            )
        )
    )

    private val assemblyStepSchema = Schema.obj(
        properties = mapOf(
            "baslik" to Schema.string(description = "Birleştirme veya son dokunuş adım başlığı"),
            "aciklama" to Schema.string(description = "Parçaların düzgün hizalanması, estetik dikimi, ip gizleme veya bloklama için ayrıntılı talimat"),
            "dikis_teknigi" to Schema.string(description = "Kullanılan teknik (ör. Görünmez Matras Dikişi, İlmek Kaydırma (cc), Toplu İğne Hizalama, Buharlı Bloklama)"),
            "diyagram_turu" to Schema.enumeration(
                values = listOf(
                    "MATTRESS_STITCH",
                    "AMIGURUMI_JOIN",
                    "SLIP_STITCH_JOIN",
                    "FINISHING_BLOCKING"
                ),
                description = "Bu adım için gösterilecek teknik dikiş diyagramı türü"
            )
        ),
        optionalProperties = listOf("dikis_teknigi", "diyagram_turu")
    )

    private val patternSchema = Schema.obj(
        properties = mapOf(
            "orgu_mu" to Schema.boolean(description = "Görselde gerçek veya taslak bir örgü/el işi ürünü varsa true, yoksa false"),
            "hata_mesaji" to Schema.string(
                description = "orgu_mu false ise 'Görselde bir örgü ürünü tespit edemedim, lütfen tığ veya şiş işi bir fotoğraf yükle' mesajı; orgu_mu true ise null",
                nullable = true
            ),
            "urun_adi" to Schema.string(description = "Tahmini örgü ürün adı"),
            "kategori" to Schema.string(description = "Kategori: Amigurumi, Hırka, Çanta, Şal, Bere veya Dekor"),
            "teknik" to Schema.string(description = "Kullanılan teknik: Tığ İşi (Crochet) veya Şiş Örgü (Knitting)"),
            "zorluk" to Schema.enumeration(
                values = listOf("Kolay", "Orta", "İleri Seviye"),
                description = "Zorluk seviyesi"
            ),
            "tahmini_sure" to Schema.string(description = "Tahmini örme süresi (Örn: 3-4 saat)"),
            "ai_gorsel_uyarisi" to Schema.string(
                description = "Görsel yapay zeka (AI) tarafından üretilmiş ve gerçekte örülmesi imkansız detaylar içeriyorsa uyarı metni, yoksa null",
                nullable = true
            ),
            "malzemeler" to materialsSchema,
            "kisaltmalar" to Schema.array(
                items = Schema.string(),
                description = "Tarifte kullanılan Türkçe kısaltmalar sözlüğü (ör. 'sh: Sihirli halka', 'x: Sık iğne', 'v: Artırma', 'a: Azaltma')"
            ),
            "parcalar" to Schema.array(
                items = partSchema,
                description = "Ürünün mantıklı parçalara bölünmüş örgü adımları"
            ),
            "birlestirme_rehberi" to Schema.array(
                items = assemblyStepSchema,
                description = "Ana parçalar örüldükten sonra parçaların estetik ve görünmez şekilde birleştirilmesi, ip gizleme ve bloklama adımları"
            ),
            "puf_noktasi" to Schema.string(description = "Bu modeli örerken dikkat edilmesi gereken en kritik ustalık tüyosu"),
            "atolye_notu" to Schema.string(description = "Atölye sipariş hatırlatma notu")
        ),
        optionalProperties = listOf(
            "hata_mesaji",
            "ai_gorsel_uyarisi",
            "tahmini_sure",
            "birlestirme_rehberi",
            "atolye_notu"
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = patternSchema
                temperature = 0.35f
            }
        )
    }

    private val jsonOnlyModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.35f
            }
        )
    }

    suspend fun analyzeKnittingImage(
        bitmap: Bitmap,
        extraNote: String = ""
    ): GeneratedPattern {
        val inlinePayload = withContext(Dispatchers.Default) {
            prepareInlineImagePayload(bitmap)
        }
        return analyzeInlinePayload(inlinePayload, extraNote)
    }

    suspend fun analyzeBase64Image(
        rawBase64OrDataUrl: String,
        extraNote: String = ""
    ): GeneratedPattern {
        val inlinePayload = withContext(Dispatchers.Default) {
            sanitizeBase64InlinePayload(rawBase64OrDataUrl)
        }
        return analyzeInlinePayload(inlinePayload, extraNote)
    }

    private suspend fun analyzeInlinePayload(
        payload: InlineImagePayload,
        extraNote: String
    ): GeneratedPattern {
        val promptText = buildPromptText(extraNote)

        return try {
            val response = try {
                withTimeout(PRIMARY_TIMEOUT_MS) {
                    structuredModel.generateContent(
                        content {
                            inlineData(payload.bytes, payload.mimeType)
                            text(promptText)
                        }
                    )
                }
            } catch (primaryError: Exception) {
                if (primaryError is CancellationException && primaryError !is TimeoutCancellationException) {
                    throw primaryError
                }
                if (primaryError is SerializationException ||
                    primaryError is ServerException ||
                    primaryError is TimeoutCancellationException
                ) {
                    Log.w(
                        TAG,
                        "Primary structured call failed (${primaryError::class.java.simpleName}: ${primaryError.message}); retrying with fast JSON-only mode."
                    )
                    withTimeout(FALLBACK_TIMEOUT_MS) {
                        jsonOnlyModel.generateContent(
                            content {
                                inlineData(payload.bytes, payload.mimeType)
                                text(promptText)
                            }
                        )
                    }
                } else {
                    throw primaryError
                }
            }

            val rawText = response.text
            if (rawText.isNullOrBlank()) {
                throw IllegalStateException("Yapay zeka modeli boş yanıt döndürdü.")
            }
            parsePatternJson(rawText)
        } catch (e: NonKnittingImageException) {
            throw e
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini recipe generation timed out", e)
            throw IllegalStateException(
                "Sunucu yanıt süresi aşıldı. Görsel optimize edildi; internet bağlantınızı kontrol edip tekrar deneyin.",
                e
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini recipe generation failed", e)
            throw IllegalStateException(formatReadableError(e), e)
        }
    }

    private fun buildPromptText(extraNote: String): String = buildString {
        append(
            """
            Sen "İlmekten İşler" uygulamasının baş örgü tasarımcısısın. Görevin, kullanıcının yüklediği el işi/örgü görselini analiz edip Türkiye standartlarında, uygulanabilir, anlaşılır bir taslak örgü tarifi çıkarmaktır.
            Yanıtını SADECE geçerli saf JSON nesnesi olarak döndür; markdown kod bloğu (```json) veya ekstra açıklama ekleme.

            ADIM 1: GÖRSEL DOĞRULAMA
            - Yüklenen görselde örgü veya el işi yoksa `orgu_mu` değerini `false` yap ve `hata_mesaji` alanına tam olarak şunu yaz: "Görselde bir örgü ürünü tespit edemedim, lütfen tığ veya şiş işi bir fotoğraf yükle".
            - Görsel yapay zeka (AI) tarafından üretilmiş ve gerçekte örülmesi imkansız detaylar içeriyorsa bunu `ai_gorsel_uyarisi` alanında mutlaka belirt; gerçekçi ve örülebilir bir görselse `null` bırak.

            ADIM 2: TEKNİK ANALİZ
            - Teknik: Tığ işi (Crochet) mi, Şiş örgü (Knitting) mü belirle (`teknik`).
            - İp ve Tığ Tahmini: İpin dokusuna göre (kadife ip, pamuklu/koton, akrilik, makrome vb.) en uygun ip türünü, tahmini renk isimlerini/kodlarını ve milimetre cinsinden tığ/şiş numarasını yaz.
            - Parça Ayrımı: Ürünü mantıklı parçalara böl (Örn: Amigurumi ise Kafa, Gövde, Kollar, Kulaklar; Hırka ise Arka Panel, Ön Paneller, Kollar ve Motif Şeması).

            ADIM 3: TÜRKÇE TERİM STANDARDI
            Tarif adımlarında YALNIZCA şu Türkçe kısaltmaları kullan ve `kisaltmalar` sözlüğünde ver:
            - sh: Sihirli halka
            - zn: Zincir
            - x: Sık iğne
            - v: Artırma (aynı ilmeğe 2 sık iğne)
            - a: Azaltma (2 ilmeği tek seferde toplama)
            - cc: İlmek kaydırma
            - hdc: Tekli trabzan
            - dc: İkili trabzan
            - (...)*6: Parantez içini 6 kez tekrarla. Satır sonlarında toplam ilmek sayısını parantez içinde belirt. Örn: "3. sıra: (2x, 1v)*6 = (24)"

            ADIM 4: ÇIKTI KURALLARI
            - `zorluk` sadece "Kolay", "Orta" veya "İleri Seviye" olabilir.
            - `atolye_notu` için şu metni temel al: "Bu ürünü kendin örmek zor gelirse @ilmektenisler atölyesinden senin için hazırlamamızı isteyebilirsin!"

            ADIM 5: BİRLEŞTİRME VE SON DOKUNUŞ REHBERİ
            - `birlestirme_rehberi` altında bu modele özel 3-4 adım üret.
            - Ana parçalar örüldükten sonra toplu iğne ile hizalama, parçaların etamin iğnesi veya tığ ile estetik/görünmez dikimi, ip uçlarının yürütülerek gizlenmesi ve son ütü/bloklama aşamasını açıkla.
            - `diyagram_turu` için uygun değeri seç: MATTRESS_STITCH, AMIGURUMI_JOIN, SLIP_STITCH_JOIN veya FINISHING_BLOCKING.
            """.trimIndent()
        )
        if (extraNote.isNotBlank()) {
            append("\n\nKullanıcının ek notu: $extraNote")
        }
    }

    internal fun prepareInlineImagePayload(source: Bitmap): InlineImagePayload {
        val scaled = scaleBitmapForUpload(source)
        val softwareBitmap = normalizeSoftwareArgbBitmap(scaled)
        var quality = INITIAL_JPEG_QUALITY
        var jpegBytes = compressToJpeg(softwareBitmap, quality)
        while (jpegBytes.size > MAX_INLINE_BYTES && quality > 45) {
            quality -= 15
            jpegBytes = compressToJpeg(softwareBitmap, quality)
        }
        if (softwareBitmap !== scaled && softwareBitmap !== source) {
            softwareBitmap.recycle()
        }
        if (scaled !== source) {
            scaled.recycle()
        }
        if (jpegBytes.isEmpty()) {
            throw IllegalStateException("Görsel JPEG formatına dönüştürülemedi.")
        }
        return InlineImagePayload(
            bytes = jpegBytes,
            mimeType = "image/jpeg"
        )
    }

    internal fun bitmapToCleanBase64(source: Bitmap): String {
        val payload = prepareInlineImagePayload(source)
        return Base64.encodeToString(payload.bytes, Base64.NO_WRAP)
    }

    internal fun sanitizeBase64InlinePayload(
        rawBase64OrDataUrl: String,
        defaultMimeType: String = "image/jpeg"
    ): InlineImagePayload {
        val trimmed = rawBase64OrDataUrl.trim()
        if (trimmed.isBlank()) {
            throw IllegalArgumentException("Gönderilecek görsel verisi boş.")
        }

        val dataUrlMatch = DATA_URL_REGEX.find(trimmed)
        val resolvedMimeType = dataUrlMatch?.groupValues?.getOrNull(1)
            ?.trim()
            ?.takeIf { it.startsWith("image/", ignoreCase = true) }
            ?: defaultMimeType
        val cleanBase64 = (dataUrlMatch?.groupValues?.getOrNull(2) ?: trimmed)
            .replace(WHITESPACE_REGEX, "")

        if (cleanBase64.isBlank()) {
            throw IllegalArgumentException("Görsel Base64 içeriği okunamadı.")
        }

        val decodedBytes = try {
            Base64.decode(cleanBase64, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Geçersiz Base64 görsel formatı.", e)
        }
        if (decodedBytes.isEmpty()) {
            throw IllegalArgumentException("Görsel verisi çözümlenemedi.")
        }

        return InlineImagePayload(
            bytes = decodedBytes,
            mimeType = resolvedMimeType.lowercase()
        )
    }

    internal fun extractCleanJson(rawResponse: String): String {
        var cleaned = rawResponse.trim()
        cleaned = cleaned.replace(MARKDOWN_FENCE_START_REGEX, "")
        cleaned = cleaned.replace(MARKDOWN_FENCE_END_REGEX, "")
        cleaned = cleaned.trim()

        if (!cleaned.startsWith("{")) {
            val firstBrace = cleaned.indexOf('{')
            val lastBrace = cleaned.lastIndexOf('}')
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                cleaned = cleaned.substring(firstBrace, lastBrace + 1).trim()
            }
        }
        return cleaned
    }

    internal fun parsePatternJson(rawJsonString: String): GeneratedPattern {
        val cleanedJson = extractCleanJson(rawJsonString)
        val json = try {
            JSONObject(cleanedJson)
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse cleaned AI JSON: ${cleanedJson.take(300)}", e)
            throw IllegalStateException(
                "Yapay zeka yanıtı JSON formatında çözümlenemedi (${e.localizedMessage ?: "format hatası"}).",
                e
            )
        }

        val isKnitting = json.optBoolean("orgu_mu", true)
        if (!isKnitting) {
            val errMsg = json.optString("hata_mesaji")
                .takeIf { it.isNotBlank() && it != "null" }
                ?: "Görselde bir örgü ürünü tespit edemedim, lütfen tığ veya şiş işi bir fotoğraf yükle"
            throw NonKnittingImageException(errMsg)
        }

        val malzemelerJson = json.optJSONObject("malzemeler")
        val materials = PatternMaterials(
            yarnType = malzemelerJson?.optString("ip_turu")
                ?.takeIf { it.isNotBlank() && it != "null" }
                ?: "%100 Pamuklu İp (Orta Kalınlık)",
            hookSize = malzemelerJson?.optString("tig_no")
                ?.takeIf { it.isNotBlank() && it != "null" }
                ?: "3.5 mm",
            colors = parseStringArray(malzemelerJson?.optJSONArray("renkler"))
                .ifEmpty { listOf("Krem", "Adaçayı Yeşili") },
            extraMaterials = parseStringArray(malzemelerJson?.optJSONArray("ek_malzemeler"))
                .ifEmpty { listOf("Etamin iğnesi", "Makas") }
        )

        val abbreviations = parseStringArray(json.optJSONArray("kisaltmalar"))
            .ifEmpty {
                listOf(
                    "sh: Sihirli halka",
                    "zn: Zincir",
                    "x: Sık iğne",
                    "v: Artırma",
                    "a: Azaltma",
                    "cc: İlmek kaydırma"
                )
            }

        val parcalarJson = json.optJSONArray("parcalar")
        val parts = buildList {
            if (parcalarJson != null) {
                for (i in 0 until parcalarJson.length()) {
                    val partObj = parcalarJson.optJSONObject(i) ?: continue
                    val partName = partObj.optString("parca_adi", "Ana Parça").trim()
                    val steps = parseStringArray(partObj.optJSONArray("adimlar"))
                    if (steps.isNotEmpty()) {
                        add(PatternPart(partName = partName.ifBlank { "Parça ${i + 1}" }, steps = steps))
                    }
                }
            }
        }.ifEmpty {
            listOf(
                PatternPart(
                    partName = "Ana Gövde",
                    steps = listOf("1. sıra: sh içine 6x = (6)", "2. sıra: her ilmeğe 1v = (12)")
                )
            )
        }

        val rawAiWarning = if (json.isNull("ai_gorsel_uyarisi")) {
            null
        } else {
            json.optString("ai_gorsel_uyarisi").trim().takeIf { it.isNotBlank() && it != "null" }
        }

        val category = json.optString("kategori", "Örgü")
            .takeIf { it.isNotBlank() && it != "null" } ?: "Örgü"
        val assemblyJson = json.optJSONArray("birlestirme_rehberi")
        val parsedAssembly = buildList {
            if (assemblyJson != null) {
                for (i in 0 until assemblyJson.length()) {
                    val obj = assemblyJson.optJSONObject(i) ?: continue
                    val title = obj.optString("baslik").trim()
                    val desc = obj.optString("aciklama").trim()
                    val stitchTech = obj.optString("dikis_teknigi").trim()
                    val diagramRaw = obj.optString("diyagram_turu").trim()
                    val diagram = AssemblyDiagramType.entries.firstOrNull { it.name == diagramRaw }
                        ?: AssemblyDiagramType.MATTRESS_STITCH
                    if (title.isNotBlank() && desc.isNotBlank()) {
                        add(
                            AssemblyGuideStep(
                                stepTitle = title,
                                instruction = desc,
                                stitchTechnique = stitchTech.ifBlank { "Estetik Birleştirme" },
                                diagramType = diagram
                            )
                        )
                    }
                }
            }
        }.ifEmpty { defaultAssemblyGuideForCategory(category) }

        return GeneratedPattern(
            title = json.optString("urun_adi", "Özel El İşi Örgü Tarifi")
                .takeIf { it.isNotBlank() && it != "null" } ?: "Özel El İşi Örgü Tarifi",
            category = category,
            technique = json.optString("teknik", "Tığ İşi (Crochet)")
                .takeIf { it.isNotBlank() && it != "null" } ?: "Tığ İşi (Crochet)",
            difficulty = json.optString("zorluk", "Orta")
                .takeIf { it.isNotBlank() && it != "null" } ?: "Orta",
            estimatedDuration = json.optString("tahmini_sure", "3-4 saat")
                .takeIf { it.isNotBlank() && it != "null" } ?: "3-4 saat",
            aiWarning = rawAiWarning,
            materials = materials,
            abbreviations = abbreviations,
            parts = parts,
            proTip = json.optString(
                "puf_noktasi",
                "İlmek gerginliğini tüm sıralar boyunca sabit tutarak düzgün bir doku elde edin."
            ).takeIf { it.isNotBlank() && it != "null" }
                ?: "İlmek gerginliğini tüm sıralar boyunca sabit tutarak düzgün bir doku elde edin.",
            atelierNote = json.optString(
                "atolye_notu",
                "Bu ürünü kendin örmek zor gelirse @ilmektenisler atölyesinden senin için hazırlamamızı isteyebilirsin!"
            ).takeIf { it.isNotBlank() && it != "null" }
                ?: "Bu ürünü kendin örmek zor gelirse @ilmektenisler atölyesinden senin için hazırlamamızı isteyebilirsin!",
            assemblyGuide = parsedAssembly
        )
    }

    private fun formatReadableError(error: Exception): String {
        val rawMsg = error.localizedMessage?.trim().orEmpty()
        val causeChain = generateSequence<Throwable>(error) { it.cause }.toList()
        val hasNetworkCause = causeChain.any { it is UnknownHostException || it is IOException }
        val isAppCheckOrAuth = rawMsg.contains("403") ||
            rawMsg.contains("app check", ignoreCase = true) ||
            rawMsg.contains("attestation", ignoreCase = true)

        return when {
            error is QuotaExceededException ->
                "AI kullanım kotası geçici olarak doldu. Lütfen biraz bekleyip tekrar deneyin."
            error is ServiceDisabledException ->
                "Firebase AI servisi bu projede henüz etkinleştirilmemiş (${rawMsg.ifBlank { "ServiceDisabled" }})."
            isAppCheckOrAuth ->
                "Güvenlik doğrulaması (App Check / HTTP 403) başarısız oldu: $rawMsg"
            hasNetworkCause && rawMsg.isBlank() ->
                "İnternet bağlantısı kurulamadı. Bağlantınızı kontrol edip tekrar deneyin."
            rawMsg.isNotBlank() -> rawMsg
            else -> "Beklenmeyen bir hata oluştu (${error::class.java.simpleName})."
        }
    }

    private fun defaultAssemblyGuideForCategory(category: String): List<AssemblyGuideStep> {
        val isAmigurumi = category.contains("Amigurumi", ignoreCase = true)
        return if (isAmigurumi) {
            listOf(
                AssemblyGuideStep(
                    stepTitle = "1. Toplu İğne ile Simetrik Hizalama",
                    instruction = "Dikime başlamadan önce kafa, kulak ve kolları toplu iğnelerle gövdeye sabitleyip her açıdan simetrisini kontrol edin.",
                    stitchTechnique = "Toplu İğne Sabitleme",
                    diagramType = AssemblyDiagramType.AMIGURUMI_JOIN
                ),
                AssemblyGuideStep(
                    stepTitle = "2. Görünmez Parça Dikimi",
                    instruction = "Etamin iğnesiyle gövdeden 1 ilmek, eklenecek parçadan 1 ilmek alarak dikiş izi bırakmadan turu tamamlayın; son 2 ilmek kala elyafı hafifçe destekleyin.",
                    stitchTechnique = "Amigurumi Görünmez Dikiş",
                    diagramType = AssemblyDiagramType.AMIGURUMI_JOIN
                ),
                AssemblyGuideStep(
                    stepTitle = "3. İp Uçlarını İçeride Gizleme",
                    instruction = "Kalan ip ucunu küçük bir düğümle sabitledikten sonra iğneyi dolgu içinden farklı noktalardan geçirip sıfır noktasından kesin.",
                    stitchTechnique = "İçten İp Yürütme",
                    diagramType = AssemblyDiagramType.FINISHING_BLOCKING
                )
            )
        } else {
            listOf(
                AssemblyGuideStep(
                    stepTitle = "1. Parçaları Ön Yüzden Eşleştirme",
                    instruction = "Ön, arka veya motif parçalarını düz bir zemine serip köşe ilmeklerini ilmek belirleyici ile karşılıklı sabitleyin.",
                    stitchTechnique = "İlmek Belirleyici Hizalama",
                    diagramType = AssemblyDiagramType.SLIP_STITCH_JOIN
                ),
                AssemblyGuideStep(
                    stepTitle = "2. Görünmez Matras (Merdiven) Dikişi",
                    instruction = "İki parçanın kenar çubuklarından sırayla yatay geçiş yaparak dikişi oluşturun; her 3-4 geçişte bir ipi nazikçe çekerek dikişi tamamen gizleyin.",
                    stitchTechnique = "Matras Dikişi",
                    diagramType = AssemblyDiagramType.MATTRESS_STITCH
                ),
                AssemblyGuideStep(
                    stepTitle = "3. İp Gizleme ve Buharlı Bloklama",
                    instruction = "Etamin iğnesiyle ip uçlarını arka yüzdeki ilmek altlarından zikzak yürütün. Ürünü nemli bez eşliğinde hafif buharla bloklayarak formunu sabitleyin.",
                    stitchTechnique = "İp Gizleme & Bloklama",
                    diagramType = AssemblyDiagramType.FINISHING_BLOCKING
                )
            )
        }
    }

    private fun parseStringArray(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val value = array.optString(i).trim()
                if (value.isNotBlank() && value != "null") add(value)
            }
        }
    }

    private fun scaleBitmapForUpload(source: Bitmap): Bitmap {
        val maxSide = MAX_IMAGE_SIDE
        val width = source.width
        val height = source.height
        if (width <= maxSide && height <= maxSide) return source
        val ratio = minOf(maxSide.toFloat() / width, maxSide.toFloat() / height)
        val targetW = (width * ratio).toInt().coerceAtLeast(1)
        val targetH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun normalizeSoftwareArgbBitmap(bitmap: Bitmap): Bitmap {
        val softwareCopy = if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
        } else {
            bitmap
        }
        if (!softwareCopy.hasAlpha()) {
            return softwareCopy
        }
        val flattened = Bitmap.createBitmap(
            softwareCopy.width.coerceAtLeast(1),
            softwareCopy.height.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(flattened)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(softwareCopy, 0f, 0f, null)
        if (softwareCopy !== bitmap) {
            softwareCopy.recycle()
        }
        return flattened
    }

    private fun compressToJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        return output.toByteArray()
    }

    private companion object {
        const val TAG = "AiPatternRepository"
        const val MODEL_NAME = "gemini-3.8-flash"
        const val MAX_IMAGE_SIDE = 800
        const val INITIAL_JPEG_QUALITY = 75
        const val PRIMARY_TIMEOUT_MS = 45_000L
        const val FALLBACK_TIMEOUT_MS = 45_000L
        const val MAX_INLINE_BYTES = 4 * 1024 * 1024
        val DATA_URL_REGEX = Regex(
            pattern = "^data:([^;,\\s]+);base64,(.*)$",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val WHITESPACE_REGEX = Regex("\\s+")
        val MARKDOWN_FENCE_START_REGEX = Regex(
            pattern = "^```(?:json)?\\s*",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
        )
        val MARKDOWN_FENCE_END_REGEX = Regex(
            pattern = "\\s*```$",
            options = setOf(RegexOption.MULTILINE)
        )
    }
}
