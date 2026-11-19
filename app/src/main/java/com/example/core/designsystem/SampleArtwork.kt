package com.example.core.designsystem

import android.graphics.Canvas as AndroidCanvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

/**
 * Curated procedural scenic artwork used for the Welcome screen preview fan and optional starter
 * templates when the user wants to test the editor before picking device photos.
 * Renders identically in Compose UI and on Android Canvas during real bitmap exports.
 */
object SampleArtwork {
    const val PAGODA = "sample_pagoda"
    const val ALPINE = "sample_alpine"
    const val BLOSSOMS = "sample_blossoms"
    const val SUNSET = "sample_sunset"
    const val LAKE = "sample_lake"
    const val SAKURA_VALLEY = "sample_sakura_valley"

    val AllKeys = listOf(PAGODA, ALPINE, BLOSSOMS, SUNSET, LAKE, SAKURA_VALLEY)

    fun labelFor(key: String): String = when (key) {
        PAGODA -> "Kyoto Pagoda"
        ALPINE -> "Alpine Peaks"
        BLOSSOMS -> "Cherry Blossoms"
        SUNSET -> "Golden Horizon"
        LAKE -> "Misty Lake"
        SAKURA_VALLEY -> "Sakura Valley"
        else -> "Memory"
    }

    @Composable
    fun ScenicCanvas(artKey: String, modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            drawScenicCompose(artKey)
        }
    }

    fun DrawScope.drawScenicCompose(artKey: String) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return

        when (artKey) {
            PAGODA -> {
                // Warm twilight sky
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF5B86E5), Color(0xFFA7BFE8), Color(0xFFFFB88C), Color(0xFFFF758C)),
                    ),
                )
                // Sun glow
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFF2B2), Color(0x00FF9A76)),
                        center = Offset(w * 0.72f, h * 0.32f),
                        radius = w * 0.35f,
                    ),
                    center = Offset(w * 0.72f, h * 0.32f),
                    radius = w * 0.35f,
                )
                // Distant mountains
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.72f)
                        lineTo(w * 0.25f, h * 0.48f)
                        lineTo(w * 0.55f, h * 0.65f)
                        lineTo(w * 0.85f, h * 0.44f)
                        lineTo(w, h * 0.62f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    },
                    color = Color(0xFF4B3D6B),
                )
                // Foreground hills
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.80f)
                        quadraticTo(w * 0.3f, h * 0.72f, w * 0.65f, h * 0.82f)
                        quadraticTo(w * 0.85f, h * 0.86f, w, h * 0.78f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    },
                    color = Color(0xFF281D38),
                )
                // Pagoda tiers
                val cx = w * 0.48f
                for (tier in 0..3) {
                    val topY = h * (0.28f + tier * 0.12f)
                    val halfW = w * (0.09f + tier * 0.045f)
                    drawPath(
                        path = Path().apply {
                            moveTo(cx - halfW * 1.3f, topY + h * 0.055f)
                            lineTo(cx, topY)
                            lineTo(cx + halfW * 1.3f, topY + h * 0.055f)
                            close()
                        },
                        color = Color(0xFF1F162B),
                    )
                    drawRect(
                        color = Color(0xFF35243D),
                        topLeft = Offset(cx - halfW * 0.65f, topY + h * 0.055f),
                        size = Size(halfW * 1.3f, h * 0.07f),
                    )
                    // Warm lantern glow
                    drawRect(
                        color = Color(0xFFFFC86B),
                        topLeft = Offset(cx - halfW * 0.22f, topY + h * 0.07f),
                        size = Size(halfW * 0.44f, h * 0.035f),
                    )
                }
                // Spire
                drawLine(
                    color = Color(0xFF1F162B),
                    start = Offset(cx, h * 0.16f),
                    end = Offset(cx, h * 0.29f),
                    strokeWidth = w * 0.018f,
                )
                // Blossom accents
                val petals = listOf(
                    Offset(w * 0.14f, h * 0.22f),
                    Offset(w * 0.22f, h * 0.28f),
                    Offset(w * 0.82f, h * 0.24f),
                    Offset(w * 0.88f, h * 0.34f),
                )
                petals.forEach { p ->
                    drawCircle(Color(0xFFFFA8C5), radius = w * 0.045f, center = p)
                    drawCircle(Color(0xFFFFD4E3), radius = w * 0.025f, center = Offset(p.x + w * 0.02f, p.y - h * 0.015f))
                }
            }

            ALPINE -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF2B6CB0), Color(0xFF63B3ED), Color(0xFFEBF8FF)),
                    ),
                )
                // Snow peak
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.72f)
                        lineTo(w * 0.34f, h * 0.32f)
                        lineTo(w * 0.56f, h * 0.50f)
                        lineTo(w * 0.76f, h * 0.24f)
                        lineTo(w, h * 0.64f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    },
                    color = Color(0xFF3A527A),
                )
                // Snow cap highlights
                drawPath(
                    path = Path().apply {
                        moveTo(w * 0.76f, h * 0.24f)
                        lineTo(w * 0.65f, h * 0.42f)
                        lineTo(w * 0.74f, h * 0.38f)
                        lineTo(w * 0.84f, h * 0.43f)
                        close()
                    },
                    color = Color(0xFFF7FAFC),
                )
                drawPath(
                    path = Path().apply {
                        moveTo(w * 0.34f, h * 0.32f)
                        lineTo(w * 0.24f, h * 0.48f)
                        lineTo(w * 0.34f, h * 0.44f)
                        lineTo(w * 0.42f, h * 0.49f)
                        close()
                    },
                    color = Color(0xFFEDF2F7),
                )
                // Emerald meadow
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.68f)
                        quadraticTo(w * 0.45f, h * 0.60f, w, h * 0.74f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    },
                    color = Color(0xFF2F855A),
                )
                // Wildflower dots
                for (i in 0..9) {
                    val x = w * (0.08f + i * 0.09f)
                    val y = h * (0.78f + (i % 3) * 0.055f)
                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFF6E05E) else Color(0xFFFFB3C6),
                        radius = w * 0.018f,
                        center = Offset(x, y),
                    )
                }
            }

            BLOSSOMS -> {
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF7B2CBF), Color(0xFFE05780), Color(0xFFFFB3C6)),
                    ),
                )
                // Branches
                drawLine(Color(0xFF4A2033), Offset(0f, h * 0.3f), Offset(w * 0.85f, h * 0.72f), strokeWidth = w * 0.03f)
                drawLine(Color(0xFF4A2033), Offset(w * 0.2f, h * 0.85f), Offset(w * 0.95f, h * 0.2f), strokeWidth = w * 0.025f)
                // Rich 5-petal sakura blooms
                val centers = listOf(
                    Triple(0.24f, 0.30f, 0.12f),
                    Triple(0.62f, 0.28f, 0.14f),
                    Triple(0.45f, 0.56f, 0.15f),
                    Triple(0.78f, 0.64f, 0.13f),
                    Triple(0.22f, 0.72f, 0.12f),
                )
                centers.forEach { (fx, fy, fr) ->
                    val cx = w * fx
                    val cy = h * fy
                    val r = minOf(w, h) * fr
                    for (p in 0..4) {
                        val ang = Math.toRadians(p * 72.0 - 18.0)
                        val px = cx + cos(ang).toFloat() * r * 0.68f
                        val py = cy + sin(ang).toFloat() * r * 0.68f
                        drawCircle(Color(0xFFFFD6E5), radius = r * 0.56f, center = Offset(px, py))
                        drawCircle(Color(0xFFFF85AE), radius = r * 0.34f, center = Offset(px, py))
                    }
                    drawCircle(Color(0xFFFFE066), radius = r * 0.24f, center = Offset(cx, cy))
                }
            }

            SUNSET -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF3B1D5E), Color(0xFFCB4B75), Color(0xFFFF9A62), Color(0xFFFFE29A)),
                    ),
                )
                drawCircle(
                    color = Color(0xFFFFF5CC),
                    radius = minOf(w, h) * 0.18f,
                    center = Offset(w * 0.5f, h * 0.54f),
                )
                // Horizon reflection
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF6B2D5C), Color(0xFF2B133D)),
                    ),
                    topLeft = Offset(0f, h * 0.64f),
                    size = Size(w, h * 0.36f),
                )
                for (i in 0..5) {
                    val rw = w * (0.34f - i * 0.045f)
                    drawRect(
                        color = Color(0xAAFFD180),
                        topLeft = Offset(w * 0.5f - rw / 2f, h * (0.68f + i * 0.045f)),
                        size = Size(rw, h * 0.018f),
                    )
                }
            }

            LAKE -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1D4E89), Color(0xFF00B2CA), Color(0xFF7DCFB6)),
                    ),
                )
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.58f)
                        lineTo(w * 0.32f, h * 0.34f)
                        lineTo(w * 0.66f, h * 0.52f)
                        lineTo(w * 0.90f, h * 0.30f)
                        lineTo(w, h * 0.56f)
                        lineTo(w, h * 0.62f)
                        lineTo(0f, h * 0.62f)
                        close()
                    },
                    color = Color(0xFF1A365D),
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF2C5282), Color(0xFF1A365D)),
                    ),
                    topLeft = Offset(0f, h * 0.62f),
                    size = Size(w, h * 0.38f),
                )
            }

            else -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFD6E0), Color(0xFFFFB3C6), Color(0xFFFF8FAB)),
                    ),
                )
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.72f)
                        quadraticTo(w * 0.35f, h * 0.45f, w * 0.72f, h * 0.68f)
                        quadraticTo(w * 0.88f, h * 0.76f, w, h * 0.60f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    },
                    color = Color(0xFFEA4C7A),
                )
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.84f)
                        quadraticTo(w * 0.5f, h * 0.66f, w, h * 0.82f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    },
                    color = Color(0xFFC2185B),
                )
            }
        }
    }

    /** Renders the same scenic artwork onto an Android Canvas for real bitmap/PDF/TIFF export. */
    fun drawScenicAndroid(canvas: AndroidCanvas, rect: RectF, artKey: String) {
        val w = rect.width()
        val h = rect.height()
        val l = rect.left
        val t = rect.top
        if (w <= 0f || h <= 0f) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colors = when (artKey) {
            PAGODA -> intArrayOf(0xFF5B86E5.toInt(), 0xFFFFB88C.toInt(), 0xFFFF758C.toInt())
            ALPINE -> intArrayOf(0xFF2B6CB0.toInt(), 0xFF63B3ED.toInt(), 0xFFEBF8FF.toInt())
            BLOSSOMS -> intArrayOf(0xFF7B2CBF.toInt(), 0xFFE05780.toInt(), 0xFFFFB3C6.toInt())
            SUNSET -> intArrayOf(0xFF3B1D5E.toInt(), 0xFFCB4B75.toInt(), 0xFFFFE29A.toInt())
            LAKE -> intArrayOf(0xFF1D4E89.toInt(), 0xFF00B2CA.toInt(), 0xFF7DCFB6.toInt())
            else -> intArrayOf(0xFFFFD6E0.toInt(), 0xFFFFB3C6.toInt(), 0xFFFF8FAB.toInt())
        }
        paint.shader = LinearGradient(l, t, l, t + h, colors, null, Shader.TileMode.CLAMP)
        canvas.drawRect(rect, paint)
        paint.shader = null

        when (artKey) {
            PAGODA -> {
                paint.color = 0xFF4B3D6B.toInt()
                val mt = AndroidPath().apply {
                    moveTo(l, t + h * 0.72f)
                    lineTo(l + w * 0.25f, t + h * 0.48f)
                    lineTo(l + w * 0.55f, t + h * 0.65f)
                    lineTo(l + w * 0.85f, t + h * 0.44f)
                    lineTo(l + w, t + h * 0.62f)
                    lineTo(l + w, t + h)
                    lineTo(l, t + h)
                    close()
                }
                canvas.drawPath(mt, paint)
                val cx = l + w * 0.48f
                for (tier in 0..3) {
                    val topY = t + h * (0.28f + tier * 0.12f)
                    val halfW = w * (0.09f + tier * 0.045f)
                    paint.color = 0xFF1F162B.toInt()
                    val roof = AndroidPath().apply {
                        moveTo(cx - halfW * 1.3f, topY + h * 0.055f)
                        lineTo(cx, topY)
                        lineTo(cx + halfW * 1.3f, topY + h * 0.055f)
                        close()
                    }
                    canvas.drawPath(roof, paint)
                    paint.color = 0xFF35243D.toInt()
                    canvas.drawRect(cx - halfW * 0.65f, topY + h * 0.055f, cx + halfW * 0.65f, topY + h * 0.125f, paint)
                    paint.color = 0xFFFFC86B.toInt()
                    canvas.drawRect(cx - halfW * 0.22f, topY + h * 0.07f, cx + halfW * 0.22f, topY + h * 0.105f, paint)
                }
            }
            ALPINE -> {
                paint.color = 0xFF3A527A.toInt()
                val mt = AndroidPath().apply {
                    moveTo(l, t + h * 0.72f)
                    lineTo(l + w * 0.34f, t + h * 0.32f)
                    lineTo(l + w * 0.56f, t + h * 0.50f)
                    lineTo(l + w * 0.76f, t + h * 0.24f)
                    lineTo(l + w, t + h * 0.64f)
                    lineTo(l + w, t + h)
                    lineTo(l, t + h)
                    close()
                }
                canvas.drawPath(mt, paint)
                paint.color = 0xFF2F855A.toInt()
                canvas.drawRect(l, t + h * 0.68f, l + w, t + h, paint)
            }
            BLOSSOMS -> {
                val centers = listOf(
                    Triple(0.24f, 0.30f, 0.12f),
                    Triple(0.62f, 0.28f, 0.14f),
                    Triple(0.45f, 0.56f, 0.15f),
                    Triple(0.78f, 0.64f, 0.13f),
                )
                centers.forEach { (fx, fy, fr) ->
                    val cx = l + w * fx
                    val cy = t + h * fy
                    val r = minOf(w, h) * fr
                    for (p in 0..4) {
                        val ang = Math.toRadians(p * 72.0 - 18.0)
                        val px = cx + cos(ang).toFloat() * r * 0.68f
                        val py = cy + sin(ang).toFloat() * r * 0.68f
                        paint.color = 0xFFFFD6E5.toInt()
                        canvas.drawCircle(px, py, r * 0.56f, paint)
                    }
                    paint.color = 0xFFFFE066.toInt()
                    canvas.drawCircle(cx, cy, r * 0.24f, paint)
                }
            }
            else -> {
                paint.color = 0xFFFFF5CC.toInt()
                canvas.drawCircle(l + w * 0.5f, t + h * 0.52f, minOf(w, h) * 0.18f, paint)
                paint.color = 0xFF4A2040.toInt()
                canvas.drawRect(l, t + h * 0.66f, l + w, t + h, paint)
            }
        }
    }
}
