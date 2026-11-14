package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CraftAchievementBadge
import com.example.data.GamificationEngine
import com.example.data.UserGamificationState

@Composable
fun AuthorRankPill(
    rankTitle: String,
    modifier: Modifier = Modifier
) {
    if (rankTitle.isBlank()) return
    val isMasterOrGold = rankTitle == "İlmek Ustası" || rankTitle == "Altın Yumak"
    val bg = if (isMasterOrGold) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val fg = if (isMasterOrGold) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = if (isMasterOrGold) Icons.Filled.WorkspacePremium else Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = rankTitle,
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BadgesShowcaseTab(
    gamification: UserGamificationState,
    onBadgeClick: (CraftAchievementBadge) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_badges_list")
    ) {
        item(key = "level_hero") {
            CraftLevelHeroCard(gamification = gamification)
        }

        item(key = "points_guide") {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "İlmek Puanı Nasıl Kazanılır?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tarif paylaş +${GamificationEngine.POINTS_PER_RECIPE} • " +
                            "Akış notu +${GamificationEngine.POINTS_PER_SOCIAL_POST} • " +
                            "Tarif kaydet +${GamificationEngine.POINTS_PER_SAVED_RECIPE} • " +
                            "Alınan beğeni +${GamificationEngine.POINTS_PER_LIKE_RECEIVED} • " +
                            "Beğeni ver +${GamificationEngine.POINTS_PER_LIKE_GIVEN}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "badges_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Başarı Rozetleri",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                CraftBadge(
                    text = "${gamification.unlockedBadgesCount} / ${gamification.badges.size} Kazanıldı",
                    isTerracotta = gamification.unlockedBadgesCount > 0
                )
            }
        }

        items(gamification.badges, key = { it.id }) { badge ->
            AchievementBadgeCard(
                badge = badge,
                onClick = { onBadgeClick(badge) }
            )
        }
    }
}

@Composable
fun CraftLevelHeroCard(
    gamification: UserGamificationState,
    modifier: Modifier = Modifier
) {
    val level = gamification.levelInfo
    val animatedProgress by animateFloatAsState(
        targetValue = level.progressFraction,
        animationSpec = tween(durationMillis = 550),
        label = "level_progress"
    )
    val accent = if (level.isTerracottaTier) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val badgeBg = if (level.isTerracottaTier) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    CraftCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("craft_level_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            badgeBg.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2f - 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            color = badgeBg,
                            radius = radius,
                            center = center
                        )
                        drawCircle(
                            color = accent,
                            radius = radius,
                            center = center,
                            style = Stroke(width = 3.5f)
                        )
                        drawCircle(
                            color = accent.copy(alpha = 0.35f),
                            radius = radius - 6f,
                            center = center,
                            style = Stroke(width = 2f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CraftBadge(
                            text = "Seviye ${level.level}",
                            isTerracotta = level.isTerracottaTier
                        )
                        Text(
                            text = "${gamification.totalPoints} İlmek Puanı",
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = level.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = level.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(50)),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                    strokeCap = StrokeCap.Round
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val nextLabel = if (level.nextLevelPoints != null && level.nextLevelTitle != null) {
                        "Sonraki: ${level.nextLevelTitle} (${level.nextLevelPoints} puan)"
                    } else {
                        "En yüksek ustalığa ulaştın!"
                    }
                    Text(
                        text = nextLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (level.nextLevelPoints != null) {
                        Text(
                            text = "Kalan: ${(level.nextLevelPoints - level.points).coerceAtLeast(0)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GamificationStatTile(
                    value = gamification.recipesShared.toString(),
                    label = "Tarif",
                    modifier = Modifier.weight(1f)
                )
                GamificationStatTile(
                    value = gamification.socialPostsShared.toString(),
                    label = "Akış",
                    modifier = Modifier.weight(1f)
                )
                GamificationStatTile(
                    value = gamification.likesReceived.toString(),
                    label = "Beğeni",
                    modifier = Modifier.weight(1f)
                )
                GamificationStatTile(
                    value = "${gamification.unlockedBadgesCount}/${gamification.badges.size}",
                    label = "Rozet",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GamificationStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AchievementBadgeCard(
    badge: CraftAchievementBadge,
    onClick: () -> Unit
) {
    val accent = if (badge.isTerracotta) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val iconContainer = when {
        !badge.isUnlocked -> MaterialTheme.colorScheme.background
        badge.isTerracotta -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val animatedProgress by animateFloatAsState(
        targetValue = badge.progressFraction,
        animationSpec = tween(durationMillis = 500),
        label = "badge_progress_${badge.id}"
    )

    CraftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("achievement_badge_card_${badge.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconContainer,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (badge.isUnlocked) accent else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = resolveBadgeIcon(badge.id),
                        contentDescription = badge.title,
                        tint = if (badge.isUnlocked) {
                            accent
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        },
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (badge.isUnlocked) 1f else 0.88f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (badge.isUnlocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Kazanıldı",
                                tint = accent,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Kazanıldı",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Kilitli",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${badge.currentValue}/${badge.targetValue}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (badge.isUnlocked) accent else MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                    strokeCap = StrokeCap.Round
                )

                Text(
                    text = badge.requirementHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (badge.isUnlocked) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (badge.isUnlocked) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun BadgeDetailDialog(
    badge: CraftAchievementBadge,
    totalPoints: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accent = if (badge.isTerracotta) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val bg = when {
        !badge.isUnlocked -> MaterialTheme.colorScheme.surface
        badge.isTerracotta -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = bg,
                border = BorderStroke(
                    1.5.dp,
                    if (badge.isUnlocked) accent else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = resolveBadgeIcon(badge.id),
                        contentDescription = badge.title,
                        tint = if (badge.isUnlocked) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                CraftBadge(
                    text = if (badge.isUnlocked) "Rozet Kazanıldı" else "İlerleme: ${badge.currentValue}/${badge.targetValue}",
                    isTerracotta = badge.isTerracotta
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = badge.requirementHint,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (badge.isUnlocked) accent else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = badge.pointsBonusLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (badge.isUnlocked) {
                CraftPillButton(
                    text = "Paylaş",
                    onClick = {
                        shareAchievementBadge(context, badge, totalPoints)
                        onDismiss()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            } else {
                CraftPillButton(
                    text = "Tamam",
                    onClick = onDismiss
                )
            }
        },
        dismissButton = {
            if (badge.isUnlocked) {
                TextButton(onClick = onDismiss) {
                    Text("Kapat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}

@Composable
fun BadgeUnlockedCelebrationDialog(
    badge: CraftAchievementBadge,
    onViewBadges: () -> Unit,
    onDismiss: () -> Unit
) {
    val scale = remember(badge.id) { Animatable(0.75f) }
    LaunchedEffect(badge.id) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val accent = if (badge.isTerracotta) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val bg = if (badge.isTerracotta) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = bg,
                border = BorderStroke(2.dp, accent),
                modifier = Modifier
                    .size(74.dp)
                    .scale(scale.value)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = resolveBadgeIcon(badge.id),
                        contentDescription = badge.title,
                        tint = accent,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CraftBadge(text = "Yeni Başarı Rozeti!", isTerracotta = badge.isTerracotta)
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = "${badge.description}\n\n${badge.requirementHint}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            CraftPillButton(
                text = "Rozetlerimi Gör",
                onClick = onViewBadges,
                containerColor = accent,
                modifier = Modifier.testTag("celebration_view_badges_button")
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("celebration_dismiss_button")
            ) {
                Text("Harika!", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

private fun resolveBadgeIcon(badgeId: String): ImageVector = when (badgeId) {
    "badge_first_stitch" -> Icons.Filled.AutoAwesome
    "badge_creative_knitter" -> Icons.Filled.EmojiEvents
    "badge_golden_yarn" -> Icons.Filled.Favorite
    "badge_thread_circle" -> Icons.Filled.Forum
    "badge_community_friend" -> Icons.Filled.Groups
    "badge_stitch_master" -> Icons.Filled.WorkspacePremium
    else -> Icons.Filled.Star
}

fun shareAchievementBadge(
    context: Context,
    badge: CraftAchievementBadge,
    totalPoints: Int
) {
    val shareText = buildString {
        appendLine("🏆 İlmekten İşler'de yeni bir başarı rozeti kazandım: ${badge.title}!")
        appendLine(badge.description)
        appendLine("Toplam İlmek Puanım: $totalPoints")
        appendLine()
        append("Sen de örgülerini ve tariflerini İlmekten İşler topluluğunda paylaş!")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "İlmekten İşler - ${badge.title} Rozeti")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    val chooser = Intent.createChooser(sendIntent, "Rozeti Paylaş").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(chooser)
    } catch (_: Exception) {
        // No handler
    }
}
