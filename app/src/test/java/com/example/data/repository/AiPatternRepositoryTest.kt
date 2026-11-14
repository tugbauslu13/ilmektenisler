package com.example.data.repository

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.example.data.model.AssemblyDiagramType
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AiPatternRepositoryTest {

    private val repository = AiPatternRepository()

    @Test
    fun `extractCleanJson strips markdown json fences and surrounding prose`() {
        val raw = """
            İşte analiz sonucunuz:
            ```json
            {
              "orgu_mu": true,
              "urun_adi": "Mini Amigurumi Ayıcık"
            }
            ```
            Umarım keyifle örersiniz!
        """.trimIndent()

        val cleaned = repository.extractCleanJson(raw)
        assertTrue(cleaned.startsWith("{"))
        assertTrue(cleaned.endsWith("}"))
        assertTrue(cleaned.contains("\"Mini Amigurumi Ayıcık\""))
    }

    @Test
    fun `extractCleanJson strips plain triple backtick fences`() {
        val raw = "```\n{\"orgu_mu\":true,\"urun_adi\":\"Motifli Çanta\"}\n```"
        val cleaned = repository.extractCleanJson(raw)
        assertEquals("{\"orgu_mu\":true,\"urun_adi\":\"Motifli Çanta\"}", cleaned)
    }

    @Test
    fun `sanitizeBase64InlinePayload removes data url prefix and preserves mime type`() {
        val rawBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        val encoded = Base64.encodeToString(rawBytes, Base64.NO_WRAP)
        val withPrefix = "data:image/png;base64, $encoded \n"

        val payload = repository.sanitizeBase64InlinePayload(withPrefix)
        assertEquals("image/png", payload.mimeType)
        assertArrayEquals(rawBytes, payload.bytes)
    }

    @Test
    fun `sanitizeBase64InlinePayload handles raw base64 without prefix`() {
        val rawBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
        val encoded = Base64.encodeToString(rawBytes, Base64.NO_WRAP)

        val payload = repository.sanitizeBase64InlinePayload(encoded)
        assertEquals("image/jpeg", payload.mimeType)
        assertArrayEquals(rawBytes, payload.bytes)
    }

    @Test
    fun `prepareInlineImagePayload normalizes alpha bitmap and scales to max 800px jpeg`() {
        val bitmap = Bitmap.createBitmap(2400, 1200, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.argb(128, 91, 127, 94))
        }

        val payload = repository.prepareInlineImagePayload(bitmap)
        assertEquals("image/jpeg", payload.mimeType)
        assertTrue(payload.bytes.size > 100)
        // JPEG SOI marker: FF D8
        assertEquals(0xFF.toByte(), payload.bytes[0])
        assertEquals(0xD8.toByte(), payload.bytes[1])

        val decoded = android.graphics.BitmapFactory.decodeByteArray(payload.bytes, 0, payload.bytes.size)
        assertEquals(800, decoded.width)
        assertEquals(400, decoded.height)
    }

    @Test
    fun `bitmapToCleanBase64 outputs raw base64 without data url prefix`() {
        val bitmap = Bitmap.createBitmap(1200, 900, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(196, 125, 100))
        }

        val cleanBase64 = repository.bitmapToCleanBase64(bitmap)
        assertTrue(cleanBase64.isNotBlank())
        assertTrue(!cleanBase64.startsWith("data:", ignoreCase = true))
        val parsedBack = repository.sanitizeBase64InlinePayload(cleanBase64)
        assertEquals("image/jpeg", parsedBack.mimeType)
        assertTrue(parsedBack.bytes.isNotEmpty())
    }

    @Test
    fun `parsePatternJson parses fenced Turkish crochet pattern JSON`() {
        val fencedJson = """
            ```json
            {
              "orgu_mu": true,
              "hata_mesaji": null,
              "urun_adi": "Adaçayı Tavşan Amigurumi",
              "kategori": "Amigurumi",
              "teknik": "Tığ İşi (Crochet)",
              "zorluk": "Orta",
              "tahmini_sure": "4-5 saat",
              "ai_gorsel_uyarisi": null,
              "malzemeler": {
                "ip_turu": "Pamuklu koton ip",
                "tig_no": "2.5 mm",
                "renkler": ["Adaçayı", "Krem"],
                "ek_malzemeler": ["Elyaf", "8mm kilitli göz"]
              },
              "kisaltmalar": ["sh: Sihirli halka", "x: Sık iğne", "v: Artırma", "a: Azaltma"],
              "parcalar": [
                {
                  "parca_adi": "Kafa",
                  "adimlar": [
                    "1. sıra: sh içine 6x = (6)",
                    "2. sıra: her ilmeğe 1v = (12)",
                    "3. sıra: (1x, 1v)*6 = (18)"
                  ]
                }
              ],
              "birlestirme_rehberi": [
                {
                  "baslik": "Kafa ve Gövde Dikimi",
                  "aciklama": "Karşılıklı ilmeklerden etamin iğnesi ile dikin.",
                  "dikis_teknigi": "Amigurumi Görünmez Dikiş",
                  "diyagram_turu": "AMIGURUMI_JOIN"
                }
              ],
              "puf_noktasi": "İlmekleri sıkı tutun ki elyaf dışarı çıkmasın.",
              "atolye_notu": "Bu ürünü kendin örmek zor gelirse @ilmektenisler atölyesinden senin için hazırlamamızı isteyebilirsin!"
            }
            ```
        """.trimIndent()

        val pattern = repository.parsePatternJson(fencedJson)
        assertEquals("Adaçayı Tavşan Amigurumi", pattern.title)
        assertEquals("Orta", pattern.difficulty)
        assertEquals("2.5 mm", pattern.materials.hookSize)
        assertNull(pattern.aiWarning)
        assertEquals(1, pattern.parts.size)
        assertEquals("Kafa", pattern.parts.first().partName)
        assertEquals(3, pattern.parts.first().steps.size)
        assertEquals(AssemblyDiagramType.AMIGURUMI_JOIN, pattern.assemblyGuide.first().diagramType)
    }

    @Test
    fun `parsePatternJson throws NonKnittingImageException when orgu_mu is false`() {
        val nonKnittingJson = """
            {
              "orgu_mu": false,
              "hata_mesaji": "Görselde bir örgü ürünü tespit edemedim, lütfen tığ veya şiş işi bir fotoğraf yükle"
            }
        """.trimIndent()

        try {
            repository.parsePatternJson(nonKnittingJson)
            fail("Expected NonKnittingImageException")
        } catch (e: NonKnittingImageException) {
            assertTrue(e.message!!.contains("örgü ürünü tespit edemedim"))
        }
    }

    @Test
    fun `parsePatternJson throws descriptive IllegalStateException on malformed JSON`() {
        val broken = "```json\n{ \"orgu_mu\": true, \"urun_adi\": \n```"
        try {
            repository.parsePatternJson(broken)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("JSON formatında çözümlenemedi"))
        }
    }
}
