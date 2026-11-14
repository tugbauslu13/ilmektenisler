package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CuratedCatalog

enum class PostCreationMode {
    SOCIAL,
    RECIPE
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostBottomSheet(
    initialMode: PostCreationMode = PostCreationMode.RECIPE,
    isPublishingRecipe: Boolean,
    isPublishingSocial: Boolean,
    onDismiss: () -> Unit,
    onSubmitRecipePost: (
        bitmap: Bitmap?,
        title: String,
        category: String,
        difficulty: String,
        yarnInfo: String,
        hookSize: String,
        recipeNote: String
    ) -> Unit,
    onSubmitSocialPost: (
        text: String,
        topic: String,
        bitmap: Bitmap?
    ) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var creationMode by remember(initialMode) { mutableStateOf(initialMode) }

    var recipeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var socialBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CuratedCatalog.postCreationCategories.first()) }
    var selectedDifficulty by remember { mutableStateOf("Orta") }
    var yarnInfo by remember { mutableStateOf("") }
    var hookSize by remember { mutableStateOf("") }
    var recipeNote by remember { mutableStateOf("") }

    var socialText by remember { mutableStateOf("") }
    var socialTopic by remember { mutableStateOf(CuratedCatalog.socialComposeTopics.first()) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            decodePostPhotoUri(context, uri)?.let { bmp ->
                if (creationMode == PostCreationMode.RECIPE) {
                    recipeBitmap = bmp
                } else {
                    socialBitmap = bmp
                }
            }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    val canSubmitRecipe = title.trim().isNotBlank() &&
        yarnInfo.trim().isNotBlank() &&
        hookSize.trim().isNotBlank() &&
        !isPublishingRecipe

    val canSubmitSocial = socialText.trim().isNotBlank() && !isPublishingSocial

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (creationMode == PostCreationMode.SOCIAL) {
                            "İlmek Akışı'na Yaz"
                        } else {
                            "Yeni Örgü Tarifi Yükle"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (creationMode == PostCreationMode.SOCIAL) {
                            "Anlık düşünceni, sorunu veya günlüğünü paylaş"
                        } else {
                            "İp, tığ ve adım adım yapılış notuyla tarif ekle"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_create_post_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Paylaşım formunu kapat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

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
                    CreationModePill(
                        label = "Sohbet / Akış",
                        icon = Icons.Outlined.Forum,
                        selected = creationMode == PostCreationMode.SOCIAL,
                        onClick = { creationMode = PostCreationMode.SOCIAL },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_mode_social")
                    )
                    CreationModePill(
                        label = "Tarif Yükle",
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        selected = creationMode == PostCreationMode.RECIPE,
                        onClick = { creationMode = PostCreationMode.RECIPE },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_mode_recipe")
                    )
                }
            }

            if (creationMode == PostCreationMode.SOCIAL) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Konu Başlığı",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CuratedCatalog.socialComposeTopics.forEach { topic ->
                            val isSelected = socialTopic == topic
                            FilterChip(
                                selected = isSelected,
                                onClick = { socialTopic = topic },
                                label = { Text(topic, style = MaterialTheme.typography.labelMedium) },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("create_social_topic_$topic")
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = socialText,
                    onValueChange = { if (it.length <= 500) socialText = it },
                    label = { Text("Neler örüyorsun? *") },
                    placeholder = { Text("Günün örgü notunu, ip sorunu veya fikrini paylaş...") },
                    minLines = 4,
                    maxLines = 7,
                    supportingText = { Text("${socialText.length}/500 karakter") },
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_social_text_input")
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("create_social_pick_photo_box")
                ) {
                    if (socialBitmap != null) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                bitmap = socialBitmap!!.asImageBitmap(),
                                contentDescription = "Seçilen paylaşım fotoğrafı",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { socialBitmap = null }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fotoğrafı kaldır",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Fotoğraf ekle",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "İsteğe bağlı fotoğraf ekle",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                CraftPillButton(
                    text = if (isPublishingSocial) "Paylaşılıyor..." else "Akışta Paylaş",
                    onClick = {
                        onSubmitSocialPost(socialText.trim(), socialTopic, socialBitmap)
                    },
                    enabled = canSubmitSocial,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_create_social_button"),
                    leadingIcon = {
                        if (isPublishingSocial) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("create_post_pick_photo_box")
                ) {
                    if (recipeBitmap != null) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                bitmap = recipeBitmap!!.asImageBitmap(),
                                contentDescription = "Seçilen ürün fotoğrafı",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Fotoğrafı Değiştir",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Fotoğraf seç",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "Tarif / Model Fotoğrafı Seç",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Galerinden bitmiş örgü fotoğrafı ekleyebilirsin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Model / Tarif Adı *") },
                    placeholder = { Text("Örn: Papatyalı Pastel Hırka") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_title_input")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CuratedCatalog.postCreationCategories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("create_post_category_$cat")
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Zorluk Seviyesi",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Başlangıç", "Orta", "İleri").forEach { diff ->
                            val isSelected = selectedDifficulty == diff
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDifficulty = diff },
                                label = { Text(diff, style = MaterialTheme.typography.labelMedium) },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                modifier = Modifier.testTag("create_post_difficulty_$diff")
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = yarnInfo,
                    onValueChange = { yarnInfo = it },
                    label = { Text("Kullanılan İp & Marka *") },
                    placeholder = { Text("Örn: Alize Cotton Gold • Krem & Adaçayı") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_yarn_input")
                )

                OutlinedTextField(
                    value = hookSize,
                    onValueChange = { hookSize = it },
                    label = { Text("Tığ / Şiş Numarası *") },
                    placeholder = { Text("Örn: 3.5 mm tığ") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_hook_input")
                )

                OutlinedTextField(
                    value = recipeNote,
                    onValueChange = { recipeNote = it },
                    label = { Text("Tarif / Yapım Notu (İsteğe Bağlı)") },
                    placeholder = { Text("İlmek sayıları, püf noktaları veya sıra notlarını paylaş...") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_note_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                CraftPillButton(
                    text = if (isPublishingRecipe) "Yükleniyor..." else "Tarifi Paylaş",
                    onClick = {
                        onSubmitRecipePost(
                            recipeBitmap,
                            title.trim(),
                            selectedCategory,
                            selectedDifficulty,
                            yarnInfo.trim(),
                            hookSize.trim(),
                            recipeNote.trim()
                        )
                    },
                    enabled = canSubmitRecipe,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_create_post_button"),
                    leadingIcon = {
                        if (isPublishingRecipe) {
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
    }
}

@Composable
private fun CreationModePill(
    label: String,
    icon: ImageVector,
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
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Suppress("DEPRECATION")
private fun decodePostPhotoUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (_: Exception) {
        null
    }
}
