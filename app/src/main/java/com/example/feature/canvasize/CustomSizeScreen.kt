package com.example.feature.canvasize

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.designsystem.CxCard
import com.example.core.designsystem.CxChip
import com.example.core.designsystem.CxIconButton
import com.example.core.designsystem.CxPrimaryButton
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxSwitchRow
import com.example.core.designsystem.CxTextField
import com.example.core.designsystem.CxTopBar
import com.example.core.designsystem.CxType
import com.example.core.model.AspectRatioPreset
import com.example.core.model.CanvasMath
import com.example.core.model.CanvasOrientation
import com.example.core.model.CanvasSpec
import com.example.core.model.CollageProject
import com.example.core.model.DimensionUnit
import com.example.core.model.ResizeMode
import com.example.feature.editor.CollageCanvasPreview
import com.example.ui.theme.CollageXThemeTokens
import java.util.Locale
import kotlin.math.abs

private val CommonDpiValues = listOf(72, 96, 150, 300)

/**
 * Screen 4 — Post-Creation Custom Size & Aspect Ratio Editor (Core Differentiator).
 * Allows resizing an existing collage non-destructively in px, in, cm, mm, or pt with DPI conversion,
 * aspect-ratio lock, orientation flip, Fit/Fill/Crop/Extend preview, and safe memory validation.
 */
@Composable
fun CustomSizeScreen(
    project: CollageProject,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onApplyChanges: (CanvasSpec) -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    val initial = project.canvas

    var unit by remember(project.id) { mutableStateOf(initial.unit) }
    var dpi by remember(project.id) { mutableIntStateOf(initial.dpi) }
    var customDpiMode by remember { mutableStateOf(initial.dpi !in CommonDpiValues) }
    var customDpiText by remember(project.id) { mutableStateOf(initial.dpi.toString()) }
    var dpiMenuExpanded by remember { mutableStateOf(false) }

    var widthVal by remember(project.id) { mutableDoubleStateOf(initial.width) }
    var heightVal by remember(project.id) { mutableDoubleStateOf(initial.height) }
    var widthText by remember(project.id) {
        mutableStateOf(CanvasMath.formatDimension(initial.width, initial.unit))
    }
    var heightText by remember(project.id) {
        mutableStateOf(CanvasMath.formatDimension(initial.height, initial.unit))
    }

    var aspectLocked by remember(project.id) { mutableStateOf(initial.aspectLocked) }
    var lockedRatio by remember(project.id) {
        mutableDoubleStateOf(if (initial.height > 0.0) initial.width / initial.height else 1.5)
    }
    var resizeMode by remember(project.id) { mutableStateOf(initial.resizeMode) }
    var selectedPresetId by remember { mutableStateOf<String?>(null) }

    val orientation = if (heightVal >= widthVal) CanvasOrientation.PORTRAIT else CanvasOrientation.LANDSCAPE

    val draftSpec = CanvasSpec(
        width = widthVal,
        height = heightVal,
        unit = unit,
        dpi = dpi,
        aspectLocked = aspectLocked,
        resizeMode = resizeMode,
        orientation = orientation,
    )
    val validation = remember(draftSpec) { CanvasMath.validate(draftSpec) }

    val previewProject = remember(project, draftSpec) {
        if (validation.valid) {
            project.copy(
                canvas = draftSpec,
                layers = CanvasMath.adaptLayersForResize(project.layers, project.canvas, draftSpec, resizeMode),
            )
        } else {
            project
        }
    }

    fun applyDimensions(newW: Double, newH: Double, newUnit: DimensionUnit = unit, newDpi: Int = dpi) {
        widthVal = newW
        heightVal = newH
        unit = newUnit
        dpi = newDpi
        widthText = CanvasMath.formatDimension(newW, newUnit)
        heightText = CanvasMath.formatDimension(newH, newUnit)
        if (newH > 0.0) lockedRatio = newW / newH
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .imePadding(),
    ) {
        CxTopBar(
            title = stringResource(R.string.custom_size_title),
            onBack = onBack,
            actions = {
                CxIconButton(
                    icon = Icons.Rounded.Home,
                    contentDescription = "Return to Home",
                    onClick = onGoHome,
                    testTag = "custom_size_home",
                )
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S8),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S24),
        ) {
            // Live Resize Preview Card
            CxCard(contentPadding = PaddingValues(CxSpace.S12)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Live Resize Preview", style = CxType.BodyMedium, color = colors.text)
                    Text(
                        text = if (validation.valid) {
                            "${validation.pixels.widthPx} × ${validation.pixels.heightPx} px"
                        } else {
                            "Invalid size"
                        },
                        style = CxType.CaptionMedium,
                        color = if (validation.valid) colors.primary else colors.danger,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clip(RoundedCornerShape(CxRadius.Small))
                        .background(colors.surface2)
                        .padding(CxSpace.S8),
                    contentAlignment = Alignment.Center,
                ) {
                    CollageCanvasPreview(
                        project = previewProject,
                        interactive = false,
                        proxyMaxEdgePx = 256,
                        modifier = Modifier.height(132.dp),
                    )
                }
                Text(
                    text = "${resizeMode.label}: ${resizeMode.description}",
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
            }

            // 1. Aspect Ratio Presets (3x2 grid matching mockup + Print presets row)
            Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S12)) {
                Text(
                    text = stringResource(R.string.custom_size_presets),
                    style = CxType.Heading,
                    color = colors.text,
                )
                CanvasMath.StandardPresets.chunked(3).forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                    ) {
                        rowPresets.forEach { preset ->
                            AspectPresetCard(
                                preset = preset,
                                selected = selectedPresetId == preset.id ||
                                    (selectedPresetId == null && abs((widthVal / heightVal.coerceAtLeast(0.01)) - (preset.ratioW / preset.ratioH)) < 0.02),
                                onClick = {
                                    selectedPresetId = preset.id
                                    val wInUnit = CanvasMath.fromPixels(preset.suggestedPxW.toDouble(), unit, dpi)
                                    val hInUnit = CanvasMath.fromPixels(preset.suggestedPxH.toDouble(), unit, dpi)
                                    applyDimensions(wInUnit, hInUnit)
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                // Additional print presets
                LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                    items(CanvasMath.PrintPresets) { preset ->
                        CxChip(
                            label = "${preset.ratioLabel} (${preset.name})",
                            selected = selectedPresetId == preset.id,
                            onClick = {
                                selectedPresetId = preset.id
                                customDpiMode = false
                                customDpiText = preset.printDpi.toString()
                                applyDimensions(preset.printW, preset.printH, preset.printUnit, preset.printDpi)
                            },
                        )
                    }
                }
            }

            // 2. Custom Dimensions Section matching mockup
            Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S12)) {
                Text(
                    text = stringResource(R.string.custom_size_dimensions),
                    style = CxType.Heading,
                    color = colors.text,
                )

                // Unit Selector Pills: Pixels (px), Inches (in), Cm (cm), Mm (mm), Pt (pt)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                    items(DimensionUnit.entries) { u ->
                        CxChip(
                            label = u.chipLabel,
                            selected = unit == u,
                            onClick = {
                                val convertedW = CanvasMath.convertUnit(widthVal, unit, u, dpi)
                                val convertedH = CanvasMath.convertUnit(heightVal, unit, u, dpi)
                                applyDimensions(convertedW, convertedH, u, dpi)
                            },
                            testTag = "unit_chip_${u.name}",
                        )
                    }
                }

                // Width + Aspect Lock + Height inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    CxTextField(
                        label = stringResource(R.string.custom_size_width),
                        value = widthText,
                        onValueChange = { raw ->
                            val clean = raw.filter { it.isDigit() || it == '.' }
                            widthText = clean
                            selectedPresetId = null
                            clean.toDoubleOrNull()?.let { parsed ->
                                widthVal = parsed
                                if (aspectLocked && lockedRatio > 0.0 && parsed > 0.0) {
                                    val recomputedH = parsed / lockedRatio
                                    heightVal = recomputedH
                                    heightText = CanvasMath.formatDimension(recomputedH, unit)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (unit == DimensionUnit.PX) KeyboardType.Number else KeyboardType.Decimal,
                        ),
                        trailingText = unit.shortLabel,
                        modifier = Modifier.weight(1f),
                        testTag = "custom_width_input",
                    )

                    CxIconButton(
                        icon = if (aspectLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = stringResource(R.string.cd_lock_aspect),
                        tint = if (aspectLocked) colors.primary else colors.textMuted,
                        onClick = {
                            aspectLocked = !aspectLocked
                            if (aspectLocked && heightVal > 0.0) {
                                lockedRatio = widthVal / heightVal
                            }
                        },
                        testTag = "aspect_lock_toggle",
                    )

                    CxTextField(
                        label = stringResource(R.string.custom_size_height),
                        value = heightText,
                        onValueChange = { raw ->
                            val clean = raw.filter { it.isDigit() || it == '.' }
                            heightText = clean
                            selectedPresetId = null
                            clean.toDoubleOrNull()?.let { parsed ->
                                heightVal = parsed
                                if (aspectLocked && lockedRatio > 0.0 && parsed > 0.0) {
                                    val recomputedW = parsed * lockedRatio
                                    widthVal = recomputedW
                                    widthText = CanvasMath.formatDimension(recomputedW, unit)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (unit == DimensionUnit.PX) KeyboardType.Number else KeyboardType.Decimal,
                        ),
                        trailingText = unit.shortLabel,
                        modifier = Modifier.weight(1f),
                        testTag = "custom_height_input",
                    )
                }

                // Resolution (DPI) Row matching mockup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.custom_size_dpi),
                        style = CxType.BodyMedium,
                        color = colors.text,
                    )
                    Box {
                        Surface(
                            shape = RoundedCornerShape(CxRadius.Small),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .clip(RoundedCornerShape(CxRadius.Small))
                                .clickable { dpiMenuExpanded = true }
                                .testTag("dpi_dropdown_trigger"),
                        ) {
                            Text(
                                text = if (customDpiMode) "Custom ($dpi DPI)" else "$dpi DPI ▾",
                                style = CxType.BodyMedium,
                                color = colors.text,
                                modifier = Modifier.padding(horizontal = CxSpace.S12, vertical = CxSpace.S8),
                            )
                        }
                        DropdownMenu(
                            expanded = dpiMenuExpanded,
                            onDismissRequest = { dpiMenuExpanded = false },
                        ) {
                            CommonDpiValues.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text("$d DPI", style = CxType.Body) },
                                    onClick = {
                                        dpi = d
                                        customDpiMode = false
                                        customDpiText = d.toString()
                                        dpiMenuExpanded = false
                                    },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Custom DPI…", style = CxType.Body) },
                                onClick = {
                                    customDpiMode = true
                                    dpiMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                if (customDpiMode) {
                    CxTextField(
                        label = "Custom DPI (36 – 1200)",
                        value = customDpiText,
                        onValueChange = { raw ->
                            val clean = raw.filter { it.isDigit() }.take(4)
                            customDpiText = clean
                            clean.toIntOrNull()?.let { dpi = it }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingText = "DPI",
                        testTag = "custom_dpi_input",
                    )
                }

                // Orientation Segmented Toggle (Portrait | Landscape) matching mockup
                Text(
                    text = stringResource(R.string.custom_size_orientation),
                    style = CxType.BodyMedium,
                    color = colors.text,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    CxChip(
                        label = stringResource(R.string.custom_size_portrait),
                        selected = orientation == CanvasOrientation.PORTRAIT,
                        leadingIcon = Icons.Rounded.StayCurrentPortrait,
                        onClick = {
                            if (widthVal > heightVal) {
                                applyDimensions(heightVal, widthVal)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "orientation_portrait",
                    )
                    CxChip(
                        label = stringResource(R.string.custom_size_landscape),
                        selected = orientation == CanvasOrientation.LANDSCAPE,
                        leadingIcon = Icons.Rounded.StayCurrentLandscape,
                        onClick = {
                            if (heightVal > widthVal) {
                                applyDimensions(heightVal, widthVal)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "orientation_landscape",
                    )
                }

                // Resize Behavior Controls (Fit, Fill, Crop, Extend + Switches matching mockup)
                Text("Content Resize Mode", style = CxType.BodyMedium, color = colors.text)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                    items(ResizeMode.entries) { mode ->
                        CxChip(
                            label = mode.label,
                            selected = resizeMode == mode,
                            onClick = { resizeMode = mode },
                            testTag = "resize_mode_${mode.name}",
                        )
                    }
                }

                CxCard(contentPadding = PaddingValues(horizontal = CxSpace.S12, vertical = CxSpace.S4)) {
                    CxSwitchRow(
                        title = stringResource(R.string.custom_size_maintain_ratio),
                        checked = aspectLocked,
                        onCheckedChange = { locked ->
                            aspectLocked = locked
                            if (locked && heightVal > 0.0) lockedRatio = widthVal / heightVal
                        },
                        testTag = "switch_maintain_aspect",
                    )
                    CxSwitchRow(
                        title = stringResource(R.string.custom_size_extend_canvas),
                        subtitle = "Fill new area with background instead of scaling",
                        checked = resizeMode == ResizeMode.EXTEND,
                        onCheckedChange = { extend ->
                            resizeMode = if (extend) ResizeMode.EXTEND else ResizeMode.FIT
                        },
                        testTag = "switch_extend_canvas",
                    )
                }

                // Dimension & Memory Validation Card
                CxCard(
                    borderColor = when {
                        !validation.valid -> colors.danger
                        validation.warningMessage != null -> colors.primary
                        else -> colors.border
                    },
                    contentPadding = PaddingValues(CxSpace.S12),
                ) {
                    if (!validation.valid) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                        ) {
                            Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = colors.danger)
                            Text(
                                text = validation.errorMessage ?: "Invalid canvas dimensions.",
                                style = CxType.CaptionMedium,
                                color = colors.danger,
                            )
                        }
                    } else {
                        Text(
                            text = "Output: ${validation.pixels.widthPx} × ${validation.pixels.heightPx} px " +
                                "(${String.format(Locale.US, "%.1f", validation.pixels.megapixels)} MP @ $dpi DPI)",
                            style = CxType.CaptionMedium,
                            color = colors.text,
                        )
                        Text(
                            text = "Est. PNG ~${String.format(Locale.US, "%.1f", validation.estimatedPngMb)} MB · " +
                                "JPEG ~${String.format(Locale.US, "%.1f", validation.estimatedJpegMb)} MB",
                            style = CxType.Caption,
                            color = colors.textMuted,
                        )
                        validation.warningMessage?.let { warn ->
                            Text(
                                text = warn,
                                style = CxType.Caption,
                                color = colors.primary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(CxSpace.S8))
        }

        // Fixed Bottom Primary Apply Changes Button (matching mockup)
        Surface(
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(CxSpace.S16),
            ) {
                CxPrimaryButton(
                    text = stringResource(R.string.custom_size_apply),
                    leadingIcon = Icons.Rounded.Check,
                    enabled = validation.valid,
                    onClick = { onApplyChanges(draftSpec) },
                    testTag = "custom_size_apply_button",
                )
            }
        }
    }
}

@Composable
private fun AspectPresetCard(
    preset: AspectRatioPreset,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CollageXThemeTokens.colors
    val shape = RoundedCornerShape(CxRadius.Card)
    val iconW = (24f * (preset.ratioW / maxOf(preset.ratioW, preset.ratioH))).toFloat().coerceAtLeast(12f).dp
    val iconH = (24f * (preset.ratioH / maxOf(preset.ratioW, preset.ratioH))).toFloat().coerceAtLeast(12f).dp

    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.primarySoft else colors.surface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.border, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = CxSpace.S12, horizontal = CxSpace.S8)
            .testTag("aspect_preset_${preset.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = iconW, height = iconH)
                    .border(
                        width = 1.8.dp,
                        color = if (selected) colors.primary else colors.textMuted,
                        shape = RoundedCornerShape(3.dp),
                    ),
            )
        }
        Text(
            text = preset.ratioLabel,
            style = CxType.BodyMedium,
            color = if (selected) colors.primary else colors.text,
            maxLines = 1,
        )
        Text(
            text = preset.name,
            style = CxType.Caption,
            color = if (selected) colors.primary else colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
