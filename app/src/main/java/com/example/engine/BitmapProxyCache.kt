package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import com.example.core.model.PhotoAdjustments
import com.example.core.model.PhotoFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Memory-safe LRU bitmap proxy loader for 50+ photo editing.
 * Decodes only downsampled proxies using inSampleSize so full-resolution bitmaps
 * are never held in RAM simultaneously during interactive editing.
 */
object BitmapProxyCache {
    private val maxCacheKb: Int = (Runtime.getRuntime().maxMemory() / 1024L / 8L)
        .toInt()
        .coerceIn(8 * 1024, 36 * 1024)

    private val cache = object : LruCache<String, Bitmap>(maxCacheKb) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    suspend fun loadProxy(
        context: Context,
        uriString: String,
        targetMaxEdgePx: Int = 512,
    ): Bitmap? = withContext(Dispatchers.IO) {
        val key = "$uriString@$targetMaxEdgePx"
        cache.get(key)?.let { cached ->
            if (!cached.isRecycled) return@withContext cached
        }
        val decoded = decodeSampledBitmap(context, Uri.parse(uriString), targetMaxEdgePx)
        if (decoded != null) {
            cache.put(key, decoded)
        }
        decoded
    }

    fun decodeSampledBitmap(
        context: Context,
        uri: Uri,
        targetMaxEdgePx: Int,
    ): Bitmap? {
        return try {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            val srcW = bounds.outWidth
            val srcH = bounds.outHeight
            if (srcW <= 0 || srcH <= 0) return null

            var sampleSize = 1
            val maxSrc = max(srcW, srcH)
            val safeTarget = targetMaxEdgePx.coerceAtLeast(64)
            while (maxSrc / (sampleSize * 2) >= safeTarget) {
                sampleSize *= 2
            }

            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val raw = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            } ?: return null

            val exifRotation = readExifRotation(context, uri)
            if (exifRotation != 0) {
                val matrix = Matrix().apply { postRotate(exifRotation.toFloat()) }
                val rotated = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
                if (rotated !== raw) raw.recycle()
                rotated
            } else {
                raw
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun readExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Throwable) {
            0
        }
    }

    fun clear() {
        cache.evictAll()
    }

    /**
     * Fast, memory-safe 3x3 unsharp-mask convolution kernel for photo sharpening.
     * Returns a newly allocated sharpened Bitmap when amount > 0.02f, or the original bitmap otherwise.
     */
    fun applySharpen(src: Bitmap, amount: Float): Bitmap {
        val clamped = amount.coerceIn(0f, 1f)
        if (clamped <= 0.02f || src.isRecycled) return src
        val w = src.width
        val h = src.height
        if (w < 3 || h < 3) return src

        val srcPixels = IntArray(w * h)
        src.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val outPixels = srcPixels.copyOf()

        // Kernel: center = 1 + 4*k, 4-neighbors = -k
        val k = clamped * 0.9f
        val centerW = 1f + 4f * k

        for (y in 1 until h - 1) {
            val rowOff = y * w
            val upOff = rowOff - w
            val dnOff = rowOff + w
            for (x in 1 until w - 1) {
                val idx = rowOff + x
                val c = srcPixels[idx]
                val u = srcPixels[upOff + x]
                val d = srcPixels[dnOff + x]
                val l = srcPixels[idx - 1]
                val r = srcPixels[idx + 1]

                val a = (c ushr 24) and 0xFF
                val cr = (c ushr 16) and 0xFF
                val cg = (c ushr 8) and 0xFF
                val cb = c and 0xFF

                val nr = (cr * centerW - (((u ushr 16) and 0xFF) + ((d ushr 16) and 0xFF) + ((l ushr 16) and 0xFF) + ((r ushr 16) and 0xFF)) * k)
                    .toInt().coerceIn(0, 255)
                val ng = (cg * centerW - (((u ushr 8) and 0xFF) + ((d ushr 8) and 0xFF) + ((l ushr 8) and 0xFF) + ((r ushr 8) and 0xFF)) * k)
                    .toInt().coerceIn(0, 255)
                val nb = (cb * centerW - ((u and 0xFF) + (d and 0xFF) + (l and 0xFF) + (r and 0xFF)) * k)
                    .toInt().coerceIn(0, 255)

                outPixels[idx] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
            }
        }
        return Bitmap.createBitmap(outPixels, w, h, Bitmap.Config.ARGB_8888)
    }
}

/**
 * Shared ColorMatrix builder used identically in Compose Canvas preview and Android Bitmap export.
 */
object PhotoColorMatrixBuilder {
    fun buildAndroidColorMatrix(adj: PhotoAdjustments): ColorMatrix {
        val matrix = ColorMatrix()

        // Preset filter base (12 curated filters)
        when (adj.filter) {
            PhotoFilter.ORIGINAL -> Unit
            PhotoFilter.BLOSSOM -> {
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            1.08f, 0f, 0f, 0f, 10f,
                            0f, 1.00f, 0f, 0f, 4f,
                            0f, 0f, 1.06f, 0f, 12f,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.WARM -> {
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            1.12f, 0f, 0f, 0f, 8f,
                            0f, 1.04f, 0f, 0f, 2f,
                            0f, 0f, 0.90f, 0f, -6f,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.CRISP -> {
                val c = 1.18f
                val t = (1f - c) * 128f
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            c, 0f, 0f, 0f, t,
                            0f, c, 0f, 0f, t,
                            0f, 0f, c, 0f, t,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.FILM -> {
                matrix.postConcat(ColorMatrix().apply { setSaturation(0.78f) })
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            1.04f, 0f, 0f, 0f, 12f,
                            0f, 1.02f, 0f, 0f, 8f,
                            0f, 0f, 0.94f, 0f, 6f,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.MONO -> {
                matrix.postConcat(ColorMatrix().apply { setSaturation(0f) })
            }
            PhotoFilter.VIVID -> {
                matrix.postConcat(ColorMatrix().apply { setSaturation(1.38f) })
                val c = 1.08f
                val t = (1f - c) * 128f + 4f
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            c, 0f, 0f, 0f, t,
                            0f, c, 0f, 0f, t,
                            0f, 0f, c, 0f, t,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.COOL -> {
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            0.92f, 0f, 0f, 0f, -4f,
                            0f, 1.02f, 0f, 0f, 2f,
                            0f, 0f, 1.15f, 0f, 12f,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.SEPIA -> {
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            0.393f, 0.769f, 0.189f, 0f, 0f,
                            0.349f, 0.686f, 0.168f, 0f, 0f,
                            0.272f, 0.534f, 0.131f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.SUNSET -> {
                matrix.postConcat(ColorMatrix().apply { setSaturation(1.18f) })
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            1.18f, 0f, 0f, 0f, 14f,
                            0f, 0.98f, 0f, 0f, 4f,
                            0f, 0f, 0.88f, 0f, 6f,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.MATTE -> {
                matrix.postConcat(ColorMatrix().apply { setSaturation(0.84f) })
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            0.88f, 0f, 0f, 0f, 24f,
                            0f, 0.88f, 0f, 0f, 22f,
                            0f, 0f, 0.90f, 0f, 26f,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
            PhotoFilter.NOIR -> {
                matrix.postConcat(ColorMatrix().apply { setSaturation(0f) })
                val c = 1.32f
                val t = (1f - c) * 128f - 8f
                matrix.postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            c, 0f, 0f, 0f, t,
                            0f, c, 0f, 0f, t,
                            0f, 0f, c, 0f, t,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
        }

        // Combined Saturation + Vibrance
        val sat = (adj.saturation + adj.vibrance * 0.45f).coerceIn(0f, 2.2f)
        if (sat != 1f) {
            matrix.postConcat(ColorMatrix().apply { setSaturation(sat) })
        }

        // Contrast + Brightness + Exposure + Highlights + Shadows
        val hl = adj.highlights.coerceIn(-0.5f, 0.5f)
        val sh = adj.shadows.coerceIn(-0.5f, 0.5f)
        val c = (adj.contrast + hl * 0.18f - sh * 0.14f).coerceIn(0.4f, 1.9f)
        val b = (
            adj.brightness.coerceIn(-0.5f, 0.5f) +
                adj.exposure.coerceIn(-0.5f, 0.5f) * 0.8f +
                sh * 0.22f +
                hl * 0.16f
            ) * 255f
        val t = (1f - c) * 128f + b
        if (c != 1f || b != 0f) {
            matrix.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        c, 0f, 0f, 0f, t,
                        0f, c, 0f, 0f, t,
                        0f, 0f, c, 0f, t,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                ),
            )
        }

        // Warmth (red/blue temperature shift)
        val w = adj.warmth.coerceIn(-0.5f, 0.5f)
        if (w != 0f) {
            matrix.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        1f + w * 0.25f, 0f, 0f, 0f, w * 18f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 1f - w * 0.25f, 0f, -w * 18f,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                ),
            )
        }

        // Tint (green <-> magenta shift)
        val tn = adj.tint.coerceIn(-0.5f, 0.5f)
        if (tn != 0f) {
            matrix.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        1f + tn * 0.14f, 0f, 0f, 0f, tn * 10f,
                        0f, 1f - tn * 0.20f, 0f, 0f, -tn * 14f,
                        0f, 0f, 1f + tn * 0.14f, 0f, tn * 10f,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                ),
            )
        }

        // Fade (lifted blacks matte film look)
        val f = adj.fade.coerceIn(0f, 0.6f)
        if (f > 0.01f) {
            val scale = 1f - f * 0.35f
            val lift = f * 72f
            matrix.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, lift,
                        0f, scale, 0f, 0f, lift,
                        0f, 0f, scale, 0f, lift,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                ),
            )
        }

        return matrix
    }

    fun buildComposeColorMatrix(adj: PhotoAdjustments): androidx.compose.ui.graphics.ColorMatrix {
        return androidx.compose.ui.graphics.ColorMatrix(buildAndroidColorMatrix(adj).array)
    }
}
