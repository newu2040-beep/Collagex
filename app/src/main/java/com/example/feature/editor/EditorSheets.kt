package com.example.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesomeMosaic
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CanvasFonts
import com.example.core.designsystem.CxChip
import com.example.core.designsystem.CxIconButton
import com.example.core.designsystem.CxListItem
import com.example.core.designsystem.CxPrimaryButton
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSecondaryButton
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxSwitchRow
import com.example.core.designsystem.CxTextField
import com.example.core.designsystem.CxType
import com.example.core.model.CanvasMath
import com.example.core.model.CanvasSpec
import com.example.core.model.CollageLayer
import com.example.core.model.CollageProject
import com.example.core.model.DimensionUnit
import com.example.core.model.LayerType
import com.example.core.model.PhotoAdjustments
import com.example.core.model.PhotoFilter
import com.example.core.model.PhotoFitMode
import com.example.core.model.StickerKind
import com.example.core.model.StylePreset
import com.example.core.model.TextStyleSpec
import com.example.engine.AutoFitEngine
import com.example.engine.AutoFitPreset
import com.example.ui.theme.CollageXThemeTokens
import kotlin.math.roundToInt

private val SwatchColors = listOf(
    0xFF1D1724.toInt(),
    0xFFFFFFFF.toInt(),
    0xFFEA2858.toInt(),
    0xFFF97316.toInt(),
    0xFFEAB308.toInt(),
    0xFF16A34A.toInt(),
    0xFF2563EB.toInt(),
    0xFF7C3AED.toInt(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorSheet(
    initialStyle: TextStyleSpec?,
    isLocked: Boolean = false,
    onDismiss: () -> Unit,
    onApply: (TextStyleSpec) -> Unit,
    onToggleLock: (() -> Unit)? = null,
    onRequestDelete: (() -> Unit)? = null,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    var text by remember { mutableStateOf(initialStyle?.text ?: "Spring Memories") }
    var fontId by remember { mutableStateOf(initialStyle?.fontId ?: "SANS") }
    var fontSize by remember { mutableFloatStateOf(initialStyle?.fontSizeSp ?: 20f) }
    var bold by remember { mutableStateOf(initialStyle?.bold ?: true) }
    var textColor by remember { mutableIntStateOf(initialStyle?.color ?: 0xFF1D1724.toInt()) }
    var useChip by remember { mutableStateOf(initialStyle?.chipColor != null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Text(
                text = if (initialStyle == null) strings.addTextLayer else strings.editTextLayer,
                style = CxType.Heading,
                color = colors.text,
            )
            CxTextField(
                label = strings.layerText,
                value = text,
                onValueChange = { text = it.take(80) },
                placeholder = strings.writeCaptionPlaceholder,
                testTag = "text_layer_input",
            )
            Text("${strings.inbuiltSystemFonts} (${CanvasFonts.AllCanvasFonts.size})", style = CxType.CaptionMedium, color = colors.textMuted)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                items(CanvasFonts.AllCanvasFonts, key = { it.id }) { fontChoice ->
                    val isSelected = fontId == fontChoice.id
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(CxRadius.Card))
                            .background(if (isSelected) colors.primarySoft else colors.surface2)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.border,
                                shape = RoundedCornerShape(CxRadius.Card),
                            )
                            .clickable { fontId = fontChoice.id }
                            .padding(horizontal = CxSpace.S12, vertical = CxSpace.S8),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = fontChoice.label,
                            fontFamily = fontChoice.family,
                            fontSize = 15.sp,
                            color = if (isSelected) colors.primary else colors.text,
                            maxLines = 1,
                        )
                        Text(
                            text = fontChoice.category,
                            style = CxType.Caption,
                            color = colors.textMuted,
                            maxLines = 1,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                CxChip(strings.boldLabel, bold, { bold = !bold })
                CxChip(strings.pillBackground, useChip, { useChip = !useChip })
            }
            LabeledSlider(
                label = strings.sizeLabel,
                valueText = "${fontSize.roundToInt()}sp",
                value = fontSize,
                valueRange = 12f..28f,
                onValueChange = { fontSize = it },
            )
            Text(strings.textColor, style = CxType.CaptionMedium, color = colors.textMuted)
            ColorSwatchRow(
                colorsList = SwatchColors,
                selectedColor = textColor,
                onSelect = { textColor = it },
            )
            CxPrimaryButton(
                text = if (initialStyle == null) strings.addToCollage else strings.updateText,
                enabled = text.isNotBlank(),
                onClick = {
                    onApply(
                        TextStyleSpec(
                            text = text.trim(),
                            fontId = fontId,
                            fontSizeSp = fontSize,
                            bold = bold,
                            color = textColor,
                            chipColor = if (useChip) 0xE6FFFFFF.toInt() else null,
                        ),
                    )
                    onDismiss()
                },
                testTag = "text_layer_apply_button",
            )
            if (initialStyle != null && (onToggleLock != null || onRequestDelete != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    if (onToggleLock != null) {
                        CxSecondaryButton(
                            text = if (isLocked) strings.protectedLabel else strings.dontDeleteLabel,
                            leadingIcon = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            onClick = onToggleLock,
                            modifier = Modifier.weight(1f),
                            testTag = "text_layer_protect_button",
                        )
                    }
                    if (onRequestDelete != null) {
                        CxSecondaryButton(
                            text = strings.deleteLabel,
                            leadingIcon = Icons.Rounded.Delete,
                            onClick = {
                                onDismiss()
                                onRequestDelete()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "text_layer_delete_button",
                        )
                    }
                }
            }
            Spacer(Modifier.height(CxSpace.S8))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPickerSheet(
    existingStickers: List<CollageLayer> = emptyList(),
    onDismiss: () -> Unit,
    onAddSticker: (StickerKind, Int) -> Unit,
    onToggleStickerLock: (String) -> Unit = {},
    onRequestDeleteSticker: (CollageLayer) -> Unit = {},
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    var selectedColor by remember { mutableIntStateOf(0xFFEA2858.toInt()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Text(strings.openSourceStickers, style = CxType.Heading, color = colors.text)

            if (existingStickers.isNotEmpty()) {
                Text(
                    text = "${strings.onCanvasKeepOrDelete} (${existingStickers.size})",
                    style = CxType.CaptionMedium,
                    color = colors.textMuted,
                )
                Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                    existingStickers.forEach { sticker ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(CxRadius.Small))
                                .background(colors.surface2)
                                .padding(horizontal = CxSpace.S12, vertical = CxSpace.S4),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                            ) {
                                Text(
                                    text = sticker.stickerKind?.glyph ?: "✦",
                                    fontSize = 20.sp,
                                    color = Color(sticker.stickerColor),
                                )
                                Column {
                                    Text(
                                        text = sticker.label.ifBlank { strings.layerSticker },
                                        style = CxType.CaptionMedium,
                                        color = colors.text,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = if (sticker.locked) "${strings.protectedLabel} (${strings.dontDeleteLabel})" else strings.editableLabel,
                                        style = CxType.Caption,
                                        color = if (sticker.locked) colors.success else colors.textMuted,
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CxIconButton(
                                    icon = if (sticker.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                    contentDescription = if (sticker.locked) strings.protectedLabel else strings.dontDeleteLabel,
                                    tint = if (sticker.locked) colors.success else colors.textMuted,
                                    onClick = { onToggleStickerLock(sticker.id) },
                                )
                                CxIconButton(
                                    icon = Icons.Rounded.Delete,
                                    contentDescription = strings.deleteLabel,
                                    tint = if (sticker.locked) colors.textMuted else colors.danger,
                                    onClick = {
                                        onDismiss()
                                        onRequestDeleteSticker(sticker)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Text(strings.addNewStickerTint, style = CxType.CaptionMedium, color = colors.textMuted)
            ColorSwatchRow(
                colorsList = SwatchColors,
                selectedColor = selectedColor,
                onSelect = { selectedColor = it },
            )
            val rows = StickerKind.entries.chunked(4)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    rowItems.forEach { kind ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(CxRadius.Card))
                                .background(colors.surface2)
                                .clickable {
                                    onAddSticker(kind, selectedColor)
                                    onDismiss()
                                }
                                .padding(vertical = CxSpace.S12),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
                        ) {
                            Text(kind.glyph, fontSize = 24.sp, color = Color(selectedColor))
                            Text(kind.label, style = CxType.Caption, color = colors.text)
                        }
                    }
                }
            }
            Spacer(Modifier.height(CxSpace.S8))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterAdjustmentsSheet(
    layer: CollageLayer,
    onDismiss: () -> Unit,
    onUpdateAdjustments: (PhotoAdjustments) -> Unit,
    onReset: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    val adj = layer.adjustments

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(strings.filterAndAdjustments, style = CxType.Heading, color = colors.text)
                CxIconButton(
                    icon = Icons.Rounded.Refresh,
                    contentDescription = strings.resetLabel,
                    onClick = onReset,
                )
            }
            Text("${strings.filtersLabel} (${PhotoFilter.entries.size})", style = CxType.CaptionMedium, color = colors.textMuted)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                items(PhotoFilter.entries) { filter ->
                    CxChip(
                        label = filter.label,
                        selected = adj.filter == filter,
                        onClick = { onUpdateAdjustments(adj.copy(filter = filter)) },
                    )
                }
            }
            Text(strings.detailAndFocus, style = CxType.CaptionMedium, color = colors.textMuted)
            LabeledSlider(
                label = strings.sharpenLabel,
                valueText = "${(adj.sharpen * 100).roundToInt()}%",
                value = adj.sharpen,
                valueRange = 0f..1f,
                onValueChange = { onUpdateAdjustments(adj.copy(sharpen = it)) },
            )
            LabeledSlider(
                label = strings.vignetteLabel,
                valueText = "${(adj.vignette * 100).roundToInt()}%",
                value = adj.vignette,
                valueRange = 0f..1f,
                onValueChange = { onUpdateAdjustments(adj.copy(vignette = it)) },
            )
            Text(strings.lightAndTone, style = CxType.CaptionMedium, color = colors.textMuted)
            LabeledSlider(
                label = strings.brightnessLabel,
                valueText = "${(adj.brightness * 100).roundToInt()}",
                value = adj.brightness,
                valueRange = -0.4f..0.4f,
                onValueChange = { onUpdateAdjustments(adj.copy(brightness = it)) },
            )
            LabeledSlider(
                label = strings.exposureLabel,
                valueText = "${(adj.exposure * 100).roundToInt()}",
                value = adj.exposure,
                valueRange = -0.4f..0.4f,
                onValueChange = { onUpdateAdjustments(adj.copy(exposure = it)) },
            )
            LabeledSlider(
                label = strings.contrastLabel,
                valueText = "${(adj.contrast * 100).roundToInt()}%",
                value = adj.contrast,
                valueRange = 0.5f..1.6f,
                onValueChange = { onUpdateAdjustments(adj.copy(contrast = it)) },
            )
            LabeledSlider(
                label = strings.highlightsLabel,
                valueText = "${(adj.highlights * 100).roundToInt()}",
                value = adj.highlights,
                valueRange = -0.5f..0.5f,
                onValueChange = { onUpdateAdjustments(adj.copy(highlights = it)) },
            )
            LabeledSlider(
                label = strings.shadowsLabel,
                valueText = "${(adj.shadows * 100).roundToInt()}",
                value = adj.shadows,
                valueRange = -0.5f..0.5f,
                onValueChange = { onUpdateAdjustments(adj.copy(shadows = it)) },
            )
            Text(strings.colorAndFinish, style = CxType.CaptionMedium, color = colors.textMuted)
            LabeledSlider(
                label = strings.saturationLabel,
                valueText = "${(adj.saturation * 100).roundToInt()}%",
                value = adj.saturation,
                valueRange = 0f..1.8f,
                onValueChange = { onUpdateAdjustments(adj.copy(saturation = it)) },
            )
            LabeledSlider(
                label = strings.vibranceLabel,
                valueText = "${(adj.vibrance * 100).roundToInt()}",
                value = adj.vibrance,
                valueRange = -0.5f..0.8f,
                onValueChange = { onUpdateAdjustments(adj.copy(vibrance = it)) },
            )
            LabeledSlider(
                label = strings.warmthLabel,
                valueText = "${(adj.warmth * 100).roundToInt()}",
                value = adj.warmth,
                valueRange = -0.4f..0.4f,
                onValueChange = { onUpdateAdjustments(adj.copy(warmth = it)) },
            )
            LabeledSlider(
                label = strings.tintLabel,
                valueText = "${(adj.tint * 100).roundToInt()}",
                value = adj.tint,
                valueRange = -0.4f..0.4f,
                onValueChange = { onUpdateAdjustments(adj.copy(tint = it)) },
            )
            LabeledSlider(
                label = strings.fadeMatteLabel,
                valueText = "${(adj.fade * 100).roundToInt()}%",
                value = adj.fade,
                valueRange = 0f..0.6f,
                onValueChange = { onUpdateAdjustments(adj.copy(fade = it)) },
            )
            Spacer(Modifier.height(CxSpace.S8))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedPhotoActionsSheet(
    layer: CollageLayer,
    onDismiss: () -> Unit,
    onReplacePhoto: () -> Unit,
    onRotate90: () -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onToggleFitMode: () -> Unit,
    onScaleChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onCornerOverride: (Float) -> Unit,
    onSwapPhoto: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleLock: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Text(
                text = layer.label.ifBlank { strings.selectedLayerTitle },
                style = CxType.Heading,
                color = colors.text,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                if (layer.type == LayerType.PHOTO) {
                    item {
                        CxChip(strings.replaceLabel, false, { onDismiss(); onReplacePhoto() }, leadingIcon = Icons.Rounded.AddPhotoAlternate)
                    }
                    item {
                        CxChip(strings.swapLabel, false, { onDismiss(); onSwapPhoto() }, leadingIcon = Icons.Rounded.SwapHoriz)
                    }
                    item {
                        CxChip(
                            label = if (layer.fitMode == PhotoFitMode.COVER) strings.fitWholeLabel else strings.fillCellLabel,
                            selected = false,
                            onClick = onToggleFitMode,
                        )
                    }
                }
                item {
                    CxChip(strings.rotate90Label, false, onRotate90, leadingIcon = Icons.Rounded.RotateRight)
                }
                item {
                    CxChip(strings.flipHLabel, layer.flipHorizontal, onFlipH, leadingIcon = Icons.Rounded.Flip)
                }
                item {
                    CxChip(strings.flipVLabel, layer.flipVertical, onFlipV)
                }
                item {
                    CxChip(strings.menuDuplicate, false, { onDuplicate(); onDismiss() }, leadingIcon = Icons.Rounded.ContentCopy)
                }
                item {
                    CxChip(
                        label = if (layer.locked) "${strings.protectedLabel} (${strings.keepLabel})" else strings.dontDeleteLabel,
                        selected = layer.locked,
                        onClick = onToggleLock,
                        leadingIcon = if (layer.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    )
                }
            }
            LabeledSlider(
                label = strings.zoomScaleLabel,
                valueText = "${(layer.scale * 100).roundToInt()}%",
                value = layer.scale,
                valueRange = 0.5f..2.5f,
                onValueChange = onScaleChange,
            )
            LabeledSlider(
                label = strings.opacityLabel,
                valueText = "${(layer.opacity * 100).roundToInt()}%",
                value = layer.opacity,
                valueRange = 0.1f..1f,
                onValueChange = onOpacityChange,
            )
            if (layer.type == LayerType.PHOTO) {
                LabeledSlider(
                    label = strings.perPhotoRadius,
                    valueText = "${(layer.cornerRadiusOverride ?: 16f).roundToInt()}px",
                    value = layer.cornerRadiusOverride ?: 16f,
                    valueRange = 0f..48f,
                    onValueChange = onCornerOverride,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                CxSecondaryButton(
                    text = strings.resetLabel,
                    leadingIcon = Icons.Rounded.Refresh,
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                )
                CxSecondaryButton(
                    text = if (layer.locked) strings.protectedLabel else strings.deleteLabel,
                    leadingIcon = if (layer.locked) Icons.Rounded.Lock else Icons.Rounded.Delete,
                    onClick = { onDismiss(); onDelete() },
                    modifier = Modifier.weight(1f),
                    testTag = "selected_layer_delete_button",
                )
            }
            Spacer(Modifier.height(CxSpace.S8))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreToolsAndLayersSheet(
    project: CollageProject,
    selectedLayerId: String?,
    showGuides: Boolean,
    stylePresets: List<StylePreset>,
    onDismiss: () -> Unit,
    onOpenCustomSize: () -> Unit,
    onAddMorePhotos: () -> Unit,
    onAddSampleBatch: (Int) -> Unit,
    onToggleGuides: () -> Unit,
    onSaveStylePreset: (String) -> Unit,
    onApplyStylePreset: (StylePreset) -> Unit,
    onSelectLayer: (String) -> Unit,
    onMoveLayerOrder: (String, Int) -> Unit,
    onToggleLayerLock: (String) -> Unit,
    onToggleLayerVisibility: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    var presetName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Text(strings.moreToolsAndLayers, style = CxType.Heading, color = colors.text)

            CxListItem(
                title = strings.canvasSizeAndRatio,
                subtitle = strings.canvasSizeAndRatioSub,
                leadingIcon = Icons.Rounded.AspectRatio,
                onClick = { onDismiss(); onOpenCustomSize() },
                testTag = "more_canvas_size_item",
            )
            CxListItem(
                title = strings.addPhotosTitle,
                subtitle = strings.addPhotosSub,
                leadingIcon = Icons.Rounded.AddPhotoAlternate,
                onClick = { onDismiss(); onAddMorePhotos() },
            )
            CxListItem(
                title = strings.addSamplePhotosTitle,
                subtitle = strings.addSamplePhotosSub,
                leadingIcon = Icons.Rounded.AddPhotoAlternate,
                onClick = { onAddSampleBatch(10); onDismiss() },
            )
            CxListItem(
                title = if (showGuides) strings.hideGuidesTitle else strings.showGuidesTitle,
                subtitle = strings.guidesSub,
                leadingIcon = Icons.Rounded.GridOn,
                onClick = { onToggleGuides(); onDismiss() },
            )

            // Style presets
            Text(strings.stylePresetsLabel, style = CxType.BodyMedium, color = colors.text)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                verticalAlignment = Alignment.Bottom,
            ) {
                CxTextField(
                    label = "",
                    value = presetName,
                    onValueChange = { presetName = it.take(36) },
                    placeholder = strings.saveStylePlaceholder,
                    modifier = Modifier.weight(1f),
                )
                CxIconButton(
                    icon = Icons.Rounded.BookmarkAdd,
                    contentDescription = strings.saveLabel,
                    tint = colors.primary,
                    onClick = {
                        onSaveStylePreset(presetName.ifBlank { "${strings.stylePresetsLabel} ${stylePresets.size + 1}" })
                        presetName = ""
                    },
                )
            }
            if (stylePresets.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                    items(stylePresets, key = { it.id }) { preset ->
                        CxChip(
                            label = preset.name,
                            selected = false,
                            onClick = { onApplyStylePreset(preset); onDismiss() },
                        )
                    }
                }
            }

            // Layer list (top to bottom)
            Text("${strings.layersLabel} (${project.layers.size})", style = CxType.BodyMedium, color = colors.text)
            val ordered = project.layers.sortedByDescending { it.zIndex }.take(20)
            ordered.forEach { layer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CxRadius.Small))
                        .background(if (layer.id == selectedLayerId) colors.primarySoft else colors.surface2)
                        .clickable { onSelectLayer(layer.id) }
                        .padding(horizontal = CxSpace.S12, vertical = CxSpace.S8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = layer.label.ifBlank { strings.layerKindLabel(layer.type) },
                            style = CxType.CaptionMedium,
                            color = colors.text,
                            maxLines = 1,
                        )
                        Text(
                            text = "${strings.layerKindLabel(layer.type)} · z=${layer.zIndex}",
                            style = CxType.Caption,
                            color = colors.textMuted,
                        )
                    }
                    Row {
                        CxIconButton(
                            icon = Icons.Rounded.ArrowUpward,
                            contentDescription = "Move layer forward",
                            onClick = { onMoveLayerOrder(layer.id, +1) },
                        )
                        CxIconButton(
                            icon = Icons.Rounded.ArrowDownward,
                            contentDescription = "Move layer backward",
                            onClick = { onMoveLayerOrder(layer.id, -1) },
                        )
                        CxIconButton(
                            icon = if (layer.visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = "Toggle layer visibility",
                            onClick = { onToggleLayerVisibility(layer.id) },
                        )
                        CxIconButton(
                            icon = if (layer.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = "Toggle layer lock",
                            onClick = { onToggleLayerLock(layer.id) },
                        )
                        CxIconButton(
                            icon = Icons.Rounded.Delete,
                            contentDescription = "Delete layer",
                            tint = colors.danger,
                            onClick = { onDeleteLayer(layer.id) },
                        )
                    }
                }
            }
            if (project.layers.size > 20) {
                Text(
                    text = "+ ${project.layers.size - 20} more layers in project",
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
            }
            Spacer(Modifier.height(CxSpace.S12))
        }
    }
}

@Composable
fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S4)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = CxType.CaptionMedium, color = colors.text)
            Text(text = valueText, style = CxType.CaptionMedium, color = colors.textMuted)
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.border,
            ),
        )
    }
}

@Composable
fun ColorSwatchRow(
    colorsList: List<Int>,
    selectedColor: Int,
    onSelect: (Int) -> Unit,
) {
    val tokens = CollageXThemeTokens.colors
    LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
        items(colorsList) { argb ->
            val isSelected = argb == selectedColor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) tokens.primary else tokens.border,
                        shape = CircleShape,
                    )
                    .clickable { onSelect(argb) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoFitStudioSheet(
    project: CollageProject,
    currentPreset: AutoFitPreset,
    autoFitEnabled: Boolean,
    onDismiss: () -> Unit,
    onToggleAutoFitEnabled: (Boolean) -> Unit,
    onApplyAutoFit: (AutoFitPreset, CanvasSpec?) -> Unit,
    onOpenCustomSize: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    var selectedPreset by remember { mutableStateOf(currentPreset) }
    var selectedRatioId by remember { mutableStateOf<String?>(null) }

    val targetCanvas = remember(selectedRatioId, project.canvas) {
        val ratio = CanvasMath.StandardPresets.firstOrNull { it.id == selectedRatioId }
        if (ratio == null) {
            project.canvas
        } else {
            val wInUnit = CanvasMath.fromPixels(ratio.suggestedPxW.toDouble(), project.canvas.unit, project.canvas.dpi)
            val hInUnit = CanvasMath.fromPixels(ratio.suggestedPxH.toDouble(), project.canvas.unit, project.canvas.dpi)
            project.canvas.copy(width = wInUnit, height = hInUnit)
        }
    }

    val photoCount = remember(project.layers) {
        project.layers.count { it.type == LayerType.PHOTO && it.visible }.coerceAtLeast(1)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.autoFitStudioTitle, style = CxType.Heading, color = colors.text)
                    Text(
                        text = "${strings.autoFitStudioSubtitle} ($photoCount ${strings.photosWord})",
                        style = CxType.Caption,
                        color = colors.textMuted,
                    )
                }
                CxIconButton(
                    icon = Icons.Rounded.AspectRatio,
                    contentDescription = strings.customSizeTitle,
                    tint = colors.primary,
                    onClick = {
                        onDismiss()
                        onOpenCustomSize()
                    },
                    testTag = "autofit_open_custom_size",
                )
            }

            // 1. Target Ratio & Size Quick Picker
            Text(strings.targetRatioAndSize, style = CxType.CaptionMedium, color = colors.textMuted)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                item {
                    val curDims = CanvasMath.toPixelDimensions(project.canvas)
                    CxChip(
                        label = "${strings.currentLabel} (${curDims.widthPx}×${curDims.heightPx})",
                        selected = selectedRatioId == null,
                        onClick = { selectedRatioId = null },
                        testTag = "autofit_ratio_current",
                    )
                }
                items(CanvasMath.StandardPresets, key = { it.id }) { ratio ->
                    CxChip(
                        label = "${ratio.ratioLabel} ${ratio.name}",
                        selected = selectedRatioId == ratio.id,
                        onClick = { selectedRatioId = ratio.id },
                        testTag = "autofit_ratio_${ratio.id}",
                    )
                }
            }

            // 2. Auto-Fit Style Presets with Live Computed Details
            Text(strings.autoFitStyleLabel, style = CxType.CaptionMedium, color = colors.textMuted)
            Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                AutoFitPreset.entries.forEach { preset ->
                    val preview = remember(photoCount, targetCanvas, preset) {
                        AutoFitEngine.compute(
                            photoCount = photoCount,
                            canvas = targetCanvas,
                            preset = preset,
                            currentBorderColor = project.border.color,
                        )
                    }
                    val isSelected = selectedPreset == preset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CxRadius.Card))
                            .background(if (isSelected) colors.primarySoft else colors.surface2)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.border,
                                shape = RoundedCornerShape(CxRadius.Card),
                            )
                            .clickable { selectedPreset = preset }
                            .padding(CxSpace.S12),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
                        ) {
                            Text(
                                text = strings.autoFitPresetLabel(preset),
                                style = CxType.BodyMedium,
                                color = if (isSelected) colors.primary else colors.text,
                            )
                            Text(
                                text = preset.subtitle,
                                style = CxType.Caption,
                                color = colors.textMuted,
                            )
                            Text(
                                text = preview.summary,
                                style = CxType.CaptionMedium,
                                color = colors.text,
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = strings.selectedLabel,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // 3. Auto-Fit toggle (still editable manually anytime)
            CxSwitchRow(
                title = strings.autoFitOnNewPhotos,
                subtitle = strings.autoFitOnNewPhotosSub,
                checked = autoFitEnabled,
                onCheckedChange = onToggleAutoFitEnabled,
                testTag = "autofit_auto_toggle",
            )

            Text(
                text = strings.autoFitManualNote,
                style = CxType.Caption,
                color = colors.textMuted,
            )

            CxPrimaryButton(
                text = strings.applyAutoFitLabel,
                leadingIcon = Icons.Rounded.AutoAwesomeMosaic,
                onClick = {
                    onApplyAutoFit(
                        selectedPreset,
                        if (selectedRatioId != null) targetCanvas else null,
                    )
                    onDismiss()
                },
                testTag = "autofit_apply_button",
            )
            Spacer(Modifier.height(CxSpace.S8))
        }
    }
}

