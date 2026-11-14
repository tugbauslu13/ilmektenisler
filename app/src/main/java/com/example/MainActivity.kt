package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOutUser
import com.example.core.configureAppCheck
import com.example.data.model.DiscoverItem
import com.example.data.model.SocialFeedItem
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.ThemePreferencesRepository
import com.example.data.repository.AiPatternRepository
import com.example.data.repository.CraftRepository
import com.example.ui.IlmekViewModel
import com.example.ui.components.AddAtelierProductBottomSheet
import com.example.ui.components.BadgeUnlockedCelebrationDialog
import com.example.ui.components.CreatePostBottomSheet
import com.example.ui.components.EditCommunityPostBottomSheet
import com.example.ui.components.EditProfileBottomSheet
import com.example.ui.components.EditSocialPostDialog
import com.example.ui.components.PostCreationMode
import com.example.ui.components.PostDetailBottomSheet
import com.example.ui.components.SocialThreadBottomSheet
import com.example.ui.components.UserAvatar
import com.example.ui.screens.AiRecipeScreen
import com.example.ui.screens.AtelierScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SocialFeedScreen
import com.example.ui.screens.shareDiscoverItem
import com.example.ui.screens.shareSocialFeedItem
import com.example.ui.theme.IlmektenIslerTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private const val ROUTE_DISCOVER = "discover"
private const val ROUTE_AI_RECIPE = "ai_recipe"
private const val ROUTE_ATELIER = "atelier"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_SETTINGS = "settings"

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val leftTabs = listOf(
    BottomNavTab(
        route = ROUTE_DISCOVER,
        label = "Keşfet",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTag = "tab_discover"
    ),
    BottomNavTab(
        route = ROUTE_AI_RECIPE,
        label = "AI Tarif",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "tab_ai_recipe"
    )
)

private val rightTabs = listOf(
    BottomNavTab(
        route = ROUTE_ATELIER,
        label = "Atölye",
        selectedIcon = Icons.Filled.Storefront,
        unselectedIcon = Icons.Outlined.Storefront,
        testTag = "tab_atelier"
    ),
    BottomNavTab(
        route = ROUTE_PROFILE,
        label = "Profilim",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "tab_profile"
    )
)

private val allTabs = leftTabs + rightTabs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        val themePreferences = ThemePreferencesRepository(applicationContext)
        enableEdgeToEdge()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val themeMode by themePreferences.themeModeFlow.collectAsStateWithLifecycle(
                initialValue = AppThemeMode.SYSTEM
            )
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            IlmektenIslerTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    themeMode = themeMode,
                    isDarkTheme = isDarkTheme,
                    onSelectThemeMode = { selectedMode ->
                        coroutineScope.launch {
                            themePreferences.setThemeMode(selectedMode)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    isDarkTheme: Boolean = false,
    onSelectThemeMode: (AppThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = this
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        val resolvedEmail = user.email
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: user.providerData
                .firstNotNullOfOrNull { it.email?.trim()?.takeIf { e -> e.isNotBlank() } }
            ?: ""
        key(user.uid) {
            MainCraftShell(
                currentUserId = user.uid,
                userDisplayName = user.displayName ?: "Örgü Sever",
                userEmail = resolvedEmail,
                userPhotoUrl = user.photoUrl?.toString() ?: "",
                themeMode = themeMode,
                isDarkTheme = isDarkTheme,
                onSelectThemeMode = onSelectThemeMode
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCraftShell(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String,
    userPhotoUrl: String,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    isDarkTheme: Boolean = false,
    onSelectThemeMode: (AppThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: IlmekViewModel = viewModel(
        key = "${currentUserId}_v2",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                IlmekViewModel(
                    craftRepository = CraftRepository(db),
                    aiRepository = AiPatternRepository(),
                    currentUserId = currentUserId,
                    currentUserName = userDisplayName,
                    currentUserPhotoUrl = userPhotoUrl,
                    currentUserEmail = userEmail
                )
            }
        }
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    var showAccountMenu by remember { mutableStateOf(false) }
    var showCreatePostSheet by remember { mutableStateOf(false) }
    var discoverFeedMode by rememberSaveable { mutableIntStateOf(0) } // 0 = İlmek Akışı (Twitter), 1 = Tarifler
    var createPostInitialMode by remember { mutableStateOf(PostCreationMode.SOCIAL) }
    var profileTabOverride by remember { mutableStateOf<Int?>(null) }

    var showEditProfileSheet by remember { mutableStateOf(false) }
    var editingCommunityPost by remember { mutableStateOf<DiscoverItem?>(null) }
    var editingSocialPost by remember { mutableStateOf<SocialFeedItem?>(null) }
    var showAddAtelierProductSheet by remember { mutableStateOf(false) }

    fun openCreateSheet(mode: PostCreationMode? = null) {
        createPostInitialMode = mode ?: if (discoverFeedMode == 1) {
            PostCreationMode.RECIPE
        } else {
            PostCreationMode.SOCIAL
        }
        showCreatePostSheet = true
    }

    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val atelierProducts by viewModel.atelierProducts.collectAsStateWithLifecycle()
    val isSavingProfile by viewModel.isSavingProfile.collectAsStateWithLifecycle()
    val isUpdatingPost by viewModel.isUpdatingPost.collectAsStateWithLifecycle()
    val isManagingAtelier by viewModel.isManagingAtelier.collectAsStateWithLifecycle()

    val discoverItems by viewModel.discoverItems.collectAsStateWithLifecycle()
    val myCommunityPosts by viewModel.myCommunityPosts.collectAsStateWithLifecycle()
    val mySocialPosts by viewModel.mySocialPosts.collectAsStateWithLifecycle()
    val socialPosts by viewModel.socialPosts.collectAsStateWithLifecycle()
    val selectedSocialTopic by viewModel.selectedSocialTopic.collectAsStateWithLifecycle()
    val likedSocialPostIds by viewModel.likedSocialPostIds.collectAsStateWithLifecycle()
    val isPublishingSocialPost by viewModel.isPublishingSocialPost.collectAsStateWithLifecycle()
    val activeSocialDetailItem by viewModel.activeSocialDetailItem.collectAsStateWithLifecycle()
    val activeSocialReplies by viewModel.activeSocialReplies.collectAsStateWithLifecycle()
    val gamificationState by viewModel.gamificationState.collectAsStateWithLifecycle()
    val authorRankBadges by viewModel.authorRankBadges.collectAsStateWithLifecycle()
    val newlyUnlockedBadge by viewModel.newlyUnlockedBadge.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsStateWithLifecycle()
    val likedPostIds by viewModel.likedPostIds.collectAsStateWithLifecycle()
    val savedRecipesState by viewModel.savedRecipesState.collectAsStateWithLifecycle()
    val selectedBitmap by viewModel.selectedBitmap.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val rowCount by viewModel.rowCount.collectAsStateWithLifecycle()
    val stitchCount by viewModel.stitchCount.collectAsStateWithLifecycle()
    val isPublishingPost by viewModel.isPublishingPost.collectAsStateWithLifecycle()
    val isPublishingMake by viewModel.isPublishingMake.collectAsStateWithLifecycle()
    val recipeRowCounts by viewModel.recipeRowCounts.collectAsStateWithLifecycle()
    val activeDetailItem by viewModel.activeDetailItem.collectAsStateWithLifecycle()
    val activeDetailComments by viewModel.activeDetailComments.collectAsStateWithLifecycle()
    val activeDetailMakes by viewModel.activeDetailMakes.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFAF6F2),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("topbar_brand_logo")
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_ilmekten_isler_logo),
                                        contentDescription = "İlmekten İşler marka logosu",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "İlmekten İşler",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "El işi topluluğu & AI örgü atölyesi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onSelectThemeMode(
                                        if (isDarkTheme) AppThemeMode.LIGHT else AppThemeMode.DARK
                                    )
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("topbar_theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                    contentDescription = if (isDarkTheme) "Açık temaya geç" else "Gece örgü moduna geç",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(modifier = Modifier.padding(end = 8.dp)) {
                                Surface(
                                    onClick = { showAccountMenu = true },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("account_menu_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        UserAvatar(
                                            photoUrl = currentUserProfile.photoUrl,
                                            displayName = currentUserProfile.displayName,
                                            size = 40.dp
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showAccountMenu,
                                    onDismissRequest = { showAccountMenu = false },
                                    containerColor = MaterialTheme.colorScheme.background
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = currentUserProfile.displayName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = currentUserProfile.username,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        onClick = {
                                            showAccountMenu = false
                                            navigateToTab(ROUTE_PROFILE)
                                        }
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Profilim",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Person,
                                                contentDescription = "Profilim",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showAccountMenu = false
                                            navigateToTab(ROUTE_PROFILE)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Ayarlar",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Settings,
                                                contentDescription = "Ayarlar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showAccountMenu = false
                                            navController.navigate(ROUTE_SETTINGS) { launchSingleTop = true }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Çıkış Yap",
                                                color = MaterialTheme.colorScheme.tertiary,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                                contentDescription = "Çıkış yap",
                                                tint = MaterialTheme.colorScheme.tertiary
                                            )
                                        },
                                        onClick = {
                                            showAccountMenu = false
                                            signOutUser(credentialManager, coroutineScope)
                                        },
                                        modifier = Modifier.testTag("sign_out_menu_item")
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    CraftCommunityBottomBar(
                        currentDestination = currentDestination,
                        onTabSelected = ::navigateToTab,
                        onCreatePostClick = { openCreateSheet() }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            FloatingActionButton(
                                onClick = { openCreateSheet() },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                                modifier = Modifier
                                    .padding(top = 8.dp, bottom = 8.dp)
                                    .testTag("rail_create_post")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Yeni paylaşım ekle"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        allTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == tab.route
                            } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DISCOVER,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(ROUTE_DISCOVER) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            FeedAreaModeBar(
                                selectedMode = discoverFeedMode,
                                onSelectMode = { discoverFeedMode = it }
                            )
                            if (discoverFeedMode == 0) {
                                SocialFeedScreen(
                                    items = socialPosts,
                                    selectedTopic = selectedSocialTopic,
                                    likedSocialPostIds = likedSocialPostIds,
                                    authorRankBadges = authorRankBadges,
                                    currentUserId = currentUserId,
                                    userDisplayName = currentUserProfile.displayName,
                                    userPhotoUrl = currentUserProfile.photoUrl,
                                    isPublishing = isPublishingSocialPost,
                                    onSelectTopic = viewModel::selectSocialTopic,
                                    onPublishQuickPost = { text, topic, bitmap, onDone ->
                                        viewModel.createSocialPost(
                                            text = text,
                                            topic = topic,
                                            bitmap = bitmap,
                                            onSuccess = onDone
                                        )
                                    },
                                    onPostClick = { item -> viewModel.openSocialDetail(item.id) },
                                    onToggleLike = viewModel::toggleSocialLike,
                                    onEditPost = { editingSocialPost = it },
                                    onDeletePost = viewModel::deleteMySocialPost,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                DiscoverScreen(
                                    items = discoverItems,
                                    selectedCategory = selectedCategory,
                                    selectedDifficulty = selectedDifficulty,
                                    likedPostIds = likedPostIds,
                                    authorRankBadges = authorRankBadges,
                                    currentUserId = currentUserId,
                                    onSelectCategory = viewModel::selectCategory,
                                    onSelectDifficulty = viewModel::selectDifficulty,
                                    onClearFilters = viewModel::clearDiscoverFilters,
                                    onPostClick = { item -> viewModel.openPostDetail(item.id) },
                                    onToggleLike = viewModel::toggleLike,
                                    onSaveItem = viewModel::saveDiscoverItem,
                                    onEditItem = { editingCommunityPost = it },
                                    onDeleteItem = viewModel::deleteMyPost,
                                    onUploadRecipeClick = { openCreateSheet(PostCreationMode.RECIPE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    composable(ROUTE_AI_RECIPE) {
                        AiRecipeScreen(
                            selectedBitmap = selectedBitmap,
                            aiState = aiState,
                            rowCount = rowCount,
                            stitchCount = stitchCount,
                            onImageSelected = viewModel::onImageSelected,
                            onAnalyzeClick = viewModel::analyzeSelectedImage,
                            onSavePattern = viewModel::saveGeneratedPattern,
                            onShareToCommunity = viewModel::shareGeneratedPatternToCommunity,
                            onIncrementRow = viewModel::incrementRow,
                            onDecrementRow = viewModel::decrementRow,
                            onIncrementStitch = viewModel::incrementStitch,
                            onDecrementStitch = viewModel::decrementStitch,
                            onCompleteRow = viewModel::completeRow,
                            onSelectRow = viewModel::selectRow,
                            onResetCounter = viewModel::resetCounter,
                            onGoToAtelierTab = { navigateToTab(ROUTE_ATELIER) },
                            isPublishingPost = isPublishingPost
                        )
                    }
                    composable(ROUTE_ATELIER) {
                        AtelierScreen(
                            products = atelierProducts,
                            isAdmin = viewModel.isAtelierAdmin,
                            onAddProductClick = {
                                if (viewModel.isAtelierAdmin) {
                                    showAddAtelierProductSheet = true
                                }
                            },
                            onDeleteProductClick = viewModel::deleteAtelierProduct,
                            onExternalLinkError = { msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        )
                    }
                    composable(ROUTE_PROFILE) {
                        ProfileScreen(
                            profile = currentUserProfile,
                            userEmail = userEmail,
                            myPosts = myCommunityPosts,
                            mySocialPosts = mySocialPosts,
                            savedRecipesState = savedRecipesState,
                            recipeRowCounts = recipeRowCounts,
                            gamificationState = gamificationState,
                            selectedTabOverride = profileTabOverride,
                            onClearTabOverride = { profileTabOverride = null },
                            onEditProfileClick = { showEditProfileSheet = true },
                            onSettingsClick = {
                                navController.navigate(ROUTE_SETTINGS) { launchSingleTop = true }
                            },
                            onPostClick = { item -> viewModel.openPostDetail(item.id) },
                            onEditMyPost = { editingCommunityPost = it },
                            onDeleteMyPost = viewModel::deleteMyPost,
                            onSocialPostClick = { item -> viewModel.openSocialDetail(item.id) },
                            onEditMySocialPost = { editingSocialPost = it },
                            onDeleteMySocialPost = viewModel::deleteMySocialPost,
                            onCreatePostClick = { openCreateSheet() },
                            onToggleFavoriteSaved = viewModel::toggleFavoriteSavedRecipe,
                            onDeleteSavedRecipe = viewModel::deleteSavedRecipe,
                            onIncrementRecipeRow = viewModel::incrementRecipeRow,
                            onResetRecipeRow = viewModel::resetRecipeRow,
                            onGoToAiTab = { navigateToTab(ROUTE_AI_RECIPE) },
                            onGoToDiscoverTab = {
                                discoverFeedMode = 1
                                navigateToTab(ROUTE_DISCOVER)
                            },
                            onGoToAtelierTab = { navigateToTab(ROUTE_ATELIER) }
                        )
                    }
                    composable(ROUTE_SETTINGS) {
                        SettingsScreen(
                            profile = currentUserProfile,
                            userEmail = userEmail,
                            isAdmin = viewModel.isAtelierAdmin,
                            themeMode = themeMode,
                            onSelectThemeMode = onSelectThemeMode,
                            onBackClick = { navController.popBackStack() },
                            onEditProfileClick = { showEditProfileSheet = true },
                            onToggleNotifyLikes = { checked ->
                                viewModel.updateNotificationPrefs(
                                    notifyLikes = checked,
                                    notifyComments = currentUserProfile.notifyComments
                                )
                            },
                            onToggleNotifyComments = { checked ->
                                viewModel.updateNotificationPrefs(
                                    notifyLikes = currentUserProfile.notifyLikes,
                                    notifyComments = checked
                                )
                            },
                            onExternalLinkError = { msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            onSignOutClick = {
                                signOutUser(credentialManager, coroutineScope)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreatePostSheet) {
        CreatePostBottomSheet(
            initialMode = createPostInitialMode,
            isPublishingRecipe = isPublishingPost,
            isPublishingSocial = isPublishingSocialPost,
            onDismiss = {
                if (!isPublishingPost && !isPublishingSocialPost) {
                    showCreatePostSheet = false
                }
            },
            onSubmitRecipePost = { bitmap, title, category, difficulty, yarnInfo, hookSize, recipeNote ->
                viewModel.createCommunityPost(
                    bitmap = bitmap,
                    title = title,
                    category = category,
                    difficulty = difficulty,
                    yarnInfo = yarnInfo,
                    hookSize = hookSize,
                    recipeNote = recipeNote,
                    onSuccess = {
                        showCreatePostSheet = false
                        discoverFeedMode = 1
                        navigateToTab(ROUTE_DISCOVER)
                    }
                )
            },
            onSubmitSocialPost = { text, topic, bitmap ->
                viewModel.createSocialPost(
                    text = text,
                    topic = topic,
                    bitmap = bitmap,
                    onSuccess = {
                        showCreatePostSheet = false
                        discoverFeedMode = 0
                        navigateToTab(ROUTE_DISCOVER)
                    }
                )
            }
        )
    }

    activeDetailItem?.let { detailItem ->
        val authorKey = detailItem.authorId.ifBlank { detailItem.authorHandle.ifBlank { detailItem.authorName } }
        PostDetailBottomSheet(
            item = detailItem,
            isLiked = likedPostIds.contains(detailItem.id),
            comments = activeDetailComments,
            makes = activeDetailMakes,
            isPublishingMake = isPublishingMake,
            rowCount = recipeRowCounts[detailItem.id] ?: 1,
            currentUserId = currentUserId,
            authorRankTitle = authorRankBadges[authorKey],
            isOwnPost = detailItem.isCommunityUserPost && detailItem.authorId == currentUserId,
            onDismiss = viewModel::closePostDetail,
            onEditPost = { editingCommunityPost = detailItem },
            onDeletePost = { viewModel.deleteMyPost(detailItem.id) },
            onToggleLike = { viewModel.toggleLike(detailItem) },
            onSaveClick = { viewModel.saveDiscoverItem(detailItem) },
            onShareClick = { shareDiscoverItem(context, detailItem) },
            onSendComment = { text -> viewModel.addComment(detailItem, text) },
            onDeleteComment = { commentId -> viewModel.deleteComment(detailItem, commentId) },
            onSubmitMake = { bitmap, note, onDone ->
                viewModel.addCommunityMake(detailItem, bitmap, note, onSuccess = onDone)
            },
            onDeleteMake = { makeId -> viewModel.deleteCommunityMake(detailItem, makeId) },
            onIncrementRow = { viewModel.incrementRecipeRow(detailItem.id) },
            onResetRow = { viewModel.resetRecipeRow(detailItem.id) },
            onGoToAtelier = { navigateToTab(ROUTE_ATELIER) }
        )
    }

    activeSocialDetailItem?.let { socialItem ->
        val authorKey = socialItem.authorId.ifBlank { socialItem.authorHandle.ifBlank { socialItem.authorName } }
        SocialThreadBottomSheet(
            item = socialItem,
            isLiked = likedSocialPostIds.contains(socialItem.id),
            replies = activeSocialReplies,
            currentUserId = currentUserId,
            authorRankTitle = authorRankBadges[authorKey],
            onDismiss = viewModel::closeSocialDetail,
            onToggleLike = { viewModel.toggleSocialLike(socialItem) },
            onShareClick = { shareSocialFeedItem(context, socialItem) },
            onSendReply = { text -> viewModel.addSocialReply(socialItem, text) },
            onDeleteReply = { replyId -> viewModel.deleteSocialReply(socialItem, replyId) }
        )
    }

    if (showEditProfileSheet) {
        EditProfileBottomSheet(
            profile = currentUserProfile,
            isSaving = isSavingProfile,
            onDismiss = {
                if (!isSavingProfile) {
                    showEditProfileSheet = false
                }
            },
            onSave = { name, username, bio, specialty, bitmap, removePhoto ->
                viewModel.saveProfile(
                    displayName = name,
                    usernameRaw = username,
                    bio = bio,
                    specialtyTag = specialty,
                    newAvatarBitmap = bitmap,
                    removeAvatar = removePhoto,
                    onSuccess = { showEditProfileSheet = false }
                )
            }
        )
    }

    editingCommunityPost?.let { post ->
        EditCommunityPostBottomSheet(
            item = post,
            isUpdating = isUpdatingPost,
            onDismiss = {
                if (!isUpdatingPost) {
                    editingCommunityPost = null
                }
            },
            onSave = { title, category, difficulty, yarn, hook, note ->
                viewModel.updateMyCommunityPost(
                    postId = post.id,
                    title = title,
                    category = category,
                    difficulty = difficulty,
                    yarnType = yarn,
                    hookSize = hook,
                    recipeNote = note,
                    onSuccess = { editingCommunityPost = null }
                )
            }
        )
    }

    editingSocialPost?.let { social ->
        EditSocialPostDialog(
            item = social,
            isUpdating = isUpdatingPost,
            onDismiss = {
                if (!isUpdatingPost) {
                    editingSocialPost = null
                }
            },
            onSave = { topic, text ->
                viewModel.updateMySocialPost(
                    postId = social.id,
                    topic = topic,
                    text = text,
                    onSuccess = { editingSocialPost = null }
                )
            }
        )
    }

    if (showAddAtelierProductSheet && viewModel.isAtelierAdmin) {
        AddAtelierProductBottomSheet(
            isSaving = isManagingAtelier,
            onDismiss = {
                if (!isManagingAtelier) {
                    showAddAtelierProductSheet = false
                }
            },
            onSubmit = { bitmap, title, subtitle, price, tag, yarn, dimensions, shopier, dolap ->
                viewModel.addAtelierProduct(
                    bitmap = bitmap,
                    title = title,
                    subtitle = subtitle,
                    priceInput = price,
                    tag = tag,
                    yarnDetail = yarn,
                    dimensions = dimensions,
                    shopierUrl = shopier,
                    dolapUrl = dolap,
                    onSuccess = { showAddAtelierProductSheet = false }
                )
            }
        )
    }

    newlyUnlockedBadge?.let { badge ->
        BadgeUnlockedCelebrationDialog(
            badge = badge,
            onViewBadges = {
                viewModel.dismissUnlockedBadgeCelebration()
                profileTabOverride = 2
                navigateToTab(ROUTE_PROFILE)
            },
            onDismiss = viewModel::dismissUnlockedBadgeCelebration
        )
    }
}

@Composable
private fun FeedAreaModeBar(
    selectedMode: Int,
    onSelectMode: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("feed_area_mode_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FeedAreaModePill(
                label = "İlmek Akışı",
                icon = Icons.Outlined.Forum,
                selected = selectedMode == 0,
                onClick = { onSelectMode(0) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("feed_mode_social")
            )
            FeedAreaModePill(
                label = "Tarifler & Yükle",
                icon = Icons.Outlined.GridView,
                selected = selectedMode == 1,
                onClick = { onSelectMode(1) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("feed_mode_recipes")
            )
        }
    }
}

@Composable
private fun FeedAreaModePill(
    label: String,
    icon: ImageVector,
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
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CraftCommunityBottomBar(
    currentDestination: NavDestination?,
    onTabSelected: (String) -> Unit,
    onCreatePostClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                leftTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    CraftBottomBarTabItem(
                        tab = tab,
                        selected = selected,
                        onClick = { onTabSelected(tab.route) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Prominent Center "+" Share Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.9f)
                        .padding(horizontal = 2.dp)
                ) {
                    Surface(
                        onClick = onCreatePostClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.background),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("tab_create_post")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Yeni paylaşım ekle",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                rightTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    CraftBottomBarTabItem(
                        tab = tab,
                        selected = selected,
                        onClick = { onTabSelected(tab.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CraftBottomBarTabItem(
    tab: BottomNavTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag(tab.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
