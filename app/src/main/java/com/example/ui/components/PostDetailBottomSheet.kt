package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CommunityMake
import com.example.data.model.DiscoverItem
import com.example.data.model.PostComment
import com.example.ui.utils.RecipeShareFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostDetailBottomSheet(
    item: DiscoverItem,
    isLiked: Boolean,
    comments: List<PostComment>,
    makes: List<CommunityMake> = emptyList(),
    isPublishingMake: Boolean = false,
    rowCount: Int = 1,
    currentUserId: String,
    authorRankTitle: String? = null,
    isOwnPost: Boolean = false,
    onDismiss: () -> Unit,
    onEditPost: () -> Unit = {},
    onDeletePost: () -> Unit = {},
    onToggleLike: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onSendComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onSubmitMake: (Bitmap, String, () -> Unit) -> Unit = { _, _, _ -> },
    onDeleteMake: (String) -> Unit = {},
    onIncrementRow: () -> Unit = {},
    onResetRow: () -> Unit = {},
    onGoToAtelier: () -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val checkedSteps = remember(item.id) { mutableStateMapOf<Int, Boolean>() }
    var commentDraft by remember(item.id) { mutableStateOf("") }
    var showMakeComposer by remember(item.id) { mutableStateOf(false) }
    var makeBitmap by remember(item.id) { mutableStateOf<Bitmap?>(null) }
    var makeNoteDraft by remember(item.id) { mutableStateOf("") }
    var previewMake by remember { mutableStateOf<CommunityMake?>(null) }
    var isCounterPinned by rememberSaveable(item.id) { mutableStateOf(true) }
    var ownerMenuExpanded by remember { mutableStateOf(false) }

    val commentDateFormatter = remember {
        SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("tr-TR"))
    }

    val makePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            decodeMakePhotoUri(context, uri)?.let { decoded ->
                makeBitmap = decoded
                showMakeComposer = true
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
                .heightIn(max = 720.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        CraftBadge(text = item.category, isTerracotta = false)
                        CraftBadge(text = item.difficulty, isTerracotta = true)
                    }
                    if (isOwnPost) {
                        Box {
                            IconButton(
                                onClick = { ownerMenuExpanded = true },
                                modifier = Modifier.testTag("detail_owner_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Gönderi seçenekleri",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = ownerMenuExpanded,
                                onDismissRequest = { ownerMenuExpanded = false },
                                containerColor = MaterialTheme.colorScheme.background
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Tarifi/Açıklamayı Düzenle",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        ownerMenuExpanded = false
                                        onEditPost()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        ownerMenuExpanded = false
                                        onDeletePost()
                                    }
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_post_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Gönderi detayını kapat",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                CraftPostImage(
                    imageUrl = item.imageUrl,
                    imageKey = item.imageKey,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UserAvatar(
                        photoUrl = item.authorPhotoUrl,
                        displayName = item.authorName,
                        size = 40.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.authorName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!authorRankTitle.isNullOrBlank()) {
                                AuthorRankPill(rankTitle = authorRankTitle)
                            }
                        }
                        Text(
                            text = buildString {
                                append(item.authorHandle)
                                if (item.authorSpecialtyTag.isNotBlank()) {
                                    append(" • ${item.authorSpecialtyTag}")
                                }
                                if (item.finishedDate.isNotBlank()) {
                                    append(" • ${item.finishedDate}")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val displayedLikes = if (item.isCommunityUserPost) {
                        item.baseLikes
                    } else {
                        item.baseLikes + if (isLiked) 1 else 0
                    }
                    CraftOutlinedPillButton(
                        text = "$displayedLikes",
                        onClick = onToggleLike,
                        borderColor = if (isLiked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                        contentColor = if (isLiked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("detail_like_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isLiked) "Beğeniyi kaldır" else "Beğen",
                                tint = if (isLiked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    CraftPillButton(
                        text = "Kaydet",
                        onClick = onSaveClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_save_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("detail_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Modeli paylaş",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                CraftCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailSpecRow("İp Türü", item.yarnType)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        DetailSpecRow("Tığ / Şiş No", item.hookSize)
                        if (item.yarnBrand.isNotBlank()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            DetailSpecRow("İp Markası", item.yarnBrand)
                        }
                        if (item.finishedDate.isNotBlank()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            DetailSpecRow("Tarih", item.finishedDate)
                        }
                    }
                }

                if (item.recipeNote.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Yapım Notu",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = item.recipeNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (item.steps.isNotEmpty()) {
                    Text(
                        text = "Yapım Tarifi (${item.steps.size} Adım)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.steps.forEachIndexed { index, stepText ->
                            val isDone = checkedSteps[index] == true
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { checkedSteps[index] = !isDone },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDone) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = if (isDone) "Tamamlandı" else "Tamamla",
                                        tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stepText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }
                            }
                        }
                    }
                }

                CraftOutlinedPillButton(
                    text = "Tarifi Paylaş / Kopyala",
                    onClick = {
                        val watermarked = RecipeShareFormatter.formatCommunityRecipe(item)
                        RecipeShareFormatter.copyAndShareText(
                            context = context,
                            subject = "${item.title} - İlmekten İşler",
                            formattedText = watermarked,
                            chooserTitle = "Tarifi Paylaş / Kopyala",
                            onCopiedFeedback = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("copy_share_community_recipe_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Topluluk Uygulamaları (${makes.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { showMakeComposer = !showMakeComposer },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("i_made_this_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ben de Ördüm!",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                AnimatedVisibility(visible = showMakeComposer) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_make_composer")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Senin Yorumunla Bu Model",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            val pickedBitmap = makeBitmap
                            if (pickedBitmap != null) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Image(
                                        bitmap = pickedBitmap.asImageBitmap(),
                                        contentDescription = "Seçilen örgü fotoğrafı",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
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
                                            onClick = { makeBitmap = null },
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
                                    text = "Ördüğün Fotoğrafı Seç",
                                    onClick = {
                                        makePhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    borderColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pick_make_photo_button"),
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
                                value = makeNoteDraft,
                                onValueChange = { makeNoteDraft = it },
                                placeholder = { Text("Kullandığın ip, renk veya deneyimini paylaş (isteğe bağlı)...") },
                                maxLines = 3,
                                shape = RoundedCornerShape(14.dp),
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
                                    .testTag("make_note_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CraftOutlinedPillButton(
                                    text = "Vazgeç",
                                    onClick = {
                                        showMakeComposer = false
                                        makeBitmap = null
                                        makeNoteDraft = ""
                                    },
                                    modifier = Modifier.weight(0.42f)
                                )
                                CraftPillButton(
                                    text = if (isPublishingMake) "Yükleniyor..." else "Galeriye Ekle",
                                    onClick = {
                                        val bmp = makeBitmap ?: return@CraftPillButton
                                        onSubmitMake(bmp, makeNoteDraft) {
                                            showMakeComposer = false
                                            makeBitmap = null
                                            makeNoteDraft = ""
                                        }
                                    },
                                    enabled = makeBitmap != null && !isPublishingMake,
                                    modifier = Modifier
                                        .weight(0.58f)
                                        .testTag("submit_community_make_button"),
                                    leadingIcon = if (isPublishingMake) {
                                        {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }

                if (makes.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Bu tarifi ilk ören sen ol! 'Ben de Ördüm!' butonuna basarak fotoğrafını topluluk uygulamalarına ekle.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_makes_gallery")
                    ) {
                        items(makes, key = { it.id }) { make ->
                            CommunityMakeGalleryCard(
                                make = make,
                                isAuthor = make.authorId == currentUserId,
                                onClick = { previewMake = make },
                                onDeleteClick = { onDeleteMake(make.id) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Yorumlar (${comments.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (comments.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Henüz yorum yok. İp numarası, ölçü veya yapım tekniği hakkında ilk soruyu sen sor!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("post_comments_list")
                    ) {
                        comments.forEach { comment ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    UserAvatar(
                                        photoUrl = comment.authorPhotoUrl,
                                        displayName = comment.authorName,
                                        size = 32.dp
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = comment.authorName,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            val timeLabel = comment.createdAt?.toDate()?.let {
                                                commentDateFormatter.format(it)
                                            } ?: "Şimdi"
                                            Text(
                                                text = timeLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = comment.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (comment.authorId == currentUserId) {
                                        IconButton(
                                            onClick = { onDeleteComment(comment.id) },
                                            modifier = Modifier.testTag("delete_comment_${comment.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Yorumu sil",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentDraft,
                        onValueChange = { commentDraft = it },
                        placeholder = { Text("Yorum yaz veya soru sor...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field")
                    )

                    Surface(
                        shape = CircleShape,
                        color = if (commentDraft.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(enabled = commentDraft.isNotBlank()) {
                                val textToSend = commentDraft.trim()
                                if (textToSend.isNotBlank()) {
                                    onSendComment(textToSend)
                                    commentDraft = ""
                                }
                            }
                            .testTag("send_comment_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Yorum gönder",
                                tint = if (commentDraft.isNotBlank()) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                AtelierOrderCtaCard(
                    onOrderClick = {
                        onDismiss()
                        onGoToAtelier()
                    }
                )
            }

            StickyRecipeRowCounterBar(
                rowCount = rowCount,
                isPinned = isCounterPinned,
                onTogglePin = { isCounterPinned = !isCounterPinned },
                onIncrementRow = onIncrementRow,
                onResetRow = onResetRow,
                modifier = Modifier.testTag("post_detail_sticky_row_counter")
            )
        }
    }

    previewMake?.let { selectedMake ->
        Dialog(onDismissRequest = { previewMake = null }) {
            CraftCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("community_make_preview_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(
                                photoUrl = selectedMake.authorPhotoUrl,
                                displayName = selectedMake.authorName,
                                size = 34.dp
                            )
                            Text(
                                text = selectedMake.authorName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { previewMake = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Önizlemeyi kapat",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    CraftPostImage(
                        imageUrl = selectedMake.imageUrl,
                        imageKey = "",
                        contentDescription = "${selectedMake.authorName} örgü uygulaması",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )

                    if (selectedMake.note.isNotBlank()) {
                        Text(
                            text = selectedMake.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityMakeGalleryCard(
    make: CommunityMake,
    isAuthor: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    CraftCard(
        modifier = Modifier
            .width(172.dp)
            .testTag("community_make_card_${make.id}"),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            CraftPostImage(
                imageUrl = make.imageUrl,
                imageKey = "",
                contentDescription = "${make.authorName} tarafından örüldü",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            if (isAuthor) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("delete_make_${make.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Uygulamayı sil",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UserAvatar(
                    photoUrl = make.authorPhotoUrl,
                    displayName = make.authorName,
                    size = 22.dp
                )
                Text(
                    text = make.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (make.note.isNotBlank()) {
                Text(
                    text = make.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DetailSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Suppress("DEPRECATION")
private fun decodeMakePhotoUri(context: Context, uri: Uri): Bitmap? {
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
