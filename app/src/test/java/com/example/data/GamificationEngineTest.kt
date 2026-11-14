package com.example.data

import com.example.data.model.DiscoverItem
import com.example.data.model.SocialFeedItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationEngineTest {

    @Test
    fun zeroActivity_startsAtLevelOneWithAllBadgesLocked() {
        val state = GamificationEngine.calculateUserGamification(
            recipesShared = 0,
            socialPostsShared = 0,
            likesReceived = 0,
            likesGiven = 0,
            savedRecipesCount = 0
        )
        assertEquals(0, state.totalPoints)
        assertEquals(1, state.levelInfo.level)
        assertEquals("Yeni İlmek", state.levelInfo.title)
        assertEquals(0, state.unlockedBadgesCount)
        assertTrue(state.badges.none { it.isUnlocked })
    }

    @Test
    fun creativeKnitterAndGoldenYarn_unlockAtExactThresholds() {
        val state = GamificationEngine.calculateUserGamification(
            recipesShared = 2, // 100 pts -> unlocks İlk İlmek + Yaratıcı Örgücü
            socialPostsShared = 1, // 20 pts
            likesReceived = 5, // 50 pts -> unlocks Altın Yumak
            likesGiven = 3, // 15 pts
            savedRecipesCount = 2 // 30 pts -> total 5 interactions -> Topluluk Dostu
        )
        // 100 + 20 + 50 + 15 + 30 = 215 pts -> Level 3 (Yaratıcı Örgücü)
        assertEquals(215, state.totalPoints)
        assertEquals(3, state.levelInfo.level)
        assertEquals("Yaratıcı Örgücü", state.levelInfo.title)

        val unlockedIds = state.badges.filter { it.isUnlocked }.map { it.id }.toSet()
        assertTrue(unlockedIds.contains("badge_first_stitch"))
        assertTrue(unlockedIds.contains("badge_creative_knitter"))
        assertTrue(unlockedIds.contains("badge_golden_yarn"))
        assertTrue(unlockedIds.contains("badge_community_friend"))
        assertFalse(unlockedIds.contains("badge_thread_circle")) // needs 3 social posts
        assertFalse(unlockedIds.contains("badge_stitch_master")) // needs 300 pts
    }

    @Test
    fun masterTier_unlocksStitchMasterAndReachesMaxLevel() {
        val state = GamificationEngine.calculateUserGamification(
            recipesShared = 4, // 200
            socialPostsShared = 4, // 80
            likesReceived = 35, // 350 -> total 630
            likesGiven = 0,
            savedRecipesCount = 0
        )
        assertEquals(630, state.totalPoints)
        assertEquals(5, state.levelInfo.level)
        assertEquals("İlmek Ustası", state.levelInfo.title)
        assertNull(state.levelInfo.nextLevelPoints)
        assertTrue(state.badges.first { it.id == "badge_stitch_master" }.isUnlocked)
    }

    @Test
    fun computeAuthorRankMap_assignsHighestEarnedBadgePerAuthor() {
        val discover = listOf(
            DiscoverItem(
                id = "d1",
                authorId = "u_master",
                title = "Hırka",
                authorName = "Selin",
                authorHandle = "@selin",
                category = "Giyim & Hırka",
                difficulty = "Orta",
                yarnType = "Pamuk",
                hookSize = "4mm",
                estimatedDuration = "10 saat",
                abbreviationsNote = "",
                steps = listOf("1"),
                imageKey = "img_crochet_cardigan",
                baseLikes = 28,
                aspectHeightRatio = 1f
            ),
            DiscoverItem(
                id = "d2",
                authorId = "u_creator",
                title = "Tavşan",
                authorName = "Ece",
                authorHandle = "@ece",
                category = "Amigurumi",
                difficulty = "Başlangıç",
                yarnType = "Pamuk",
                hookSize = "2.5mm",
                estimatedDuration = "4 saat",
                abbreviationsNote = "",
                steps = listOf("1"),
                imageKey = "img_amigurumi_bunny",
                baseLikes = 1,
                aspectHeightRatio = 1f
            ),
            DiscoverItem(
                id = "d3",
                authorId = "u_creator",
                title = "Çanta",
                authorName = "Ece",
                authorHandle = "@ece",
                category = "Çanta",
                difficulty = "Orta",
                yarnType = "Kağıt",
                hookSize = "4.5mm",
                estimatedDuration = "6 saat",
                abbreviationsNote = "",
                steps = listOf("1"),
                imageKey = "img_knit_tote",
                baseLikes = 1,
                aspectHeightRatio = 1f
            )
        )
        val social = listOf(
            SocialFeedItem(
                id = "s1",
                authorId = "u_master",
                authorName = "Selin",
                authorHandle = "@selin",
                topic = "Örgü Günlüğü",
                text = "Motif bitti",
                timeLabel = "1s",
                baseLikes = 2,
                repliesCount = 0
            )
        )

        val map = GamificationEngine.computeAuthorRankMap(discover, social)
        // u_master: 50 + 20 + 300 = 370 pts -> İlmek Ustası
        assertEquals("İlmek Ustası", map["u_master"])
        // u_creator: 2 recipes, 120 pts -> Yaratıcı Örgücü
        assertEquals("Yaratıcı Örgücü", map["u_creator"])
    }
}
