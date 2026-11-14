package com.example.ui.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.data.model.DiscoverItem
import com.example.data.model.GeneratedPattern

object RecipeShareFormatter {

    const val AI_WATERMARK = "✨ İlmekten İşler (@ilmektenisler) AI Atölyesi ile oluşturuldu"

    fun formatGeneratedPattern(pattern: GeneratedPattern): String = buildString {
        appendLine("🧶 ${pattern.title}")
        appendLine("Kategori: ${pattern.category} • Teknik: ${pattern.technique}")
        appendLine("Zorluk: ${pattern.difficulty} • Süre: ${pattern.estimatedDuration}")
        appendLine()
        appendLine("📌 Malzemeler:")
        appendLine("• İp: ${pattern.materials.yarnType}")
        appendLine("• Tığ / Şiş: ${pattern.materials.hookSize}")
        if (pattern.materials.colors.isNotEmpty()) {
            appendLine("• Renkler: ${pattern.materials.colors.joinToString(", ")}")
        }
        if (pattern.materials.extraMaterials.isNotEmpty()) {
            appendLine("• Ek Malzemeler: ${pattern.materials.extraMaterials.joinToString(", ")}")
        }
        if (pattern.abbreviations.isNotEmpty()) {
            appendLine()
            appendLine("🔤 Kısaltmalar: ${pattern.abbreviations.joinToString(" • ")}")
        }
        if (pattern.parts.isNotEmpty()) {
            appendLine()
            appendLine("🪡 Yapım Adımları:")
            pattern.parts.forEach { part ->
                appendLine("▸ ${part.partName}")
                part.steps.forEach { step ->
                    appendLine("  - $step")
                }
            }
        }
        if (pattern.proTip.isNotBlank()) {
            appendLine()
            appendLine("💡 Püf Noktası: ${pattern.proTip}")
        }
        appendLine()
        append(AI_WATERMARK)
    }

    fun formatCommunityRecipe(item: DiscoverItem): String = buildString {
        appendLine("🧶 ${item.title}")
        appendLine("Tasarım: ${item.authorName} (${item.authorHandle})")
        appendLine("Kategori: ${item.category} • Zorluk: ${item.difficulty}")
        appendLine("İp: ${item.yarnType} • Tığ / Şiş: ${item.hookSize}")
        if (item.yarnBrand.isNotBlank() || item.finishedDate.isNotBlank()) {
            appendLine("Proje Künyesi: ${item.yarnBrand} • Bitiş: ${item.finishedDate}")
        }
        if (item.recipeNote.isNotBlank()) {
            appendLine()
            appendLine("📝 Yapım Notu:")
            appendLine(item.recipeNote)
        }
        if (item.steps.isNotEmpty()) {
            appendLine()
            appendLine("🪡 Tarif Adımları:")
            item.steps.forEachIndexed { index, step ->
                appendLine("${index + 1}. $step")
            }
        }
        appendLine()
        append(AI_WATERMARK)
    }

    fun formatCustomOrderMessage(
        productType: String,
        colors: String,
        note: String
    ): String = buildString {
        appendLine("Merhaba @ilmektenisler atölyesi! 🧶")
        appendLine("İlmekten İşler uygulaması üzerinden kişiye özel tasarım siparişi oluşturmak istiyorum:")
        appendLine()
        appendLine("• Ürün Türü: ${productType.ifBlank { "Özel Tasarım" }}")
        appendLine("• İstenen Renkler: ${colors.trim().ifBlank { "Atölye önerisine açığım" }}")
        if (note.isNotBlank()) {
            appendLine("• Ölçü & Notum: ${note.trim()}")
        }
        appendLine()
        append("Uygunluk ve fiyat bilgisi paylaşabilir misiniz?")
    }

    fun copyToClipboard(
        context: Context,
        label: String,
        text: String
    ): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return false
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
            true
        } catch (_: Exception) {
            false
        }
    }

    fun copyAndShareText(
        context: Context,
        subject: String,
        formattedText: String,
        chooserTitle: String = "Tarifi Paylaş",
        onCopiedFeedback: ((String) -> Unit)? = null
    ) {
        val copied = copyToClipboard(context, subject, formattedText)
        if (copied) {
            onCopiedFeedback?.invoke("Tarif imzasıyla panoya kopyalandı!")
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, formattedText)
        }
        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (_: Exception) {
            if (!copied) {
                onCopiedFeedback?.invoke("Tarif paylaşılamadı.")
            }
        }
    }
}
