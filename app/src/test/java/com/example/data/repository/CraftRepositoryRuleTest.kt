package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AtelierRemoteProduct
import com.example.data.model.CommunityPost
import com.example.data.model.SavedRecipe
import com.example.data.model.SocialPost
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CraftRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveSavedRecipe_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = CraftRepository(firestore)
        val recipeId = "rec_${UUID.randomUUID()}"

        val recipe = SavedRecipe(
            title = "Papatya Motifli Hırka",
            sourceType = "ai",
            category = "Giyim & Hırka",
            difficulty = "Orta",
            yarnType = "Pamuklu DK (3 numara)",
            hookSize = "3.5 mm tığ",
            estimatedDuration = "12 saat",
            abbreviationsNote = "zn: zincir, x: sık iğne, v: artırma, a: eksiltme",
            steps = listOf("1. Tur: Sihirli halka içine 6x (6)", "2. Tur: Her ilmeğe v (12)"),
            imageKey = "img_crochet_cardigan",
            authorName = "İlmekten İşler AI",
            isFavorite = false
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveRecipe(uid, recipe, explicitId = recipeId)
        }
        assertTrue(saveResult.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSavedRecipes(uid).first { list -> list.any { it.id == recipeId } }
        }
        assertEquals("Papatya Motifli Hırka", observed.first { it.id == recipeId }.title)

        val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteSavedRecipe(uid, recipeId)
        }
        assertTrue(deleteResult.isSuccess)
    }

    @Test
    fun observeSavedRecipes_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)
        val repository = CraftRepository(firestore)

        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeSavedRecipes(aliceUid).first()
            }
        } catch (e: Exception) {
            caught = e
        }

        val firestoreEx = checkNotNull(caught?.unwrapFirestoreException()) {
            "Expected FirebaseFirestoreException for cross-user access ($bobUid -> $aliceUid), got $caught"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    @Test
    fun observeSavedRecipes_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = CraftRepository(firestore)

        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeSavedRecipes("some_user_uid").first()
            }
        } catch (e: Exception) {
            caught = e
        }

        val firestoreEx = checkNotNull(caught?.unwrapFirestoreException()) {
            "Expected FirebaseFirestoreException when unauthenticated, got $caught"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    @Test
    fun publishLikeCommentAndDeleteCommunityPost_authenticatedUsers_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CraftRepository(firestore)
        val postId = "post_${UUID.randomUUID()}"

        val post = CommunityPost(
            title = "Pastel Amigurumi Tavşan",
            category = "Amigurumi",
            difficulty = "Başlangıç",
            yarnType = "%100 Pamuklu Amigurumi İpi",
            yarnBrand = "YarnArt Jeans",
            hookSize = "2.5 mm tığ",
            recipeNote = "Gövdeyi sıkı örerek elyaf görünmemesini sağlayın.",
            steps = listOf("1. Tur: Sihirli halka 6x (6)", "2. Tur: 6v (12)"),
            imageKey = "img_amigurumi_bunny"
        )

        val publishResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishCommunityPost(
                userId = aliceUid,
                authorName = "Ayşe Örgü",
                post = post,
                authorPhotoUrl = "",
                explicitId = postId
            )
        }
        assertTrue(publishResult.isSuccess)

        val posts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCommunityPosts().first { list -> list.any { it.id == postId } }
        }
        assertTrue(posts.any { it.id == postId })

        // Bob likes and comments on Alice's post
        val bobUid = signInTestUser(BOB_EMAIL)
        val likeResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.togglePostLike(
                userId = bobUid,
                postId = postId,
                currentlyLiked = false,
                isRemoteCommunityPost = true
            )
        }
        assertTrue(likeResult.isSuccess)

        val commentResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addComment(
                postId = postId,
                userId = bobUid,
                authorName = "Bob Örgücü",
                authorPhotoUrl = "",
                text = "Çok tatlı olmuş, kaç yumak gitti?",
                isRemoteCommunityPost = true
            )
        }
        assertTrue(commentResult.isSuccess)
        val commentId = commentResult.getOrThrow()

        val comments = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeComments(postId).first { list -> list.any { it.id == commentId } }
        }
        assertEquals("Çok tatlı olmuş, kaç yumak gitti?", comments.first { it.id == commentId }.text)

        // Alice cannot delete Bob's comment
        signInTestUser(ALICE_EMAIL)
        val crossDeleteComment = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteComment(postId, commentId, isRemoteCommunityPost = true)
        }
        assertFalse(crossDeleteComment.isSuccess)

        // Bob deletes own comment
        signInTestUser(BOB_EMAIL)
        val ownDeleteComment = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteComment(postId, commentId, isRemoteCommunityPost = true)
        }
        assertTrue(ownDeleteComment.isSuccess)

        // Alice deletes own post
        signInTestUser(ALICE_EMAIL)
        val deletePost = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteCommunityPost(postId)
        }
        assertTrue(deletePost.isSuccess)
    }

    @Test
    fun publishLikeReplyAndDeleteSocialPost_authenticatedUsers_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CraftRepository(firestore)
        val postId = "sp_${UUID.randomUUID()}"

        val post = SocialPost(
            topic = "Örgü Günlüğü",
            text = "Bugün bebek hırkasının kol kesimine geçtim, kaç ilmek bırakmalı?"
        )

        val publishResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishSocialPost(
                userId = aliceUid,
                authorName = "Ayşe Örgü",
                authorHandle = "@ayseorgu",
                authorPhotoUrl = "",
                post = post,
                explicitId = postId
            )
        }
        assertTrue(publishResult.isSuccess)

        val feed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSocialPosts().first { list -> list.any { it.id == postId } }
        }
        assertTrue(feed.any { it.id == postId })

        val bobUid = signInTestUser(BOB_EMAIL)
        val likeResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleSocialPostLike(
                userId = bobUid,
                postId = postId,
                currentlyLiked = false,
                isRemotePost = true
            )
        }
        assertTrue(likeResult.isSuccess)

        val replyResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addSocialReply(
                postId = postId,
                userId = bobUid,
                authorName = "Bob Örgü",
                authorHandle = "@boborgu",
                authorPhotoUrl = "",
                text = "Omuzda 14 ilmek bırakmak çok düzgün duruyor!",
                isRemotePost = true
            )
        }
        assertTrue(replyResult.isSuccess)
        val replyId = replyResult.getOrThrow()

        val replies = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSocialReplies(postId).first { list -> list.any { it.id == replyId } }
        }
        assertEquals("Omuzda 14 ilmek bırakmak çok düzgün duruyor!", replies.first { it.id == replyId }.text)

        signInTestUser(ALICE_EMAIL)
        val crossDeleteReply = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteSocialReply(postId, replyId, isRemotePost = true)
        }
        assertFalse(crossDeleteReply.isSuccess)

        signInTestUser(BOB_EMAIL)
        val ownDeleteReply = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteSocialReply(postId, replyId, isRemotePost = true)
        }
        assertTrue(ownDeleteReply.isSuccess)

        signInTestUser(ALICE_EMAIL)
        val deleteSocial = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteSocialPost(postId)
        }
        assertTrue(deleteSocial.isSuccess)
    }

    @Test
    fun addObserveAndDeleteCommunityMake_authenticatedUsers_succeeds() = runBlocking {
        val bobUid = signInTestUser(BOB_EMAIL)
        val repository = CraftRepository(firestore)
        val targetPostId = "disc_1"

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addCommunityMake(
                postId = targetPostId,
                userId = bobUid,
                authorName = "Bob Örgücü",
                authorPhotoUrl = "",
                imageUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
                note = "3.5 mm tığla tam ölçü oldu!"
            )
        }
        assertTrue(addResult.isSuccess)
        val makeId = addResult.getOrThrow()

        val makes = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCommunityMakes(targetPostId).first { list -> list.any { it.id == makeId } }
        }
        assertEquals("3.5 mm tığla tam ölçü oldu!", makes.first { it.id == makeId }.note)

        // Alice cannot delete Bob's community make
        signInTestUser(ALICE_EMAIL)
        val crossDelete = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteCommunityMake(targetPostId, makeId)
        }
        assertFalse(crossDelete.isSuccess)

        // Bob can delete own community make
        signInTestUser(BOB_EMAIL)
        val ownDelete = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteCommunityMake(targetPostId, makeId)
        }
        assertTrue(ownDelete.isSuccess)
    }

    @Test
    fun saveAndUpdateUserProfile_uniqueUsernameAndNotificationPrefs_enforced() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CraftRepository(firestore)
        val uniqueSuffix = UUID.randomUUID().toString().take(6)
        val aliceHandle = "ayse$uniqueSuffix"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveOrUpdateUserProfile(
                uid = aliceUid,
                profile = UserProfile(
                    userId = aliceUid,
                    displayName = "Ayşe Örgü",
                    username = "@$aliceHandle",
                    bio = "Kadife ip tutkunu",
                    specialtyTag = "Amigurumi Ustası",
                    notifyLikes = true,
                    notifyComments = false
                )
            )
        }
        assertTrue(createResult.isSuccess)

        val profiles = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAllUserProfiles().first { list ->
                list.any { it.userId == aliceUid && it.usernameLower == aliceHandle }
            }
        }
        val aliceProfile = profiles.first { it.userId == aliceUid }
        assertEquals("Amigurumi Ustası", aliceProfile.specialtyTag)
        assertFalse(aliceProfile.notifyComments)

        // Update notification preference directly
        val prefResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateNotificationPreferences(
                uid = aliceUid,
                fallbackDisplayName = "Ayşe Örgü",
                fallbackPhotoUrl = "",
                notifyLikes = false,
                notifyComments = true
            )
        }
        assertTrue(prefResult.isSuccess)

        // Bob cannot claim Alice's username
        val bobUid = signInTestUser(BOB_EMAIL)
        val duplicateUsernameResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveOrUpdateUserProfile(
                uid = bobUid,
                profile = UserProfile(
                    userId = bobUid,
                    displayName = "Bob Örgü",
                    username = "@$aliceHandle",
                    bio = "",
                    specialtyTag = ""
                )
            )
        }
        assertFalse(duplicateUsernameResult.isSuccess)

        // Bob cannot overwrite Alice's profile document directly
        val crossWrite = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveOrUpdateUserProfile(
                uid = aliceUid,
                profile = UserProfile(
                    userId = aliceUid,
                    displayName = "Hacked",
                    username = "@bobhack$uniqueSuffix"
                )
            )
        }
        assertFalse(crossWrite.isSuccess)
    }

    @Test
    fun updateCommunityPostAndSocialPost_authorOnly_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CraftRepository(firestore)
        val cPostId = "post_${UUID.randomUUID()}"
        val sPostId = "sp_${UUID.randomUUID()}"

        assertTrue(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.publishCommunityPost(
                    userId = aliceUid,
                    authorName = "Ayşe",
                    post = CommunityPost(
                        title = "İlk Başlık",
                        category = "Amigurumi",
                        difficulty = "Başlangıç",
                        yarnType = "Pamuk",
                        hookSize = "2.5 mm",
                        recipeNote = "İlk not"
                    ),
                    explicitId = cPostId
                )
            }.isSuccess
        )

        assertTrue(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.publishSocialPost(
                    userId = aliceUid,
                    authorName = "Ayşe",
                    authorHandle = "@ayse",
                    authorPhotoUrl = "",
                    post = SocialPost(topic = "Örgü Günlüğü", text = "İlk akış notu"),
                    explicitId = sPostId
                )
            }.isSuccess
        )

        // Bob cannot update Alice's community or social posts
        signInTestUser(BOB_EMAIL)
        assertFalse(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.updateCommunityPost(
                    postId = cPostId,
                    title = "Yetkisiz",
                    category = "Çanta",
                    difficulty = "Orta",
                    yarnType = "Makrome",
                    hookSize = "4 mm",
                    recipeNote = "Yetkisiz",
                    steps = emptyList()
                )
            }.isSuccess
        )
        assertFalse(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.updateSocialPost(sPostId, "Soru Sor", "Yetkisiz")
            }.isSuccess
        )

        // Alice can update her own posts
        signInTestUser(ALICE_EMAIL)
        assertTrue(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.updateCommunityPost(
                    postId = cPostId,
                    title = "Güncel Tavşan Tarifi",
                    category = "Amigurumi",
                    difficulty = "Orta",
                    yarnType = "Alize Cotton Gold",
                    hookSize = "3.0 mm",
                    recipeNote = "Güncellenen tarif adımı",
                    steps = listOf("Güncellenen tarif adımı")
                )
            }.isSuccess
        )
        assertTrue(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.updateSocialPost(sPostId, "İp Tavsiyesi", "Güncellenen akış notu")
            }.isSuccess
        )

        val updatedCommunity = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCommunityPosts().first { list ->
                list.any { it.id == cPostId && it.title == "Güncel Tavşan Tarifi" }
            }
        }
        assertEquals("Güncel Tavşan Tarifi", updatedCommunity.first { it.id == cPostId }.title)

        val updatedSocial = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSocialPosts().first { list ->
                list.any { it.id == sPostId && it.text == "Güncellenen akış notu" }
            }
        }
        assertEquals("İp Tavsiyesi", updatedSocial.first { it.id == sPostId }.topic)

        // Cleanup
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { repository.deleteCommunityPost(cPostId) }.isSuccess)
        assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) { repository.deleteSocialPost(sPostId) }.isSuccess)
    }

    @Test
    fun addAndDeleteAtelierProduct_adminOnly_enforced() = runBlocking {
        val repository = CraftRepository(firestore)
        val nonAdminUid = signInTestUser(ALICE_EMAIL)

        val product = AtelierRemoteProduct(
            authorId = nonAdminUid,
            title = "El Yapımı Şal",
            subtitle = "Sınırlı üretim",
            priceFormatted = "950 ₺",
            tag = "Hazır Ürün"
        )

        // Non-admin cannot add or hide Atelier products
        val nonAdminAdd = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addAtelierProduct(nonAdminUid, product)
        }
        assertFalse(nonAdminAdd.isSuccess)

        val nonAdminHide = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.hideCuratedAtelierProduct(nonAdminUid, "atelier_1")
        }
        assertFalse(nonAdminHide.isSuccess)

        // Admin user can add and delete Atelier products
        val adminUid = signInTestUser(ADMIN_EMAIL)
        val adminAdd = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addAtelierProduct(
                uid = adminUid,
                product = product.copy(authorId = adminUid)
            )
        }
        assertTrue(adminAdd.isSuccess)
        val productId = adminAdd.getOrThrow()

        val observedProducts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAtelierRemoteProducts().first { list ->
                list.any { it.id == productId }
            }
        }
        assertEquals("El Yapımı Şal", observedProducts.first { it.id == productId }.title)

        // Non-admin cannot delete admin product
        signInTestUser(ALICE_EMAIL)
        assertFalse(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.deleteAtelierProduct(productId)
            }.isSuccess
        )

        // Admin deletes own product
        signInTestUser(ADMIN_EMAIL)
        assertTrue(
            withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.deleteAtelierProduct(productId)
            }.isSuccess
        )
    }

    private companion object {
        const val ALICE_EMAIL = "alice_ilmek@test.com"
        const val BOB_EMAIL = "bob_ilmek@test.com"
        const val ADMIN_EMAIL = "tubisuslu171@gmail.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
