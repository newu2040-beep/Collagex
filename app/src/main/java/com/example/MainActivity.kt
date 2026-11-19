package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesomeMosaic
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.database.CollageDatabase
import com.example.core.datastore.PreferencesDataStore
import com.example.core.designsystem.CxBottomNav
import com.example.core.designsystem.CxListItem
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxToastBanner
import com.example.core.designsystem.CxType
import com.example.core.designsystem.MainTab
import com.example.core.model.CanvasSpec
import com.example.core.model.LayoutType
import com.example.core.model.UserPreferences
import com.example.data.firebase.CloudSyncRepository
import com.example.data.firebase.authStateFlow
import com.example.data.repository.CollageRepository
import com.example.engine.CollageExportEngine
import com.example.feature.CollageXViewModel
import com.example.feature.canvasize.CustomSizeScreen
import com.example.feature.editor.EditorScreen
import com.example.feature.export.ExportScreen
import com.example.feature.home.ExportsTabScreen
import com.example.feature.home.HomeScreen
import com.example.feature.home.ProjectsTabScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.welcome.WelcomeScreen
import com.example.ui.theme.CollageXTheme
import com.example.ui.theme.CollageXThemeTokens
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private object CxRoutes {
    const val MAIN = "main"
    const val EDITOR = "editor"
    const val CUSTOM_SIZE = "custom_size"
    const val EXPORT = "export"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val preferencesStore = remember(context) { PreferencesDataStore(context.applicationContext) }
            val preferences by preferencesStore.preferencesFlow.collectAsStateWithLifecycle(
                initialValue = UserPreferences(),
            )

            CollageXTheme(
                palette = preferences.themePalette,
                darkModeOption = preferences.darkMode,
            ) {
                AppNavigation(preferencesStore = preferencesStore)
            }
        }
    }
}

@Composable
private fun AppNavigation(
    preferencesStore: PreferencesDataStore,
) {
    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    val user = currentUser
    if (user == null) {
        WelcomeScreen(
            onAuthSuccess = {
                // Auth state flow automatically transitions into AuthenticatedShell
            },
        )
    } else {
        // Scope ViewModel & Firestore listeners strictly to the authenticated user UID
        key(user.uid) {
            AuthenticatedShell(
                user = user,
                preferencesStore = preferencesStore,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedShell(
    user: FirebaseUser,
    preferencesStore: PreferencesDataStore,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val colors = CollageXThemeTokens.colors

    // Resolve provisioned named Firestore database ID at the ViewModel factory boundary
    val viewModel: CollageXViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val databaseId = appContext.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(databaseId)
                val cloudSync = CloudSyncRepository(db = firestore)
                val localDb = CollageDatabase.getInstance(appContext)
                val repository = CollageRepository(database = localDb, cloudSync = cloudSync)
                val exportEngine = CollageExportEngine(appContext)
                CollageXViewModel(
                    currentUserId = user.uid,
                    userDisplayName = user.displayName ?: "Creator",
                    userEmail = user.email ?: "",
                    userPhotoUrl = user.photoUrl?.toString(),
                    repository = repository,
                    preferencesStore = preferencesStore,
                    exportEngine = exportEngine,
                )
            }
        },
    )

    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var showCreateChooser by remember { mutableStateOf(false) }

    // Pending launch parameters for Android Photo Picker
    var pendingLayout by remember { mutableStateOf(LayoutType.BENTO) }
    var pendingCanvasSpec by remember { mutableStateOf<CanvasSpec?>(null) }

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val projectsState by viewModel.projectsState.collectAsStateWithLifecycle()
    val exportsState by viewModel.exportsState.collectAsStateWithLifecycle()
    val stylePresets by viewModel.stylePresets.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()

    // Zero-permission Android Photo Picker supporting up to 100 photos (2–50+ requirement)
    val multiPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 100),
    ) { uris ->
        if (uris.isNotEmpty()) {
            // Take persistable read permission if provided by picker
            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
            }
            val title = "Collage (${uris.size} Photos)"
            viewModel.createProjectFromPhotos(
                uris = uris,
                title = title,
                layoutType = pendingLayout,
                canvasSpec = pendingCanvasSpec,
                useSamplePack = false,
            ) {
                navController.navigate(CxRoutes.EDITOR)
            }
        }
    }

    fun launchPhotoPickerForNewCollage(layout: LayoutType, spec: CanvasSpec?) {
        pendingLayout = layout
        pendingCanvasSpec = spec
        multiPhotoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = CxRoutes.MAIN,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(180)) },
        ) {
            composable(CxRoutes.MAIN) {
                // Back button from non-Home tabs returns to Home tab first
                BackHandler(enabled = selectedTab != MainTab.HOME) {
                    selectedTab = MainTab.HOME
                }

                Scaffold(
                    containerColor = colors.bg,
                    bottomBar = {
                        CxBottomNav(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it },
                            onCreateClick = { showCreateChooser = true },
                        )
                    },
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(150)) },
                        label = "main_tab_transition",
                    ) { tab ->
                        when (tab) {
                            MainTab.HOME -> HomeScreen(
                                userDisplayName = viewModel.userDisplayName,
                                userPhotoUrl = viewModel.userPhotoUrl,
                                projectsState = projectsState,
                                onPickPhotosForNewCollage = { layout, spec ->
                                    launchPhotoPickerForNewCollage(layout, spec)
                                },
                                onCreateSampleCollage = { title, layout, spec ->
                                    viewModel.createProjectFromPhotos(
                                        uris = emptyList(),
                                        title = title,
                                        layoutType = layout,
                                        canvasSpec = spec,
                                        useSamplePack = true,
                                    ) {
                                        navController.navigate(CxRoutes.EDITOR)
                                    }
                                },
                                onOpenProject = { proj ->
                                    viewModel.openProjectInEditor(proj)
                                    navController.navigate(CxRoutes.EDITOR)
                                },
                                onResizeProject = { proj ->
                                    viewModel.openProjectInEditor(proj)
                                    navController.navigate(CxRoutes.CUSTOM_SIZE)
                                },
                                onRenameProject = viewModel::renameProject,
                                onDuplicateProject = viewModel::duplicateProject,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onDeleteLocalOnly = viewModel::deleteLocalProjectOnly,
                                onDeleteCloudOnly = viewModel::deleteCloudBackupOnly,
                                onDeleteEverywhere = viewModel::deleteProjectEverywhere,
                                onSeeAllProjects = { selectedTab = MainTab.PROJECTS },
                                onOpenSettings = { selectedTab = MainTab.SETTINGS },
                                bottomPadding = innerPadding,
                            )

                            MainTab.PROJECTS -> ProjectsTabScreen(
                                projectsState = projectsState,
                                onOpenProject = { proj ->
                                    viewModel.openProjectInEditor(proj)
                                    navController.navigate(CxRoutes.EDITOR)
                                },
                                onResizeProject = { proj ->
                                    viewModel.openProjectInEditor(proj)
                                    navController.navigate(CxRoutes.CUSTOM_SIZE)
                                },
                                onRenameProject = viewModel::renameProject,
                                onDuplicateProject = viewModel::duplicateProject,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onToggleArchive = viewModel::toggleArchive,
                                onDeleteLocalOnly = viewModel::deleteLocalProjectOnly,
                                onDeleteCloudOnly = viewModel::deleteCloudBackupOnly,
                                onDeleteEverywhere = viewModel::deleteProjectEverywhere,
                                onCreateNew = { showCreateChooser = true },
                                bottomPadding = innerPadding,
                            )

                            MainTab.EXPORTS -> ExportsTabScreen(
                                exportsState = exportsState,
                                onDeleteExport = viewModel::deleteExportRecord,
                                bottomPadding = innerPadding,
                            )

                            MainTab.SETTINGS -> SettingsScreen(
                                userDisplayName = viewModel.userDisplayName,
                                userEmail = viewModel.userEmail,
                                userPhotoUrl = viewModel.userPhotoUrl,
                                preferences = preferences,
                                onUpdatePreferences = viewModel::updatePreferences,
                                onDeleteAllData = viewModel::deleteAllUserData,
                                onShowToast = { msg -> viewModel.showToast(msg) },
                                bottomPadding = innerPadding,
                            )
                        }
                    }
                }
            }

            composable(CxRoutes.EDITOR) {
                EditorScreen(
                    editorState = editorState,
                    stylePresets = stylePresets,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenCustomSize = { navController.navigate(CxRoutes.CUSTOM_SIZE) },
                    onOpenExport = {
                        viewModel.resetExportState()
                        navController.navigate(CxRoutes.EXPORT)
                    },
                )
            }

            composable(CxRoutes.CUSTOM_SIZE) {
                val activeProject = editorState.project
                if (activeProject != null) {
                    CustomSizeScreen(
                        project = activeProject,
                        onBack = { navController.popBackStack() },
                        onGoHome = {
                            navController.popBackStack(CxRoutes.MAIN, inclusive = false)
                        },
                        onApplyChanges = { updatedSpec ->
                            viewModel.applyCustomCanvasSize(updatedSpec)
                            // If opened directly from Home -> Resize, navigate into Editor; otherwise pop back to Editor
                            val prevRoute = navController.previousBackStackEntry?.destination?.route
                            if (prevRoute == CxRoutes.EDITOR) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(CxRoutes.EDITOR) {
                                    popUpTo(CxRoutes.CUSTOM_SIZE) { inclusive = true }
                                }
                            }
                        },
                    )
                }
            }

            composable(CxRoutes.EXPORT) {
                val activeProject = editorState.project
                if (activeProject != null) {
                    ExportScreen(
                        project = activeProject,
                        defaultStripExif = preferences.stripExifDefault,
                        exportState = exportState,
                        onBack = { navController.popBackStack() },
                        onStartExport = viewModel::startExport,
                        onCancelExport = viewModel::cancelExport,
                        onResetExportState = viewModel::resetExportState,
                    )
                }
            }
        }

        // Global non-blocking Toast overlay (z-index top)
        CxToastBanner(
            message = toast?.text,
            isError = toast?.isError == true,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    // Create Collage Chooser Bottom Sheet (triggered by central '+' button)
    if (showCreateChooser) {
        ModalBottomSheet(
            onDismissRequest = { showCreateChooser = false },
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text("Create New Collage", style = CxType.Heading, color = colors.text)
                Text(
                    "Pick 2 to 50+ photos from your device or start immediately with curated scenes.",
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
                CxListItem(
                    title = "Pick Photos from Gallery (2–50+)",
                    subtitle = "Uses Android Photo Picker · Zero storage permissions",
                    leadingIcon = Icons.Rounded.PhotoLibrary,
                    onClick = {
                        showCreateChooser = false
                        launchPhotoPickerForNewCollage(LayoutType.BENTO, null)
                    },
                    testTag = "create_pick_photos",
                )
                CxListItem(
                    title = "50+ Photo Wall Mosaic",
                    subtitle = "Adaptive dense grid for large collections",
                    leadingIcon = Icons.Rounded.GridView,
                    onClick = {
                        showCreateChooser = false
                        launchPhotoPickerForNewCollage(LayoutType.MOSAIC, CanvasSpec(3000.0, 3000.0))
                    },
                    testTag = "create_mosaic_photos",
                )
                CxListItem(
                    title = "Start with 6-Photo Starter Pack",
                    subtitle = "Explore layouts, custom sizing, and export right away",
                    leadingIcon = Icons.Rounded.AutoAwesomeMosaic,
                    onClick = {
                        showCreateChooser = false
                        viewModel.createProjectFromPhotos(
                            uris = emptyList(),
                            title = "Spring Blossom Collage",
                            layoutType = LayoutType.BENTO,
                            canvasSpec = CanvasSpec(3000.0, 2000.0),
                            useSamplePack = true,
                        ) {
                            navController.navigate(CxRoutes.EDITOR)
                        }
                    },
                    testTag = "create_starter_pack",
                )
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }
}
