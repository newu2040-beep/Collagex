package com.example.engine

import com.example.core.model.CollageLayer
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Normalized rectangle in canvas coordinates [0f..1f]. */
data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = (right - left).coerceAtLeast(0.01f)
    val height: Float get() = (bottom - top).coerceAtLeast(0.01f)
}

data class ComputedCell(
    val layer: CollageLayer,
    val rect: NormalizedRect,
)

/**
 * Deterministic multi-photo layout engine supporting 1 to 100+ photos across
 * Grid, Asymmetric Hero, Masonry, Bento, Strip, Mosaic, and Freeform arrangements.
 */
object LayoutEngine {

    fun computePhotoCells(
        layers: List<CollageLayer>,
        layoutType: LayoutType,
        canvasAspectRatio: Float,
        paddingFraction: Float,
        spacingFraction: Float,
    ): List<ComputedCell> {
        val photos = layers.filter { it.type == LayerType.PHOTO && it.visible }.sortedBy { it.zIndex }
        if (photos.isEmpty()) return emptyList()

        val pad = paddingFraction.coerceIn(0f, 0.14f)
        val gap = spacingFraction.coerceIn(0f, 0.06f)
        val innerW = (1f - pad * 2f).coerceAtLeast(0.2f)
        val innerH = (1f - pad * 2f).coerceAtLeast(0.2f)

        if (layoutType == LayoutType.FREEFORM) {
            return photos.mapIndexed { idx, layer ->
                val halfW = (layer.normWidth / 2f).coerceIn(0.06f, 0.48f)
                val halfH = (layer.normHeight / 2f).coerceIn(0.06f, 0.48f)
                val cx = if (layer.normX == 0.5f && layer.normY == 0.5f && photos.size > 1) {
                    val cols = ceil(sqrt(photos.size.toDouble())).toInt().coerceAtLeast(2)
                    val col = idx % cols
                    val row = idx / cols
                    val rows = ceil(photos.size.toDouble() / cols).toInt().coerceAtLeast(1)
                    pad + ((col + 0.5f) / cols) * innerW
                } else {
                    layer.normX
                }
                val cy = if (layer.normX == 0.5f && layer.normY == 0.5f && photos.size > 1) {
                    val cols = ceil(sqrt(photos.size.toDouble())).toInt().coerceAtLeast(2)
                    val row = idx / cols
                    val rows = ceil(photos.size.toDouble() / cols).toInt().coerceAtLeast(1)
                    pad + ((row + 0.5f) / rows) * innerH
                } else {
                    layer.normY
                }
                ComputedCell(
                    layer = layer,
                    rect = NormalizedRect(
                        left = (cx - halfW).coerceIn(0.01f, 0.90f),
                        top = (cy - halfH).coerceIn(0.01f, 0.90f),
                        right = (cx + halfW).coerceIn(0.10f, 0.99f),
                        bottom = (cy + halfH).coerceIn(0.10f, 0.99f),
                    ),
                )
            }
        }

        val n = photos.size
        if (n == 1) {
            return listOf(ComputedCell(photos[0], NormalizedRect(pad, pad, pad + innerW, pad + innerH)))
        }

        val rects = when (layoutType) {
            LayoutType.GRID -> computeGrid(n, canvasAspectRatio, pad, gap, innerW, innerH)
            LayoutType.STRIP -> computeStrip(n, canvasAspectRatio, pad, gap, innerW, innerH)
            LayoutType.ASYMMETRIC -> computeAsymmetric(n, canvasAspectRatio, pad, gap, innerW, innerH)
            LayoutType.BENTO -> computeBento(n, pad, gap, innerW, innerH)
            LayoutType.MASONRY -> computeMasonry(n, canvasAspectRatio, pad, gap, innerW, innerH)
            LayoutType.MOSAIC -> computeMosaic(n, canvasAspectRatio, pad, gap, innerW, innerH)
            LayoutType.FREEFORM -> emptyList()
        }

        return photos.zip(rects) { layer, rect -> ComputedCell(layer, rect) }
    }

    private fun computeGrid(
        n: Int,
        aspect: Float,
        pad: Float,
        gap: Float,
        innerW: Float,
        innerH: Float,
    ): List<NormalizedRect> {
        val rawCols = ceil(sqrt(n * aspect.coerceIn(0.5f, 2.0f).toDouble())).toInt()
        val cols = rawCols.coerceIn(1, min(n, 10))
        val rows = ceil(n.toDouble() / cols).toInt().coerceAtLeast(1)
        val effectiveGap = if (n > 20) gap * 0.45f else gap
        val cellH = (innerH - effectiveGap * (rows - 1)) / rows

        val out = ArrayList<NormalizedRect>(n)
        for (r in 0 until rows) {
            val startIdx = r * cols
            val countInRow = min(cols, n - startIdx)
            val cellW = (innerW - effectiveGap * (countInRow - 1)) / countInRow
            val top = pad + r * (cellH + effectiveGap)
            for (c in 0 until countInRow) {
                val left = pad + c * (cellW + effectiveGap)
                out.add(NormalizedRect(left, top, left + cellW, top + cellH))
            }
        }
        return out
    }

    private fun computeStrip(
        n: Int,
        aspect: Float,
        pad: Float,
        gap: Float,
        innerW: Float,
        innerH: Float,
    ): List<NormalizedRect> {
        // For >8 photos, switch to dual-strip so cells stay visible and balanced
        if (n > 8) {
            val cols = if (aspect >= 1f) ceil(n / 2.0).toInt() else 2
            val rows = ceil(n.toDouble() / cols).toInt()
            val g = gap * 0.5f
            val cellH = (innerH - g * (rows - 1)) / rows
            val out = ArrayList<NormalizedRect>(n)
            for (r in 0 until rows) {
                val count = min(cols, n - r * cols)
                val cellW = (innerW - g * (count - 1)) / count
                val top = pad + r * (cellH + g)
                for (c in 0 until count) {
                    val left = pad + c * (cellW + g)
                    out.add(NormalizedRect(left, top, left + cellW, top + cellH))
                }
            }
            return out
        }
        val horizontal = aspect >= 1f
        val out = ArrayList<NormalizedRect>(n)
        if (horizontal) {
            val cellW = (innerW - gap * (n - 1)) / n
            for (i in 0 until n) {
                val left = pad + i * (cellW + gap)
                out.add(NormalizedRect(left, pad, left + cellW, pad + innerH))
            }
        } else {
            val cellH = (innerH - gap * (n - 1)) / n
            for (i in 0 until n) {
                val top = pad + i * (cellH + gap)
                out.add(NormalizedRect(pad, top, pad + innerW, top + cellH))
            }
        }
        return out
    }

    private fun computeAsymmetric(
        n: Int,
        aspect: Float,
        pad: Float,
        gap: Float,
        innerW: Float,
        innerH: Float,
    ): List<NormalizedRect> {
        if (n == 2) {
            val w1 = (innerW - gap) * 0.62f
            val w2 = (innerW - gap) * 0.38f
            return listOf(
                NormalizedRect(pad, pad, pad + w1, pad + innerH),
                NormalizedRect(pad + w1 + gap, pad, pad + innerW, pad + innerH),
            )
        }
        // Hero cell occupies top 52% (portrait) or left 52% (landscape), rest tiled neatly
        val out = ArrayList<NormalizedRect>(n)
        val remaining = n - 1
        val effectiveGap = if (n > 16) gap * 0.5f else gap
        if (aspect < 1.15f) {
            val heroH = (innerH - effectiveGap) * 0.52f
            out.add(NormalizedRect(pad, pad, pad + innerW, pad + heroH))
            val remTop = pad + heroH + effectiveGap
            val remH = innerH - heroH - effectiveGap
            val subRects = computeGrid(remaining, aspect * 1.4f, 0f, effectiveGap, innerW, remH)
            subRects.forEach { r ->
                out.add(NormalizedRect(pad + r.left, remTop + r.top, pad + r.right, remTop + r.bottom))
            }
        } else {
            val heroW = (innerW - effectiveGap) * 0.52f
            out.add(NormalizedRect(pad, pad, pad + heroW, pad + innerH))
            val remLeft = pad + heroW + effectiveGap
            val remW = innerW - heroW - effectiveGap
            val subRects = computeGrid(remaining, aspect * 0.65f, 0f, effectiveGap, remW, innerH)
            subRects.forEach { r ->
                out.add(NormalizedRect(remLeft + r.left, pad + r.top, remLeft + r.right, pad + r.bottom))
            }
        }
        return out
    }

    /**
     * Signature CollageX Bento layout matching the mockup's organic asymmetric 2-column / 6-photo balance.
     */
    private fun computeBento(
        n: Int,
        pad: Float,
        gap: Float,
        innerW: Float,
        innerH: Float,
    ): List<NormalizedRect> {
        if (n == 6) {
            val g = gap
            val r1H = (innerH - g * 2f) * 0.38f
            val r2H = (innerH - g * 2f) * 0.36f
            val r3H = innerH - g * 2f - r1H - r2H
            val y1 = pad
            val y2 = y1 + r1H + g
            val y3 = y2 + r2H + g
            val wHalf = (innerW - g) * 0.5f
            val wWide = (innerW - g) * 0.58f
            val wNarrow = innerW - g - wWide
            return listOf(
                NormalizedRect(pad, y1, pad + wHalf, y1 + r1H),
                NormalizedRect(pad + wHalf + g, y1, pad + innerW, y1 + r1H),
                NormalizedRect(pad, y2, pad + wWide, y2 + r2H),
                NormalizedRect(pad + wWide + g, y2, pad + innerW, y2 + r2H),
                NormalizedRect(pad, y3, pad + wNarrow, y3 + r3H),
                NormalizedRect(pad + wNarrow + g, y3, pad + innerW, y3 + r3H),
            )
        }
        if (n == 4) {
            val g = gap
            val r1H = (innerH - g) * 0.56f
            val r2H = innerH - g - r1H
            val w1 = (innerW - g) * 0.58f
            val w2 = innerW - g - w1
            return listOf(
                NormalizedRect(pad, pad, pad + w1, pad + r1H),
                NormalizedRect(pad + w1 + g, pad, pad + innerW, pad + r1H),
                NormalizedRect(pad, pad + r1H + g, pad + w2, pad + innerH),
                NormalizedRect(pad + w2 + g, pad + r1H + g, pad + innerW, pad + innerH),
            )
        }
        if (n == 3) {
            val g = gap
            val w1 = (innerW - g) * 0.55f
            val w2 = innerW - g - w1
            val hHalf = (innerH - g) * 0.5f
            return listOf(
                NormalizedRect(pad, pad, pad + w1, pad + innerH),
                NormalizedRect(pad + w1 + g, pad, pad + innerW, pad + hHalf),
                NormalizedRect(pad + w1 + g, pad + hHalf + g, pad + innerW, pad + innerH),
            )
        }
        // Generalized bento for any count (including 10, 25, 50+ photos) with alternating weighted rows
        val g = if (n > 20) gap * 0.45f else gap
        val baseCols = when {
            n <= 8 -> 2
            n <= 18 -> 3
            n <= 36 -> 4
            else -> 6
        }
        val rows = ceil(n.toDouble() / baseCols).toInt().coerceAtLeast(1)
        val rowH = (innerH - g * (rows - 1)) / rows
        val out = ArrayList<NormalizedRect>(n)
        for (r in 0 until rows) {
            val start = r * baseCols
            val count = min(baseCols, n - start)
            val weights = FloatArray(count) { c ->
                when {
                    count == 1 -> 1f
                    (r + c) % 3 == 0 -> 1.32f
                    else -> 0.88f
                }
            }
            val sumW = weights.sum()
            val availW = innerW - g * (count - 1)
            var cursorX = pad
            val top = pad + r * (rowH + g)
            for (c in 0 until count) {
                val cw = availW * (weights[c] / sumW)
                out.add(NormalizedRect(cursorX, top, cursorX + cw, top + rowH))
                cursorX += cw + g
            }
        }
        return out
    }

    private fun computeMasonry(
        n: Int,
        aspect: Float,
        pad: Float,
        gap: Float,
        innerW: Float,
        innerH: Float,
    ): List<NormalizedRect> {
        val g = if (n > 20) gap * 0.45f else gap
        val cols = when {
            n <= 4 -> 2
            n <= 12 -> if (aspect >= 1.2f) 4 else 3
            n <= 30 -> 5
            else -> 7
        }.coerceAtMost(n)
        val colW = (innerW - g * (cols - 1)) / cols
        val perCol = Array(cols) { mutableListOf<Int>() }
        for (i in 0 until n) {
            perCol[i % cols].add(i)
        }

        val result = arrayOfNulls<NormalizedRect>(n)
        for (c in 0 until cols) {
            val items = perCol[c]
            val weights = FloatArray(items.size) { idx ->
                when ((c + idx) % 3) {
                    0 -> 1.28f
                    1 -> 0.85f
                    else -> 1.0f
                }
            }
            val sum = weights.sum().coerceAtLeast(0.1f)
            val availH = innerH - g * max(0, items.size - 1)
            val left = pad + c * (colW + g)
            var cursorY = pad
            items.forEachIndexed { idx, originalIndex ->
                val ch = availH * (weights[idx] / sum)
                result[originalIndex] = NormalizedRect(left, cursorY, left + colW, cursorY + ch)
                cursorY += ch + g
            }
        }
        return result.filterNotNull()
    }

    private fun computeMosaic(
        n: Int,
        aspect: Float,
        pad: Float,
        gap: Float,
        innerW: Float,
        innerH: Float,
    ): List<NormalizedRect> {
        // Tight dense mosaic ideal for 10–50+ photos
        val tightGap = (gap * 0.4f).coerceAtMost(0.012f)
        return computeGrid(n, aspect, pad * 0.6f, tightGap, 1f - pad * 1.2f, 1f - pad * 1.2f)
    }
}
