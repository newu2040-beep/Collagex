package com.example.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesomeMosaic
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FileDownloadDone
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.designsystem.CxCard
import com.example.core.designsystem.CxChip
import com.example.core.designsystem.CxEmptyState
import com.example.core.designsystem.CxErrorState
import com.example.core.designsystem.CxIconButton
import com.example.core.designsystem.CxListItem
import com.example.core.designsystem.CxLoadingState
import com.example.core.designsystem.CxPrimaryButton
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSecondaryButton
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxTextField
import com.example.core.designsystem.CxType
import com.example.core.designsystem.SampleArtwork
import com.example.core.model.CanvasMath
import com.example.core.model.CanvasSpec
import com.example.core.model.CollageProject
import com.example.core.model.ExportRecord
import com.example.core.model.LayoutType
import com.example.core.model.UiState
import com.example.feature.editor.CollageCanvasPreview
import com.example.ui.theme.CollageXThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StarterTemplate(
    val id: String,
    val name: String,
    val subtitle: String,
    val layoutType: LayoutType,
    val canvas: CanvasSpec,
)

private val CuratedTemplates = listOf(
    StarterTemplate("t_bento", "Classic Bento", "6-photo balanced grid · 3:2", LayoutType.BENTO, CanvasSpec(3000.0, 2000.0)),
    StarterTemplate("t_story", "Story Trio", "Vertical 9:16 social frame", LayoutType.ASYMMETRIC, CanvasSpec(1800.0, 3200.0)),
    StarterTemplate("t_square", "Square Mosaic", "Clean 1:1 album grid", LayoutType.GRID, CanvasSpec(2400.0, 2400.0)),
    StarterTemplate("t_masonry", "Editorial Masonry", "Dynamic 4:5 portrait flow", LayoutType.MASONRY, CanvasSpec(2160.0, 2700.0)),
    StarterTemplate("t_strip", "Film Strip", "Panoramic photo strip · 16:9", LayoutType.STRIP, CanvasSpec(3840.0, 2160.0)),
    StarterTemplate("t_free", "Scrapbook Freeform", "Drag & layer freely · 3:4", LayoutType.FREEFORM, CanvasSpec(2400.0, 3200.0)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userDisplayName: String,
    userPhotoUrl: String?,
    projectsState: UiState<List<CollageProject>>,
    onPickPhotosForNewCollage: (LayoutType, CanvasSpec?) -> Unit,
    onCreateSampleCollage: (String, LayoutType, CanvasSpec?) -> Unit,
    onOpenProject: (CollageProject) -> Unit,
    onResizeProject: (CollageProject) -> Unit,
    onRenameProject: (CollageProject, String) -> Unit,
    onDuplicateProject: (CollageProject) -> Unit,
    onToggleFavorite: (CollageProject) -> Unit,
    onDeleteLocalOnly: (String) -> Unit,
    onDeleteCloudOnly: (String) -> Unit,
    onDeleteEverywhere: (String) -> Unit,
    onSeeAllProjects: () -> Unit,
    onOpenSettings: () -> Unit,
    bottomPadding: PaddingValues,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var showLayoutsSheet by remember { mutableStateOf(false) }
    var showToolsSheet by remember { mutableStateOf(false) }
    var renamingProject by remember { mutableStateOf<CollageProject?>(null) }
    var deletingProject by remember { mutableStateOf<CollageProject?>(null) }

    val firstName = remember(userDisplayName) {
        userDisplayName.trim().substringBefore(" ").ifBlank { "Creator" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = CxSpace.S16,
            end = CxSpace.S16,
            top = CxSpace.S16,
            bottom = bottomPadding.calculateBottomPadding() + CxSpace.S24,
        ),
        verticalArrangement = Arrangement.spacedBy(CxSpace.S24),
    ) {
        // 1. Greeting Header + Optional Profile Avatar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
                ) {
                    Text(
                        text = strings.homeGreetingPrefix,
                        style = CxType.Heading,
                        color = colors.textMuted,
                    )
                    Text(
                        text = "$firstName!",
                        style = CxType.Title,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = strings.homeSubtitle,
                        style = CxType.Caption,
                        color = colors.textMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(CxSpace.S12))
                UserAvatarButton(
                    name = firstName,
                    photoUrl = userPhotoUrl,
                    onClick = onOpenSettings,
                )
            }
        }

        // 2. New Collage Hero Banner Card matching mockup
        item {
            Surface(
                shape = RoundedCornerShape(CxRadius.Card),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CxRadius.Card))
                    .clickable(role = Role.Button) {
                        onPickPhotosForNewCollage(LayoutType.BENTO, null)
                    }
                    .testTag("home_new_collage_card"),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(colors.heroEnd, colors.heroStart),
                            ),
                        )
                        .padding(CxSpace.S16),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
                        ) {
                            Text(
                                text = strings.homeNewCollage,
                                style = CxType.Title,
                                color = Color.White,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(CxSpace.S4),
                            ) {
                                Text(
                                    text = strings.homeStartCreating,
                                    style = CxType.CaptionMedium,
                                    color = Color.White.copy(alpha = 0.92f),
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(CxSpace.S12))
                        // Tilted miniature collage preview cluster
                        Box(
                            modifier = Modifier.size(width = 104.dp, height = 84.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(CxRadius.Small),
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier
                                    .offset(x = (-18).dp, y = (-8).dp)
                                    .size(width = 48.dp, height = 56.dp)
                                    .rotate(-8f),
                            ) {
                                SampleArtwork.ScenicCanvas(SampleArtwork.PAGODA, Modifier.fillMaxSize())
                            }
                            Surface(
                                shape = RoundedCornerShape(CxRadius.Small),
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier
                                    .offset(x = 20.dp, y = (-6).dp)
                                    .size(width = 46.dp, height = 42.dp)
                                    .rotate(8f),
                            ) {
                                SampleArtwork.ScenicCanvas(SampleArtwork.ALPINE, Modifier.fillMaxSize())
                            }
                            Surface(
                                shape = RoundedCornerShape(CxRadius.Small),
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier
                                    .offset(x = 6.dp, y = 20.dp)
                                    .size(width = 56.dp, height = 42.dp)
                                    .rotate(4f),
                            ) {
                                SampleArtwork.ScenicCanvas(SampleArtwork.BLOSSOMS, Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Tools Row (Templates, Gallery, Layout, Tools)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S12)) {
                SectionHeaderRow(
                    title = strings.homeQuickTools,
                    actionLabel = strings.actionSeeAll,
                    onAction = { showToolsSheet = true },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S12),
                ) {
                    QuickToolTile(
                        label = strings.toolTemplates,
                        icon = Icons.Rounded.GridView,
                        containerColor = colors.toolPink,
                        iconTint = colors.primary,
                        onClick = { showTemplatesSheet = true },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_tool_templates",
                    )
                    QuickToolTile(
                        label = strings.toolGallery,
                        icon = Icons.Rounded.PhotoLibrary,
                        containerColor = colors.toolBlue,
                        iconTint = colors.text,
                        onClick = { onPickPhotosForNewCollage(LayoutType.GRID, null) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_tool_gallery",
                    )
                    QuickToolTile(
                        label = strings.toolLayout,
                        icon = Icons.Rounded.Dashboard,
                        containerColor = colors.toolPeach,
                        iconTint = colors.text,
                        onClick = { showLayoutsSheet = true },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_tool_layout",
                    )
                    QuickToolTile(
                        label = strings.toolTools,
                        icon = Icons.Rounded.Build,
                        containerColor = colors.toolLavender,
                        iconTint = colors.text,
                        onClick = { showToolsSheet = true },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_tool_tools",
                    )
                }
            }
        }

        // 4. Recent Collages
        item {
            SectionHeaderRow(
                title = strings.homeRecentCollages,
                actionLabel = strings.actionSeeAll,
                onAction = onSeeAllProjects,
            )
        }

        when (projectsState) {
            is UiState.Loading -> {
                item { CxLoadingState(strings.loadingCollages) }
            }
            is UiState.Error -> {
                item { CxErrorState(projectsState.message) }
            }
            is UiState.Success -> {
                val activeProjects = projectsState.data.filterNot { it.archived }.take(6)
                if (activeProjects.isEmpty()) {
                    item {
                        CxEmptyState(
                            icon = Icons.Rounded.Collections,
                            title = strings.noCollagesTitle,
                            message = strings.noCollagesMsg,
                            actionLabel = strings.tryStarterCollage,
                            onAction = {
                                onCreateSampleCollage("Kyoto Memories", LayoutType.BENTO, CanvasSpec(3000.0, 2000.0))
                            },
                        )
                    }
                } else {
                    val rows = activeProjects.chunked(2)
                    items(rows, key = { pair -> pair.joinToString("_") { it.id } }) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(CxSpace.S12),
                        ) {
                            pair.forEach { proj ->
                                ProjectThumbnailCard(
                                    project = proj,
                                    onOpen = { onOpenProject(proj) },
                                    onResize = { onResizeProject(proj) },
                                    onRename = { renamingProject = proj },
                                    onDuplicate = { onDuplicateProject(proj) },
                                    onToggleFavorite = { onToggleFavorite(proj) },
                                    onRequestDelete = { deletingProject = proj },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Templates Bottom Sheet
    if (showTemplatesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplatesSheet = false },
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text(strings.starterTemplatesTitle, style = CxType.Heading, color = colors.text)
                Text(
                    strings.starterTemplatesSubtitle,
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
                CuratedTemplates.forEach { tpl ->
                    CxListItem(
                        title = tpl.name,
                        subtitle = tpl.subtitle,
                        leadingIcon = Icons.Rounded.AutoAwesomeMosaic,
                        onClick = {
                            showTemplatesSheet = false
                            onCreateSampleCollage(tpl.name, tpl.layoutType, tpl.canvas)
                        },
                    )
                }
                Spacer(Modifier.height(CxSpace.S16))
            }
        }
    }

    // Layout Browser Sheet
    if (showLayoutsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLayoutsSheet = false },
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text(strings.createWithLayoutTitle, style = CxType.Heading, color = colors.text)
                LayoutType.entries.forEach { layout ->
                    CxListItem(
                        title = strings.layoutLabel(layout),
                        subtitle = strings.pickPhotosFromGallery,
                        leadingIcon = Icons.Rounded.Dashboard,
                        onClick = {
                            showLayoutsSheet = false
                            onPickPhotosForNewCollage(layout, null)
                        },
                    )
                }
                Spacer(Modifier.height(CxSpace.S16))
            }
        }
    }

    // Quick Tools Sheet
    if (showToolsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showToolsSheet = false },
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text(strings.collageToolsTitle, style = CxType.Heading, color = colors.text)
                CxListItem(
                    title = strings.pickPhotosFromGallery,
                    subtitle = strings.galleryPickerTitle,
                    leadingIcon = Icons.Rounded.PhotoLibrary,
                    onClick = {
                        showToolsSheet = false
                        onPickPhotosForNewCollage(LayoutType.MOSAIC, null)
                    },
                )
                CxListItem(
                    title = strings.starterPackTitle,
                    subtitle = strings.starterPackSubtitle,
                    leadingIcon = Icons.Rounded.AutoAwesomeMosaic,
                    onClick = {
                        showToolsSheet = false
                        onCreateSampleCollage("Cherry Blossom Bento", LayoutType.BENTO, CanvasSpec(3000.0, 2000.0))
                    },
                )
                CxListItem(
                    title = strings.printReadyA4Title,
                    subtitle = "210 × 297 mm · 2480 × 3508 px",
                    leadingIcon = Icons.Rounded.AspectRatio,
                    onClick = {
                        showToolsSheet = false
                        val a4 = CanvasMath.PrintPresets.first()
                        onCreateSampleCollage(
                            "A4 Print Collage",
                            LayoutType.MASONRY,
                            CanvasSpec(a4.printW, a4.printH, a4.printUnit, a4.printDpi),
                        )
                    },
                )
                Spacer(Modifier.height(CxSpace.S16))
            }
        }
    }

    renamingProject?.let { target ->
        RenameProjectDialog(
            currentTitle = target.title,
            onDismiss = { renamingProject = null },
            onConfirm = { newTitle ->
                onRenameProject(target, newTitle)
                renamingProject = null
            },
        )
    }

    deletingProject?.let { target ->
        DeleteProjectDialog(
            project = target,
            onDismiss = { deletingProject = null },
            onDeleteLocal = {
                onDeleteLocalOnly(target.id)
                deletingProject = null
            },
            onDeleteCloud = {
                onDeleteCloudOnly(target.id)
                deletingProject = null
            },
            onDeleteBoth = {
                onDeleteEverywhere(target.id)
                deletingProject = null
            },
        )
    }
}

@Composable
fun ProjectsTabScreen(
    projectsState: UiState<List<CollageProject>>,
    onOpenProject: (CollageProject) -> Unit,
    onResizeProject: (CollageProject) -> Unit,
    onRenameProject: (CollageProject, String) -> Unit,
    onDuplicateProject: (CollageProject) -> Unit,
    onToggleFavorite: (CollageProject) -> Unit,
    onToggleArchive: (CollageProject) -> Unit,
    onDeleteLocalOnly: (String) -> Unit,
    onDeleteCloudOnly: (String) -> Unit,
    onDeleteEverywhere: (String) -> Unit,
    onCreateNew: () -> Unit,
    bottomPadding: PaddingValues,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    var query by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("ALL") } // ALL, FAVORITES, ARCHIVED
    var sortMode by remember { mutableStateOf("RECENT") } // RECENT, NAME, PHOTOS
    var renamingProject by remember { mutableStateOf<CollageProject?>(null) }
    var deletingProject by remember { mutableStateOf<CollageProject?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = CxSpace.S16,
            end = CxSpace.S16,
            top = CxSpace.S16,
            bottom = bottomPadding.calculateBottomPadding() + CxSpace.S24,
        ),
        verticalArrangement = Arrangement.spacedBy(CxSpace.S16),
    ) {
        item {
            Text(text = strings.projectsTitle, style = CxType.Title, color = colors.text)
        }
        item {
            CxTextField(
                label = "",
                value = query,
                onValueChange = { query = it },
                placeholder = strings.searchCollagesPlaceholder,
                testTag = "projects_search_input",
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                item { CxChip(strings.filterAll, filterMode == "ALL", { filterMode = "ALL" }) }
                item { CxChip(strings.filterFavorites, filterMode == "FAVORITES", { filterMode = "FAVORITES" }) }
                item { CxChip(strings.filterArchived, filterMode == "ARCHIVED", { filterMode = "ARCHIVED" }) }
                item {
                    CxChip(
                        label = when (sortMode) {
                            "NAME" -> strings.sortName
                            "PHOTOS" -> strings.sortPhotos
                            else -> strings.sortRecent
                        },
                        selected = true,
                        onClick = {
                            sortMode = when (sortMode) {
                                "RECENT" -> "NAME"
                                "NAME" -> "PHOTOS"
                                else -> "RECENT"
                            }
                        },
                    )
                }
            }
        }

        when (projectsState) {
            is UiState.Loading -> item { CxLoadingState(strings.loadingProjects) }
            is UiState.Error -> item { CxErrorState(projectsState.message) }
            is UiState.Success -> {
                val filtered = projectsState.data
                    .filter { p ->
                        when (filterMode) {
                            "FAVORITES" -> p.favorite && !p.archived
                            "ARCHIVED" -> p.archived
                            else -> !p.archived
                        }
                    }
                    .filter { p -> query.isBlank() || p.title.contains(query, ignoreCase = true) }
                    .let { list ->
                        when (sortMode) {
                            "NAME" -> list.sortedBy { it.title.lowercase(Locale.getDefault()) }
                            "PHOTOS" -> list.sortedByDescending { it.photoCount }
                            else -> list.sortedByDescending { it.updatedAtMs }
                        }
                    }

                if (filtered.isEmpty()) {
                    item {
                        CxEmptyState(
                            icon = Icons.Rounded.Collections,
                            title = strings.noMatchingProjectsTitle,
                            message = strings.noMatchingProjectsMsg,
                            actionLabel = strings.navCreate,
                            onAction = onCreateNew,
                        )
                    }
                } else {
                    val rows = filtered.chunked(2)
                    items(rows, key = { pair -> pair.joinToString("_") { it.id } }) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(CxSpace.S12),
                        ) {
                            pair.forEach { proj ->
                                ProjectThumbnailCard(
                                    project = proj,
                                    onOpen = { onOpenProject(proj) },
                                    onResize = { onResizeProject(proj) },
                                    onRename = { renamingProject = proj },
                                    onDuplicate = { onDuplicateProject(proj) },
                                    onToggleFavorite = { onToggleFavorite(proj) },
                                    onToggleArchive = { onToggleArchive(proj) },
                                    onRequestDelete = { deletingProject = proj },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    renamingProject?.let { target ->
        RenameProjectDialog(
            currentTitle = target.title,
            onDismiss = { renamingProject = null },
            onConfirm = { newTitle ->
                onRenameProject(target, newTitle)
                renamingProject = null
            },
        )
    }

    deletingProject?.let { target ->
        DeleteProjectDialog(
            project = target,
            onDismiss = { deletingProject = null },
            onDeleteLocal = {
                onDeleteLocalOnly(target.id)
                deletingProject = null
            },
            onDeleteCloud = {
                onDeleteCloudOnly(target.id)
                deletingProject = null
            },
            onDeleteBoth = {
                onDeleteEverywhere(target.id)
                deletingProject = null
            },
        )
    }
}

@Composable
fun ExportsTabScreen(
    exportsState: UiState<List<ExportRecord>>,
    onDeleteExport: (String) -> Unit,
    bottomPadding: PaddingValues,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    val context = LocalContext.current
    val dateFmt = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = CxSpace.S16,
            end = CxSpace.S16,
            top = CxSpace.S16,
            bottom = bottomPadding.calculateBottomPadding() + CxSpace.S24,
        ),
        verticalArrangement = Arrangement.spacedBy(CxSpace.S16),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S4)) {
                Text(text = strings.exportsTitle, style = CxType.Title, color = colors.text)
                Text(
                    text = strings.exportsSubtitle,
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
            }
        }

        when (exportsState) {
            is UiState.Loading -> item { CxLoadingState(strings.loadingExports) }
            is UiState.Error -> item { CxErrorState(exportsState.message) }
            is UiState.Success -> {
                val list = exportsState.data
                if (list.isEmpty()) {
                    item {
                        CxEmptyState(
                            icon = Icons.Rounded.FileDownloadDone,
                            title = strings.noExportsTitle,
                            message = strings.noExportsMsg,
                        )
                    }
                } else {
                    items(list, key = { it.id }) { rec ->
                        CxCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
                                ) {
                                    Text(
                                        text = rec.projectTitle,
                                        style = CxType.BodyMedium,
                                        color = colors.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${rec.format.label} · ${rec.widthPx}×${rec.heightPx}px · ${rec.dpi} DPI",
                                        style = CxType.Caption,
                                        color = colors.textMuted,
                                    )
                                    Text(
                                        text = "${formatBytes(rec.fileSizeBytes)} · ${dateFmt.format(Date(rec.createdAtMs))}",
                                        style = CxType.Caption,
                                        color = colors.textMuted,
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(CxSpace.S4)) {
                                    if (!rec.localUri.isNullOrBlank()) {
                                        CxIconButton(
                                            icon = Icons.Rounded.OpenInNew,
                                            contentDescription = strings.exportOpen,
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(Uri.parse(rec.localUri), rec.format.mimeType)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                runCatching { context.startActivity(intent) }
                                            },
                                            tint = colors.primary,
                                        )
                                        CxIconButton(
                                            icon = Icons.Rounded.Share,
                                            contentDescription = strings.exportDirectShare,
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = rec.format.mimeType
                                                    putExtra(Intent.EXTRA_STREAM, Uri.parse(rec.localUri))
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                runCatching {
                                                    context.startActivity(Intent.createChooser(intent, strings.exportDirectShare))
                                                }
                                            },
                                        )
                                    }
                                    CxIconButton(
                                        icon = Icons.Rounded.Delete,
                                        contentDescription = strings.deleteLabel,
                                        onClick = { onDeleteExport(rec.id) },
                                        tint = colors.textMuted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserAvatarButton(
    name: String,
    photoUrl: String?,
    onClick: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(colors.primarySoft)
            .border(2.dp, colors.surface, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("home_avatar_button"),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = stringResource(R.string.cd_profile),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = name.take(1).uppercase(Locale.getDefault()),
                style = CxType.Heading,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = CxType.Heading, color = colors.text)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(CxRadius.Pill))
                .clickable(role = Role.Button, onClick = onAction)
                .padding(horizontal = CxSpace.S8, vertical = CxSpace.S4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CxSpace.S4),
        ) {
            Text(text = actionLabel, style = CxType.CaptionMedium, color = colors.primary)
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun QuickToolTile(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    val colors = CollageXThemeTokens.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CxRadius.Card))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = CxSpace.S4)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(CxRadius.Card))
                .background(containerColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = label,
            style = CxType.CaptionMedium,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProjectThumbnailCard(
    project: CollageProject,
    onOpen: () -> Unit,
    onResize: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleArchive: (() -> Unit)? = null,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    var menuExpanded by remember { mutableStateOf(false) }
    val dims = remember(project.canvas) { CanvasMath.toPixelDimensions(project.canvas) }

    CxCard(
        modifier = modifier.testTag("project_card_${project.id}"),
        onClick = onOpen,
        contentPadding = PaddingValues(CxSpace.S8),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            contentAlignment = Alignment.Center,
        ) {
            CollageCanvasPreview(
                project = project,
                interactive = false,
                proxyMaxEdgePx = 256,
                modifier = Modifier.fillMaxSize(),
            )
            // Favorite indicator badge
            if (project.favorite) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(CxSpace.S4)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.surface.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Favorite",
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            // Overflow menu trigger
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Box(
                    modifier = Modifier
                        .padding(CxSpace.S4)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.surface.copy(alpha = 0.9f))
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Project options",
                        tint = colors.text,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.menuResizeCanvas, style = CxType.Body) },
                        leadingIcon = { Icon(Icons.Rounded.AspectRatio, null) },
                        onClick = { menuExpanded = false; onResize() },
                    )
                    DropdownMenuItem(
                        text = { Text(strings.menuRename, style = CxType.Body) },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                        onClick = { menuExpanded = false; onRename() },
                    )
                    DropdownMenuItem(
                        text = { Text(strings.menuDuplicate, style = CxType.Body) },
                        leadingIcon = { Icon(Icons.Rounded.ContentCopy, null) },
                        onClick = { menuExpanded = false; onDuplicate() },
                    )
                    DropdownMenuItem(
                        text = { Text(if (project.favorite) strings.menuUnfavorite else strings.menuFavorite, style = CxType.Body) },
                        leadingIcon = {
                            Icon(if (project.favorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite, null)
                        },
                        onClick = { menuExpanded = false; onToggleFavorite() },
                    )
                    if (onToggleArchive != null) {
                        DropdownMenuItem(
                            text = { Text(if (project.archived) strings.menuUnarchive else strings.menuArchive, style = CxType.Body) },
                            leadingIcon = { Icon(Icons.Rounded.Archive, null) },
                            onClick = { menuExpanded = false; onToggleArchive() },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("${strings.deleteLabel}…", style = CxType.Body, color = colors.danger) },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = colors.danger) },
                        onClick = { menuExpanded = false; onRequestDelete() },
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S4)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = project.title,
                    style = CxType.BodyMedium,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (project.cloudBackedUp) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                    contentDescription = if (project.cloudBackedUp) strings.syncedStatus else strings.savedStatus,
                    tint = if (project.cloudBackedUp) colors.success else colors.textMuted,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = "${project.photoCount} ${strings.photosWord} · ${dims.widthPx}×${dims.heightPx}",
                style = CxType.Caption,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RenameProjectDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val strings = CollageXThemeTokens.strings
    var title by remember(currentTitle) { mutableStateOf(currentTitle) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.renameCollageTitle, style = CxType.Heading) },
        text = {
            CxTextField(
                label = strings.projectTitleLabel,
                value = title,
                onValueChange = { title = it.take(120) },
                testTag = "rename_project_input",
            )
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onConfirm(title) }) {
                Text(strings.saveLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancelLabel) }
        },
    )
}

@Composable
private fun DeleteProjectDialog(
    project: CollageProject,
    onDismiss: () -> Unit,
    onDeleteLocal: () -> Unit,
    onDeleteCloud: () -> Unit,
    onDeleteBoth: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${strings.deleteLabel} \"${project.title}\"?", style = CxType.Heading) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                Text(
                    text = strings.deleteProjectMsg,
                    style = CxType.Body,
                    color = colors.textMuted,
                )
                CxSecondaryButton(text = strings.removeLocalOnly, onClick = onDeleteLocal)
                if (project.cloudBackedUp) {
                    CxSecondaryButton(text = strings.deleteCloudOnly, onClick = onDeleteCloud)
                }
                CxPrimaryButton(text = strings.deleteLocalAndCloud, onClick = onDeleteBoth)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancelLabel) }
        },
    )
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> "${bytes / 1024L} KB"
        else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
