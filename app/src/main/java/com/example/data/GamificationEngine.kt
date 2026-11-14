package com.example.data

import com.example.data.model.DiscoverItem
import com.example.data.model.SocialFeedItem

data class CraftLevelInfo(
    val level: Int,
    val title: String,
    val subtitle: String,
    val points: Int,
    val currentTierFloor: Int,
    val nextLevelPoints: Int?,
    val nextLevelTitle: String?,
    val progressFraction: Float,
    val isTerracottaTier: Boolean
)

data class CraftAchievementBadge(
    val id: String,
    val title: String,
    val shortTag: String,
    val description: String,
    val requirementHint: String,
    val currentValue: Int,
    val targetValue: Int,
    val isUnlocked: Boolean,
    val pointsBonusLabel: String,
    val isTerracotta: Boolean
) {
    val progressFraction: Float
        get() = if (targetValue <= 0) 1f else (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
}

data class UserGamificationState(
    val totalPoints: Int,
    val levelInfo: CraftLevelInfo,
    val recipesShared: Int,
    val socialPostsShared: Int,
    val likesReceived: Int,
    val likesGiven: Int,
    val savedRecipesCount: Int,
    val badges: List<CraftAchievementBadge>
) {
    val unlockedBadgesCount: Int
        get() = badges.count { it.isUnlocked }
}

object GamificationEngine {

    const val POINTS_PER_RECIPE = 50
    const val POINTS_PER_SOCIAL_POST = 20
    const val POINTS_PER_LIKE_RECEIVED = 10
    const val POINTS_PER_SAVED_RECIPE = 15
    const val POINTS_PER_LIKE_GIVEN = 5

    fun calculateUserGamification(
        recipesShared: Int,
        socialPostsShared: Int,
        likesReceived: Int,
        likesGiven: Int,
        savedRecipesCount: Int
    ): UserGamificationState {
        val safeRecipes = recipesShared.coerceAtLeast(0)
        val safeSocial = socialPostsShared.coerceAtLeast(0)
        val safeLikesReceived = likesReceived.coerceAtLeast(0)
        val safeLikesGiven = likesGiven.coerceAtLeast(0)
        val safeSaved = savedRecipesCount.coerceAtLeast(0)

        val totalPoints = (safeRecipes * POINTS_PER_RECIPE) +
            (safeSocial * POINTS_PER_SOCIAL_POST) +
            (safeLikesReceived * POINTS_PER_LIKE_RECEIVED) +
            (safeSaved * POINTS_PER_SAVED_RECIPE) +
            (safeLikesGiven * POINTS_PER_LIKE_GIVEN)

        val levelInfo = resolveLevelInfo(totalPoints)
        val totalContributions = safeRecipes + safeSocial
        val communitySupportCount = safeLikesGiven + safeSaved

        val badges = listOf(
            CraftAchievementBadge(
                id = "badge_first_stitch",
                title = "İlk İlmek",
                shortTag = "İlk İlmek",
                description = "Topluluğa ilk eserini veya sohbet notunu bırakarak örgü çemberine katıldın.",
                requirementHint = if (totalContributions >= 1) {
                    "İlk paylaşımınla kazanıldı"
                } else {
                    "Kazanmak için 1 tarif veya akış gönderisi paylaş"
                },
                currentValue = totalContributions.coerceAtMost(1),
                targetValue = 1,
                isUnlocked = totalContributions >= 1,
                pointsBonusLabel = "+20/50 İlmek Puanı",
                isTerracotta = false
            ),
            CraftAchievementBadge(
                id = "badge_creative_knitter",
                title = "Yaratıcı Örgücü",
                shortTag = "Yaratıcı Örgücü",
                description = "İlmek ilmek özgün tarifler paylaşarak diğer örgü severlere ilham verdin.",
                requirementHint = if (safeRecipes >= 2) {
                    "2+ tarif paylaşımı ile kazanıldı"
                } else {
                    "Kazanmak için ${2 - safeRecipes} tarif daha paylaş"
                },
                currentValue = safeRecipes.coerceAtMost(2),
                targetValue = 2,
                isUnlocked = safeRecipes >= 2,
                pointsBonusLabel = "Her tarif +50 Puan",
                isTerracotta = false
            ),
            CraftAchievementBadge(
                id = "badge_golden_yarn",
                title = "Altın Yumak",
                shortTag = "Altın Yumak",
                description = "Paylaşımların topluluktan büyük beğeni topladı ve favoriler arasına girdi.",
                requirementHint = if (safeLikesReceived >= 5) {
                    "5+ beğeni alarak kazanıldı"
                } else {
                    "Kazanmak için ${5 - safeLikesReceived} beğeni daha al"
                },
                currentValue = safeLikesReceived.coerceAtMost(5),
                targetValue = 5,
                isUnlocked = safeLikesReceived >= 5,
                pointsBonusLabel = "Her beğeni +10 Puan",
                isTerracotta = true
            ),
            CraftAchievementBadge(
                id = "badge_thread_circle",
                title = "Sohbet Çemberi",
                shortTag = "Sohbet Çemberi",
                description = "İlmek Akışı'nda sorular, ip önerileri ve günlük notlarla sohbeti canlı tuttun.",
                requirementHint = if (safeSocial >= 3) {
                    "3+ İlmek Akışı gönderisiyle kazanıldı"
                } else {
                    "Kazanmak için ${3 - safeSocial} İlmek Akışı notu daha paylaş"
                },
                currentValue = safeSocial.coerceAtMost(3),
                targetValue = 3,
                isUnlocked = safeSocial >= 3,
                pointsBonusLabel = "Her akış notu +20 Puan",
                isTerracotta = false
            ),
            CraftAchievementBadge(
                id = "badge_community_friend",
                title = "Topluluk Dostu",
                shortTag = "Topluluk Dostu",
                description = "Diğer örgücülerin emeklerini beğenerek ve tarif arşivini büyüterek destek oldun.",
                requirementHint = if (communitySupportCount >= 5) {
                    "5+ etkileşimle kazanıldı"
                } else {
                    "Kazanmak için ${5 - communitySupportCount} beğeni veya tarif kaydı daha yap"
                },
                currentValue = communitySupportCount.coerceAtMost(5),
                targetValue = 5,
                isUnlocked = communitySupportCount >= 5,
                pointsBonusLabel = "Destek & Arşiv Puanı",
                isTerracotta = false
            ),
            CraftAchievementBadge(
                id = "badge_stitch_master",
                title = "İlmek Ustası",
                shortTag = "İlmek Ustası",
                description = "Tariflerin, akış paylaşımların ve topluluktan aldığın destekle atölyenin usta ismi oldun.",
                requirementHint = if (totalPoints >= 300) {
                    "300+ İlmek Puanı ile zirve rozeti kazanıldı"
                } else {
                    "Kazanmak için ${300 - totalPoints} İlmek Puanı daha topla"
                },
                currentValue = totalPoints.coerceAtMost(300),
                targetValue = 300,
                isUnlocked = totalPoints >= 300,
                pointsBonusLabel = "300 İlmek Puanı Hedefi",
                isTerracotta = true
            )
        )

        return UserGamificationState(
            totalPoints = totalPoints,
            levelInfo = levelInfo,
            recipesShared = safeRecipes,
            socialPostsShared = safeSocial,
            likesReceived = safeLikesReceived,
            likesGiven = safeLikesGiven,
            savedRecipesCount = safeSaved,
            badges = badges
        )
    }

    fun resolveLevelInfo(points: Int): CraftLevelInfo {
        return when {
            points >= 600 -> CraftLevelInfo(
                level = 5,
                title = "İlmek Ustası",
                subtitle = "İlmekten İşler atölyesinin efsane tasarımcısı",
                points = points,
                currentTierFloor = 600,
                nextLevelPoints = null,
                nextLevelTitle = null,
                progressFraction = 1f,
                isTerracottaTier = true
            )
            points >= 300 -> CraftLevelInfo(
                level = 4,
                title = "Usta Tığcı",
                subtitle = "Zorlu motiflerde topluluğa yol gösteren usta",
                points = points,
                currentTierFloor = 300,
                nextLevelPoints = 600,
                nextLevelTitle = "İlmek Ustası",
                progressFraction = ((points - 300f) / 300f).coerceIn(0f, 1f),
                isTerracottaTier = true
            )
            points >= 150 -> CraftLevelInfo(
                level = 3,
                title = "Yaratıcı Örgücü",
                subtitle = "Özgün modelleriyle ilham veren üretici",
                points = points,
                currentTierFloor = 150,
                nextLevelPoints = 300,
                nextLevelTitle = "Usta Tığcı",
                progressFraction = ((points - 150f) / 150f).coerceIn(0f, 1f),
                isTerracottaTier = false
            )
            points >= 50 -> CraftLevelInfo(
                level = 2,
                title = "Hevesli Örgücü",
                subtitle = "İlk yumaklarını toplulukla paylaşan üretici",
                points = points,
                currentTierFloor = 50,
                nextLevelPoints = 150,
                nextLevelTitle = "Yaratıcı Örgücü",
                progressFraction = ((points - 50f) / 100f).coerceIn(0f, 1f),
                isTerracottaTier = false
            )
            else -> CraftLevelInfo(
                level = 1,
                title = "Yeni İlmek",
                subtitle = "İlk tarifin veya notunla yolculuğa başla",
                points = points,
                currentTierFloor = 0,
                nextLevelPoints = 50,
                nextLevelTitle = "Hevesli Örgücü",
                progressFraction = (points / 50f).coerceIn(0f, 1f),
                isTerracottaTier = false
            )
        }
    }

    fun computeAuthorRankMap(
        discoverItems: List<DiscoverItem>,
        socialItems: List<SocialFeedItem>
    ): Map<String, String> {
        val recipesByAuthor = mutableMapOf<String, Int>()
        val socialByAuthor = mutableMapOf<String, Int>()
        val likesByAuthor = mutableMapOf<String, Int>()

        discoverItems.forEach { item ->
            val key = item.authorId.ifBlank { item.authorHandle.ifBlank { item.authorName } }
            if (key.isNotBlank()) {
                recipesByAuthor[key] = (recipesByAuthor[key] ?: 0) + 1
                likesByAuthor[key] = (likesByAuthor[key] ?: 0) + item.baseLikes
            }
        }
        socialItems.forEach { item ->
            val key = item.authorId.ifBlank { item.authorHandle.ifBlank { item.authorName } }
            if (key.isNotBlank()) {
                socialByAuthor[key] = (socialByAuthor[key] ?: 0) + 1
                likesByAuthor[key] = (likesByAuthor[key] ?: 0) + item.baseLikes
            }
        }

        val allKeys = recipesByAuthor.keys + socialByAuthor.keys + likesByAuthor.keys
        return allKeys.mapNotNull { key ->
            val recipes = recipesByAuthor[key] ?: 0
            val social = socialByAuthor[key] ?: 0
            val likes = likesByAuthor[key] ?: 0
            val points = (recipes * POINTS_PER_RECIPE) +
                (social * POINTS_PER_SOCIAL_POST) +
                (likes * POINTS_PER_LIKE_RECEIVED)

            val topBadge = when {
                points >= 300 -> "İlmek Ustası"
                recipes >= 2 -> "Yaratıcı Örgücü"
                likes >= 5 -> "Altın Yumak"
                social >= 3 -> "Sohbet Çemberi"
                (recipes + social) >= 1 -> "İlk İlmek"
                else -> null
            }
            if (topBadge != null) key to topBadge else null
        }.toMap()
    }
}
