package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.model.AtelierRemoteProduct
import com.example.data.model.CommunityMake
import com.example.data.model.CommunityPost
import com.example.data.model.PostComment
import com.example.data.model.SavedRecipe
import com.example.data.model.SocialPost
import com.example.data.model.SocialReply
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class CraftRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun currentUid(): String? = try {
        Firebase.auth.currentUser?.uid
    } catch (_: Exception) {
        null
    }

    fun observeSavedRecipes(userId: String): Flow<List<SavedRecipe>> {
        val path = "users/$userId/savedRecipes"
        return db.collection("users")
            .document(userId)
            .collection("savedRecipes")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    SavedRecipe::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeLikedPostIds(userId: String): Flow<Set<String>> {
        val path = "users/$userId/likedPosts"
        return db.collection("users")
            .document(userId)
            .collection("likedPosts")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.id }.toSet()
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeCommunityPosts(): Flow<List<CommunityPost>> {
        val path = "communityPosts"
        return db.collection(path)
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CommunityPost::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeComments(postId: String): Flow<List<PostComment>> {
        val path = "communityPosts/$postId/comments"
        return db.collection("communityPosts")
            .document(postId)
            .collection("comments")
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    PostComment::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun uploadCommunityPhoto(
        userId: String,
        postId: String,
        bitmap: Bitmap
    ): String = withContext(Dispatchers.IO) {
        val storageBytes = compressBitmapToJpeg(bitmap, maxSide = 960, quality = 80)
        try {
            val storageRef = Firebase.storage.reference
                .child("community_posts/$userId/$postId.jpg")
            storageRef.putBytes(storageBytes).await()
            val url = storageRef.downloadUrl.await().toString()
            return@withContext url
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Storage unavailable or blocked; falling back to compact inline image", e)
            val compactBytes = compressBitmapToJpeg(bitmap, maxSide = 440, quality = 55)
            val base64 = Base64.encodeToString(compactBytes, Base64.NO_WRAP)
            return@withContext "data:image/jpeg;base64,$base64"
        }
    }

    suspend fun uploadAvatarPhoto(
        userId: String,
        bitmap: Bitmap
    ): String = withContext(Dispatchers.IO) {
        val storageBytes = compressBitmapToJpeg(bitmap, maxSide = 480, quality = 80)
        try {
            val storageRef = Firebase.storage.reference
                .child("user_avatars/$userId/avatar.jpg")
            storageRef.putBytes(storageBytes).await()
            val url = storageRef.downloadUrl.await().toString()
            return@withContext url
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Storage unavailable for avatar; using compact inline avatar", e)
            val compactBytes = compressBitmapToJpeg(bitmap, maxSide = 220, quality = 60)
            val base64 = Base64.encodeToString(compactBytes, Base64.NO_WRAP)
            return@withContext "data:image/jpeg;base64,$base64"
        }
    }

    suspend fun saveRecipe(
        userId: String,
        recipe: SavedRecipe,
        explicitId: String? = null
    ): Result<String> {
        val docId = explicitId?.takeIf { it.isNotBlank() } ?: "rec_${UUID.randomUUID()}"
        val docRef = db.collection("users")
            .document(userId)
            .collection("savedRecipes")
            .document(docId)
        return try {
            docRef.set(recipe.toCreatePayload(userId)).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleRecipeFavorite(userId: String, recipe: SavedRecipe): Result<Unit> {
        require(recipe.id.isNotBlank()) { "Tarif kimliği boş olamaz." }
        val docRef = db.collection("users")
            .document(userId)
            .collection("savedRecipes")
            .document(recipe.id)
        return try {
            docRef.update(
                mapOf(
                    "isFavorite" to !recipe.isFavorite,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteSavedRecipe(userId: String, recipeId: String): Result<Unit> {
        require(recipeId.isNotBlank()) { "Tarif kimliği boş olamaz." }
        val docRef = db.collection("users")
            .document(userId)
            .collection("savedRecipes")
            .document(recipeId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun togglePostLike(
        userId: String,
        postId: String,
        currentlyLiked: Boolean,
        isRemoteCommunityPost: Boolean = false
    ): Result<Unit> {
        val likeDocRef = db.collection("users")
            .document(userId)
            .collection("likedPosts")
            .document(postId)
        val postDocRef = db.collection("communityPosts").document(postId)
        return try {
            if (isRemoteCommunityPost) {
                val batch = db.batch()
                if (currentlyLiked) {
                    batch.delete(likeDocRef)
                    batch.update(
                        postDocRef,
                        mapOf(
                            "likesCount" to FieldValue.increment(-1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    batch.set(
                        likeDocRef,
                        mapOf(
                            "userId" to userId,
                            "postId" to postId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    batch.update(
                        postDocRef,
                        mapOf(
                            "likesCount" to FieldValue.increment(1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                batch.commit().await()
            } else {
                if (currentlyLiked) {
                    likeDocRef.delete().await()
                } else {
                    likeDocRef.set(
                        mapOf(
                            "userId" to userId,
                            "postId" to postId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val op = if (currentlyLiked) OperationType.DELETE else OperationType.CREATE
            handleFirestoreError(e, op, likeDocRef.path)
            Result.failure(e)
        }
    }

    suspend fun publishCommunityPost(
        userId: String,
        authorName: String,
        post: CommunityPost,
        authorPhotoUrl: String = post.authorPhotoUrl,
        explicitId: String? = null
    ): Result<String> {
        val docId = explicitId?.takeIf { it.isNotBlank() } ?: "post_${UUID.randomUUID()}"
        val docRef = db.collection("communityPosts").document(docId)
        return try {
            docRef.set(post.toCreatePayload(userId, authorName, authorPhotoUrl)).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCommunityPost(
        postId: String,
        title: String,
        category: String,
        difficulty: String,
        yarnType: String,
        hookSize: String,
        recipeNote: String,
        steps: List<String>
    ): Result<Unit> {
        require(postId.isNotBlank()) { "Gönderi kimliği boş olamaz." }
        val docRef = db.collection("communityPosts").document(postId)
        val conciseBrand = yarnType
            .substringBefore("•")
            .substringBefore("(")
            .trim()
            .ifBlank { yarnType.trim() }
            .take(120)
        return try {
            docRef.update(
                mapOf(
                    "title" to title.trim().ifBlank { "Örgü Projesi" }.take(120),
                    "category" to category.trim().ifBlank { "Giyim & Hırka" }.take(60),
                    "difficulty" to difficulty.trim().ifBlank { "Orta" }.take(40),
                    "yarnType" to yarnType.trim().ifBlank { "Pamuklu İp" }.take(120),
                    "yarnBrand" to conciseBrand,
                    "hookSize" to hookSize.trim().ifBlank { "3.5 mm tığ" }.take(60),
                    "recipeNote" to recipeNote.trim().take(2500),
                    "steps" to steps.take(40),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteCommunityPost(postId: String): Result<Unit> {
        require(postId.isNotBlank()) { "Gönderi kimliği boş olamaz." }
        val docRef = db.collection("communityPosts").document(postId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addComment(
        postId: String,
        userId: String,
        authorName: String,
        authorPhotoUrl: String,
        text: String,
        isRemoteCommunityPost: Boolean = false
    ): Result<String> {
        val trimmed = text.trim()
        require(trimmed.isNotBlank()) { "Yorum boş olamaz." }
        val commentId = "cmt_${UUID.randomUUID()}"
        val commentRef = db.collection("communityPosts")
            .document(postId)
            .collection("comments")
            .document(commentId)
        val payload = PostComment(
            postId = postId,
            authorId = userId,
            authorName = authorName,
            authorPhotoUrl = authorPhotoUrl,
            text = trimmed
        ).toCreatePayload(userId, authorName, authorPhotoUrl)

        return try {
            if (isRemoteCommunityPost) {
                val postRef = db.collection("communityPosts").document(postId)
                val batch = db.batch()
                batch.set(commentRef, payload)
                batch.update(
                    postRef,
                    mapOf(
                        "commentsCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } else {
                commentRef.set(payload).await()
            }
            Result.success(commentId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteComment(
        postId: String,
        commentId: String,
        isRemoteCommunityPost: Boolean = false
    ): Result<Unit> {
        val commentRef = db.collection("communityPosts")
            .document(postId)
            .collection("comments")
            .document(commentId)
        return try {
            if (isRemoteCommunityPost) {
                val postRef = db.collection("communityPosts").document(postId)
                val batch = db.batch()
                batch.delete(commentRef)
                batch.update(
                    postRef,
                    mapOf(
                        "commentsCount" to FieldValue.increment(-1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } else {
                commentRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, commentRef.path)
            Result.failure(e)
        }
    }

    fun observeCommunityMakes(postId: String): Flow<List<CommunityMake>> {
        val path = "communityPosts/$postId/makes"
        return db.collection("communityPosts")
            .document(postId)
            .collection("makes")
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CommunityMake::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun addCommunityMake(
        postId: String,
        userId: String,
        authorName: String,
        authorPhotoUrl: String,
        imageUrl: String,
        note: String,
        explicitId: String? = null
    ): Result<String> {
        require(imageUrl.isNotBlank()) { "Fotoğraf gereklidir." }
        val makeId = explicitId?.takeIf { it.isNotBlank() } ?: "mk_${UUID.randomUUID()}"
        val makeRef = db.collection("communityPosts")
            .document(postId)
            .collection("makes")
            .document(makeId)
        val payload = CommunityMake(
            postId = postId,
            authorId = userId,
            authorName = authorName,
            authorPhotoUrl = authorPhotoUrl,
            imageUrl = imageUrl,
            note = note.trim()
        ).toCreatePayload(userId, authorName, authorPhotoUrl)

        return try {
            makeRef.set(payload).await()
            Result.success(makeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, makeRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteCommunityMake(
        postId: String,
        makeId: String
    ): Result<Unit> {
        val makeRef = db.collection("communityPosts")
            .document(postId)
            .collection("makes")
            .document(makeId)
        return try {
            makeRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, makeRef.path)
            Result.failure(e)
        }
    }

    fun observeSocialPosts(): Flow<List<SocialPost>> {
        val path = "socialPosts"
        return db.collection(path)
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    SocialPost::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeLikedSocialPostIds(userId: String): Flow<Set<String>> {
        val path = "users/$userId/likedSocialPosts"
        return db.collection("users")
            .document(userId)
            .collection("likedSocialPosts")
            .snapshots()
            .map { snapshot -> snapshot.documents.map { it.id }.toSet() }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeSocialReplies(postId: String): Flow<List<SocialReply>> {
        val path = "socialPosts/$postId/replies"
        return db.collection("socialPosts")
            .document(postId)
            .collection("replies")
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    SocialReply::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun publishSocialPost(
        userId: String,
        authorName: String,
        authorHandle: String,
        authorPhotoUrl: String,
        post: SocialPost,
        explicitId: String? = null
    ): Result<String> {
        val docId = explicitId?.takeIf { it.isNotBlank() } ?: "sp_${UUID.randomUUID()}"
        val docRef = db.collection("socialPosts").document(docId)
        return try {
            docRef.set(post.toCreatePayload(userId, authorName, authorHandle, authorPhotoUrl)).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateSocialPost(
        postId: String,
        topic: String,
        text: String
    ): Result<Unit> {
        require(postId.isNotBlank()) { "Gönderi kimliği boş olamaz." }
        val trimmedText = text.trim().take(600)
        require(trimmedText.isNotBlank()) { "Gönderi metni boş olamaz." }
        val docRef = db.collection("socialPosts").document(postId)
        return try {
            docRef.update(
                mapOf(
                    "topic" to topic.trim().ifBlank { "Örgü Günlüğü" }.take(60),
                    "text" to trimmedText,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteSocialPost(postId: String): Result<Unit> {
        require(postId.isNotBlank()) { "Gönderi kimliği boş olamaz." }
        val docRef = db.collection("socialPosts").document(postId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleSocialPostLike(
        userId: String,
        postId: String,
        currentlyLiked: Boolean,
        isRemotePost: Boolean = false
    ): Result<Unit> {
        val likeDocRef = db.collection("users")
            .document(userId)
            .collection("likedSocialPosts")
            .document(postId)
        val postDocRef = db.collection("socialPosts").document(postId)
        return try {
            if (isRemotePost) {
                val batch = db.batch()
                if (currentlyLiked) {
                    batch.delete(likeDocRef)
                    batch.update(
                        postDocRef,
                        mapOf(
                            "likesCount" to FieldValue.increment(-1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    batch.set(
                        likeDocRef,
                        mapOf(
                            "userId" to userId,
                            "postId" to postId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    batch.update(
                        postDocRef,
                        mapOf(
                            "likesCount" to FieldValue.increment(1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                batch.commit().await()
            } else {
                if (currentlyLiked) {
                    likeDocRef.delete().await()
                } else {
                    likeDocRef.set(
                        mapOf(
                            "userId" to userId,
                            "postId" to postId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val op = if (currentlyLiked) OperationType.DELETE else OperationType.CREATE
            handleFirestoreError(e, op, likeDocRef.path)
            Result.failure(e)
        }
    }

    suspend fun addSocialReply(
        postId: String,
        userId: String,
        authorName: String,
        authorHandle: String,
        authorPhotoUrl: String,
        text: String,
        isRemotePost: Boolean = false
    ): Result<String> {
        val trimmed = text.trim()
        require(trimmed.isNotBlank()) { "Yanıt boş olamaz." }
        val replyId = "rep_${UUID.randomUUID()}"
        val replyRef = db.collection("socialPosts")
            .document(postId)
            .collection("replies")
            .document(replyId)
        val payload = SocialReply(
            postId = postId,
            authorId = userId,
            authorName = authorName,
            authorHandle = authorHandle,
            authorPhotoUrl = authorPhotoUrl,
            text = trimmed
        ).toCreatePayload(userId, authorName, authorHandle, authorPhotoUrl)

        return try {
            if (isRemotePost) {
                val postRef = db.collection("socialPosts").document(postId)
                val batch = db.batch()
                batch.set(replyRef, payload)
                batch.update(
                    postRef,
                    mapOf(
                        "repliesCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } else {
                replyRef.set(payload).await()
            }
            Result.success(replyId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, replyRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteSocialReply(
        postId: String,
        replyId: String,
        isRemotePost: Boolean = false
    ): Result<Unit> {
        val replyRef = db.collection("socialPosts")
            .document(postId)
            .collection("replies")
            .document(replyId)
        return try {
            if (isRemotePost) {
                val postRef = db.collection("socialPosts").document(postId)
                val batch = db.batch()
                batch.delete(replyRef)
                batch.update(
                    postRef,
                    mapOf(
                        "repliesCount" to FieldValue.increment(-1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } else {
                replyRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, replyRef.path)
            Result.failure(e)
        }
    }

    fun observeAllUserProfiles(): Flow<List<UserProfile>> {
        val path = "userProfiles"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun saveOrUpdateUserProfile(
        uid: String,
        profile: UserProfile
    ): Result<Unit> {
        require(uid.isNotBlank()) { "Kullanıcı kimliği boş olamaz." }
        val normalizedLower = UserProfile.normalizeUsernameLower(
            raw = profile.username,
            fallbackName = profile.displayName
        )
        val docRef = db.collection("userProfiles").document(uid)
        return try {
            val conflicting = db.collection("userProfiles")
                .whereEqualTo("usernameLower", normalizedLower)
                .get()
                .await()
                .documents
                .any { it.id != uid }
            if (conflicting) {
                return Result.failure(
                    IllegalArgumentException("Bu kullanıcı adı (@$normalizedLower) başka bir üye tarafından kullanılıyor.")
                )
            }
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(profile.toUpdateMap()).await()
            } else {
                docRef.set(profile.toCreatePayload(uid)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateNotificationPreferences(
        uid: String,
        fallbackDisplayName: String,
        fallbackPhotoUrl: String,
        notifyLikes: Boolean,
        notifyComments: Boolean
    ): Result<Unit> {
        require(uid.isNotBlank()) { "Kullanıcı kimliği boş olamaz." }
        val docRef = db.collection("userProfiles").document(uid)
        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "notifyLikes" to notifyLikes,
                        "notifyComments" to notifyComments,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val initialProfile = UserProfile(
                    userId = uid,
                    displayName = fallbackDisplayName.ifBlank { "Örgü Sever" },
                    username = "@" + UserProfile.normalizeUsernameLower("", fallbackDisplayName),
                    photoUrl = fallbackPhotoUrl,
                    notifyLikes = notifyLikes,
                    notifyComments = notifyComments
                )
                docRef.set(initialProfile.toCreatePayload(uid)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeAtelierRemoteProducts(): Flow<List<AtelierRemoteProduct>> {
        val path = "atelierProducts"
        return db.collection(path)
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    AtelierRemoteProduct::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeAtelierHiddenProductIds(): Flow<Set<String>> {
        val path = "atelierHiddenProducts"
        return db.collection(path)
            .snapshots()
            .map { snapshot -> snapshot.documents.map { it.id }.toSet() }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && currentUid() != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun addAtelierProduct(
        uid: String,
        product: AtelierRemoteProduct,
        explicitId: String? = null
    ): Result<String> {
        val docId = explicitId?.takeIf { it.isNotBlank() } ?: "ap_${UUID.randomUUID()}"
        val docRef = db.collection("atelierProducts").document(docId)
        return try {
            docRef.set(product.toCreatePayload(uid)).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAtelierProduct(productId: String): Result<Unit> {
        require(productId.isNotBlank()) { "Ürün kimliği boş olamaz." }
        val docRef = db.collection("atelierProducts").document(productId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun hideCuratedAtelierProduct(uid: String, productId: String): Result<Unit> {
        require(productId.isNotBlank()) { "Ürün kimliği boş olamaz." }
        val docRef = db.collection("atelierHiddenProducts").document(productId)
        return try {
            docRef.set(
                mapOf(
                    "productId" to productId,
                    "hiddenBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    private fun compressBitmapToJpeg(source: Bitmap, maxSide: Int, quality: Int): ByteArray {
        val width = source.width
        val height = source.height
        val scaled = if (width > maxSide || height > maxSide) {
            val ratio = minOf(maxSide.toFloat() / width, maxSide.toFloat() / height)
            Bitmap.createScaledBitmap(
                source,
                (width * ratio).toInt().coerceAtLeast(1),
                (height * ratio).toInt().coerceAtLeast(1),
                true
            )
        } else {
            source
        }
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return out.toByteArray()
    }

    private companion object {
        const val TAG = "CraftRepository"
    }
}
