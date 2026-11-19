package com.example.feature.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CanvasFonts
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.SampleArtwork
import com.example.core.model.BackgroundType
import com.example.core.model.CanvasMath
import com.example.core.model.CollageLayer
import com.example.core.model.CollageProject
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import com.example.core.model.PhotoFitMode
import com.example.engine.BitmapProxyCache
import com.example.engine.LayoutEngine
import com.example.engine.PhotoColorMatrixBuilder
import com.example.ui.theme.CollageXThemeTokens
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Live interactive Collage Canvas used in the Editor, Home recent thumbnails, and Custom Size preview.
 * Uses memory-safe downsampled proxy bitmaps for 50+ photos so UI stays silky smooth.
 */
@Composable
fun CollageCanvasPreview(
    project: CollageProject,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    selectedLayerId: String? = null,
    swapSourceLayerId: String? = null,
    showGuides: Boolean = false,
    proxyMaxEdgePx: Int = 512,
    onSelectLayer: ((String) -> Unit)? = null,
    onDragLayer: ((layerId: String, deltaNormX: Float, deltaNormY: Float) -> Unit)? = null,
) {
    val colors = CollageXThemeTokens.colors
    val dims = remember(project.canvas) { CanvasMath.toPixelDimensions(project.canvas) }
    val clampedAspect = dims.aspectRatio.coerceIn(0.42f, 2.35f)
    val density = LocalDensity.current

    val cells = remember(
        project.layers,
        project.layoutType,
        clampedAspect,
        project.border.padding,
        project.border.spacing,
    ) {
        val padFrac = (project.border.padding / 400f).coerceIn(0f, 0.14f)
        val gapFrac = (project.border.spacing / 400f).coerceIn(0f, 0.06f)
        LayoutEngine.computePhotoCells(
            layers = project.layers,
            layoutType = project.layoutType,
            canvasAspectRatio = clampedAspect,
            paddingFraction = padFrac,
            spacingFraction = gapFrac,
        )
    }

    val overlays = remember(project.layers) {
        project.layers.filter { it.type != LayerType.PHOTO && it.visible }.sortedBy { it.zIndex }
    }

    val bgPrimary = Color(project.background.primaryColor)
    val bgSecondary = Color(project.background.secondaryColor)
    val bgBrush = when (project.background.type) {
        BackgroundType.GRADIENT -> Brush.linearGradient(listOf(bgPrimary, bgSecondary))
        else -> Brush.linearGradient(listOf(bgPrimary, bgPrimary))
    }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(clampedAspect)
            .clip(RoundedCornerShape(CxRadius.Card))
            .background(bgBrush)
            .border(1.dp, colors.border, RoundedCornerShape(CxRadius.Card)),
    ) {
        val canvasWpx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val canvasHpx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        val minCanvasPx = min(canvasWpx, canvasHpx)

        // Optional imported background image
        if (project.background.type == BackgroundType.IMAGE && !project.background.imageUri.isNullOrBlank()) {
            ProxyPhotoImage(
                uriString = project.background.imageUri,
                sampleArtKey = null,
                proxyMaxEdgePx = proxyMaxEdgePx,
                contentScale = ContentScale.Crop,
                colorFilter = null,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Subtle paper/grain texture overlay if enabled
        if (project.background.textureIntensity > 0.01f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 14f
                val alpha = (project.background.textureIntensity * 0.14f).coerceIn(0f, 0.18f)
                var x = 0f
                var row = 0
                while (x < size.width) {
                    var y = (row % 2) * (step / 2f)
                    while (y < size.height) {
                        drawCircle(
                            color = Color.Black.copy(alpha = alpha),
                            radius = 1.1f,
                            center = Offset(x, y),
                        )
                        y += step
                    }
                    x += step
                    row++
                }
            }
        }

        // Photo cells
        cells.forEach { cell ->
            val layer = cell.layer
            val cellLeftPx = cell.rect.left * canvasWpx
            val cellTopPx = cell.rect.top * canvasHpx
            val cellWidthDp = with(density) { (cell.rect.width * canvasWpx).toDp() }
            val cellHeightDp = with(density) { (cell.rect.height * canvasHpx).toDp() }
            val radiusDp = with(density) {
                (((layer.cornerRadiusOverride ?: project.border.cornerRadius) / 360f) * minCanvasPx).toDp()
            }
            val shape = RoundedCornerShape(radiusDp)
            val isSelected = interactive && selectedLayerId == layer.id
            val isSwapSrc = interactive && swapSourceLayerId == layer.id

            val borderColor = when {
                isSwapSrc -> colors.success
                isSelected -> colors.primary
                project.border.color != project.background.primaryColor -> Color(project.border.color)
                else -> Color.Transparent
            }
            val borderWidth = when {
                isSwapSrc || isSelected -> 2.5.dp
                project.border.color != project.background.primaryColor -> 1.5.dp
                else -> 0.dp
            }

            val colorMatrix = remember(layer.adjustments) {
                PhotoColorMatrixBuilder.buildComposeColorMatrix(layer.adjustments)
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(cellLeftPx.roundToInt(), cellTopPx.roundToInt()) }
                    .size(width = cellWidthDp, height = cellHeightDp)
                    .then(
                        if (project.border.shadowEnabled) {
                            Modifier.shadow(4.dp, shape, clip = false)
                        } else {
                            Modifier
                        },
                    )
                    .clip(shape)
                    .then(
                        if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, shape) else Modifier,
                    )
                    .then(
                        if (interactive && onSelectLayer != null) {
                            Modifier.clickable { onSelectLayer(layer.id) }
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (interactive && !layer.locked && onDragLayer != null &&
                            (project.layoutType == LayoutType.FREEFORM || isSelected)
                        ) {
                            Modifier.pointerInput(layer.id, project.layoutType) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onDragLayer(
                                        layer.id,
                                        dragAmount.x / canvasWpx,
                                        dragAmount.y / canvasHpx,
                                    )
                                }
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                ProxyPhotoImage(
                    uriString = layer.localUri,
                    sampleArtKey = layer.sampleArtKey,
                    proxyMaxEdgePx = if (cells.size > 20) min(proxyMaxEdgePx, 256) else proxyMaxEdgePx,
                    contentScale = if (layer.fitMode == PhotoFitMode.COVER) ContentScale.Crop else ContentScale.Fit,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(layer.opacity.coerceIn(0.1f, 1f))
                        .graphicsLayer {
                            scaleX = layer.scale * (if (layer.flipHorizontal) -1f else 1f)
                            scaleY = layer.scale * (if (layer.flipVertical) -1f else 1f)
                            rotationZ = layer.rotationDeg
                            translationX = layer.panX * size.width
                            translationY = layer.panY * size.height
                        },
                )
            }
        }

        // Text & Sticker overlays
        overlays.forEach { layer ->
            val cx = layer.normX * canvasWpx
            val cy = layer.normY * canvasHpx
            val isSelected = interactive && selectedLayerId == layer.id

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (cx - canvasWpx * 0.18f).roundToInt(),
                            (cy - canvasHpx * 0.08f).roundToInt(),
                        )
                    }
                    .graphicsLayer {
                        rotationZ = layer.rotationDeg
                        scaleX = layer.scale
                        scaleY = layer.scale
                        alpha = layer.opacity.coerceIn(0.1f, 1f)
                    }
                    .then(
                        if (isSelected) {
                            Modifier.border(1.5.dp, colors.primary, RoundedCornerShape(CxRadius.Small))
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (interactive && onSelectLayer != null) {
                            Modifier.clickable { onSelectLayer(layer.id) }
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (interactive && !layer.locked && onDragLayer != null) {
                            Modifier.pointerInput(layer.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onDragLayer(layer.id, dragAmount.x / canvasWpx, dragAmount.y / canvasHpx)
                                }
                            }
                        } else {
                            Modifier
                        },
                    )
                    .padding(CxSpace.S4),
                contentAlignment = Alignment.Center,
            ) {
                when (layer.type) {
                    LayerType.TEXT -> {
                        val ts = layer.textStyle
                        if (ts != null) {
                            val chipMod = if (ts.chipColor != null) {
                                Modifier
                                    .clip(RoundedCornerShape(CxRadius.Pill))
                                    .background(Color(ts.chipColor))
                                    .padding(horizontal = CxSpace.S12, vertical = CxSpace.S4)
                            } else {
                                Modifier.padding(CxSpace.S4)
                            }
                            Text(
                                text = ts.text,
                                fontFamily = CanvasFonts.byId(ts.fontId),
                                fontSize = (ts.fontSizeSp * (if (interactive) 1f else 0.45f)).coerceIn(12f, 28f).sp,
                                fontWeight = if (ts.bold) FontWeight.Bold else FontWeight.Normal,
                                color = Color(ts.color),
                                textAlign = when (ts.alignment) {
                                    -1 -> TextAlign.Start
                                    1 -> TextAlign.End
                                    else -> TextAlign.Center
                                },
                                modifier = chipMod,
                            )
                        }
                    }
                    LayerType.STICKER -> {
                        val glyph = layer.stickerKind?.glyph ?: "✦"
                        Text(
                            text = glyph,
                            fontSize = if (interactive) 28.sp else 14.sp,
                            color = Color(layer.stickerColor),
                        )
                    }
                    LayerType.PHOTO -> Unit
                }
            }
        }

        // Optional alignment / center / safe margin snap guides
        if (showGuides && interactive) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                val guideColor = colors.primary.copy(alpha = 0.55f)
                // Center crosshair
                drawLine(guideColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), strokeWidth = 1.5f, pathEffect = dash)
                drawLine(guideColor, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), strokeWidth = 1.5f, pathEffect = dash)
                // Safe margin frame (5%)
                val mx = size.width * 0.05f
                val my = size.height * 0.05f
                drawRect(
                    color = guideColor.copy(alpha = 0.35f),
                    topLeft = Offset(mx, my),
                    size = androidx.compose.ui.geometry.Size(size.width - mx * 2f, size.height - my * 2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f, pathEffect = dash),
                )
            }
        }
    }
}

@Composable
private fun ProxyPhotoImage(
    uriString: String?,
    sampleArtKey: String?,
    proxyMaxEdgePx: Int,
    contentScale: ContentScale,
    colorFilter: ColorFilter?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var bitmap by remember(uriString, proxyMaxEdgePx) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uriString, proxyMaxEdgePx) {
        if (!uriString.isNullOrBlank()) {
            bitmap = BitmapProxyCache.loadProxy(context, uriString, proxyMaxEdgePx)
        } else {
            bitmap = null
        }
    }

    val loaded = bitmap
    if (loaded != null && !loaded.isRecycled) {
        Image(
            bitmap = loaded.asImageBitmap(),
            contentDescription = null,
            contentScale = contentScale,
            colorFilter = colorFilter,
            modifier = modifier,
        )
    } else {
        SampleArtwork.ScenicCanvas(
            artKey = sampleArtKey ?: SampleArtwork.PAGODA,
            modifier = modifier,
        )
    }
}
