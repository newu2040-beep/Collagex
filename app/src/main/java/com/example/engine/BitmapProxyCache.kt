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
}

/**
 * Shared ColorMatrix builder used identically in Compose Canvas preview and Android Bitmap export.
 */
object PhotoColorMatrixBuilder {
    fun buildAndroidColorMatrix(adj: PhotoAdjustments): ColorMatrix {
        val matrix = ColorMatrix()

        // Preset filter base
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
                val sat = ColorMatrix().apply { setSaturation(0.78f) }
                matrix.postConcat(sat)
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
        }

        // Saturation
        if (adj.saturation != 1f) {
            matrix.postConcat(ColorMatrix().apply { setSaturation(adj.saturation.coerceIn(0f, 2f)) })
        }

        // Contrast + brightness + exposure
        val c = adj.contrast.coerceIn(0.4f, 1.8f)
        val b = (adj.brightness.coerceIn(-0.5f, 0.5f) + adj.exposure.coerceIn(-0.5f, 0.5f) * 0.8f) * 255f
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

        // Warmth
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

        return matrix
    }

    fun buildComposeColorMatrix(adj: PhotoAdjustments): androidx.compose.ui.graphics.ColorMatrix {
        return androidx.compose.ui.graphics.ColorMatrix(buildAndroidColorMatrix(adj).array)
    }
}
