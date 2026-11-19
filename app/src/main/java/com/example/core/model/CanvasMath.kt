package com.example.core.model

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class PixelDimensions(
    val widthPx: Int,
    val heightPx: Int,
) {
    val totalPixels: Long get() = widthPx.toLong() * heightPx.toLong()
    val megapixels: Double get() = totalPixels.toDouble() / 1_000_000.0
    val rawRgbaBytes: Long get() = totalPixels * 4L
    val aspectRatio: Float get() = if (heightPx > 0) widthPx.toFloat() / heightPx.toFloat() else 1f
}

data class CanvasValidationResult(
    val valid: Boolean,
    val pixels: PixelDimensions,
    val estimatedPngMb: Double,
    val estimatedJpegMb: Double,
    val requiresTiledRender: Boolean,
    val warningMessage: String? = null,
    val errorMessage: String? = null,
)

data class AspectRatioPreset(
    val id: String,
    val ratioLabel: String,
    val name: String,
    val ratioW: Double,
    val ratioH: Double,
    val suggestedPxW: Int,
    val suggestedPxH: Int,
    val isPrint: Boolean = false,
    val printUnit: DimensionUnit = DimensionUnit.PX,
    val printW: Double = suggestedPxW.toDouble(),
    val printH: Double = suggestedPxH.toDouble(),
    val printDpi: Int = 300,
)

object CanvasMath {
    const val MIN_PX = 64
    const val SAFE_SINGLE_BITMAP_MAX_PIXELS = 24_000_000L // ~96MB RGBA
    const val MAX_TILED_DIM_PX = 8192
    const val MAX_TILED_PIXELS = 67_108_864L // 8192 x 8192

    val StandardPresets: List<AspectRatioPreset> = listOf(
        AspectRatioPreset("1_1", "1:1", "Square", 1.0, 1.0, 2400, 2400),
        AspectRatioPreset("4_5", "4:5", "Portrait", 4.0, 5.0, 2160, 2700),
        AspectRatioPreset("3_4", "3:4", "Photo", 3.0, 4.0, 2400, 3200),
        AspectRatioPreset("9_16", "9:16", "Story", 9.0, 16.0, 1800, 3200),
        AspectRatioPreset("16_9", "16:9", "Landscape", 16.0, 9.0, 3840, 2160),
        AspectRatioPreset("3_2", "3:2", "Classic", 3.0, 2.0, 3000, 2000),
    )

    val PrintPresets: List<AspectRatioPreset> = listOf(
        AspectRatioPreset("a4", "A4", "210×297 mm", 210.0, 297.0, 2480, 3508, true, DimensionUnit.MM, 210.0, 297.0, 300),
        AspectRatioPreset("4x6", "4×6\"", "Print Photo", 4.0, 6.0, 1200, 1800, true, DimensionUnit.IN, 4.0, 6.0, 300),
        AspectRatioPreset("8x10", "8×10\"", "Frame Print", 8.0, 10.0, 2400, 3000, true, DimensionUnit.IN, 8.0, 10.0, 300),
    )

    fun toPixels(value: Double, unit: DimensionUnit, dpi: Int): Long {
        if (!value.isFinite() || value <= 0.0 || dpi <= 0) return 0L
        val safeDpi = dpi.toDouble()
        val raw = when (unit) {
            DimensionUnit.PX -> value
            DimensionUnit.IN -> value * safeDpi
            DimensionUnit.CM -> (value / 2.54) * safeDpi
            DimensionUnit.MM -> (value / 25.4) * safeDpi
            DimensionUnit.PT -> (value / 72.0) * safeDpi
        }
        return raw.roundToLong().coerceAtLeast(0L)
    }

    fun fromPixels(pixels: Double, unit: DimensionUnit, dpi: Int): Double {
        if (!pixels.isFinite() || pixels <= 0.0 || dpi <= 0) return 0.0
        val safeDpi = dpi.toDouble()
        return when (unit) {
            DimensionUnit.PX -> pixels
            DimensionUnit.IN -> pixels / safeDpi
            DimensionUnit.CM -> (pixels / safeDpi) * 2.54
            DimensionUnit.MM -> (pixels / safeDpi) * 25.4
            DimensionUnit.PT -> (pixels / safeDpi) * 72.0
        }
    }

    fun convertUnit(value: Double, from: DimensionUnit, to: DimensionUnit, dpi: Int): Double {
        if (from == to) return value
        val px = when (from) {
            DimensionUnit.PX -> value
            DimensionUnit.IN -> value * dpi
            DimensionUnit.CM -> (value / 2.54) * dpi
            DimensionUnit.MM -> (value / 25.4) * dpi
            DimensionUnit.PT -> (value / 72.0) * dpi
        }
        return fromPixels(px, to, dpi)
    }

    fun toPixelDimensions(spec: CanvasSpec): PixelDimensions {
        val w = toPixels(spec.width, spec.unit, spec.dpi).coerceIn(1L, 100_000L).toInt()
        val h = toPixels(spec.height, spec.unit, spec.dpi).coerceIn(1L, 100_000L).toInt()
        return PixelDimensions(w, h)
    }

    fun formatDimension(value: Double, unit: DimensionUnit): String {
        return if (unit == DimensionUnit.PX) {
            value.roundToInt().toString()
        } else {
            val rounded = (value * 100.0).roundToInt() / 100.0
            if (rounded == rounded.toLong().toDouble()) {
                rounded.toLong().toString()
            } else {
                String.format(java.util.Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
            }
        }
    }

    fun validate(spec: CanvasSpec): CanvasValidationResult {
        val wLong = toPixels(spec.width, spec.unit, spec.dpi)
        val hLong = toPixels(spec.height, spec.unit, spec.dpi)

        if (spec.width <= 0.0 || spec.height <= 0.0 || !spec.width.isFinite() || !spec.height.isFinite()) {
            return CanvasValidationResult(
                valid = false,
                pixels = PixelDimensions(0, 0),
                estimatedPngMb = 0.0,
                estimatedJpegMb = 0.0,
                requiresTiledRender = false,
                errorMessage = "Enter positive width and height values.",
            )
        }
        if (spec.dpi !in 36..1200) {
            return CanvasValidationResult(
                valid = false,
                pixels = PixelDimensions(wLong.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), hLong.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()),
                estimatedPngMb = 0.0,
                estimatedJpegMb = 0.0,
                requiresTiledRender = false,
                errorMessage = "DPI must be between 36 and 1200.",
            )
        }
        if (wLong < MIN_PX || hLong < MIN_PX) {
            return CanvasValidationResult(
                valid = false,
                pixels = PixelDimensions(wLong.toInt(), hLong.toInt()),
                estimatedPngMb = 0.0,
                estimatedJpegMb = 0.0,
                requiresTiledRender = false,
                errorMessage = "Minimum output dimension is ${MIN_PX}px.",
            )
        }
        if (wLong > MAX_TILED_DIM_PX || hLong > MAX_TILED_DIM_PX) {
            return CanvasValidationResult(
                valid = false,
                pixels = PixelDimensions(wLong.coerceAtMost(100_000L).toInt(), hLong.coerceAtMost(100_000L).toInt()),
                estimatedPngMb = 0.0,
                estimatedJpegMb = 0.0,
                requiresTiledRender = true,
                errorMessage = "Maximum supported edge on this device is ${MAX_TILED_DIM_PX}px (8K). Reduce dimensions or DPI.",
            )
        }

        val totalPixels = wLong * hLong
        if (totalPixels > MAX_TILED_PIXELS) {
            return CanvasValidationResult(
                valid = false,
                pixels = PixelDimensions(wLong.toInt(), hLong.toInt()),
                estimatedPngMb = 0.0,
                estimatedJpegMb = 0.0,
                requiresTiledRender = true,
                errorMessage = "Canvas exceeds 67MP device safety cap. Reduce dimensions slightly.",
            )
        }

        val pixels = PixelDimensions(wLong.toInt(), hLong.toInt())
        val rawMb = (totalPixels * 4.0) / (1024.0 * 1024.0)
        val estPngMb = rawMb * 0.42
        val estJpegMb = rawMb * 0.14
        val requiresTiled = totalPixels > SAFE_SINGLE_BITMAP_MAX_PIXELS
        val warning = if (requiresTiled) {
            "High-resolution output (${String.format(java.util.Locale.US, "%.1f", pixels.megapixels)} MP) uses memory-safe strip rendering."
        } else {
            null
        }
        return CanvasValidationResult(
            valid = true,
            pixels = pixels,
            estimatedPngMb = estPngMb,
            estimatedJpegMb = estJpegMb,
            requiresTiledRender = requiresTiled,
            warningMessage = warning,
        )
    }

    /**
     * Computes scaled output dimensions for export presets while preserving exact aspect ratio.
     */
    fun computeExportDimensions(canvas: CanvasSpec, config: ExportJobConfig): PixelDimensions {
        val base = toPixelDimensions(canvas)
        if (config.customWidthPx != null && config.customHeightPx != null &&
            config.customWidthPx >= MIN_PX && config.customHeightPx >= MIN_PX
        ) {
            return PixelDimensions(
                config.customWidthPx.coerceAtMost(MAX_TILED_DIM_PX),
                config.customHeightPx.coerceAtMost(MAX_TILED_DIM_PX),
            )
        }
        val targetLong = config.resolutionPreset.maxLongEdgePx ?: return base
        val currentLong = max(base.widthPx, base.heightPx).coerceAtLeast(1)
        val scale = targetLong.toDouble() / currentLong.toDouble()
        val outW = (base.widthPx * scale).roundToInt().coerceIn(MIN_PX, MAX_TILED_DIM_PX)
        val outH = (base.heightPx * scale).roundToInt().coerceIn(MIN_PX, MAX_TILED_DIM_PX)
        return PixelDimensions(outW, outH)
    }

    /**
     * Non-destructively adapts existing overlay/freeform layer coordinates when canvas dimensions change.
     * Preserves all source photo references, masks, borders, text, stickers, filters, and z-order.
     */
    fun adaptLayersForResize(
        layers: List<CollageLayer>,
        oldSpec: CanvasSpec,
        newSpec: CanvasSpec,
        mode: ResizeMode,
    ): List<CollageLayer> {
        val oldPx = toPixelDimensions(oldSpec)
        val newPx = toPixelDimensions(newSpec)
        if (oldPx.widthPx <= 0 || oldPx.heightPx <= 0 || newPx.widthPx <= 0 || newPx.heightPx <= 0) {
            return layers
        }
        val oldRatio = oldPx.aspectRatio
        val newRatio = newPx.aspectRatio
        if (kotlin.math.abs(oldRatio - newRatio) < 0.001f && mode != ResizeMode.EXTEND) {
            return layers
        }

        return layers.map { layer ->
            val sx: Float
            val sy: Float
            when (mode) {
                ResizeMode.FIT -> {
                    val scale = min(newPx.widthPx.toFloat() / oldPx.widthPx, newPx.heightPx.toFloat() / oldPx.heightPx)
                    sx = (oldPx.widthPx * scale) / newPx.widthPx
                    sy = (oldPx.heightPx * scale) / newPx.heightPx
                }
                ResizeMode.FILL -> {
                    val scale = max(newPx.widthPx.toFloat() / oldPx.widthPx, newPx.heightPx.toFloat() / oldPx.heightPx)
                    sx = (oldPx.widthPx * scale) / newPx.widthPx
                    sy = (oldPx.heightPx * scale) / newPx.heightPx
                }
                ResizeMode.CROP -> {
                    sx = oldPx.widthPx.toFloat() / newPx.widthPx
                    sy = oldPx.heightPx.toFloat() / newPx.heightPx
                }
                ResizeMode.EXTEND -> {
                    val scale = min(1f, min(newPx.widthPx.toFloat() / oldPx.widthPx, newPx.heightPx.toFloat() / oldPx.heightPx))
                    sx = (oldPx.widthPx * scale) / newPx.widthPx
                    sy = (oldPx.heightPx * scale) / newPx.heightPx
                }
            }
            val newX = (0.5f + (layer.normX - 0.5f) * sx).coerceIn(0.05f, 0.95f)
            val newY = (0.5f + (layer.normY - 0.5f) * sy).coerceIn(0.05f, 0.95f)
            val newW = (layer.normWidth * sx).coerceIn(0.08f, 0.95f)
            val newH = (layer.normHeight * sy).coerceIn(0.08f, 0.95f)
            layer.copy(
                normX = newX,
                normY = newY,
                normWidth = newW,
                normHeight = newH,
            )
        }
    }
}
