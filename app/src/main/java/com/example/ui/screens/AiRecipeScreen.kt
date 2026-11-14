package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GeneratedPattern
import com.example.ui.AiPatternUiState
import com.example.ui.components.AssemblyGuideContent
import com.example.ui.components.CraftBadge
import com.example.ui.components.CraftCard
import com.example.ui.components.CraftOutlinedPillButton
import com.example.ui.components.CraftPillButton
import com.example.ui.components.GeneratedPatternResultCard
import com.example.ui.components.StitchLoadingCard
import com.example.ui.components.defaultAtelierAssemblySteps
import com.example.ui.utils.RecipeShareFormatter

private data class SampleCraftOption(
    val label: String,
    val drawableRes: Int
)

private data class YarnAdaptationRule(
    val id: String,
    val label: String,
    val recommendedHook: String,
    val sizeChange: String,
    val tip: String
)

private val yarnAdaptationRules = listOf(
    YarnAdaptationRule(
        id = "cotton_to_velvet",
        label = "Koton → Kadife İp",
        recommendedHook = "4.5 mm – 5.5 mm (2.5 mm koton tığı yerine)",
        sizeChange = "~%65–80 Daha Büyük • Puf ve yumuşak doku",
        tip = "Amigurumi oyuncaklar yaklaşık 1.7 kat büyür. İlmekler zor görüneceği için ilk sıraya belirleyici takın ve sihirli halka yerine 2 zn ile başlayın."
    ),
    YarnAdaptationRule(
        id = "acrylic_to_chunky",
        label = "Akrilik → Kalın Yün",
        recommendedHook = "6.0 mm – 8.0 mm (4.0 mm orta tığ yerine)",
        sizeChange = "~%45–60 Daha Büyük • Tok ve dolgun form",
        tip = "Hırka veya berelerde beden büyümesini dengelemek için motif/sıra sayısını yaklaşık %25 azaltabilir, yumuşak döküm için yarım numara büyük tığ seçebilirsiniz."
    ),
    YarnAdaptationRule(
        id = "cotton_to_lace",
        label = "Koton → İnce Dantel İpi",
        recommendedHook = "1.25 mm – 1.75 mm (3.0 mm tığ yerine)",
        sizeChange = "~%40–50 Daha Küçük • Minyatür ve zarif doku",
        tip = "Anahtarlık ve yaka detayları için ideal küçülme sağlar. İlmekleri çok sıkmadan, mümkünse iyi aydınlatma altında örmeniz sayımı kolaylaştırır."
    ),
    YarnAdaptationRule(
        id = "cotton_to_macrame",
        label = "Pamuklu → Makrome / Kağıt İp",
        recommendedHook = "4.0 mm – 5.0 mm (3.5 mm tığ yerine)",
        sizeChange = "~%25–35 Daha Büyük • Sert ve dik duruş",
        tip = "Çanta, sepet ve supla modellerinde formun çökmemesi için idealdir. Bilek yorgunluğunu azaltmak için ergonomik saplı tığ tercih edin."
    )
)

@Composable
fun AiRecipeScreen(
    selectedBitmap: Bitmap?,
    aiState: AiPatternUiState,
    rowCount: Int,
    stitchCount: Int,
    onImageSelected: (Bitmap) -> Unit,
    onAnalyzeClick: (String) -> Unit,
    onSavePattern: (GeneratedPattern) -> Unit,
    onShareToCommunity: (GeneratedPattern) -> Unit,
    onIncrementRow: () -> Unit,
    onDecrementRow: () -> Unit,
    onIncrementStitch: () -> Unit,
    onDecrementStitch: () -> Unit,
    onCompleteRow: () -> Unit,
    onSelectRow: (Int) -> Unit,
    onResetCounter: () -> Unit,
    onGoToAtelierTab: () -> Unit,
    isPublishingPost: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var extraNote by remember { mutableStateOf("") }
    var activeScreenTab by rememberSaveable { mutableIntStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    decodeUriToBitmap(context, uri)
                }
                if (bmp != null) {
                    onImageSelected(bmp)
                }
            }
        }
    }

    val sampleOptions = remember {
        listOf(
            SampleCraftOption("Motifli Hırka", R.drawable.img_crochet_cardigan),
            SampleCraftOption("Tavşan Amigurumi", R.drawable.img_amigurumi_bunny),
            SampleCraftOption("File Çanta", R.drawable.img_knit_tote)
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Segmented top tabs: AI Tarif Oluştur vs Birleştirme ve Son Dokunuş
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AiModeTabPill(
                        label = "Tarif Oluştur",
                        selected = activeScreenTab == 0,
                        onClick = { activeScreenTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_mode_tab_create")
                    )
                    AiModeTabPill(
                        label = "Birleştirme ve Son Dokunuş",
                        selected = activeScreenTab == 1,
                        onClick = { activeScreenTab = 1 },
                        modifier = Modifier
                            .weight(1.35f)
                            .testTag("ai_mode_tab_assembly")
                    )
                }
            }

            if (activeScreenTab == 1) {
                val activePattern = (aiState as? AiPatternUiState.Success)?.pattern
                AssemblyGuideContent(
                    steps = activePattern?.assemblyGuide ?: defaultAtelierAssemblySteps,
                    patternTitle = activePattern?.title
                )
                Spacer(modifier = Modifier.height(16.dp))
                return@Column
            }

            // Header banner
            CraftCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CraftBadge(text = "Gemini Görsel Analiz", isTerracotta = false)
                        CraftBadge(text = "x • v • a • zn", isTerracotta = true)
                    }
                    Text(
                        text = "Fotoğraftan Örgü Tarifi Çıkar",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Örgü fotoğrafını yükle; yapay zeka zorluk seviyesini, ip türünü, tığ/şiş numarasını ve Türkçe kısaltmalı adımları senin için çıkarsın.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Image upload area
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("pick_knitting_photo_area"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                if (selectedBitmap != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            bitmap = selectedBitmap.asImageBitmap(),
                            contentDescription = "Seçilen örgü fotoğrafı",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Görseli Değiştir",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Örgü fotoğrafı yükle",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Text(
                            text = "Galeriden Örgü Görseli Seç",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tığ işi, şiş örgü veya amigurumi fotoğrafı yükleyebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick studio sample presets
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Veya atölye örnekleriyle hemen dene:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(sampleOptions) { sample ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    coroutineScope.launch {
                                        val bmp = withContext(Dispatchers.IO) {
                                            decodeSampleDrawableToBitmap(context, sample.drawableRes)
                                        }
                                        if (bmp != null) {
                                            onImageSelected(bmp)
                                        }
                                    }
                                }
                                .testTag("sample_image_${sample.label}")
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = sample.drawableRes),
                                    contentDescription = sample.label,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(50)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = sample.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Optional note input
            OutlinedTextField(
                value = extraNote,
                onValueChange = { extraNote = it },
                label = { Text("İsteğe bağlı not (örn. Bebek boyu olsun, pamuk ip)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_extra_note_input")
            )

            // Primary Pill Generate Button
            CraftPillButton(
                text = if (aiState is AiPatternUiState.Loading) "İlmekler sayılıyor..." else "AI ile Tarif Çıkar",
                onClick = { onAnalyzeClick(extraNote) },
                enabled = selectedBitmap != null && aiState !is AiPatternUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_recipe_button"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            // Interactive Row & Stitch Counter
            StitchRowCounterCard(
                rowCount = rowCount,
                stitchCount = stitchCount,
                totalSteps = (aiState as? AiPatternUiState.Success)?.pattern?.steps?.size,
                onIncrementRow = onIncrementRow,
                onDecrementRow = onDecrementRow,
                onIncrementStitch = onIncrementStitch,
                onDecrementStitch = onDecrementStitch,
                onCompleteRow = onCompleteRow,
                onResetCounter = onResetCounter
            )

            // Expandable Yarn & Hook Adaptation Guide
            YarnAdaptationAccordionCard(
                activePattern = (aiState as? AiPatternUiState.Success)?.pattern
            )

            // State rendering
            when (aiState) {
                is AiPatternUiState.Idle -> Unit
                is AiPatternUiState.Loading -> {
                    StitchLoadingCard()
                }
                is AiPatternUiState.Error -> {
                    val isNonKnittingImage = aiState.message.contains("örgü ürünü tespit edemedim", ignoreCase = true)
                    val accentColor = if (isNonKnittingImage) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    val displayErrorText = if (!isNonKnittingImage &&
                        !aiState.message.startsWith("Tarif oluşturulamadı:", ignoreCase = true)
                    ) {
                        "Tarif oluşturulamadı: ${aiState.message}"
                    } else {
                        aiState.message
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isNonKnittingImage) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
                        },
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_error_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = accentColor.copy(alpha = 0.14f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isNonKnittingImage) {
                                                Icons.Outlined.WarningAmber
                                            } else {
                                                Icons.Outlined.ErrorOutline
                                            },
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isNonKnittingImage) {
                                        "Örgü Ürünü Tespit Edilemedi"
                                    } else {
                                        "Tarif Oluşturulamadı"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = accentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = displayErrorText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isNonKnittingImage) {
                                    CraftOutlinedPillButton(
                                        text = "Görsel Değiştir",
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        borderColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("change_image_on_error_button"),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                                CraftPillButton(
                                    text = "Tekrar Dene",
                                    onClick = { onAnalyzeClick(extraNote) },
                                    enabled = selectedBitmap != null,
                                    containerColor = accentColor,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("retry_generate_recipe_button"),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                is AiPatternUiState.Success -> {
                    GeneratedPatternResultCard(
                        pattern = aiState.pattern,
                        activeRow = rowCount,
                        onSelectStepRow = onSelectRow,
                        onSaveClick = { onSavePattern(aiState.pattern) },
                        onShareClick = {
                            val watermarked = RecipeShareFormatter.formatGeneratedPattern(aiState.pattern)
                            RecipeShareFormatter.copyAndShareText(
                                context = context,
                                subject = "${aiState.pattern.title} - İlmekten İşler",
                                formattedText = watermarked,
                                chooserTitle = "Tarifi Paylaş / Kopyala",
                                onCopiedFeedback = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onGoToAtelierTab = onGoToAtelierTab
                    )

                    CraftPillButton(
                        text = if (isPublishingPost) "Toplulukta Paylaşılıyor..." else "Bu Tarifi Toplulukta Paylaş",
                        onClick = { onShareToCommunity(aiState.pattern) },
                        enabled = !isPublishingPost,
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_ai_to_community_bottom_button"),
                        leadingIcon = {
                            if (isPublishingPost) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Publish,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StitchRowCounterCard(
    rowCount: Int,
    stitchCount: Int,
    totalSteps: Int?,
    onIncrementRow: () -> Unit,
    onDecrementRow: () -> Unit,
    onIncrementStitch: () -> Unit,
    onDecrementStitch: () -> Unit,
    onCompleteRow: () -> Unit,
    onResetCounter: () -> Unit
) {
    CraftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stitch_row_counter_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Satır & İlmek Sayacı",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (totalSteps != null) {
                            "Tarif adımı: $rowCount / $totalSteps"
                        } else {
                            "Örerken kaldığın ilmeği ve sırayı kaybetme"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onResetCounter,
                    modifier = Modifier.testTag("counter_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sayacı sıfırla",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CounterModule(
                    label = "Satır / Tur",
                    value = rowCount,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    decrementTag = "row_decrement_button",
                    incrementTag = "row_increment_button",
                    decrementDescription = "Satırı azalt",
                    incrementDescription = "Satırı artır",
                    onDecrement = onDecrementRow,
                    onIncrement = onIncrementRow,
                    modifier = Modifier.weight(1f)
                )

                CounterModule(
                    label = "İlmek",
                    value = stitchCount,
                    accentColor = MaterialTheme.colorScheme.primary,
                    decrementTag = "stitch_decrement_button",
                    incrementTag = "stitch_increment_button",
                    decrementDescription = "İlmeği azalt",
                    incrementDescription = "İlmeği artır",
                    onDecrement = onDecrementStitch,
                    onIncrement = onIncrementStitch,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CraftPillButton(
                    text = "+1 İlmek",
                    onClick = onIncrementStitch,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_stitch_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                CraftOutlinedPillButton(
                    text = "Sırayı Bitir",
                    onClick = onCompleteRow,
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("complete_row_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CounterModule(
    label: String,
    value: Int,
    accentColor: Color,
    decrementTag: String,
    incrementTag: String,
    decrementDescription: String,
    incrementDescription: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val incrementContentColor = if (accentColor == MaterialTheme.colorScheme.tertiary) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDecrement)
                        .testTag(decrementTag)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = decrementDescription,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = accentColor,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onIncrement)
                        .testTag(incrementTag)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = incrementDescription,
                            tint = incrementContentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiModeTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun decodeSampleDrawableToBitmap(
    context: Context,
    drawableRes: Int,
    maxDimension: Int = 800
): Bitmap? {
    return try {
        val boundsOpts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources, drawableRes, boundsOpts)
        val largestSide = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
        var sampleSize = 1
        while (largestSide > 0 && largestSide / sampleSize > maxDimension) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeResource(context.resources, drawableRes, decodeOpts)
            ?: return null
        scaleDownToMaxDimension(decoded, maxDimension)
    } catch (_: Exception) {
        null
    }
}

private fun decodeUriToBitmap(
    context: Context,
    uri: Uri,
    maxDimension: Int = 800
): Bitmap? {
    return try {
        val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
                val largestSide = maxOf(info.size.width, info.size.height)
                if (largestSide > maxDimension) {
                    var sampleSize = 1
                    while (largestSide / sampleSize > maxDimension) {
                        sampleSize *= 2
                    }
                    decoder.setTargetSampleSize(sampleSize)
                }
            }
        } else {
            val boundsOpts = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            val largestSide = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
            var sampleSize = 1
            while (largestSide > 0 && largestSide / sampleSize > maxDimension) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            }
        } ?: return null
        scaleDownToMaxDimension(decoded, maxDimension)
    } catch (_: Exception) {
        null
    }
}

private fun scaleDownToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= maxDimension && height <= maxDimension) return bitmap
    val ratio = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
    val targetW = (width * ratio).toInt().coerceAtLeast(1)
    val targetH = (height * ratio).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    if (scaled !== bitmap) {
        bitmap.recycle()
    }
    return scaled
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YarnAdaptationAccordionCard(
    activePattern: GeneratedPattern?
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedRuleId by rememberSaveable { mutableStateOf(yarnAdaptationRules.first().id) }
    val activeRule = yarnAdaptationRules.firstOrNull { it.id == selectedRuleId } ?: yarnAdaptationRules.first()

    CraftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("yarn_adaptation_accordion")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("yarn_adaptation_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "İp Değiştirirsem Ne Olur?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "İp türüne göre tığ numarası ve boyut uyarlama rehberi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Rehberi daralt" else "Rehberi aç",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    if (activePattern != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Mevcut tarif: ${activePattern.materials.yarnType} • ${activePattern.materials.hookSize}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Text(
                        text = "Geçiş yapmak istediğin ip türünü seç:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        yarnAdaptationRules.forEach { rule ->
                            val isSelected = rule.id == selectedRuleId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRuleId = rule.id },
                                label = {
                                    Text(
                                        text = rule.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("yarn_preset_${rule.id}")
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("yarn_adaptation_result_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "Önerilen Tığ No",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeRule.recommendedHook,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "Boyut Değişimi",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeRule.sizeChange,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = activeRule.tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
