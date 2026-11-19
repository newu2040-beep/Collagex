package com.example.engine

import com.example.core.model.BorderSpec
import com.example.core.model.CanvasMath
import com.example.core.model.CanvasSpec
import com.example.core.model.CollageProject
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import com.example.core.model.PhotoFitMode
import kotlin.math.roundToInt

/**
 * Curated Auto-Fit presets that automatically compute harmonious layout, spacing,
 * padding, border framing, corner radius, and photo framing for any photo count
 * and canvas ratio/size while keeping every parameter manually editable afterwards.
 */
enum class AutoFitPreset(
    val label: String,
    val subtitle: String,
) {
    BALANCED(
        label = "Balanced Fit",
        subtitle = "Smart layout, balanced spacing & soft corners for your ratio",
    ),
    SEAMLESS(
        label = "Seamless Edge",
        subtitle = "Edge-to-edge photos with hairline gaps and crisp corners",
    ),
    GALLERY(
        label = "Gallery Frame",
        subtitle = "Editorial white mat border, clean spacing & subtle shadow",
    ),
    SOFT_CARDS(
        label = "Soft Rounded",
        subtitle = "Generous rounded corners, airy spacing & organic balance",
    ),
    WHOLE_PHOTO(
        label = "Fit Whole Photo",
        subtitle = "Shows every entire photo without cropping inside clean frames",
    ),
}

data class AutoFitResult(
    val layoutType: LayoutType,
    val border: BorderSpec,
    val photoFitMode: PhotoFitMode,
    val summary: String,
)

object AutoFitEngine {

    /**
     * Computes optimal layout, spacing, padding, corner radius, border color, shadow,
     * and photo fit mode for the given [photoCount], [canvas], and [preset].
     */
    fun compute(
        photoCount: Int,
        canvas: CanvasSpec,
        preset: AutoFitPreset,
        currentBorderColor: Int = 0xFFFFFFFF.toInt(),
        preserveLayout: LayoutType? = null,
    ): AutoFitResult {
        val count = photoCount.coerceAtLeast(1)
        val dims = CanvasMath.toPixelDimensions(canvas)
        val aspect = dims.aspectRatio.coerceIn(0.35f, 3.0f)

        val recommendedLayout = preserveLayout ?: pickOptimalLayout(count, aspect, preset)

        // Scale spacing/padding/corners gracefully as photo density rises (1 to 50+ photos)
        val densityScale = when {
            count <= 2 -> 1.15f
            count <= 6 -> 1.0f
            count <= 12 -> 0.78f
            count <= 24 -> 0.56f
            count <= 40 -> 0.38f
            else -> 0.28f
        }

        // Slightly refine margins on very tall or panoramic aspect ratios so photos stay large
        val ratioScale = if (aspect < 0.65f || aspect > 1.75f) 0.86f else 1.0f
        val scale = densityScale * ratioScale

        val (border, fitMode) = when (preset) {
            AutoFitPreset.BALANCED -> {
                val radius = (16f * scale).roundToInt().coerceIn(3, 24).toFloat()
                val spacing = (10f * scale).roundToInt().coerceIn(2, 16).toFloat()
                val padding = (14f * scale).roundToInt().coerceIn(4, 22).toFloat()
                BorderSpec(
                    cornerRadius = radius,
                    spacing = spacing,
                    padding = padding,
                    color = currentBorderColor,
                    shadowEnabled = count <= 12,
                ) to PhotoFitMode.COVER
            }

            AutoFitPreset.SEAMLESS -> {
                val spacing = if (count <= 12) 2f else 1f
                BorderSpec(
                    cornerRadius = 0f,
                    spacing = spacing,
                    padding = 0f,
                    color = 0xFFFFFFFF.toInt(),
                    shadowEnabled = false,
                ) to PhotoFitMode.COVER
            }

            AutoFitPreset.GALLERY -> {
                val radius = (8f * scale).roundToInt().coerceIn(2, 12).toFloat()
                val spacing = (12f * scale).roundToInt().coerceIn(4, 18).toFloat()
                val padding = (24f * scale).roundToInt().coerceIn(10, 32).toFloat()
                BorderSpec(
                    cornerRadius = radius,
                    spacing = spacing,
                    padding = padding,
                    color = 0xFFFFFFFF.toInt(),
                    shadowEnabled = count <= 18,
                ) to PhotoFitMode.COVER
            }

            AutoFitPreset.SOFT_CARDS -> {
                val radius = (24f * scale).roundToInt().coerceIn(6, 32).toFloat()
                val spacing = (12f * scale).roundToInt().coerceIn(4, 18).toFloat()
                val padding = (16f * scale).roundToInt().coerceIn(6, 24).toFloat()
                BorderSpec(
                    cornerRadius = radius,
                    spacing = spacing,
                    padding = padding,
                    color = 0xFFFFFFFF.toInt(),
                    shadowEnabled = count <= 16,
                ) to PhotoFitMode.COVER
            }

            AutoFitPreset.WHOLE_PHOTO -> {
                val radius = (12f * scale).roundToInt().coerceIn(2, 18).toFloat()
                val spacing = (10f * scale).roundToInt().coerceIn(3, 16).toFloat()
                val padding = (16f * scale).roundToInt().coerceIn(6, 24).toFloat()
                BorderSpec(
                    cornerRadius = radius,
                    spacing = spacing,
                    padding = padding,
                    color = 0xFFFFFFFF.toInt(),
                    shadowEnabled = false,
                ) to PhotoFitMode.CONTAIN
            }
        }

        val summary = "${recommendedLayout.label} · Gap ${border.spacing.roundToInt()}px · " +
            "Pad ${border.padding.roundToInt()}px · Radius ${border.cornerRadius.roundToInt()}px"

        return AutoFitResult(
            layoutType = recommendedLayout,
            border = border,
            photoFitMode = fitMode,
            summary = summary,
        )
    }

    /**
     * Applies Auto-Fit cleanly to [project], updating canvas (if [targetCanvas] is supplied),
     * layout, border/spacing/corners, and unlocked photo framing while preserving locked
     * ("Don't Delete") layers, filters, text layers, and stickers.
     */
    fun applyToProject(
        project: CollageProject,
        preset: AutoFitPreset,
        targetCanvas: CanvasSpec? = null,
        preserveLayout: Boolean = false,
    ): CollageProject {
        val nextCanvas = targetCanvas ?: project.canvas
        val photoCount = project.layers.count { it.type == LayerType.PHOTO && it.visible }
        val result = compute(
            photoCount = photoCount,
            canvas = nextCanvas,
            preset = preset,
            currentBorderColor = project.border.color,
            preserveLayout = if (preserveLayout) project.layoutType else null,
        )

        val resizedLayers = if (targetCanvas != null && targetCanvas != project.canvas) {
            CanvasMath.adaptLayersForResize(
                layers = project.layers,
                oldSpec = project.canvas,
                newSpec = targetCanvas,
                mode = targetCanvas.resizeMode,
            )
        } else {
            project.layers
        }

        val organizedLayers = resizedLayers.map { layer ->
            if (layer.type == LayerType.PHOTO && !layer.locked) {
                layer.copy(
                    fitMode = result.photoFitMode,
                    scale = 1f,
                    panX = 0f,
                    panY = 0f,
                    cornerRadiusOverride = null,
                )
            } else {
                layer
            }
        }

        return project.copy(
            canvas = nextCanvas,
            layoutType = result.layoutType,
            border = result.border,
            layers = organizedLayers,
        )
    }

    private fun pickOptimalLayout(
        count: Int,
        aspect: Float,
        preset: AutoFitPreset,
    ): LayoutType {
        if (count <= 1) return LayoutType.GRID
        if (count >= 21) return LayoutType.MOSAIC

        if (preset == AutoFitPreset.WHOLE_PHOTO) {
            return if (count <= 3 && (aspect >= 1.55f || aspect <= 0.65f)) LayoutType.STRIP else LayoutType.GRID
        }
        if (preset == AutoFitPreset.SOFT_CARDS) {
            return when {
                count in 3..8 -> LayoutType.BENTO
                aspect < 0.85f -> LayoutType.MASONRY
                else -> LayoutType.GRID
            }
        }
        if (preset == AutoFitPreset.SEAMLESS) {
            return when {
                count <= 3 && (aspect >= 1.6f || aspect <= 0.62f) -> LayoutType.STRIP
                count in 3..6 -> LayoutType.BENTO
                else -> LayoutType.GRID
            }
        }

        // Balanced & Gallery ratio-aware layout selection
        return when {
            count == 2 -> if (aspect >= 1.45f || aspect <= 0.68f) LayoutType.STRIP else LayoutType.GRID
            count in 3..4 && (aspect >= 1.65f || aspect <= 0.62f) -> LayoutType.STRIP
            count in 3..6 -> LayoutType.BENTO
            count in 7..12 && aspect < 0.82f -> LayoutType.MASONRY
            count in 7..12 -> LayoutType.GRID
            count in 13..20 -> if (aspect < 0.9f) LayoutType.MASONRY else LayoutType.BENTO
            else -> LayoutType.MOSAIC
        }
    }
}
