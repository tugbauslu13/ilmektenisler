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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.CuratedCatalog
import com.example.data.model.DiscoverItem
import com.example.data.model.SocialFeedItem
import com.example.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileBottomSheet(
    profile: UserProfile,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        displayName: String,
        username: String,
        bio: String,
        specialtyTag: String,
        newAvatarBitmap: Bitmap?,
        removeAvatar: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var displayName by remember(profile.userId) { mutableStateOf(profile.displayName) }
    var usernameInput by remember(profile.userId) { mutableStateOf(profile.username.removePrefix("@")) }
    var bio by remember(profile.userId) { mutableStateOf(profile.bio) }
    var specialtyTag by remember(profile.userId) { mutableStateOf(profile.specialtyTag) }
    var pickedAvatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var removeAvatar by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            decodePickerUriToBitmap(context, uri)?.let { bmp ->
                pickedAvatarBitmap = bmp
                removeAvatar = false
            }
        }
    }

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
                        text = "Profili Düzenle",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Adın, @kullanıcıadın ve uzmanlığın tüm paylaşımlarda güncellenir",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.testTag("close_edit_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            CraftCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val currentBmp = pickedAvatarBitmap
                    if (currentBmp != null) {
                        Image(
                            bitmap = currentBmp.asImageBitmap(),
                            contentDescription = "Yeni profil fotoğrafı",
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        UserAvatar(
                            photoUrl = if (removeAvatar) "" else profile.photoUrl,
                            displayName = displayName.ifBlank { profile.displayName },
                            size = 68.dp
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CraftOutlinedPillButton(
                            text = "Profil Fotoğrafı Seç",
                            onClick = {
                                avatarPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_profile_pick_photo_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        if (pickedAvatarBitmap != null || (!removeAvatar && profile.photoUrl.isNotBlank())) {
                            CraftOutlinedPillButton(
                                text = "Fotoğrafı Kaldır",
                                onClick = {
                                    pickedAvatarBitmap = null
                                    removeAvatar = true
                                },
                                borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_profile_remove_photo_button"),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { if (it.length <= 80) displayName = it },
                label = { Text("Görünen Ad *") },
                placeholder = { Text("Örn: Tuba Süslü") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_name_input")
            )

            OutlinedTextField(
                value = usernameInput,
                onValueChange = { raw ->
                    val filtered = raw.removePrefix("@").take(30)
                    usernameInput = filtered
                },
                label = { Text("Kullanıcı Adı *") },
                prefix = {
                    Text(
                        text = "@",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingText = {
                    Text("Benzersiz kullanıcı adın (küçük harf, rakam, nokta veya alt çizgi)")
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_username_input")
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 200) bio = it },
                label = { Text("Kısa Biyografi") },
                placeholder = { Text("Örgü tarzından, favori iplerinden veya projelerinden bahset...") },
                supportingText = { Text("${bio.length}/200") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_bio_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Favori Tığ / Uzmanlık Etiketi",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CuratedCatalog.specialtyTags.forEach { tag ->
                        val isSelected = specialtyTag == tag
                        FilterChip(
                            selected = isSelected,
                            onClick = { specialtyTag = if (isSelected) "" else tag },
                            label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
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
                            modifier = Modifier.testTag("specialty_chip_$tag")
                        )
                    }
                }
                OutlinedTextField(
                    value = specialtyTag,
                    onValueChange = { if (it.length <= 60) specialtyTag = it },
                    label = { Text("Veya özel uzmanlık yaz") },
                    placeholder = { Text("Örn: 3.5 mm Pamuk Sever") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = editFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_specialty_input")
                )
            }

            val canSave = displayName.trim().isNotBlank() &&
                UserProfile.normalizeUsernameLower(usernameInput, displayName).isNotBlank() &&
                !isSaving

            CraftPillButton(
                text = if (isSaving) "Kaydediliyor..." else "Değişiklikleri Kaydet",
                onClick = {
                    onSave(
                        displayName.trim(),
                        usernameInput.trim(),
                        bio.trim(),
                        specialtyTag.trim(),
                        pickedAvatarBitmap,
                        removeAvatar
                    )
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_profile_button"),
                leadingIcon = {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditCommunityPostBottomSheet(
    item: DiscoverItem,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        difficulty: String,
        yarnType: String,
        hookSize: String,
        recipeNote: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(item.id) { mutableStateOf(item.title) }
    var category by remember(item.id) {
        mutableStateOf(
            if (CuratedCatalog.postCreationCategories.contains(item.category)) {
                item.category
            } else {
                CuratedCatalog.postCreationCategories.first()
            }
        )
    }
    var difficulty by remember(item.id) {
        mutableStateOf(
            if (listOf("Başlangıç", "Orta", "İleri").contains(item.difficulty)) item.difficulty else "Orta"
        )
    }
    var yarnType by remember(item.id) { mutableStateOf(item.yarnType) }
    var hookSize by remember(item.id) { mutableStateOf(item.hookSize) }
    var recipeNote by remember(item.id) {
        mutableStateOf(
            item.recipeNote.ifBlank { item.steps.joinToString("\n") }
        )
    }

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
                Text(
                    text = "Tarifi Düzenle",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    enabled = !isUpdating,
                    modifier = Modifier.testTag("close_edit_community_post_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 120) title = it },
                label = { Text("Ürün Adı *") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_community_title_input")
            )

            Text(
                text = "Kategori",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CuratedCatalog.postCreationCategories.forEach { cat ->
                    val selected = category == cat
                    FilterChip(
                        selected = selected,
                        onClick = { category = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Text(
                text = "Zorluk",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Başlangıç", "Orta", "İleri").forEach { diff ->
                    val selected = difficulty == diff
                    FilterChip(
                        selected = selected,
                        onClick = { difficulty = diff },
                        label = { Text(diff, style = MaterialTheme.typography.labelMedium) },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        )
                    )
                }
            }

            OutlinedTextField(
                value = yarnType,
                onValueChange = { if (it.length <= 120) yarnType = it },
                label = { Text("Kullanılan İp *") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_community_yarn_input")
            )

            OutlinedTextField(
                value = hookSize,
                onValueChange = { if (it.length <= 60) hookSize = it },
                label = { Text("Tığ / Şiş Numarası *") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_community_hook_input")
            )

            OutlinedTextField(
                value = recipeNote,
                onValueChange = { if (it.length <= 2500) recipeNote = it },
                label = { Text("Tarif / Açıklama & Adımlar") },
                minLines = 4,
                maxLines = 8,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_community_note_input")
            )

            val canSave = title.trim().isNotBlank() &&
                yarnType.trim().isNotBlank() &&
                hookSize.trim().isNotBlank() &&
                !isUpdating

            CraftPillButton(
                text = if (isUpdating) "Güncelleniyor..." else "Değişiklikleri Kaydet",
                onClick = {
                    onSave(
                        title.trim(),
                        category,
                        difficulty,
                        yarnType.trim(),
                        hookSize.trim(),
                        recipeNote.trim()
                    )
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_edited_community_post_button"),
                leadingIcon = {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSocialPostDialog(
    item: SocialFeedItem,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (topic: String, text: String) -> Unit
) {
    var topic by remember(item.id) { mutableStateOf(item.topic) }
    var text by remember(item.id) { mutableStateOf(item.text) }

    Dialog(onDismissRequest = onDismiss) {
        CraftCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_social_post_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gönderiyi Düzenle",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, enabled = !isUpdating) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CuratedCatalog.socialComposeTopics.forEach { option ->
                        val isSelected = topic == option
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { topic = option }
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 500) text = it },
                    label = { Text("Gönderi Metni *") },
                    supportingText = { Text("${text.length}/500") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(16.dp),
                    colors = editFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_social_text_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CraftOutlinedPillButton(
                        text = "Vazgeç",
                        onClick = onDismiss,
                        enabled = !isUpdating,
                        modifier = Modifier.weight(0.42f)
                    )
                    CraftPillButton(
                        text = if (isUpdating) "Kaydediliyor" else "Kaydet",
                        onClick = { onSave(topic, text.trim()) },
                        enabled = text.trim().isNotBlank() && !isUpdating,
                        modifier = Modifier
                            .weight(0.58f)
                            .testTag("save_edited_social_post_button")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAtelierProductBottomSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        bitmap: Bitmap?,
        title: String,
        subtitle: String,
        priceInput: String,
        tag: String,
        yarnDetail: String,
        dimensions: String,
        shopierUrl: String,
        dolapUrl: String
    ) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf(CuratedCatalog.atelierProductTags.first()) }
    var yarnDetail by remember { mutableStateOf("") }
    var dimensions by remember { mutableStateOf("") }
    var shopierUrl by remember { mutableStateOf("https://www.shopier.com/ilmektenisler") }
    var dolapUrl by remember { mutableStateOf("https://dolap.com/profil/ilmektenisler") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            decodePickerUriToBitmap(context, uri)?.let { selectedBitmap = it }
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Yeni Atölye Ürünü Ekle",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@ilmektenisler vitrininde anında yayınlanır",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.testTag("close_add_atelier_product_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (selectedBitmap != null) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Seçilen ürün fotoğrafı",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(
                            onClick = { selectedBitmap = null },
                            modifier = Modifier.size(36.dp)
                        ) {
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
                CraftOutlinedPillButton(
                    text = "Ürün Fotoğrafı Seç",
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_atelier_product_photo_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 120) title = it },
                label = { Text("Ürün Adı *") },
                placeholder = { Text("Örn: Kadife İp Tavşan Uyku Arkadaşı") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("atelier_product_title_input")
            )

            OutlinedTextField(
                value = priceInput,
                onValueChange = { if (it.length <= 30) priceInput = it },
                label = { Text("Fiyat (₺) *") },
                placeholder = { Text("Örn: 750 ₺") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("atelier_product_price_input")
            )

            OutlinedTextField(
                value = subtitle,
                onValueChange = { if (it.length <= 400) subtitle = it },
                label = { Text("Ürün Açıklaması *") },
                placeholder = { Text("İp içeriği, örgü tekniği, beden veya kullanım detayı...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("atelier_product_subtitle_input")
            )

            Text(
                text = "Vitrin Etiketi",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CuratedCatalog.atelierProductTags.forEach { tag ->
                    val selected = selectedTag == tag
                    FilterChip(
                        selected = selected,
                        onClick = { selectedTag = tag },
                        label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = yarnDetail,
                    onValueChange = { if (it.length <= 120) yarnDetail = it },
                    label = { Text("Materyal") },
                    placeholder = { Text("Organik Pamuk") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = editFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dimensions,
                    onValueChange = { if (it.length <= 80) dimensions = it },
                    label = { Text("Ölçü") },
                    placeholder = { Text("28 cm") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = editFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = shopierUrl,
                onValueChange = { if (it.length <= 500) shopierUrl = it },
                label = { Text("Shopier Ürün Linki") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("atelier_product_shopier_input")
            )

            OutlinedTextField(
                value = dolapUrl,
                onValueChange = { if (it.length <= 500) dolapUrl = it },
                label = { Text("Dolap Ürün Linki") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = editFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("atelier_product_dolap_input")
            )

            val canSubmit = title.trim().isNotBlank() &&
                priceInput.trim().isNotBlank() &&
                subtitle.trim().isNotBlank() &&
                !isSaving

            CraftPillButton(
                text = if (isSaving) "Ekleniyor..." else "Vitrine Ekle",
                onClick = {
                    onSubmit(
                        selectedBitmap,
                        title.trim(),
                        subtitle.trim(),
                        priceInput.trim(),
                        selectedTag,
                        yarnDetail.trim(),
                        dimensions.trim(),
                        shopierUrl.trim(),
                        dolapUrl.trim()
                    )
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_atelier_product_button"),
                leadingIcon = {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)

@Suppress("DEPRECATION")
private fun decodePickerUriToBitmap(context: Context, uri: Uri): Bitmap? {
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
