package com.example.feature.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.designsystem.CxChip
import com.example.core.designsystem.CxEmptyState
import com.example.core.designsystem.CxIconButton
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSizing
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxSwitchRow
import com.example.core.designsystem.CxType
import com.example.core.model.BackgroundType
import com.example.core.model.CanvasMath
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import com.example.core.model.PhotoFitMode
import com.example.core.model.StylePreset
import com.example.feature.CollageXViewModel
import com.example.feature.EditorUiState
import com.example.ui.theme.CollageXThemeTokens
import kotlin.math.roundToInt

private enum class EditorSectionTab { LAYOUT, BORDER, BACKGROUND }

private val BorderPaletteColors = listOf(
    0xFFEA2858.toInt(),
    0xFFF97316.toInt(),
    0xFFEAB308.toInt(),
    0xFF84CC16.toInt(),
    0xFF10B981.toInt(),
    0xFF2563EB.toInt(),
    0xFF8B5CF6.toInt(),
    0xFFFFFFFF.toInt(),
    0xFF1D1724.toInt(),
)

private val PastelBgColors = listOf(
    0xFFFFFFFF.toInt(),
    0xFFFFF5F7.toInt(),
    0xFFFDE6EC.toInt(),
    0xFFF3E8FF.toInt(),
    0xFFE0F2FE.toInt(),
    0xFFDCFCE7.toInt(),
    0xFFFFEDD5.toInt(),
    0xFF1D1724.toInt(),
)

private val PastelGradients = listOf(
    Triple("Blossom", 0xFFFFD6E0.toInt(), 0xFFFFF5F7.toInt()),
    Triple("Sunset", 0xFFFF9A8B.toInt(), 0xFFFF6A88.toInt()),
    Triple("Lavender", 0xFFE9D5FF.toInt(), 0xFFFCE7F3.toInt()),
    Triple("Mint", 0xFFCCFBF1.toInt(), 0xFFE0F2FE.toInt()),
    Triple("Peach", 0xFFFFEDD5.toInt(), 0xFFFECDD3.toInt()),
)

@Composable
fun EditorScreen(
    editorState: EditorUiState,
    stylePresets: List<StylePreset>,
    viewModel: CollageXViewModel,
    onBack: () -> Unit,
    onOpenCustomSize: () -> Unit,
    onOpenExport: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    val project = editorState.project

    var activeSection by remember { mutableStateOf(EditorSectionTab.LAYOUT) }
    var showTextSheet by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSelectedLayerSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }

    // Android Photo Picker for adding up to 50+ photos without storage permissions
    val addPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 100),
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.addPhotosToProject(uris)
    }

    val replacePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) viewModel.replaceSelectedPhoto(uri)
    }

    val bgImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateBackground {
                it.copy(type = BackgroundType.IMAGE, imageUri = uri.toString())
            }
        }
    }

    if (project == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(CxSpace.S16),
            contentAlignment = Alignment.Center,
        ) {
            CxEmptyState(
                icon = Icons.Rounded.Image,
                title = "No active collage",
                message = "Return home to pick photos or open an existing project.",
                actionLabel = "Go Home",
                onAction = onBack,
            )
        }
        return
    }

    val selectedLayer = project.layers.firstOrNull { it.id == editorState.selectedLayerId }
    val dims = remember(project.canvas) { CanvasMath.toPixelDimensions(project.canvas) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
    ) {
        // 1. Top Bar matching mockup: Back | Edit Collage (+ save status) | Undo/Redo | Export pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CxSizing.TopBarHeight)
                .padding(horizontal = CxSpace.S8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CxSpace.S4),
        ) {
            CxIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                onClick = onBack,
                testTag = "editor_back_button",
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.editor_title),
                    style = CxType.Heading,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${dims.widthPx}×${dims.heightPx}px · ${editorState.saveStatus}",
                    style = CxType.Caption,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CxIconButton(
                icon = Icons.AutoMirrored.Rounded.Undo,
                contentDescription = stringResource(R.string.cd_undo),
                enabled = editorState.canUndo,
                onClick = { viewModel.undo() },
                testTag = "editor_undo_button",
            )
            CxIconButton(
                icon = Icons.AutoMirrored.Rounded.Redo,
                contentDescription = stringResource(R.string.cd_redo),
                enabled = editorState.canRedo,
                onClick = { viewModel.redo() },
                testTag = "editor_redo_button",
            )
            Button(
                onClick = onOpenExport,
                shape = RoundedCornerShape(CxRadius.Pill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ),
                contentPadding = PaddingValues(horizontal = CxSpace.S16, vertical = CxSpace.S8),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("editor_export_button"),
            ) {
                Text(
                    text = stringResource(R.string.editor_export),
                    style = CxType.CaptionMedium,
                )
            }
        }

        // 2. Central Live Canvas Preview + Floating Quick Edit & Post-Creation Canvas Size Chips
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S8),
            contentAlignment = Alignment.Center,
        ) {
            CollageCanvasPreview(
                project = project,
                interactive = true,
                selectedLayerId = editorState.selectedLayerId,
                swapSourceLayerId = editorState.swapSourceLayerId,
                showGuides = editorState.showGuides,
                onSelectLayer = { viewModel.selectLayer(it) },
                onDragLayer = { layerId, dx, dy ->
                    viewModel.updateLayer(layerId, pushUndo = false) { l ->
                        if (l.type == LayerType.PHOTO && project.layoutType != LayoutType.FREEFORM) {
                            l.copy(
                                panX = (l.panX + dx).coerceIn(-0.45f, 0.45f),
                                panY = (l.panY + dy).coerceIn(-0.45f, 0.45f),
                            )
                        } else {
                            l.copy(
                                normX = (l.normX + dx).coerceIn(0.05f, 0.95f),
                                normY = (l.normY + dy).coerceIn(0.05f, 0.95f),
                            )
                        }
                    }
                },
            )

            // Top-right floating circular pencil button (matches mockup!) -> opens selected layer controls
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(CxSpace.S8)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.text.copy(alpha = 0.72f))
                    .clickable(role = Role.Button) {
                        if (selectedLayer != null) showSelectedLayerSheet = true
                        else showMoreSheet = true
                    }
                    .testTag("editor_layer_quick_edit"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit selected layer",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Top-left floating Canvas Size badge -> direct post-creation resize entry point!
            Surface(
                shape = RoundedCornerShape(CxRadius.Pill),
                color = colors.surface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(CxSpace.S8)
                    .clip(RoundedCornerShape(CxRadius.Pill))
                    .clickable(role = Role.Button, onClick = onOpenCustomSize)
                    .testTag("editor_canvas_size_badge"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = CxSpace.S12, vertical = CxSpace.S4),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S4),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AspectRatio,
                        contentDescription = stringResource(R.string.cd_canvas_size),
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "${CanvasMath.formatDimension(project.canvas.width, project.canvas.unit)}×${CanvasMath.formatDimension(project.canvas.height, project.canvas.unit)} ${project.canvas.unit.shortLabel}",
                        style = CxType.CaptionMedium,
                        color = colors.text,
                    )
                }
            }
        }

        // 3. Editor Section Controls Panel (Layout | Border | Background)
        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(topStart = CxRadius.Card, topEnd = CxRadius.Card),
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                // Pill section tabs matching mockup: Layout | Border | Background
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    CxChip(
                        label = stringResource(R.string.editor_tab_layout),
                        selected = activeSection == EditorSectionTab.LAYOUT,
                        onClick = { activeSection = EditorSectionTab.LAYOUT },
                        modifier = Modifier.weight(1f),
                        testTag = "editor_tab_layout",
                    )
                    CxChip(
                        label = stringResource(R.string.editor_tab_border),
                        selected = activeSection == EditorSectionTab.BORDER,
                        onClick = { activeSection = EditorSectionTab.BORDER },
                        modifier = Modifier.weight(1f),
                        testTag = "editor_tab_border",
                    )
                    CxChip(
                        label = stringResource(R.string.editor_tab_background),
                        selected = activeSection == EditorSectionTab.BACKGROUND,
                        onClick = { activeSection = EditorSectionTab.BACKGROUND },
                        modifier = Modifier.weight(1f),
                        testTag = "editor_tab_background",
                    )
                }

                // Active Section Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    when (activeSection) {
                        EditorSectionTab.LAYOUT -> {
                            // Horizontal layout carousel starting with rose-outlined '+' add photos tile (exact mockup!)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(CxRadius.Small))
                                            .background(colors.primarySoft)
                                            .border(1.5.dp, colors.primary, RoundedCornerShape(CxRadius.Small))
                                            .clickable {
                                                addPhotosLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                                )
                                            }
                                            .testTag("editor_add_photos_tile"),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = stringResource(R.string.cd_add_photos),
                                            tint = colors.primary,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                                items(LayoutType.entries) { layout ->
                                    val selected = project.layoutType == layout
                                    Surface(
                                        shape = RoundedCornerShape(CxRadius.Small),
                                        color = if (selected) colors.primarySoft else colors.surface2,
                                        border = BorderStroke(
                                            if (selected) 1.5.dp else 1.dp,
                                            if (selected) colors.primary else colors.border,
                                        ),
                                        modifier = Modifier
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(CxRadius.Small))
                                            .clickable { viewModel.updateLayoutType(layout) }
                                            .testTag("layout_option_${layout.name}"),
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = CxSpace.S12),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = layout.label,
                                                style = CxType.CaptionMedium,
                                                color = if (selected) colors.primary else colors.text,
                                            )
                                        }
                                    }
                                }
                            }
                            LabeledSlider(
                                label = stringResource(R.string.editor_border_radius),
                                valueText = "${project.border.cornerRadius.roundToInt()}px",
                                value = project.border.cornerRadius,
                                valueRange = 0f..48f,
                                onValueChange = { r -> viewModel.updateBorder { it.copy(cornerRadius = r) } },
                            )
                            Text(
                                text = stringResource(R.string.editor_border_color),
                                style = CxType.CaptionMedium,
                                color = colors.text,
                            )
                            ColorSwatchRow(
                                colorsList = BorderPaletteColors,
                                selectedColor = project.border.color,
                                onSelect = { c -> viewModel.updateBorder { it.copy(color = c) } },
                            )
                        }

                        EditorSectionTab.BORDER -> {
                            LabeledSlider(
                                label = stringResource(R.string.editor_border_radius),
                                valueText = "${project.border.cornerRadius.roundToInt()}px",
                                value = project.border.cornerRadius,
                                valueRange = 0f..48f,
                                onValueChange = { r -> viewModel.updateBorder { it.copy(cornerRadius = r) } },
                            )
                            LabeledSlider(
                                label = stringResource(R.string.editor_border_spacing),
                                valueText = "${project.border.spacing.roundToInt()}px",
                                value = project.border.spacing,
                                valueRange = 0f..36f,
                                onValueChange = { s -> viewModel.updateBorder { it.copy(spacing = s) } },
                            )
                            LabeledSlider(
                                label = stringResource(R.string.editor_border_padding),
                                valueText = "${project.border.padding.roundToInt()}px",
                                value = project.border.padding,
                                valueRange = 0f..48f,
                                onValueChange = { p -> viewModel.updateBorder { it.copy(padding = p) } },
                            )
                            CxSwitchRow(
                                title = "Soft Cell Shadow",
                                checked = project.border.shadowEnabled,
                                onCheckedChange = { on -> viewModel.updateBorder { it.copy(shadowEnabled = on) } },
                            )
                        }

                        EditorSectionTab.BACKGROUND -> {
                            Text("Solid Pastel Palette", style = CxType.CaptionMedium, color = colors.text)
                            ColorSwatchRow(
                                colorsList = PastelBgColors,
                                selectedColor = if (project.background.type == BackgroundType.SOLID) project.background.primaryColor else -999,
                                onSelect = { c ->
                                    viewModel.updateBackground {
                                        it.copy(type = BackgroundType.SOLID, primaryColor = c)
                                    }
                                },
                            )
                            Text("Pastel Gradients & Image", style = CxType.CaptionMedium, color = colors.text)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                                item {
                                    CxChip(
                                        label = "Pick Image",
                                        selected = project.background.type == BackgroundType.IMAGE,
                                        leadingIcon = Icons.Rounded.Image,
                                        onClick = {
                                            bgImageLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                            )
                                        },
                                    )
                                }
                                items(PastelGradients) { (name, c1, c2) ->
                                    val selected = project.background.type == BackgroundType.GRADIENT &&
                                        project.background.primaryColor == c1
                                    Row(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(CxRadius.Pill))
                                            .background(Brush.linearGradient(listOf(Color(c1), Color(c2))))
                                            .border(
                                                if (selected) 2.dp else 1.dp,
                                                if (selected) colors.primary else colors.border,
                                                RoundedCornerShape(CxRadius.Pill),
                                            )
                                            .clickable {
                                                viewModel.updateBackground {
                                                    it.copy(
                                                        type = BackgroundType.GRADIENT,
                                                        primaryColor = c1,
                                                        secondaryColor = c2,
                                                    )
                                                }
                                            }
                                            .padding(horizontal = CxSpace.S12),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(name, style = CxType.CaptionMedium, color = colors.text)
                                    }
                                }
                            }
                            LabeledSlider(
                                label = "Paper Texture",
                                valueText = "${(project.background.textureIntensity * 100).roundToInt()}%",
                                value = project.background.textureIntensity,
                                valueRange = 0f..1f,
                                onValueChange = { t -> viewModel.updateBackground { it.copy(textureIntensity = t) } },
                            )
                        }
                    }
                }
            }
        }

        // 4. Bottom Editing Toolbar matching mockup: Text | Sticker | Filter | Background | More
        Surface(
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(CxSizing.BottomNavHeight)
                    .padding(horizontal = CxSpace.S8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EditorToolbarItem(
                    label = stringResource(R.string.editor_tool_text),
                    icon = Icons.Rounded.TextFields,
                    selected = showTextSheet,
                    onClick = { showTextSheet = true },
                    modifier = Modifier.weight(1f),
                    testTag = "editor_tool_text",
                )
                EditorToolbarItem(
                    label = stringResource(R.string.editor_tool_sticker),
                    icon = Icons.Rounded.EmojiEmotions,
                    selected = showStickerSheet,
                    onClick = { showStickerSheet = true },
                    modifier = Modifier.weight(1f),
                    testTag = "editor_tool_sticker",
                )
                EditorToolbarItem(
                    label = stringResource(R.string.editor_tool_filter),
                    icon = Icons.Rounded.AutoFixHigh,
                    selected = showFilterSheet,
                    onClick = {
                        if (selectedLayer?.type == LayerType.PHOTO) showFilterSheet = true
                        else {
                            val firstPhoto = project.layers.firstOrNull { it.type == LayerType.PHOTO }
                            if (firstPhoto != null) {
                                viewModel.selectLayer(firstPhoto.id)
                                showFilterSheet = true
                            } else {
                                viewModel.showToast("Add a photo first to apply filters")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "editor_tool_filter",
                )
                EditorToolbarItem(
                    label = stringResource(R.string.editor_tool_background),
                    icon = Icons.Rounded.Gradient,
                    selected = activeSection == EditorSectionTab.BACKGROUND,
                    onClick = { activeSection = EditorSectionTab.BACKGROUND },
                    modifier = Modifier.weight(1f),
                    testTag = "editor_tool_background",
                )
                EditorToolbarItem(
                    label = stringResource(R.string.editor_tool_more),
                    icon = Icons.Rounded.MoreHoriz,
                    selected = showMoreSheet,
                    onClick = { showMoreSheet = true },
                    modifier = Modifier.weight(1f),
                    testTag = "editor_tool_more",
                )
            }
        }
    }

    // --- Interactive Tool Sheets ---

    if (showTextSheet) {
        val editingText = selectedLayer?.takeIf { it.type == LayerType.TEXT }
        TextEditorSheet(
            initialStyle = editingText?.textStyle,
            onDismiss = { showTextSheet = false },
            onApply = { style ->
                if (editingText != null) {
                    viewModel.updateLayer(editingText.id) {
                        it.copy(textStyle = style, label = style.text.take(24))
                    }
                } else {
                    viewModel.addTextLayer(style)
                }
            },
        )
    }

    if (showStickerSheet) {
        StickerPickerSheet(
            onDismiss = { showStickerSheet = false },
            onAddSticker = { kind, color -> viewModel.addStickerLayer(kind, color) },
        )
    }

    if (showFilterSheet && selectedLayer != null && selectedLayer.type == LayerType.PHOTO) {
        FilterAdjustmentsSheet(
            layer = selectedLayer,
            onDismiss = { showFilterSheet = false },
            onUpdateAdjustments = { adj ->
                viewModel.updateLayer(selectedLayer.id) { it.copy(adjustments = adj) }
            },
            onReset = { viewModel.resetSelectedLayer() },
        )
    }

    if (showSelectedLayerSheet && selectedLayer != null) {
        SelectedPhotoActionsSheet(
            layer = selectedLayer,
            onDismiss = { showSelectedLayerSheet = false },
            onReplacePhoto = {
                replacePhotoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onRotate90 = {
                viewModel.updateLayer(selectedLayer.id) { it.copy(rotationDeg = (it.rotationDeg + 90f) % 360f) }
            },
            onFlipH = {
                viewModel.updateLayer(selectedLayer.id) { it.copy(flipHorizontal = !it.flipHorizontal) }
            },
            onFlipV = {
                viewModel.updateLayer(selectedLayer.id) { it.copy(flipVertical = !it.flipVertical) }
            },
            onToggleFitMode = {
                viewModel.updateLayer(selectedLayer.id) {
                    it.copy(
                        fitMode = if (it.fitMode == PhotoFitMode.COVER) PhotoFitMode.CONTAIN else PhotoFitMode.COVER,
                    )
                }
            },
            onScaleChange = { s ->
                viewModel.updateLayer(selectedLayer.id, pushUndo = false) { it.copy(scale = s) }
            },
            onOpacityChange = { op ->
                viewModel.updateLayer(selectedLayer.id, pushUndo = false) { it.copy(opacity = op) }
            },
            onCornerOverride = { r ->
                viewModel.updateLayer(selectedLayer.id, pushUndo = false) { it.copy(cornerRadiusOverride = r) }
            },
            onSwapPhoto = { viewModel.toggleSwapMode() },
            onDuplicate = { viewModel.duplicateLayer(selectedLayer.id) },
            onReset = { viewModel.resetSelectedLayer() },
            onDelete = { viewModel.deleteLayer(selectedLayer.id) },
        )
    }

    if (showMoreSheet) {
        MoreToolsAndLayersSheet(
            project = project,
            selectedLayerId = editorState.selectedLayerId,
            showGuides = editorState.showGuides,
            stylePresets = stylePresets,
            onDismiss = { showMoreSheet = false },
            onOpenCustomSize = onOpenCustomSize,
            onAddMorePhotos = {
                addPhotosLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onAddSampleBatch = { count -> viewModel.addSamplePhotos(count) },
            onToggleGuides = { viewModel.toggleGuides() },
            onSaveStylePreset = { name -> viewModel.saveCurrentStylePreset(name) },
            onApplyStylePreset = { preset -> viewModel.applyStylePreset(preset) },
            onSelectLayer = { id -> viewModel.selectLayer(id) },
            onMoveLayerOrder = { id, delta -> viewModel.moveLayerOrder(id, delta) },
            onToggleLayerLock = { id -> viewModel.toggleLayerLock(id) },
            onToggleLayerVisibility = { id -> viewModel.toggleLayerVisibility(id) },
            onDeleteLayer = { id -> viewModel.deleteLayer(id) },
        )
    }
}

@Composable
private fun EditorToolbarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    val colors = CollageXThemeTokens.colors
    val tint = if (selected) colors.primary else colors.textMuted
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CxRadius.Small))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = CxSpace.S4)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = CxType.Caption,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
