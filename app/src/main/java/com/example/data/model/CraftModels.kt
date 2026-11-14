package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class SavedRecipe(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val sourceType: String = "ai",
    val category: String = "",
    val difficulty: String = "",
    val yarnType: String = "",
    val hookSize: String = "",
    val estimatedDuration: String = "",
    val abbreviationsNote: String = "",
    val steps: List<String> = emptyList(),
    val imageKey: String = "",
    val imageUrl: String = "",
    val authorName: String = "",
    val isFavorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreatePayload(uid: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to uid,
            "title" to title.take(120),
            "sourceType" to if (sourceType == "community") "community" else "ai",
            "category" to category.ifBlank { "Örgü" }.take(60),
            "difficulty" to difficulty.ifBlank { "Orta" }.take(40),
            "yarnType" to yarnType.ifBlank { "Pamuklu İp" }.take(120),
            "hookSize" to hookSize.ifBlank { "3.5 mm tığ" }.take(60),
            "steps" to steps.ifEmpty { listOf("1. Tur: 6x ile başlayın.") }.take(40),
            "isFavorite" to isFavorite,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (estimatedDuration.isNotBlank()) {
            map["estimatedDuration"] = estimatedDuration.take(60)
        }
        if (abbreviationsNote.isNotBlank()) {
            map["abbreviationsNote"] = abbreviationsNote.take(300)
        }
        if (imageKey.isNotBlank()) {
            map["imageKey"] = imageKey.take(120)
        }
        if (imageUrl.isNotBlank()) {
            map["imageUrl"] = imageUrl.take(250_000)
        }
        if (authorName.isNotBlank()) {
            map["authorName"] = authorName.take(80)
        }
        return map
    }
}

@IgnoreExtraProperties
data class LikedPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CommunityPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val title: String = "",
    val category: String = "",
    val difficulty: String = "",
    val yarnType: String = "",
    val yarnBrand: String = "",
    val hookSize: String = "",
    val recipeNote: String = "",
    val steps: List<String> = emptyList(),
    val imageKey: String = "img_crochet_cardigan",
    val imageUrl: String = "",
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreatePayload(
        uid: String,
        displayName: String,
        photoUrl: String = authorPhotoUrl
    ): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "authorId" to uid,
            "authorName" to displayName.ifBlank { "Örgü Sever" }.take(80),
            "title" to title.take(120),
            "category" to category.ifBlank { "Giyim & Hırka" }.take(60),
            "difficulty" to difficulty.ifBlank { "Orta" }.take(40),
            "yarnType" to yarnType.ifBlank { "Pamuklu İp" }.take(120),
            "hookSize" to hookSize.ifBlank { "3.5 mm tığ" }.take(60),
            "steps" to steps.take(40),
            "imageKey" to imageKey.ifBlank { "img_crochet_cardigan" }.take(120),
            "likesCount" to 0L,
            "commentsCount" to 0L,
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (photoUrl.isNotBlank()) {
            map["authorPhotoUrl"] = photoUrl.take(250_000)
        }
        if (yarnBrand.isNotBlank()) {
            map["yarnBrand"] = yarnBrand.take(120)
        }
        if (recipeNote.isNotBlank()) {
            map["recipeNote"] = recipeNote.take(2500)
        }
        if (imageUrl.isNotBlank()) {
            map["imageUrl"] = imageUrl.take(250_000)
        }
        return map
    }
}

@IgnoreExtraProperties
data class PostComment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val text: String = "",
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreatePayload(
        uid: String,
        displayName: String,
        photoUrl: String = authorPhotoUrl
    ): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "postId" to postId.take(128),
            "authorId" to uid,
            "authorName" to displayName.ifBlank { "Örgü Sever" }.take(80),
            "text" to text.trim().take(600),
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (photoUrl.isNotBlank()) {
            map["authorPhotoUrl"] = photoUrl.take(250_000)
        }
        return map
    }
}

@IgnoreExtraProperties
data class CommunityMake(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val imageUrl: String = "",
    val note: String = "",
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreatePayload(
        uid: String,
        displayName: String,
        photoUrl: String = authorPhotoUrl
    ): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "postId" to postId.take(128),
            "authorId" to uid,
            "authorName" to displayName.ifBlank { "Örgü Sever" }.take(80),
            "imageUrl" to imageUrl.take(250_000),
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (photoUrl.isNotBlank()) {
            map["authorPhotoUrl"] = photoUrl.take(250_000)
        }
        if (note.trim().isNotBlank()) {
            map["note"] = note.trim().take(300)
        }
        return map
    }
}

@IgnoreExtraProperties
data class SocialPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val authorPhotoUrl: String = "",
    val topic: String = "Örgü Günlüğü",
    val text: String = "",
    val imageUrl: String = "",
    val likesCount: Long = 0L,
    val repliesCount: Long = 0L,
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreatePayload(
        uid: String,
        displayName: String,
        handle: String,
        photoUrl: String = authorPhotoUrl
    ): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "authorId" to uid,
            "authorName" to displayName.ifBlank { "Örgü Sever" }.take(80),
            "authorHandle" to handle.ifBlank { "@orgusever" }.take(80),
            "topic" to topic.ifBlank { "Örgü Günlüğü" }.take(60),
            "text" to text.trim().take(600),
            "likesCount" to 0L,
            "repliesCount" to 0L,
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (photoUrl.isNotBlank()) {
            map["authorPhotoUrl"] = photoUrl.take(250_000)
        }
        if (imageUrl.isNotBlank()) {
            map["imageUrl"] = imageUrl.take(250_000)
        }
        return map
    }
}

@IgnoreExtraProperties
data class SocialReply(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val authorPhotoUrl: String = "",
    val text: String = "",
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreatePayload(
        uid: String,
        displayName: String,
        handle: String,
        photoUrl: String = authorPhotoUrl
    ): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "postId" to postId.take(128),
            "authorId" to uid,
            "authorName" to displayName.ifBlank { "Örgü Sever" }.take(80),
            "authorHandle" to handle.ifBlank { "@orgusever" }.take(80),
            "text" to text.trim().take(600),
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (photoUrl.isNotBlank()) {
            map["authorPhotoUrl"] = photoUrl.take(250_000)
        }
        return map
    }
}

data class SocialFeedItem(
    val id: String,
    val authorId: String = "",
    val authorName: String,
    val authorHandle: String,
    val authorPhotoUrl: String = "",
    val authorSpecialtyTag: String = "",
    val topic: String,
    val text: String,
    val imageUrl: String = "",
    val imageKey: String = "",
    val timeLabel: String,
    val baseLikes: Int,
    val repliesCount: Int,
    val isRemotePost: Boolean = false
)

data class DiscoverItem(
    val id: String,
    val authorId: String = "",
    val title: String,
    val authorName: String,
    val authorHandle: String,
    val authorPhotoUrl: String = "",
    val authorSpecialtyTag: String = "",
    val category: String,
    val difficulty: String,
    val yarnType: String,
    val hookSize: String,
    val estimatedDuration: String,
    val abbreviationsNote: String,
    val recipeNote: String = "",
    val steps: List<String>,
    val imageKey: String,
    val imageUrl: String = "",
    val baseLikes: Int,
    val commentsCount: Int = 0,
    val aspectHeightRatio: Float,
    val finishedDate: String = "",
    val yarnBrand: String = "",
    val isCommunityUserPost: Boolean = false
)

data class AtelierProduct(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceFormatted: String,
    val tag: String,
    val yarnDetail: String,
    val dimensions: String,
    @DrawableRes val imageRes: Int = R.drawable.img_atelier_shawl,
    val shopierUrl: String,
    val dolapUrl: String,
    val inStock: Boolean = true,
    val imageUrl: String = "",
    val imageKey: String = "",
    val isRemoteProduct: Boolean = false
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val usernameLower: String = "",
    val bio: String = "",
    val specialtyTag: String = "",
    val photoUrl: String = "",
    val notifyLikes: Boolean = true,
    val notifyComments: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreatePayload(uid: String): Map<String, Any> {
        val normalizedLower = normalizeUsernameLower(username, displayName)
        val normalizedHandle = "@$normalizedLower".take(40)
        val map = mutableMapOf<String, Any>(
            "userId" to uid,
            "displayName" to displayName.trim().ifBlank { "Örgü Sever" }.take(80),
            "username" to normalizedHandle,
            "usernameLower" to normalizedLower.take(40),
            "notifyLikes" to notifyLikes,
            "notifyComments" to notifyComments,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (bio.trim().isNotBlank()) {
            map["bio"] = bio.trim().take(200)
        }
        if (specialtyTag.trim().isNotBlank()) {
            map["specialtyTag"] = specialtyTag.trim().take(60)
        }
        if (photoUrl.isNotBlank()) {
            map["photoUrl"] = photoUrl.take(250_000)
        }
        return map
    }

    fun toUpdateMap(): Map<String, Any> {
        val normalizedLower = normalizeUsernameLower(username, displayName)
        val normalizedHandle = "@$normalizedLower".take(40)
        val map = mutableMapOf<String, Any>(
            "displayName" to displayName.trim().ifBlank { "Örgü Sever" }.take(80),
            "username" to normalizedHandle,
            "usernameLower" to normalizedLower.take(40),
            "bio" to bio.trim().take(200),
            "specialtyTag" to specialtyTag.trim().take(60),
            "photoUrl" to photoUrl.take(250_000),
            "notifyLikes" to notifyLikes,
            "notifyComments" to notifyComments,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map
    }

    companion object {
        fun normalizeUsernameLower(raw: String, fallbackName: String): String {
            val cleanedRaw = raw.trim().removePrefix("@").lowercase()
                .replace("ç", "c")
                .replace("ğ", "g")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ş", "s")
                .replace("ü", "u")
                .replace(Regex("[^a-z0-9._]"), "")
            if (cleanedRaw.isNotBlank()) return cleanedRaw.take(30)
            val fallback = fallbackName.trim().lowercase()
                .replace("ç", "c")
                .replace("ğ", "g")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ş", "s")
                .replace("ü", "u")
                .replace(Regex("[^a-z0-9]"), "")
            return fallback.ifBlank { "orgusever" }.take(30)
        }
    }
}

@IgnoreExtraProperties
data class AtelierRemoteProduct(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val priceFormatted: String = "",
    val tag: String = "Hazır Ürün",
    val yarnDetail: String = "",
    val dimensions: String = "",
    val imageKey: String = "img_atelier_shawl",
    val imageUrl: String = "",
    val shopierUrl: String = "",
    val dolapUrl: String = "",
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreatePayload(uid: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "authorId" to uid,
            "title" to title.trim().take(120),
            "subtitle" to subtitle.trim().ifBlank { "El emeği @ilmektenisler tasarımı" }.take(400),
            "priceFormatted" to priceFormatted.trim().take(40),
            "tag" to tag.trim().ifBlank { "Hazır Ürün" }.take(40),
            "imageKey" to imageKey.ifBlank { "img_atelier_shawl" }.take(120),
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (yarnDetail.trim().isNotBlank()) {
            map["yarnDetail"] = yarnDetail.trim().take(120)
        }
        if (dimensions.trim().isNotBlank()) {
            map["dimensions"] = dimensions.trim().take(80)
        }
        if (imageUrl.isNotBlank()) {
            map["imageUrl"] = imageUrl.take(250_000)
        }
        if (shopierUrl.trim().isNotBlank()) {
            map["shopierUrl"] = shopierUrl.trim().take(500)
        }
        if (dolapUrl.trim().isNotBlank()) {
            map["dolapUrl"] = dolapUrl.trim().take(500)
        }
        return map
    }
}

@IgnoreExtraProperties
data class AtelierHiddenProduct(
    @DocumentId val id: String = "",
    val productId: String = "",
    val hiddenBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class PatternMaterials(
    val yarnType: String,
    val hookSize: String,
    val colors: List<String>,
    val extraMaterials: List<String>
)

data class PatternPart(
    val partName: String,
    val steps: List<String>
)

enum class AssemblyDiagramType {
    MATTRESS_STITCH,
    AMIGURUMI_JOIN,
    SLIP_STITCH_JOIN,
    FINISHING_BLOCKING
}

data class AssemblyGuideStep(
    val stepTitle: String,
    val instruction: String,
    val stitchTechnique: String,
    val diagramType: AssemblyDiagramType
)

data class GeneratedPattern(
    val title: String,
    val category: String,
    val technique: String,
    val difficulty: String,
    val estimatedDuration: String,
    val aiWarning: String?,
    val materials: PatternMaterials,
    val abbreviations: List<String>,
    val parts: List<PatternPart>,
    val proTip: String,
    val atelierNote: String,
    val assemblyGuide: List<AssemblyGuideStep> = emptyList()
) {
    val yarnType: String get() = materials.yarnType
    val hookSize: String get() = materials.hookSize
    val abbreviationsNote: String get() = abbreviations.joinToString(" • ")
    val steps: List<String>
        get() = parts.flatMap { part ->
            if (parts.size > 1) {
                part.steps.map { step -> "[${part.partName}] $step" }
            } else {
                part.steps
            }
        }
}

@DrawableRes
fun resolveCraftDrawable(imageKey: String): Int = when (imageKey) {
    "img_crochet_cardigan" -> R.drawable.img_crochet_cardigan
    "img_amigurumi_bunny" -> R.drawable.img_amigurumi_bunny
    "img_knit_tote" -> R.drawable.img_knit_tote
    "img_atelier_shawl" -> R.drawable.img_atelier_shawl
    else -> R.drawable.img_crochet_cardigan
}
