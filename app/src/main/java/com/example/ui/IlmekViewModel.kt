package com.example.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CraftAchievementBadge
import com.example.data.CuratedCatalog
import com.example.data.GamificationEngine
import com.example.data.UserGamificationState
import com.example.data.model.AtelierProduct
import com.example.data.model.AtelierRemoteProduct
import com.example.data.model.CommunityMake
import com.example.data.model.CommunityPost
import com.example.data.model.DiscoverItem
import com.example.data.model.GeneratedPattern
import com.example.data.model.PostComment
import com.example.data.model.SavedRecipe
import com.example.data.model.SocialFeedItem
import com.example.data.model.SocialPost
import com.example.data.model.SocialReply
import com.example.data.model.UserProfile
import com.example.data.repository.AiPatternRepository
import com.example.data.repository.CraftRepository
import com.example.data.repository.NonKnittingImageException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiPatternUiState {
    data object Idle : AiPatternUiState
    data object Loading : AiPatternUiState
    data class Success(val pattern: GeneratedPattern) : AiPatternUiState
    data class Error(val message: String) : AiPatternUiState
}

class IlmekViewModel(
    private val craftRepository: CraftRepository,
    private val aiRepository: AiPatternRepository,
    private val currentUserId: String,
    private val currentUserName: String,
    private val currentUserPhotoUrl: String = "",
    private val currentUserEmail: String = ""
) : ViewModel() {

    val isAtelierAdmin: Boolean
        get() = currentUserEmail.trim().equals("tubisuslu171@gmail.com", ignoreCase = true)

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("Tümü")
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _aiState = MutableStateFlow<AiPatternUiState>(AiPatternUiState.Idle)
    val aiState: StateFlow<AiPatternUiState> = _aiState.asStateFlow()

    private val _rowCount = MutableStateFlow(1)
    val rowCount: StateFlow<Int> = _rowCount.asStateFlow()

    private val _stitchCount = MutableStateFlow(0)
    val stitchCount: StateFlow<Int> = _stitchCount.asStateFlow()

    private val _isPublishingPost = MutableStateFlow(false)
    val isPublishingPost: StateFlow<Boolean> = _isPublishingPost.asStateFlow()

    private val _isPublishingMake = MutableStateFlow(false)
    val isPublishingMake: StateFlow<Boolean> = _isPublishingMake.asStateFlow()

    private val _isPublishingSocialPost = MutableStateFlow(false)
    val isPublishingSocialPost: StateFlow<Boolean> = _isPublishingSocialPost.asStateFlow()

    private val _isSavingProfile = MutableStateFlow(false)
    val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

    private val _isUpdatingPost = MutableStateFlow(false)
    val isUpdatingPost: StateFlow<Boolean> = _isUpdatingPost.asStateFlow()

    private val _isManagingAtelier = MutableStateFlow(false)
    val isManagingAtelier: StateFlow<Boolean> = _isManagingAtelier.asStateFlow()

    private val _activeDetailPostId = MutableStateFlow<String?>(null)
    val activeDetailPostId: StateFlow<String?> = _activeDetailPostId.asStateFlow()

    private val _recipeRowCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val recipeRowCounts: StateFlow<Map<String, Int>> = _recipeRowCounts.asStateFlow()

    private val _selectedSocialTopic = MutableStateFlow("Tümü")
    val selectedSocialTopic: StateFlow<String> = _selectedSocialTopic.asStateFlow()

    private val _activeSocialDetailPostId = MutableStateFlow<String?>(null)
    val activeSocialDetailPostId: StateFlow<String?> = _activeSocialDetailPostId.asStateFlow()

    private val _newlyUnlockedBadge = MutableStateFlow<CraftAchievementBadge?>(null)
    val newlyUnlockedBadge: StateFlow<CraftAchievementBadge?> = _newlyUnlockedBadge.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    val profilesById: StateFlow<Map<String, UserProfile>> =
        craftRepository.observeAllUserProfiles()
            .map { list -> list.associateBy { it.userId.ifBlank { it.id } } }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing user profiles", firestoreError ?: error)
                emit(emptyMap())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptyMap()
            )

    private fun defaultFallbackProfile(): UserProfile {
        val safeName = currentUserName.ifBlank { "Örgü Sever" }
        val defaultHandle = "@" + UserProfile.normalizeUsernameLower("", safeName)
        return UserProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = safeName,
            username = defaultHandle,
            usernameLower = defaultHandle.removePrefix("@"),
            bio = "",
            specialtyTag = "",
            photoUrl = currentUserPhotoUrl,
            notifyLikes = true,
            notifyComments = true
        )
    }

    val currentUserProfile: StateFlow<UserProfile> = profilesById
        .map { map ->
            val fallback = defaultFallbackProfile()
            val stored = map[currentUserId]
            if (stored == null) {
                fallback
            } else {
                stored.copy(
                    displayName = stored.displayName.ifBlank { fallback.displayName },
                    username = stored.username.ifBlank { fallback.username },
                    photoUrl = stored.photoUrl
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = defaultFallbackProfile()
        )

    private val remoteAtelierProducts: StateFlow<List<AtelierRemoteProduct>> =
        craftRepository.observeAtelierRemoteProducts()
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing remote atelier products", firestoreError ?: error)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptyList()
            )

    private val hiddenAtelierProductIds: StateFlow<Set<String>> =
        craftRepository.observeAtelierHiddenProductIds()
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing hidden atelier products", firestoreError ?: error)
                emit(emptySet())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptySet()
            )

    val atelierProducts: StateFlow<List<AtelierProduct>> = combine(
        remoteAtelierProducts,
        hiddenAtelierProductIds
    ) { remoteList, hiddenIds ->
        val remoteMapped = remoteList.map { remote ->
            AtelierProduct(
                id = remote.id,
                title = remote.title,
                subtitle = remote.subtitle,
                priceFormatted = remote.priceFormatted,
                tag = remote.tag,
                yarnDetail = remote.yarnDetail.ifBlank { "El Emeği Doğal İplik" },
                dimensions = remote.dimensions.ifBlank { "Standart Atölye Ölçüsü" },
                shopierUrl = remote.shopierUrl.ifBlank { "https://www.shopier.com/ilmektenisler" },
                dolapUrl = remote.dolapUrl.ifBlank { "https://dolap.com/profil/ilmektenisler" },
                inStock = true,
                imageUrl = remote.imageUrl,
                imageKey = remote.imageKey,
                isRemoteProduct = true
            )
        }
        (remoteMapped + CuratedCatalog.atelierProducts).filterNot { hiddenIds.contains(it.id) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CuratedCatalog.atelierProducts
    )

    val savedRecipesState: StateFlow<UiState<List<SavedRecipe>>> =
        craftRepository.observeSavedRecipes(currentUserId)
            .map<List<SavedRecipe>, UiState<List<SavedRecipe>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing saved recipes", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "Tarifler yüklenemedi."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val likedPostIds: StateFlow<Set<String>> =
        craftRepository.observeLikedPostIds(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing liked posts", firestoreError ?: error)
                emit(emptySet())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptySet()
            )

    private val remoteCommunityPosts: StateFlow<List<CommunityPost>> =
        craftRepository.observeCommunityPosts()
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing community posts", firestoreError ?: error)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptyList())

    private val allDiscoverItems: StateFlow<List<DiscoverItem>> = combine(
        remoteCommunityPosts,
        profilesById
    ) { remotePosts, profiles ->
        val turkishLocale = Locale.forLanguageTag("tr-TR")
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", turkishLocale)
        val remoteMapped = remotePosts.mapIndexed { idx, post ->
            val profile = profiles[post.authorId]
            val formattedDate = post.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "Yeni Paylaşım"
            val conciseBrand = post.yarnBrand.ifBlank {
                post.yarnType.substringBefore("(").substringBefore("•").trim().ifBlank { "Örgü İpi" }
            }
            val displayAuthorName = profile?.displayName?.ifBlank { post.authorName } ?: post.authorName
            val fallbackHandle = "@" + displayAuthorName
                .lowercase(turkishLocale)
                .replace(" ", "")
                .ifBlank { "orgusever" }
            val resolvedHandle = profile?.username?.ifBlank { fallbackHandle } ?: fallbackHandle
            val resolvedPhoto = profile?.photoUrl ?: post.authorPhotoUrl
            val resolvedSpecialty = profile?.specialtyTag ?: ""
            DiscoverItem(
                id = post.id,
                authorId = post.authorId,
                title = post.title,
                authorName = displayAuthorName,
                authorHandle = resolvedHandle,
                authorPhotoUrl = resolvedPhoto,
                authorSpecialtyTag = resolvedSpecialty,
                category = post.category,
                difficulty = post.difficulty,
                yarnType = post.yarnType,
                hookSize = post.hookSize,
                estimatedDuration = "Topluluk Paylaşımı",
                abbreviationsNote = "zn: zincir • x: sık iğne • v: artırma • a: eksiltme",
                recipeNote = post.recipeNote,
                steps = post.steps,
                imageKey = post.imageKey,
                imageUrl = post.imageUrl,
                baseLikes = post.likesCount.toInt(),
                commentsCount = post.commentsCount.toInt(),
                aspectHeightRatio = if (idx % 2 == 0) 1.12f else 0.94f,
                finishedDate = formattedDate,
                yarnBrand = conciseBrand,
                isCommunityUserPost = true
            )
        }
        remoteMapped + CuratedCatalog.curatedDiscoverItems
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CuratedCatalog.curatedDiscoverItems
    )

    val discoverItems: StateFlow<List<DiscoverItem>> = combine(
        allDiscoverItems,
        _selectedCategory,
        _selectedDifficulty
    ) { combined, category, difficulty ->
        combined.filter { item ->
            val matchesCategory = category == "Tümü" || item.category.equals(category, ignoreCase = true)
            val matchesDifficulty = difficulty == "Tümü" || item.difficulty.equals(difficulty, ignoreCase = true)
            matchesCategory && matchesDifficulty
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CuratedCatalog.curatedDiscoverItems
    )

    val myCommunityPosts: StateFlow<List<DiscoverItem>> = allDiscoverItems
        .map { items ->
            items.filter { it.isCommunityUserPost && it.authorId == currentUserId }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val activeDetailItem: StateFlow<DiscoverItem?> = combine(
        _activeDetailPostId,
        allDiscoverItems
    ) { postId, items ->
        if (postId == null) null else items.find { it.id == postId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeDetailComments: StateFlow<List<PostComment>> = combine(
        _activeDetailPostId.flatMapLatest { postId ->
            if (postId == null) {
                flowOf(emptyList())
            } else {
                craftRepository.observeComments(postId).catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing comments for $postId", firestoreError ?: error)
                    emit(emptyList())
                }
            }
        },
        profilesById
    ) { comments, profiles ->
        comments.map { comment ->
            val profile = profiles[comment.authorId]
            if (profile != null) {
                comment.copy(
                    authorName = profile.displayName.ifBlank { comment.authorName },
                    authorPhotoUrl = profile.photoUrl
                )
            } else {
                comment
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeDetailMakes: StateFlow<List<CommunityMake>> = combine(
        _activeDetailPostId.flatMapLatest { postId ->
            if (postId == null) {
                flowOf(emptyList())
            } else {
                craftRepository.observeCommunityMakes(postId).catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing community makes for $postId", firestoreError ?: error)
                    emit(emptyList())
                }
            }
        },
        profilesById
    ) { makes, profiles ->
        makes.map { make ->
            val profile = profiles[make.authorId]
            if (profile != null) {
                make.copy(
                    authorName = profile.displayName.ifBlank { make.authorName },
                    authorPhotoUrl = profile.photoUrl
                )
            } else {
                make
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    val likedSocialPostIds: StateFlow<Set<String>> =
        craftRepository.observeLikedSocialPostIds(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing liked social posts", firestoreError ?: error)
                emit(emptySet())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptySet()
            )

    private val remoteSocialPosts: StateFlow<List<SocialPost>> =
        craftRepository.observeSocialPosts()
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error observing social posts", firestoreError ?: error)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptyList()
            )

    private val allSocialPosts: StateFlow<List<SocialFeedItem>> = combine(
        remoteSocialPosts,
        profilesById
    ) { remoteList, profiles ->
        val turkishLocale = Locale.forLanguageTag("tr-TR")
        val dateFormatter = SimpleDateFormat("dd MMM, HH:mm", turkishLocale)
        val mapped = remoteList.map { post ->
            val profile = profiles[post.authorId]
            val timeText = post.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "Az önce"
            val displayAuthorName = profile?.displayName?.ifBlank { post.authorName } ?: post.authorName
            val fallbackHandle = post.authorHandle.ifBlank {
                "@${displayAuthorName.lowercase(turkishLocale).replace(" ", "").ifBlank { "orgusever" }}"
            }
            val resolvedHandle = profile?.username?.ifBlank { fallbackHandle } ?: fallbackHandle
            val resolvedPhoto = profile?.photoUrl ?: post.authorPhotoUrl
            val resolvedSpecialty = profile?.specialtyTag ?: ""
            SocialFeedItem(
                id = post.id,
                authorId = post.authorId,
                authorName = displayAuthorName,
                authorHandle = resolvedHandle,
                authorPhotoUrl = resolvedPhoto,
                authorSpecialtyTag = resolvedSpecialty,
                topic = post.topic,
                text = post.text,
                imageUrl = post.imageUrl,
                timeLabel = timeText,
                baseLikes = post.likesCount.toInt(),
                repliesCount = post.repliesCount.toInt(),
                isRemotePost = true
            )
        }
        mapped + CuratedCatalog.curatedSocialPosts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CuratedCatalog.curatedSocialPosts
    )

    val socialPosts: StateFlow<List<SocialFeedItem>> = combine(
        allSocialPosts,
        _selectedSocialTopic
    ) { all, topic ->
        if (topic == "Tümü") all else all.filter { it.topic.equals(topic, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CuratedCatalog.curatedSocialPosts
    )

    val mySocialPosts: StateFlow<List<SocialFeedItem>> = allSocialPosts
        .map { items ->
            items.filter { it.isRemotePost && it.authorId == currentUserId }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val activeSocialDetailItem: StateFlow<SocialFeedItem?> = combine(
        _activeSocialDetailPostId,
        allSocialPosts
    ) { postId, items ->
        if (postId == null) null else items.find { it.id == postId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSocialReplies: StateFlow<List<SocialReply>> = combine(
        _activeSocialDetailPostId.flatMapLatest { postId ->
            if (postId == null) {
                flowOf(emptyList())
            } else {
                craftRepository.observeSocialReplies(postId).catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing replies for $postId", firestoreError ?: error)
                    emit(emptyList())
                }
            }
        },
        profilesById
    ) { replies, profiles ->
        replies.map { reply ->
            val profile = profiles[reply.authorId]
            if (profile != null) {
                reply.copy(
                    authorName = profile.displayName.ifBlank { reply.authorName },
                    authorHandle = profile.username.ifBlank { reply.authorHandle },
                    authorPhotoUrl = profile.photoUrl
                )
            } else {
                reply
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    val gamificationState: StateFlow<UserGamificationState> = combine(
        myCommunityPosts,
        mySocialPosts,
        savedRecipesState,
        likedPostIds,
        likedSocialPostIds
    ) { myRecipes, mySocial, savedState, likedRecipes, likedSocial ->
        val savedCount = (savedState as? UiState.Success)?.data?.size ?: 0
        val likesReceived = myRecipes.sumOf { it.baseLikes } + mySocial.sumOf { it.baseLikes }
        GamificationEngine.calculateUserGamification(
            recipesShared = myRecipes.size,
            socialPostsShared = mySocial.size,
            likesReceived = likesReceived,
            likesGiven = likedRecipes.size + likedSocial.size,
            savedRecipesCount = savedCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = GamificationEngine.calculateUserGamification(0, 0, 0, 0, 0)
    )

    val authorRankBadges: StateFlow<Map<String, String>> = combine(
        allDiscoverItems,
        allSocialPosts
    ) { discoverList, socialList ->
        GamificationEngine.computeAuthorRankMap(discoverList, socialList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyMap()
    )

    init {
        viewModelScope.launch {
            var knownUnlockedIds: MutableSet<String>? = null
            combine(savedRecipesState, gamificationState) { savedState, gamification ->
                savedState to gamification
            }.collect { (savedState, gamification) ->
                if (savedState is UiState.Loading) return@collect
                val unlockedNow = gamification.badges
                    .filter { it.isUnlocked }
                    .map { it.id }
                    .toSet()
                val existingKnown = knownUnlockedIds
                if (existingKnown == null) {
                    knownUnlockedIds = unlockedNow.toMutableSet()
                } else {
                    val newlyEarnedId = unlockedNow.firstOrNull { !existingKnown.contains(it) }
                    if (newlyEarnedId != null) {
                        _newlyUnlockedBadge.value = gamification.badges.firstOrNull { it.id == newlyEarnedId }
                    }
                    existingKnown.clear()
                    existingKnown.addAll(unlockedNow)
                }
            }
        }
    }

    fun dismissUnlockedBadgeCelebration() {
        _newlyUnlockedBadge.value = null
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectDifficulty(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun clearDiscoverFilters() {
        _selectedCategory.value = "Tümü"
        _selectedDifficulty.value = "Tümü"
    }

    fun openPostDetail(postId: String) {
        _activeDetailPostId.value = postId
    }

    fun closePostDetail() {
        _activeDetailPostId.value = null
    }

    fun toggleLike(item: DiscoverItem) {
        val currentlyLiked = likedPostIds.value.contains(item.id)
        viewModelScope.launch {
            val result = craftRepository.togglePostLike(
                userId = currentUserId,
                postId = item.id,
                currentlyLiked = currentlyLiked,
                isRemoteCommunityPost = item.isCommunityUserPost
            )
            if (result.isFailure) {
                _snackbarEvents.tryEmit("Beğeni güncellenemedi. Lütfen tekrar deneyin.")
            }
        }
    }

    fun saveDiscoverItem(item: DiscoverItem) {
        viewModelScope.launch {
            val combinedSteps = if (item.steps.isNotEmpty()) {
                item.steps
            } else if (item.recipeNote.isNotBlank()) {
                item.recipeNote.lines().map { it.trim() }.filter { it.isNotBlank() }
            } else {
                listOf("Tarif notu: ${item.yarnType} ile ${item.hookSize} kullanılarak örüldü.")
            }
            val recipe = SavedRecipe(
                title = item.title,
                sourceType = "community",
                category = item.category,
                difficulty = item.difficulty,
                yarnType = item.yarnType,
                hookSize = item.hookSize,
                estimatedDuration = item.estimatedDuration,
                abbreviationsNote = item.abbreviationsNote,
                steps = combinedSteps,
                imageKey = item.imageKey,
                imageUrl = item.imageUrl,
                authorName = item.authorName,
                isFavorite = false
            )
            val result = craftRepository.saveRecipe(
                userId = currentUserId,
                recipe = recipe,
                explicitId = "saved_${item.id}"
            )
            if (result.isSuccess) {
                _snackbarEvents.tryEmit("Tarif Kaydedilenler'e eklendi!")
            } else {
                _snackbarEvents.tryEmit("Tarif kaydedilirken bir hata oluştu.")
            }
        }
    }

    fun createCommunityPost(
        bitmap: Bitmap?,
        title: String,
        category: String,
        difficulty: String,
        yarnInfo: String,
        hookSize: String,
        recipeNote: String,
        onSuccess: () -> Unit
    ) {
        if (_isPublishingPost.value) return
        viewModelScope.launch {
            _isPublishingPost.value = true
            try {
                val postId = "post_${UUID.randomUUID()}"
                val uploadedUrl = if (bitmap != null) {
                    craftRepository.uploadCommunityPhoto(currentUserId, postId, bitmap)
                } else {
                    ""
                }
                val noteSteps = recipeNote.trim()
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .take(40)
                val conciseBrand = yarnInfo
                    .substringBefore("•")
                    .substringBefore("(")
                    .trim()
                    .ifBlank { yarnInfo.trim() }
                val post = CommunityPost(
                    title = title.trim(),
                    category = category,
                    difficulty = difficulty,
                    yarnType = yarnInfo.trim(),
                    yarnBrand = conciseBrand,
                    hookSize = hookSize.trim(),
                    recipeNote = recipeNote.trim(),
                    steps = noteSteps,
                    imageKey = resolveCategoryImageKey(category),
                    imageUrl = uploadedUrl
                )
                val activeProfile = currentUserProfile.value
                val result = craftRepository.publishCommunityPost(
                    userId = currentUserId,
                    authorName = activeProfile.displayName.ifBlank { "Örgü Sever" },
                    post = post,
                    authorPhotoUrl = activeProfile.photoUrl,
                    explicitId = postId
                )
                if (result.isSuccess) {
                    _snackbarEvents.tryEmit("Gönderiniz Keşfet topluluğunda paylaşıldı!")
                    onSuccess()
                } else {
                    _snackbarEvents.tryEmit("Gönderi paylaşılırken hata oluştu.")
                }
            } finally {
                _isPublishingPost.value = false
            }
        }
    }

    fun addComment(item: DiscoverItem, text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val activeProfile = currentUserProfile.value
            val result = craftRepository.addComment(
                postId = item.id,
                userId = currentUserId,
                authorName = activeProfile.displayName.ifBlank { "Örgü Sever" },
                authorPhotoUrl = activeProfile.photoUrl,
                text = trimmed,
                isRemoteCommunityPost = item.isCommunityUserPost
            )
            if (result.isFailure) {
                _snackbarEvents.tryEmit("Yorum gönderilemedi. Lütfen tekrar deneyin.")
            }
        }
    }

    fun deleteComment(item: DiscoverItem, commentId: String) {
        viewModelScope.launch {
            val result = craftRepository.deleteComment(
                postId = item.id,
                commentId = commentId,
                isRemoteCommunityPost = item.isCommunityUserPost
            )
            if (result.isFailure) {
                _snackbarEvents.tryEmit("Yorum silinemedi.")
            }
        }
    }

    fun addCommunityMake(
        item: DiscoverItem,
        bitmap: Bitmap,
        note: String,
        onSuccess: () -> Unit = {}
    ) {
        if (_isPublishingMake.value) return
        viewModelScope.launch {
            _isPublishingMake.value = true
            try {
                val makeId = "mk_${UUID.randomUUID()}"
                val uploadedUrl = craftRepository.uploadCommunityPhoto(currentUserId, makeId, bitmap)
                val activeProfile = currentUserProfile.value
                val result = craftRepository.addCommunityMake(
                    postId = item.id,
                    userId = currentUserId,
                    authorName = activeProfile.displayName.ifBlank { "Örgü Sever" },
                    authorPhotoUrl = activeProfile.photoUrl,
                    imageUrl = uploadedUrl,
                    note = note.trim(),
                    explicitId = makeId
                )
                if (result.isSuccess) {
                    _snackbarEvents.tryEmit("Örgün Topluluk Uygulamaları galerisine eklendi!")
                    onSuccess()
                } else {
                    _snackbarEvents.tryEmit("Fotoğraf paylaşılırken bir hata oluştu.")
                }
            } finally {
                _isPublishingMake.value = false
            }
        }
    }

    fun deleteCommunityMake(item: DiscoverItem, makeId: String) {
        viewModelScope.launch {
            val result = craftRepository.deleteCommunityMake(
                postId = item.id,
                makeId = makeId
            )
            if (result.isSuccess) {
                _snackbarEvents.tryEmit("Uygulaman galeriden kaldırıldı.")
            } else {
                _snackbarEvents.tryEmit("Uygulama silinemedi.")
            }
        }
    }

    fun incrementRecipeRow(recipeId: String) {
        val current = _recipeRowCounts.value[recipeId] ?: 1
        _recipeRowCounts.value = _recipeRowCounts.value + (recipeId to (current + 1))
    }

    fun resetRecipeRow(recipeId: String) {
        _recipeRowCounts.value = _recipeRowCounts.value + (recipeId to 1)
    }

    fun updateMyCommunityPost(
        postId: String,
        title: String,
        category: String,
        difficulty: String,
        yarnType: String,
        hookSize: String,
        recipeNote: String,
        onSuccess: () -> Unit = {}
    ) {
        if (_isUpdatingPost.value) return
        viewModelScope.launch {
            _isUpdatingPost.value = true
            try {
                val steps = recipeNote.trim()
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .take(40)
                val result = craftRepository.updateCommunityPost(
                    postId = postId,
                    title = title,
                    category = category,
                    difficulty = difficulty,
                    yarnType = yarnType,
                    hookSize = hookSize,
                    recipeNote = recipeNote,
                    steps = steps
                )
                if (result.isSuccess) {
                    _snackbarEvents.tryEmit("Tarif paylaşımınız güncellendi.")
                    onSuccess()
                } else {
                    _snackbarEvents.tryEmit("Tarif güncellenemedi.")
                }
            } finally {
                _isUpdatingPost.value = false
            }
        }
    }

    fun deleteMyPost(postId: String) {
        viewModelScope.launch {
            val result = craftRepository.deleteCommunityPost(postId)
            if (result.isSuccess) {
                if (_activeDetailPostId.value == postId) {
                    _activeDetailPostId.value = null
                }
                _snackbarEvents.tryEmit("Tarif paylaşımınız kaldırıldı.")
            } else {
                _snackbarEvents.tryEmit("Tarif silinemedi.")
            }
        }
    }

    fun selectSocialTopic(topic: String) {
        _selectedSocialTopic.value = topic
    }

    fun openSocialDetail(postId: String) {
        _activeSocialDetailPostId.value = postId
    }

    fun closeSocialDetail() {
        _activeSocialDetailPostId.value = null
    }

    fun createSocialPost(
        text: String,
        topic: String,
        bitmap: Bitmap?,
        onSuccess: () -> Unit = {}
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isPublishingSocialPost.value) return
        viewModelScope.launch {
            _isPublishingSocialPost.value = true
            try {
                val postId = "sp_${UUID.randomUUID()}"
                val uploadedUrl = if (bitmap != null) {
                    craftRepository.uploadCommunityPhoto(currentUserId, postId, bitmap)
                } else {
                    ""
                }
                val activeProfile = currentUserProfile.value
                val post = SocialPost(
                    topic = topic.ifBlank { "Örgü Günlüğü" },
                    text = trimmed,
                    imageUrl = uploadedUrl
                )
                val result = craftRepository.publishSocialPost(
                    userId = currentUserId,
                    authorName = activeProfile.displayName.ifBlank { "Örgü Sever" },
                    authorHandle = activeProfile.username,
                    authorPhotoUrl = activeProfile.photoUrl,
                    post = post,
                    explicitId = postId
                )
                if (result.isSuccess) {
                    _snackbarEvents.tryEmit("İlmek Akışı'nda paylaşıldı!")
                    onSuccess()
                } else {
                    _snackbarEvents.tryEmit("Gönderi paylaşılırken hata oluştu.")
                }
            } finally {
                _isPublishingSocialPost.value = false
            }
        }
    }

    fun toggleSocialLike(item: SocialFeedItem) {
        val currentlyLiked = likedSocialPostIds.value.contains(item.id)
        viewModelScope.launch {
            val result = craftRepository.toggleSocialPostLike(
                userId = currentUserId,
                postId = item.id,
                currentlyLiked = currentlyLiked,
                isRemotePost = item.isRemotePost
            )
            if (result.isFailure) {
                _snackbarEvents.tryEmit("Beğeni güncellenemedi.")
            }
        }
    }

    fun addSocialReply(item: SocialFeedItem, text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val activeProfile = currentUserProfile.value
            val result = craftRepository.addSocialReply(
                postId = item.id,
                userId = currentUserId,
                authorName = activeProfile.displayName.ifBlank { "Örgü Sever" },
                authorHandle = activeProfile.username,
                authorPhotoUrl = activeProfile.photoUrl,
                text = trimmed,
                isRemotePost = item.isRemotePost
            )
            if (result.isFailure) {
                _snackbarEvents.tryEmit("Yanıt gönderilemedi.")
            }
        }
    }

    fun deleteSocialReply(item: SocialFeedItem, replyId: String) {
        viewModelScope.launch {
            val result = craftRepository.deleteSocialReply(
                postId = item.id,
                replyId = replyId,
                isRemotePost = item.isRemotePost
            )
            if (result.isFailure) {
                _snackbarEvents.tryEmit("Yanıt silinemedi.")
            }
        }
    }

    fun updateMySocialPost(
        postId: String,
        topic: String,
        text: String,
        onSuccess: () -> Unit = {}
    ) {
        if (_isUpdatingPost.value) return
        viewModelScope.launch {
            _isUpdatingPost.value = true
            try {
                val result = craftRepository.updateSocialPost(
                    postId = postId,
                    topic = topic,
                    text = text
                )
                if (result.isSuccess) {
                    _snackbarEvents.tryEmit("Akış gönderiniz güncellendi.")
                    onSuccess()
                } else {
                    _snackbarEvents.tryEmit("Gönderi güncellenemedi.")
                }
            } finally {
                _isUpdatingPost.value = false
            }
        }
    }

    fun deleteMySocialPost(postId: String) {
        viewModelScope.launch {
            val result = craftRepository.deleteSocialPost(postId)
            if (result.isSuccess) {
                if (_activeSocialDetailPostId.value == postId) {
                    _activeSocialDetailPostId.value = null
                }
                _snackbarEvents.tryEmit("Akış gönderiniz silindi.")
            } else {
                _snackbarEvents.tryEmit("Gönderi silinemedi.")
            }
        }
    }

    fun saveProfile(
        displayName: String,
        usernameRaw: String,
        bio: String,
        specialtyTag: String,
        newAvatarBitmap: Bitmap?,
        removeAvatar: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        if (_isSavingProfile.value) return
        viewModelScope.launch {
            _isSavingProfile.value = true
            try {
                val current = currentUserProfile.value
                val finalPhotoUrl = when {
                    newAvatarBitmap != null -> craftRepository.uploadAvatarPhoto(currentUserId, newAvatarBitmap)
                    removeAvatar -> ""
                    else -> current.photoUrl
                }
                val normalizedLower = UserProfile.normalizeUsernameLower(
                    raw = usernameRaw,
                    fallbackName = displayName.ifBlank { current.displayName }
                )
                val updated = current.copy(
                    userId = currentUserId,
                    displayName = displayName.trim().ifBlank { "Örgü Sever" },
                    username = "@$normalizedLower",
                    usernameLower = normalizedLower,
                    bio = bio.trim(),
                    specialtyTag = specialtyTag.trim(),
                    photoUrl = finalPhotoUrl
                )
                val result = craftRepository.saveOrUpdateUserProfile(currentUserId, updated)
                if (result.isSuccess) {
                    _snackbarEvents.tryEmit("Profiliniz güncellendi!")
                    onSuccess()
                } else {
                    _snackbarEvents.tryEmit(
                        result.exceptionOrNull()?.message ?: "Profil kaydedilemedi."
                    )
                }
            } finally {
                _isSavingProfile.value = false
            }
        }
    }

    fun updateNotificationPrefs(notifyLikes: Boolean, notifyComments: Boolean) {
        viewModelScope.launch {
            val current = currentUserProfile.value
            val result = craftRepository.updateNotificationPreferences(
                uid = currentUserId,
                fallbackDisplayName = current.displayName,
                fallbackPhotoUrl = current.photoUrl,
                notifyLikes = notifyLikes,
                notifyComments = notifyComments
            )
            if (result.isFailure) {
                _snackbarEvents.tryEmit("Bildirim tercihi güncellenemedi.")
            }
        }
    }

    fun addAtelierProduct(
        bitmap: Bitmap?,
        title: String,
        subtitle: String,
        priceInput: String,
        tag: String,
        yarnDetail: String,
        dimensions: String,
        shopierUrl: String,
        dolapUrl: String,
        onSuccess: () -> Unit = {}
    ) {
        if (!isAtelierAdmin || _isManagingAtelier.value) return
        viewModelScope.launch {
            _isManagingAtelier.value = true
            try {
                val productId = "ap_${UUID.randomUUID()}"
                val uploadedUrl = if (bitmap != null) {
                    craftRepository.uploadCommunityPhoto(currentUserId, productId, bitmap)
                } else {
                    ""
                }
                val cleanPrice = priceInput.trim().let {
                    if (it.contains("₺") || it.contains("TL", ignoreCase = true)) it else "$it ₺"
                }
                val remote = AtelierRemoteProduct(
                    authorId = currentUserId,
                    title = title.trim(),
                    subtitle = subtitle.trim().ifBlank { "El emeği @ilmektenisler tasarımı" },
                    priceFormatted = cleanPrice,
                    tag = tag.trim().ifBlank { "Hazır Ürün" },
                    yarnDetail = yarnDetail.trim(),
                    dimensions = dimensions.trim(),
                    imageKey = "img_atelier_shawl",
                    imageUrl = uploadedUrl,
                    shopierUrl = shopierUrl.trim().ifBlank { "https://www.shopier.com/ilmektenisler" },
                    dolapUrl = dolapUrl.trim().ifBlank { "https://dolap.com/profil/ilmektenisler" }
                )
                val result = craftRepository.addAtelierProduct(currentUserId, remote, explicitId = productId)
                if (result.isSuccess) {
                    _snackbarEvents.tryEmit("Yeni ürün Atölye vitrinine eklendi!")
                    onSuccess()
                } else {
                    _snackbarEvents.tryEmit("Ürün eklenirken hata oluştu.")
                }
            } finally {
                _isManagingAtelier.value = false
            }
        }
    }

    fun deleteAtelierProduct(product: AtelierProduct) {
        if (!isAtelierAdmin) return
        viewModelScope.launch {
            val result = if (product.isRemoteProduct) {
                craftRepository.deleteAtelierProduct(product.id)
            } else {
                craftRepository.hideCuratedAtelierProduct(currentUserId, product.id)
            }
            if (result.isSuccess) {
                _snackbarEvents.tryEmit("Ürün atölye vitrininden kaldırıldı.")
            } else {
                _snackbarEvents.tryEmit("Ürün kaldırılamadı.")
            }
        }
    }

    fun onImageSelected(bitmap: Bitmap) {
        _selectedBitmap.value = bitmap
        _aiState.value = AiPatternUiState.Idle
    }

    fun analyzeSelectedImage(extraNote: String) {
        if (_aiState.value is AiPatternUiState.Loading) return
        val bitmap = _selectedBitmap.value
        if (bitmap == null) {
            _snackbarEvents.tryEmit("Lütfen önce bir örgü fotoğrafı seçin.")
            return
        }
        viewModelScope.launch {
            _aiState.value = AiPatternUiState.Loading
            try {
                val pattern = aiRepository.analyzeKnittingImage(bitmap, extraNote)
                _aiState.value = AiPatternUiState.Success(pattern)
            } catch (e: NonKnittingImageException) {
                _aiState.value = AiPatternUiState.Error(
                    e.message?.takeIf { it.isNotBlank() }
                        ?: "Görselde bir örgü ürünü tespit edemedim, lütfen tığ veya şiş işi bir fotoğraf yükle"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI pattern analysis failed", e)
                val detail = e.localizedMessage?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Bilinmeyen API veya ağ hatası (${e::class.java.simpleName})."
                val formatted = if (detail.startsWith("Tarif oluşturulamadı:", ignoreCase = true)) {
                    detail
                } else {
                    "Tarif oluşturulamadı: $detail"
                }
                _aiState.value = AiPatternUiState.Error(formatted)
            }
        }
    }

    fun saveGeneratedPattern(pattern: GeneratedPattern) {
        viewModelScope.launch {
            val recipe = SavedRecipe(
                title = pattern.title,
                sourceType = "ai",
                category = normalizeCategoryForFeed(pattern.category),
                difficulty = normalizeDifficultyForFeed(pattern.difficulty),
                yarnType = pattern.yarnType,
                hookSize = pattern.hookSize,
                estimatedDuration = pattern.estimatedDuration,
                abbreviationsNote = pattern.abbreviationsNote,
                steps = pattern.steps,
                imageKey = resolveCategoryImageKey(pattern.category),
                authorName = "İlmekten İşler AI",
                isFavorite = true
            )
            val result = craftRepository.saveRecipe(currentUserId, recipe)
            if (result.isSuccess) {
                _snackbarEvents.tryEmit("AI tarifi Kaydedilenler'e kaydedildi!")
            } else {
                _snackbarEvents.tryEmit("Tarif kaydedilemedi.")
            }
        }
    }

    fun shareGeneratedPatternToCommunity(pattern: GeneratedPattern) {
        if (_isPublishingPost.value) return
        viewModelScope.launch {
            _isPublishingPost.value = true
            try {
                val postId = "post_${UUID.randomUUID()}"
                val uploadedUrl = _selectedBitmap.value?.let { bmp ->
                    craftRepository.uploadCommunityPhoto(currentUserId, postId, bmp)
                } ?: ""
                val conciseBrand = pattern.materials.yarnType
                    .substringBefore("(")
                    .substringBefore("•")
                    .trim()
                    .ifBlank { "AI Tarif İpi" }
                val noteText = buildString {
                    if (pattern.proTip.isNotBlank()) {
                        append(pattern.proTip)
                    }
                    if (pattern.abbreviationsNote.isNotBlank()) {
                        if (isNotEmpty()) append("\n\n")
                        append("Kısaltmalar: ${pattern.abbreviationsNote}")
                    }
                }
                val post = CommunityPost(
                    title = pattern.title,
                    category = normalizeCategoryForFeed(pattern.category),
                    difficulty = normalizeDifficultyForFeed(pattern.difficulty),
                    yarnType = pattern.yarnType,
                    yarnBrand = conciseBrand,
                    hookSize = pattern.hookSize,
                    recipeNote = noteText,
                    steps = pattern.steps,
                    imageKey = resolveCategoryImageKey(pattern.category),
                    imageUrl = uploadedUrl
                )
                val activeProfile = currentUserProfile.value
                val result = craftRepository.publishCommunityPost(
                    userId = currentUserId,
                    authorName = activeProfile.displayName.ifBlank { "Örgü Sever" },
                    post = post,
                    authorPhotoUrl = activeProfile.photoUrl,
                    explicitId = postId
                )
                if (result.isSuccess) {
                    _snackbarEvents.tryEmit("Tarifiniz Keşfet topluluğunda paylaşıldı!")
                } else {
                    _snackbarEvents.tryEmit("Toplulukta paylaşılırken hata oluştu.")
                }
            } finally {
                _isPublishingPost.value = false
            }
        }
    }

    fun toggleFavoriteSavedRecipe(recipe: SavedRecipe) {
        viewModelScope.launch {
            val result = craftRepository.toggleRecipeFavorite(currentUserId, recipe)
            if (result.isFailure) {
                _snackbarEvents.tryEmit("Favori durumu güncellenemedi.")
            }
        }
    }

    fun deleteSavedRecipe(recipeId: String) {
        viewModelScope.launch {
            val result = craftRepository.deleteSavedRecipe(currentUserId, recipeId)
            if (result.isSuccess) {
                _snackbarEvents.tryEmit("Tarif silindi.")
            } else {
                _snackbarEvents.tryEmit("Tarif silinemedi.")
            }
        }
    }

    fun incrementRow() {
        _rowCount.value += 1
    }

    fun decrementRow() {
        if (_rowCount.value > 1) {
            _rowCount.value -= 1
        }
    }

    fun incrementStitch() {
        _stitchCount.value += 1
    }

    fun decrementStitch() {
        if (_stitchCount.value > 0) {
            _stitchCount.value -= 1
        }
    }

    fun completeRow() {
        _rowCount.value += 1
        _stitchCount.value = 0
    }

    fun selectRow(rowNumber: Int) {
        _rowCount.value = rowNumber.coerceAtLeast(1)
        _stitchCount.value = 0
    }

    fun resetCounter() {
        _rowCount.value = 1
        _stitchCount.value = 0
    }

    private fun resolveCategoryImageKey(category: String): String = when {
        category.contains("Amigurumi", ignoreCase = true) -> "img_amigurumi_bunny"
        category.contains("Çanta", ignoreCase = true) -> "img_knit_tote"
        category.contains("Anahtarlık", ignoreCase = true) ||
            category.contains("Aksesuar", ignoreCase = true) -> "img_atelier_shawl"
        else -> "img_crochet_cardigan"
    }

    private fun normalizeCategoryForFeed(rawCategory: String): String = when {
        rawCategory.contains("Amigurumi", ignoreCase = true) -> "Amigurumi"
        rawCategory.contains("Çanta", ignoreCase = true) -> "Çanta"
        rawCategory.contains("Anahtarlık", ignoreCase = true) ||
            rawCategory.contains("Aksesuar", ignoreCase = true) ||
            rawCategory.contains("Şal", ignoreCase = true) -> "Anahtarlık & Aksesuar"
        else -> "Giyim & Hırka"
    }

    private fun normalizeDifficultyForFeed(rawDifficulty: String): String = when {
        rawDifficulty.contains("Kolay", ignoreCase = true) ||
            rawDifficulty.contains("Başlangıç", ignoreCase = true) -> "Başlangıç"
        rawDifficulty.contains("İleri", ignoreCase = true) ||
            rawDifficulty.contains("Zor", ignoreCase = true) -> "İleri"
        else -> "Orta"
    }

    private companion object {
        const val TAG = "IlmekViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
