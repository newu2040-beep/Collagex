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
import androidx.compose.material.icons.rounded.BookmarkAdd
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
import com.example.core.designsystem.CxChip
import com.example.core.designsystem.CxIconButton
import com.example.core.designsystem.CxListItem
import com.example.core.designsystem.CxPrimaryButton
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSecondaryButton
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxTextField
import com.example.core.designsystem.CxType
import com.example.core.model.CollageLayer
import com.example.core.model.CollageProject
import com.example.core.model.LayerType
import com.example.core.model.PhotoAdjustments
import com.example.core.model.PhotoFilter
import com.example.core.model.PhotoFitMode
import com.example.core.model.StickerKind
import com.example.core.model.StylePreset
import com.example.core.model.TextStyleSpec
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
    onDismiss: () -> Unit,
    onApply: (TextStyleSpec) -> Unit,
) {
    val colors = CollageXThemeTokens.colors
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
                text = if (initialStyle == null) "Add Text Layer" else "Edit Text Layer",
                style = CxType.Heading,
                color = colors.text,
            )
            CxTextField(
                label = "Text",
                value = text,
                onValueChange = { text = it.take(80) },
                placeholder = "Write a caption…",
                testTag = "text_layer_input",
            )
            Text("Font Family", style = CxType.CaptionMedium, color = colors.textMuted)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                val fonts = listOf(
                    "SANS" to "Jakarta",
                    "SERIF" to "Playfair",
                    "SCRIPT" to "Pacifico",
                    "GEOMETRIC" to "Grotesk",
                )
                items(fonts) { (id, label) ->
                    CxChip(label = label, selected = fontId == id, onClick = { fontId = id })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                CxChip("Bold", bold, { bold = !bold })
                CxChip("Pill Background", useChip, { useChip = !useChip })
            }
            LabeledSlider(
                label = "Size",
                valueText = "${fontSize.roundToInt()}sp",
                value = fontSize,
                valueRange = 12f..28f,
                onValueChange = { fontSize = it },
            )
            Text("Text Color", style = CxType.CaptionMedium, color = colors.textMuted)
            ColorSwatchRow(
                colorsList = SwatchColors,
                selectedColor = textColor,
                onSelect = { textColor = it },
            )
            CxPrimaryButton(
                text = if (initialStyle == null) "Add to Collage" else "Update Text",
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
            Spacer(Modifier.height(CxSpace.S8))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPickerSheet(
    onDismiss: () -> Unit,
    onAddSticker: (StickerKind, Int) -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    var selectedColor by remember { mutableIntStateOf(0xFFEA2858.toInt()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Text("Open-Source Stickers", style = CxType.Heading, color = colors.text)
            Text("Sticker Tint", style = CxType.CaptionMedium, color = colors.textMuted)
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
                Text("Filter & Adjustments", style = CxType.Heading, color = colors.text)
                CxIconButton(
                    icon = Icons.Rounded.Refresh,
                    contentDescription = "Reset adjustments",
                    onClick = onReset,
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                items(PhotoFilter.entries) { filter ->
                    CxChip(
                        label = filter.label,
                        selected = adj.filter == filter,
                        onClick = { onUpdateAdjustments(adj.copy(filter = filter)) },
                    )
                }
            }
            LabeledSlider(
                label = "Brightness",
                valueText = "${(adj.brightness * 100).roundToInt()}",
                value = adj.brightness,
                valueRange = -0.4f..0.4f,
                onValueChange = { onUpdateAdjustments(adj.copy(brightness = it)) },
            )
            LabeledSlider(
                label = "Contrast",
                valueText = "${(adj.contrast * 100).roundToInt()}%",
                value = adj.contrast,
                valueRange = 0.5f..1.6f,
                onValueChange = { onUpdateAdjustments(adj.copy(contrast = it)) },
            )
            LabeledSlider(
                label = "Saturation",
                valueText = "${(adj.saturation * 100).roundToInt()}%",
                value = adj.saturation,
                valueRange = 0f..1.8f,
                onValueChange = { onUpdateAdjustments(adj.copy(saturation = it)) },
            )
            LabeledSlider(
                label = "Warmth",
                valueText = "${(adj.warmth * 100).roundToInt()}",
                value = adj.warmth,
                valueRange = -0.4f..0.4f,
                onValueChange = { onUpdateAdjustments(adj.copy(warmth = it)) },
            )
            LabeledSlider(
                label = "Exposure",
                valueText = "${(adj.exposure * 100).roundToInt()}",
                value = adj.exposure,
                valueRange = -0.4f..0.4f,
                onValueChange = { onUpdateAdjustments(adj.copy(exposure = it)) },
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
    onReset: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
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
                text = layer.label.ifBlank { "Selected Layer" },
                style = CxType.Heading,
                color = colors.text,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                if (layer.type == LayerType.PHOTO) {
                    item {
                        CxChip("Replace", false, { onDismiss(); onReplacePhoto() }, leadingIcon = Icons.Rounded.AddPhotoAlternate)
                    }
                    item {
                        CxChip("Swap", false, { onDismiss(); onSwapPhoto() }, leadingIcon = Icons.Rounded.SwapHoriz)
                    }
                    item {
                        CxChip(
                            label = if (layer.fitMode == PhotoFitMode.COVER) "Fit Whole" else "Fill Cell",
                            selected = false,
                            onClick = onToggleFitMode,
                        )
                    }
                }
                item {
                    CxChip("Rotate 90°", false, onRotate90, leadingIcon = Icons.Rounded.RotateRight)
                }
                item {
                    CxChip("Flip H", layer.flipHorizontal, onFlipH, leadingIcon = Icons.Rounded.Flip)
                }
                item {
                    CxChip("Flip V", layer.flipVertical, onFlipV)
                }
                item {
                    CxChip("Duplicate", false, { onDuplicate(); onDismiss() }, leadingIcon = Icons.Rounded.ContentCopy)
                }
            }
            LabeledSlider(
                label = "Zoom / Scale",
                valueText = "${(layer.scale * 100).roundToInt()}%",
                value = layer.scale,
                valueRange = 0.5f..2.5f,
                onValueChange = onScaleChange,
            )
            LabeledSlider(
                label = "Opacity",
                valueText = "${(layer.opacity * 100).roundToInt()}%",
                value = layer.opacity,
                valueRange = 0.1f..1f,
                onValueChange = onOpacityChange,
            )
            if (layer.type == LayerType.PHOTO) {
                LabeledSlider(
                    label = "Per-Photo Radius",
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
                    text = "Reset",
                    leadingIcon = Icons.Rounded.Refresh,
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                )
                CxSecondaryButton(
                    text = "Delete",
                    leadingIcon = Icons.Rounded.Delete,
                    onClick = { onDelete(); onDismiss() },
                    modifier = Modifier.weight(1f),
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
            Text("More Tools & Layers", style = CxType.Heading, color = colors.text)

            CxListItem(
                title = "Canvas Size & Aspect Ratio",
                subtitle = "Resize this existing collage in px, in, cm, mm or pt",
                leadingIcon = Icons.Rounded.AspectRatio,
                onClick = { onDismiss(); onOpenCustomSize() },
                testTag = "more_canvas_size_item",
            )
            CxListItem(
                title = "Add Photos (2–50+)",
                subtitle = "Import additional photos from your gallery",
                leadingIcon = Icons.Rounded.AddPhotoAlternate,
                onClick = { onDismiss(); onAddMorePhotos() },
            )
            CxListItem(
                title = "Add 10 Sample Photos (Stress Test)",
                subtitle = "Quickly test 25–50+ multi-photo layouts",
                leadingIcon = Icons.Rounded.AddPhotoAlternate,
                onClick = { onAddSampleBatch(10); onDismiss() },
            )
            CxListItem(
                title = if (showGuides) "Hide Snap & Center Guides" else "Show Snap & Center Guides",
                subtitle = "Alignment crosshair and safe margin frame",
                leadingIcon = Icons.Rounded.GridOn,
                onClick = { onToggleGuides(); onDismiss() },
            )

            // Style presets
            Text("Style Presets", style = CxType.BodyMedium, color = colors.text)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                verticalAlignment = Alignment.Bottom,
            ) {
                CxTextField(
                    label = "",
                    value = presetName,
                    onValueChange = { presetName = it.take(36) },
                    placeholder = "Save current border & bg as…",
                    modifier = Modifier.weight(1f),
                )
                CxIconButton(
                    icon = Icons.Rounded.BookmarkAdd,
                    contentDescription = "Save style preset",
                    tint = colors.primary,
                    onClick = {
                        onSaveStylePreset(presetName.ifBlank { "Style ${stylePresets.size + 1}" })
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
            Text("Layers (${project.layers.size})", style = CxType.BodyMedium, color = colors.text)
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
                            text = layer.label.ifBlank { layer.type.name },
                            style = CxType.CaptionMedium,
                            color = colors.text,
                            maxLines = 1,
                        )
                        Text(
                            text = "${layer.type.name} · z=${layer.zIndex}",
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
