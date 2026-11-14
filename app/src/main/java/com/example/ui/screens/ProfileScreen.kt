package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CraftAchievementBadge
import com.example.data.GamificationEngine
import com.example.data.UserGamificationState
import com.example.data.model.DiscoverItem
import com.example.data.model.SavedRecipe
import com.example.data.model.SocialFeedItem
import com.example.data.model.UserProfile
import com.example.ui.UiState
import com.example.ui.components.BadgeDetailDialog
import com.example.ui.components.BadgesShowcaseTab
import com.example.ui.components.CraftBadge
import com.example.ui.components.CraftCard
import com.example.ui.components.CraftOutlinedPillButton
import com.example.ui.components.CraftPillButton
import com.example.ui.components.CraftPostImage
import com.example.ui.components.UserAvatar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: UserProfile,
    userEmail: String,
    myPosts: List<DiscoverItem>,
    mySocialPosts: List<SocialFeedItem> = emptyList(),
    savedRecipesState: UiState<List<SavedRecipe>>,
    recipeRowCounts: Map<String, Int> = emptyMap(),
    gamificationState: UserGamificationState = GamificationEngine.calculateUserGamification(0, 0, 0, 0, 0),
    selectedTabOverride: Int? = null,
    onClearTabOverride: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPostClick: (DiscoverItem) -> Unit,
    onEditMyPost: (DiscoverItem) -> Unit = {},
    onDeleteMyPost: (String) -> Unit,
    onSocialPostClick: (SocialFeedItem) -> Unit = {},
    onEditMySocialPost: (SocialFeedItem) -> Unit = {},
    onDeleteMySocialPost: (String) -> Unit = {},
    onCreatePostClick: () -> Unit,
    onToggleFavoriteSaved: (SavedRecipe) -> Unit,
    onDeleteSavedRecipe: (String) -> Unit,
    onIncrementRecipeRow: (String) -> Unit = {},
    onResetRecipeRow: (String) -> Unit = {},
    onGoToAiTab: () -> Unit,
    onGoToDiscoverTab: () -> Unit,
    onGoToAtelierTab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var inspectedBadge by remember { mutableStateOf<CraftAchievementBadge?>(null) }

    LaunchedEffect(selectedTabOverride) {
        if (selectedTabOverride != null) {
            selectedTab = selectedTabOverride
            onClearTabOverride()
        }
    }

    val savedCount = (savedRecipesState as? UiState.Success)?.data?.size ?: 0
    val totalMyPostsCount = myPosts.size + mySocialPosts.size
    val level = gamificationState.levelInfo

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            // Profile Header Card
            CraftCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        UserAvatar(
                            photoUrl = profile.photoUrl,
                            displayName = profile.displayName,
                            size = 68.dp
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = profile.username,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (userEmail.isNotBlank()) {
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("profile_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Ayarlar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (profile.bio.isNotBlank()) {
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (profile.specialtyTag.isNotBlank()) {
                            CraftBadge(
                                text = profile.specialtyTag,
                                isTerracotta = true
                            )
                        }
                        CraftBadge(
                            text = "$totalMyPostsCount Paylaşım",
                            isTerracotta = false
                        )
                        CraftBadge(
                            text = "${gamificationState.totalPoints} Puan",
                            isTerracotta = true
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (level.isTerracottaTier) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            border = BorderStroke(
                                1.dp,
                                (if (level.isTerracottaTier) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }).copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedTab = 2 }
                                .testTag("profile_rank_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WorkspacePremium,
                                    contentDescription = "Başarı seviyesi",
                                    tint = if (level.isTerracottaTier) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Sv.${level.level} • ${level.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (level.isTerracottaTier) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    CraftOutlinedPillButton(
                        text = "Profili Düzenle",
                        onClick = onEditProfileClick,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_edit_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Three Profile Sub-tabs
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProfileTabPill(
                        label = "Paylaşımlar ($totalMyPostsCount)",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_tab_my_posts")
                    )
                    ProfileTabPill(
                        label = "Kayıtlarım ($savedCount)",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_tab_saved")
                    )
                    ProfileTabPill(
                        label = "Rozetler (${gamificationState.unlockedBadgesCount}/${gamificationState.badges.size})",
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_tab_badges")
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> {
                        if (myPosts.isEmpty() && mySocialPosts.isEmpty()) {
                            EmptyMyPostsState(onCreatePostClick = onCreatePostClick)
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(mySocialPosts, key = { "social_${it.id}" }) { socialPost ->
                                    MySocialPostRowCard(
                                        item = socialPost,
                                        onClick = { onSocialPostClick(socialPost) },
                                        onEditClick = { onEditMySocialPost(socialPost) },
                                        onDeleteClick = { onDeleteMySocialPost(socialPost.id) }
                                    )
                                }
                                items(myPosts, key = { "recipe_${it.id}" }) { post ->
                                    MyPostRowCard(
                                        item = post,
                                        onClick = { onPostClick(post) },
                                        onEditClick = { onEditMyPost(post) },
                                        onDeleteClick = { onDeleteMyPost(post.id) }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        SavedRecipesScreen(
                            uiState = savedRecipesState,
                            recipeRowCounts = recipeRowCounts,
                            onIncrementRecipeRow = onIncrementRecipeRow,
                            onResetRecipeRow = onResetRecipeRow,
                            onToggleFavorite = onToggleFavoriteSaved,
                            onDeleteRecipe = onDeleteSavedRecipe,
                            onGoToAiTab = onGoToAiTab,
                            onGoToDiscoverTab = onGoToDiscoverTab,
                            onGoToAtelierTab = onGoToAtelierTab
                        )
                    }
                    else -> {
                        BadgesShowcaseTab(
                            gamification = gamificationState,
                            onBadgeClick = { badge -> inspectedBadge = badge }
                        )
                    }
                }
            }
        }
    }

    inspectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            totalPoints = gamificationState.totalPoints,
            onDismiss = { inspectedBadge = null }
        )
    }
}

@Composable
private fun MyPostRowCard(
    item: DiscoverItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    CraftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my_post_card_${item.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            CraftPostImage(
                imageUrl = item.imageUrl,
                imageKey = item.imageKey,
                contentDescription = item.title,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CraftBadge(text = "Tarif • ${item.category}", isTerracotta = false)
                    CraftBadge(text = item.difficulty, isTerracotta = true)
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.yarnType} • ${item.hookSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Beğeni sayısı",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = item.baseLikes.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Yorum sayısı",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = item.commentsCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("menu_my_post_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Paylaşım seçenekleri",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    DropdownMenuItem(
                        text = { Text("Tarifi/Açıklamayı Düzenle", color = MaterialTheme.colorScheme.onSurface) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
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
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MySocialPostRowCard(
    item: SocialFeedItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    CraftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my_social_post_card_${item.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (item.imageUrl.isNotBlank() || item.imageKey.isNotBlank()) {
                CraftPostImage(
                    imageUrl = item.imageUrl,
                    imageKey = item.imageKey,
                    contentDescription = "Akış fotoğrafı",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CraftBadge(text = "İlmek Akışı", isTerracotta = false)
                    CraftBadge(text = item.topic, isTerracotta = item.topic == "Soru Sor")
                }
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Beğeni sayısı",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = item.baseLikes.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Yanıt sayısı",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = item.repliesCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("menu_my_social_post_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Gönderi seçenekleri",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    DropdownMenuItem(
                        text = { Text("Açıklamayı Düzenle", color = MaterialTheme.colorScheme.onSurface) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
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
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMyPostsState(onCreatePostClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CraftCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text(
                    text = "Henüz Paylaşımın Yok",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Ördüğün ilk ürünü veya AI ile çıkardığın bir tarifi toplulukla paylaşarak profilini oluştur.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                CraftPillButton(
                    text = "İlk Paylaşımını Yap",
                    onClick = onCreatePostClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_empty_create_post_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
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
private fun ProfileTabPill(
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
