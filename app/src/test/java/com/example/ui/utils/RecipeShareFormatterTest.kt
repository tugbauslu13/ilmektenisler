package com.example.ui.utils

import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DiscoverItem
import com.example.data.model.GeneratedPattern
import com.example.data.model.PatternMaterials
import com.example.data.model.PatternPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RecipeShareFormatterTest {

    @Test
    fun `formatGeneratedPattern appends AI watermark at the bottom`() {
        val pattern = GeneratedPattern(
            title = "Papatya Motifli Çanta",
            category = "Çanta",
            technique = "Tığ İşi (Crochet)",
            difficulty = "Orta",
            estimatedDuration = "5-6 saat",
            materials = PatternMaterials(
                yarnType = "Pamuklu Koton",
                hookSize = "3.5 mm",
                colors = listOf("Krem", "Sarı"),
                extraMaterials = listOf("Etamin iğnesi")
            ),
            abbreviations = listOf("sh: Sihirli halka", "dc: İkili trabzan"),
            parts = listOf(
                PatternPart(
                    partName = "Motif",
                    steps = listOf("1. sıra: sh içine 12dc = (12)")
                )
            ),
            proTip = "İp gerginliğini sabit tutun.",
            aiWarning = null,
            atelierNote = "Atölye notu"
        )

        val formatted = RecipeShareFormatter.formatGeneratedPattern(pattern)
        assertTrue(formatted.contains("Papatya Motifli Çanta"))
        assertTrue(formatted.contains("Pamuklu Koton"))
        assertTrue(formatted.contains("1. sıra: sh içine 12dc = (12)"))
        assertTrue(formatted.endsWith("✨ İlmekten İşler (@ilmektenisler) AI Atölyesi ile oluşturuldu"))
    }

    @Test
    fun `formatCommunityRecipe appends AI watermark at the bottom`() {
        val item = DiscoverItem(
            id = "d1",
            title = "Adaçayı Tavşan",
            authorName = "Zeynep",
            authorHandle = "@zeynep",
            category = "Amigurumi",
            difficulty = "Orta",
            yarnType = "Organik Koton",
            hookSize = "2.5 mm",
            estimatedDuration = "4 saat",
            abbreviationsNote = "sh: sihirli halka • x: sık iğne",
            recipeNote = "Gözleri 8. sıraya takın.",
            steps = listOf("1. sıra: sh içine 6x = (6)"),
            imageKey = "img_amigurumi_bunny",
            baseLikes = 12,
            aspectHeightRatio = 1.0f
        )

        val formatted = RecipeShareFormatter.formatCommunityRecipe(item)
        assertTrue(formatted.contains("Adaçayı Tavşan"))
        assertTrue(formatted.contains("Gözleri 8. sıraya takın."))
        assertTrue(formatted.endsWith("✨ İlmekten İşler (@ilmektenisler) AI Atölyesi ile oluşturuldu"))
    }

    @Test
    fun `formatCustomOrderMessage formats product type colors and note`() {
        val msg = RecipeShareFormatter.formatCustomOrderMessage(
            productType = "Hırka",
            colors = "Adaçayı yeşili ve krem",
            note = "38 beden olsun"
        )
        assertTrue(msg.contains("Hırka"))
        assertTrue(msg.contains("Adaçayı yeşili ve krem"))
        assertTrue(msg.contains("38 beden olsun"))
        assertTrue(msg.contains("@ilmektenisler"))
    }

    @Test
    fun `copyToClipboard places watermarked recipe on system clipboard`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sample = "Test tarifi\n${RecipeShareFormatter.AI_WATERMARK}"
        val copied = RecipeShareFormatter.copyToClipboard(context, "Tarif", sample)
        assertTrue(copied)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertEquals(sample, clipboard.primaryClip?.getItemAt(0)?.text?.toString())
    }
}
