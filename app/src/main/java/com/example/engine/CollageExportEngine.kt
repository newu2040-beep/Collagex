package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.core.designsystem.SampleArtwork
import com.example.core.model.BackgroundType
import com.example.core.model.CanvasMath
import com.example.core.model.CollageLayer
import com.example.core.model.CollageProject
import com.example.core.model.ExportFormat
import com.example.core.model.ExportJobConfig
import com.example.core.model.LayerType
import com.example.core.model.PhotoFitMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ExportOutputResult(
    val uriString: String,
    val displayName: String,
    val widthPx: Int,
    val heightPx: Int,
    val fileSizeBytes: Long,
    val mimeType: String,
)

/**
 * Off-main-thread CollageX rendering and export pipeline.
 * - Supports JPEG, PNG, WebP, TIFF (genuine baseline strip-streamed encoder), and PDF.
 * - Lazily decodes each photo at exact cell resolution and recycles promptly so 50+ photo
 *   and 4K/8K outputs stay within device memory limits.
 * - Zero watermarks, zero locked formats, EXIF-free clean output.
 */
class CollageExportEngine(private val context: Context) {

    suspend fun exportProject(
        project: CollageProject,
        config: ExportJobConfig,
        customTargetUri: Uri? = null,
        onProgress: (Float, String) -> Unit,
    ): ExportOutputResult = withContext(Dispatchers.Default) {
        val targetDims = CanvasMath.computeExportDimensions(project.canvas, config)
        val outW = targetDims.widthPx
        val outH = targetDims.heightPx
        val safeTitle = project.title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "CollageX" }.take(40)
        val fileName = "${safeTitle}_${outW}x${outH}.${config.format.extension}"

        onProgress(0.05f, "Preparing ${outW} × ${outH}px canvas…")
        coroutineContext.ensureActive()

        // TIFF supports genuine strip-by-strip streaming for huge 4K/8K exports without allocating full RGBA bitmap
        if (config.format == ExportFormat.TIFF) {
            return@withContext exportTiffStreamed(
                project = project,
                config = config,
                outW = outW,
                outH = outH,
                fileName = fileName,
                customTargetUri = customTargetUri,
                onProgress = onProgress,
            )
        }

        // PDF renders onto native PdfDocument page sized accurately from DPI
        if (config.format == ExportFormat.PDF) {
            return@withContext exportPdf(
                project = project,
                config = config,
                outW = outW,
                outH = outH,
                fileName = fileName,
                customTargetUri = customTargetUri,
                onProgress = onProgress,
            )
        }

        // Check memory safety before allocating raster bitmap for JPEG/PNG/WebP
        val maxRuntimeMem = Runtime.getRuntime().maxMemory()
        val neededBytes = outW.toLong() * outH.toLong() * 4L
        if (neededBytes > (maxRuntimeMem * 0.55).toLong()) {
            throw IllegalStateException(
                "Requested ${outW}×${outH}px exceeds safe raster RAM on this device. " +
                    "Use TIFF (strip-streamed 8K) or choose 4K/2K for ${config.format.label}.",
            )
        }

        val outputBitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(outputBitmap)
            renderFullScene(
                canvas = canvas,
                project = project,
                fullWidth = outW,
                fullHeight = outH,
                transparentBg = config.transparentBg && config.format.supportsAlpha,
                stripTop = 0,
                stripHeight = outH,
                onProgress = { frac, msg -> onProgress(0.08f + frac * 0.72f, msg) },
            )

            coroutineContext.ensureActive()
            onProgress(0.84f, "Encoding ${config.format.label}…")

            val (savedUri, fileSize) = writeToDestination(
                fileName = fileName,
                mimeType = config.format.mimeType,
                customTargetUri = customTargetUri,
            ) { outStream ->
                when (config.format) {
                    ExportFormat.HEIC -> {
                        HighQualityImageEncoders.writeHeic(
                            context = context,
                            bitmap = outputBitmap,
                            quality = config.quality,
                            outputStream = outStream,
                        )
                    }
                    ExportFormat.BMP -> {
                        HighQualityImageEncoders.writeBmp(
                            bitmap = outputBitmap,
                            includeAlpha = config.transparentBg,
                            dpi = project.canvas.dpi,
                            outputStream = outStream,
                        )
                    }
                    ExportFormat.WEBP_LOSSLESS -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            outputBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outStream)
                        } else {
                            @Suppress("DEPRECATION")
                            outputBitmap.compress(Bitmap.CompressFormat.WEBP, 100, outStream)
                        }
                    }
                    ExportFormat.JPEG -> {
                        outputBitmap.compress(Bitmap.CompressFormat.JPEG, config.quality.coerceIn(10, 100), outStream)
                    }
                    ExportFormat.PNG -> {
                        outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                    }
                    ExportFormat.WEBP -> {
                        val fmt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (config.quality >= 100) Bitmap.CompressFormat.WEBP_LOSSLESS
                            else Bitmap.CompressFormat.WEBP_LOSSY
                        } else {
                            @Suppress("DEPRECATION")
                            Bitmap.CompressFormat.WEBP
                        }
                        outputBitmap.compress(fmt, config.quality.coerceIn(10, 100), outStream)
                    }
                    else -> {
                        outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                    }
                }
            }

            onProgress(1f, "Saved $fileName")
            ExportOutputResult(
                uriString = savedUri.toString(),
                displayName = fileName,
                widthPx = outW,
                heightPx = outH,
                fileSizeBytes = fileSize,
                mimeType = config.format.mimeType,
            )
        } finally {
            if (!outputBitmap.isRecycled) outputBitmap.recycle()
        }
    }

    private suspend fun exportPdf(
        project: CollageProject,
        config: ExportJobConfig,
        outW: Int,
        outH: Int,
        fileName: String,
        customTargetUri: Uri?,
        onProgress: (Float, String) -> Unit,
    ): ExportOutputResult {
        val dpi = project.canvas.dpi.coerceIn(72, 600)
        val pageWidthPt = ((outW.toDouble() / dpi) * 72.0).roundToInt().coerceIn(72, 2400)
        val pageHeightPt = ((outH.toDouble() / dpi) * 72.0).roundToInt().coerceIn(72, 2400)
        val doc = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas
            renderFullScene(
                canvas = canvas,
                project = project,
                fullWidth = pageWidthPt,
                fullHeight = pageHeightPt,
                transparentBg = false,
                stripTop = 0,
                stripHeight = pageHeightPt,
                onProgress = { frac, msg -> onProgress(0.1f + frac * 0.72f, msg) },
            )
            doc.finishPage(page)

            coroutineContext.ensureActive()
            onProgress(0.86f, "Writing PDF document…")
            val (savedUri, fileSize) = writeToDestination(
                fileName = fileName,
                mimeType = ExportFormat.PDF.mimeType,
                customTargetUri = customTargetUri,
            ) { outStream ->
                doc.writeTo(outStream)
            }
            onProgress(1f, "Saved $fileName")
            return ExportOutputResult(
                uriString = savedUri.toString(),
                displayName = fileName,
                widthPx = outW,
                heightPx = outH,
                fileSizeBytes = fileSize,
                mimeType = ExportFormat.PDF.mimeType,
            )
        } finally {
            doc.close()
        }
    }

    /**
     * Genuine uncompressed Baseline RGB/RGBA TIFF writer using horizontal strips
     * so even 4K/8K outputs never allocate a full-frame RGBA bitmap in memory.
     */
    private suspend fun exportTiffStreamed(
        project: CollageProject,
        config: ExportJobConfig,
        outW: Int,
        outH: Int,
        fileName: String,
        customTargetUri: Uri?,
        onProgress: (Float, String) -> Unit,
    ): ExportOutputResult {
        val includeAlpha = config.transparentBg
        val samplesPerPixel = if (includeAlpha) 4 else 3
        val stripRows = 256.coerceAtMost(outH)
        val pixelDataBytes = outW.toLong() * outH.toLong() * samplesPerPixel.toLong()
        if (pixelDataBytes > 400_000_000L) {
            throw IllegalStateException("TIFF file size exceeds 400MB storage safety guard. Reduce target dimensions.")
        }

        val (savedUri, fileSize) = writeToDestination(
            fileName = fileName,
            mimeType = ExportFormat.TIFF.mimeType,
            customTargetUri = customTargetUri,
        ) { rawOut ->
            val out = BufferedOutputStream(rawOut, 65536)
            val numEntries = if (includeAlpha) 12 else 11
            val ifdOffset = 8
            val ifdSize = 2 + numEntries * 12 + 4
            val extraOffset = ifdOffset + ifdSize
            // Extra data: BitsPerSample (6 or 8 bytes) + XRes (8) + YRes (8)
            val bpsBytes = samplesPerPixel * 2
            val xResOffset = extraOffset + bpsBytes
            val yResOffset = xResOffset + 8
            val pixelDataOffset = yResOffset + 8

            val header = ByteBuffer.allocate(pixelDataOffset).order(ByteOrder.LITTLE_ENDIAN)
            // TIFF Little-Endian magic "II", 42, IFD offset
            header.put('I'.code.toByte())
            header.put('I'.code.toByte())
            header.putShort(42)
            header.putInt(ifdOffset)

            // IFD entry count
            header.putShort(numEntries.toShort())

            fun putEntry(tag: Int, type: Int, count: Int, valueOrOffset: Int) {
                header.putShort(tag.toShort())
                header.putShort(type.toShort())
                header.putInt(count)
                header.putInt(valueOrOffset)
            }

            // Tags MUST be in ascending numerical order
            putEntry(256, 4, 1, outW) // ImageWidth
            putEntry(257, 4, 1, outH) // ImageLength
            putEntry(258, 3, samplesPerPixel, extraOffset) // BitsPerSample
            putEntry(259, 3, 1, 1) // Compression = 1 (None)
            putEntry(262, 3, 1, 2) // PhotometricInterpretation = 2 (RGB)
            putEntry(273, 4, 1, pixelDataOffset) // StripOffsets
            putEntry(277, 3, 1, samplesPerPixel) // SamplesPerPixel
            putEntry(278, 4, 1, outH) // RowsPerStrip
            putEntry(279, 4, 1, pixelDataBytes.toInt()) // StripByteCounts
            putEntry(282, 5, 1, xResOffset) // XResolution
            putEntry(283, 5, 1, yResOffset) // YResolution
            if (includeAlpha) {
                putEntry(338, 3, 1, 2) // ExtraSamples = Unassociated Alpha
            }
            header.putInt(0) // Next IFD offset = 0

            // BitsPerSample shorts
            repeat(samplesPerPixel) { header.putShort(8) }
            // XResolution & YResolution rationals (dpi / 1)
            val dpi = project.canvas.dpi.coerceIn(36, 1200)
            header.putInt(dpi)
            header.putInt(1)
            header.putInt(dpi)
            header.putInt(1)

            out.write(header.array())

            // Stream horizontal strips one at a time
            var y = 0
            val stripBitmap = Bitmap.createBitmap(outW, stripRows, Bitmap.Config.ARGB_8888)
            val rowPixels = IntArray(outW)
            val rowBytes = ByteArray(outW * samplesPerPixel)
            try {
                while (y < outH) {
                    coroutineContext.ensureActive()
                    val currentH = min(stripRows, outH - y)
                    stripBitmap.eraseColor(Color.TRANSPARENT)
                    val stripCanvas = Canvas(stripBitmap)
                    stripCanvas.save()
                    stripCanvas.translate(0f, -y.toFloat())
                    renderFullScene(
                        canvas = stripCanvas,
                        project = project,
                        fullWidth = outW,
                        fullHeight = outH,
                        transparentBg = includeAlpha,
                        stripTop = y,
                        stripHeight = currentH,
                        onProgress = { _, _ -> },
                    )
                    stripCanvas.restore()

                    for (row in 0 until currentH) {
                        stripBitmap.getPixels(rowPixels, 0, outW, 0, row, outW, 1)
                        var bi = 0
                        for (px in rowPixels) {
                            rowBytes[bi++] = ((px ushr 16) and 0xFF).toByte()
                            rowBytes[bi++] = ((px ushr 8) and 0xFF).toByte()
                            rowBytes[bi++] = (px and 0xFF).toByte()
                            if (includeAlpha) {
                                rowBytes[bi++] = ((px ushr 24) and 0xFF).toByte()
                            }
                        }
                        out.write(rowBytes)
                    }
                    y += currentH
                    val pct = y.toFloat() / outH.toFloat()
                    onProgress(0.10f + pct * 0.88f, "Rendering strip ${(pct * 100).roundToInt()}%…")
                }
            } finally {
                if (!stripBitmap.isRecycled) stripBitmap.recycle()
            }
            out.flush()
        }

        onProgress(1f, "Saved $fileName")
        return ExportOutputResult(
            uriString = savedUri.toString(),
            displayName = fileName,
            widthPx = outW,
            heightPx = outH,
            fileSizeBytes = fileSize,
            mimeType = ExportFormat.TIFF.mimeType,
        )
    }

    private suspend fun renderFullScene(
        canvas: Canvas,
        project: CollageProject,
        fullWidth: Int,
        fullHeight: Int,
        transparentBg: Boolean,
        stripTop: Int,
        stripHeight: Int,
        onProgress: (Float, String) -> Unit,
    ) {
        val w = fullWidth.toFloat()
        val h = fullHeight.toFloat()
        val stripBottom = (stripTop + stripHeight).toFloat()

        // 1. Background
        if (!transparentBg) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            when (project.background.type) {
                BackgroundType.SOLID -> {
                    bgPaint.color = project.background.primaryColor
                    canvas.drawRect(0f, stripTop.toFloat(), w, stripBottom, bgPaint)
                }
                BackgroundType.GRADIENT -> {
                    bgPaint.shader = LinearGradient(
                        0f,
                        0f,
                        w,
                        h,
                        project.background.primaryColor,
                        project.background.secondaryColor,
                        Shader.TileMode.CLAMP,
                    )
                    canvas.drawRect(0f, stripTop.toFloat(), w, stripBottom, bgPaint)
                }
                BackgroundType.IMAGE -> {
                    bgPaint.color = project.background.primaryColor
                    canvas.drawRect(0f, stripTop.toFloat(), w, stripBottom, bgPaint)
                    val uriStr = project.background.imageUri
                    if (!uriStr.isNullOrBlank()) {
                        val bgBmp = BitmapProxyCache.decodeSampledBitmap(
                            context,
                            Uri.parse(uriStr),
                            max(fullWidth, fullHeight).coerceAtMost(2048),
                        )
                        if (bgBmp != null) {
                            drawBitmapInRect(canvas, bgBmp, RectF(0f, 0f, w, h), PhotoFitMode.COVER, Paint(Paint.FILTER_BITMAP_FLAG))
                            bgBmp.recycle()
                        }
                    }
                }
            }
        }

        // 2. Photo cells
        val minDim = min(w, h)
        val padFrac = (project.border.padding / 400f).coerceIn(0f, 0.14f)
        val gapFrac = (project.border.spacing / 400f).coerceIn(0f, 0.06f)
        val cells = LayoutEngine.computePhotoCells(
            layers = project.layers,
            layoutType = project.layoutType,
            canvasAspectRatio = if (h > 0f) w / h else 1f,
            paddingFraction = padFrac,
            spacingFraction = gapFrac,
        )

        val totalSteps = (cells.size + 1).coerceAtLeast(1)
        cells.forEachIndexed { idx, cell ->
            coroutineContext.ensureActive()
            val rect = RectF(
                cell.rect.left * w,
                cell.rect.top * h,
                cell.rect.right * w,
                cell.rect.bottom * h,
            )
            if (rect.bottom < stripTop || rect.top > stripBottom) return@forEachIndexed

            val radiusPx = ((cell.layer.cornerRadiusOverride ?: project.border.cornerRadius) / 360f) * minDim
            val clipPath = Path().apply { addRoundRect(rect, radiusPx, radiusPx, Path.Direction.CW) }

            if (project.border.shadowEnabled) {
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(44, 24, 16, 32)
                }
                val shadowRect = RectF(rect).apply { offset(0f, minDim * 0.008f) }
                canvas.drawRoundRect(shadowRect, radiusPx, radiusPx, shadowPaint)
            }

            canvas.save()
            canvas.clipPath(clipPath)

            val cx = rect.centerX()
            val cy = rect.centerY()
            canvas.translate(cx + cell.layer.panX * rect.width(), cy + cell.layer.panY * rect.height())
            canvas.rotate(cell.layer.rotationDeg)
            canvas.scale(
                cell.layer.scale * (if (cell.layer.flipHorizontal) -1f else 1f),
                cell.layer.scale * (if (cell.layer.flipVertical) -1f else 1f),
            )
            canvas.translate(-cx, -cy)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                alpha = (cell.layer.opacity.coerceIn(0f, 1f) * 255).roundToInt()
                colorFilter = ColorMatrixColorFilter(
                    PhotoColorMatrixBuilder.buildAndroidColorMatrix(cell.layer.adjustments),
                )
            }

            val uriStr = cell.layer.localUri
            if (!uriStr.isNullOrBlank()) {
                val targetCellEdge = max(rect.width(), rect.height()).roundToInt().coerceIn(256, 2048)
                val bmp = BitmapProxyCache.decodeSampledBitmap(context, Uri.parse(uriStr), targetCellEdge)
                if (bmp != null) {
                    val renderBmp = if (cell.layer.adjustments.sharpen > 0.02f) {
                        BitmapProxyCache.applySharpen(bmp, cell.layer.adjustments.sharpen)
                    } else {
                        bmp
                    }
                    drawBitmapInRect(canvas, renderBmp, rect, cell.layer.fitMode, paint)
                    if (renderBmp !== bmp && !renderBmp.isRecycled) renderBmp.recycle()
                    if (!bmp.isRecycled) bmp.recycle()
                } else {
                    SampleArtwork.drawScenicAndroid(canvas, rect, SampleArtwork.PAGODA)
                }
            } else {
                SampleArtwork.drawScenicAndroid(canvas, rect, cell.layer.sampleArtKey ?: SampleArtwork.PAGODA)
            }
            canvas.restore()

            // Radial vignette overlay inside clipped cell
            if (cell.layer.adjustments.vignette > 0.02f) {
                canvas.save()
                canvas.clipPath(clipPath)
                val vigAlpha = (cell.layer.adjustments.vignette * 0.78f * 255f).roundToInt().coerceIn(0, 220)
                val vigRadius = max(rect.width(), rect.height()) * 0.76f
                val vigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        rect.centerX(),
                        rect.centerY(),
                        vigRadius.coerceAtLeast(1f),
                        intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.argb(vigAlpha, 0, 0, 0)),
                        floatArrayOf(0f, 0.38f, 1f),
                        Shader.TileMode.CLAMP,
                    )
                }
                canvas.drawRect(rect, vigPaint)
                canvas.restore()
            }

            // Cell border stroke if visible
            if (project.border.color != project.background.primaryColor && project.border.spacing > 0f) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = project.border.color
                    strokeWidth = (project.border.spacing / 360f) * minDim * 0.35f
                }
                canvas.drawRoundRect(rect, radiusPx, radiusPx, strokePaint)
            }

            onProgress((idx + 1).toFloat() / totalSteps.toFloat(), "Compositing photo ${idx + 1} of ${cells.size}…")
        }

        // 3. Text & Sticker overlays in z-order
        val overlays = project.layers
            .filter { it.type != LayerType.PHOTO && it.visible }
            .sortedBy { it.zIndex }

        overlays.forEach { layer ->
            val cx = layer.normX * w
            val cy = layer.normY * h
            canvas.save()
            canvas.translate(cx, cy)
            canvas.rotate(layer.rotationDeg)
            canvas.scale(layer.scale, layer.scale)

            when (layer.type) {
                LayerType.TEXT -> drawTextLayer(canvas, layer, minDim)
                LayerType.STICKER -> drawStickerLayer(canvas, layer, minDim)
                LayerType.PHOTO -> Unit
            }
            canvas.restore()
        }
    }

    private fun drawTextLayer(canvas: Canvas, layer: CollageLayer, minDim: Float) {
        val style = layer.textStyle ?: return
        val textSizePx = (style.fontSizeSp / 360f) * minDim * 1.4f
        val baseTypeface = resolveExportTypeface(style.fontId)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.color
            textSize = textSizePx
            typeface = Typeface.create(
                baseTypeface,
                if (style.bold) Typeface.BOLD else Typeface.NORMAL,
            )
            textAlign = Paint.Align.CENTER
            alpha = (layer.opacity.coerceIn(0f, 1f) * 255).roundToInt()
        }
        val textWidth = textPaint.measureText(style.text)
        val fm = textPaint.fontMetrics
        val textHeight = fm.descent - fm.ascent
        val padX = textSizePx * 0.55f
        val padY = textSizePx * 0.32f
        val bgRect = RectF(
            -textWidth / 2f - padX,
            -textHeight / 2f - padY,
            textWidth / 2f + padX,
            textHeight / 2f + padY,
        )
        style.chipColor?.let { chip ->
            val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = chip }
            canvas.drawRoundRect(bgRect, bgRect.height() / 2f, bgRect.height() / 2f, chipPaint)
        }
        val baseline = -(fm.ascent + fm.descent) / 2f
        canvas.drawText(style.text, 0f, baseline, textPaint)
    }

    private fun resolveExportTypeface(fontId: String): Typeface {
        val fontRes = when (fontId) {
            "PLUS_JAKARTA", "SANS" -> R.font.plus_jakarta_sans
            "POPPINS" -> R.font.poppins
            "MONTSERRAT" -> R.font.montserrat
            "SERIF" -> R.font.playfair_display
            "LORA" -> R.font.lora
            "SCRIPT" -> R.font.pacifico
            "DANCING" -> R.font.dancing_script
            "GEOMETRIC" -> R.font.space_grotesk
            "DISPLAY" -> R.font.bebas_neue
            "MONO" -> R.font.jetbrains_mono
            else -> null
        }
        if (fontRes != null) {
            try {
                ResourcesCompat.getFont(context, fontRes)?.let { return it }
            } catch (_: Throwable) {
                // Fall back to system typeface below
            }
        }
        return when (fontId) {
            "SYS_SERIF", "SERIF", "LORA" -> Typeface.SERIF
            "SYS_MONO", "MONO" -> Typeface.MONOSPACE
            "SYS_CURSIVE", "SCRIPT", "DANCING" -> Typeface.create("cursive", Typeface.NORMAL)
            else -> Typeface.SANS_SERIF
        }
    }

    private fun drawStickerLayer(canvas: Canvas, layer: CollageLayer, minDim: Float) {
        val kind = layer.stickerKind ?: return
        val sizePx = minDim * 0.13f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.stickerColor
            textSize = sizePx
            textAlign = Paint.Align.CENTER
            alpha = (layer.opacity.coerceIn(0f, 1f) * 255).roundToInt()
        }
        val fm = paint.fontMetrics
        val baseline = -(fm.ascent + fm.descent) / 2f
        canvas.drawText(kind.glyph, 0f, baseline, paint)
    }

    private fun drawBitmapInRect(
        canvas: Canvas,
        bmp: Bitmap,
        dest: RectF,
        fitMode: PhotoFitMode,
        paint: Paint,
    ) {
        val bw = bmp.width.toFloat()
        val bh = bmp.height.toFloat()
        val dw = dest.width()
        val dh = dest.height()
        if (bw <= 0f || bh <= 0f || dw <= 0f || dh <= 0f) return

        val srcAspect = bw / bh
        val dstAspect = dw / dh

        if (fitMode == PhotoFitMode.COVER) {
            val srcRect = if (srcAspect > dstAspect) {
                val cropW = bh * dstAspect
                val left = ((bw - cropW) / 2f).roundToInt()
                Rect(left, 0, (left + cropW).roundToInt().coerceAtMost(bmp.width), bmp.height)
            } else {
                val cropH = bw / dstAspect
                val top = ((bh - cropH) / 2f).roundToInt()
                Rect(0, top, bmp.width, (top + cropH).roundToInt().coerceAtMost(bmp.height))
            }
            canvas.drawBitmap(bmp, srcRect, dest, paint)
        } else {
            val scale = min(dw / bw, dh / bh)
            val rw = bw * scale
            val rh = bh * scale
            val outRect = RectF(
                dest.centerX() - rw / 2f,
                dest.centerY() - rh / 2f,
                dest.centerX() + rw / 2f,
                dest.centerY() + rh / 2f,
            )
            canvas.drawBitmap(bmp, null, outRect, paint)
        }
    }

    private suspend fun writeToDestination(
        fileName: String,
        mimeType: String,
        customTargetUri: Uri?,
        writer: suspend (OutputStream) -> Unit,
    ): Pair<Uri, Long> = withContext(Dispatchers.IO) {
        if (customTargetUri != null) {
            context.contentResolver.openOutputStream(customTargetUri)?.use { stream ->
                writer(stream)
            } ?: throw IllegalStateException("Could not open selected destination file.")
            val size = context.contentResolver.openFileDescriptor(customTargetUri, "r")?.use { it.statSize } ?: 0L
            return@withContext customTargetUri to size
        }

        // MediaStore save for image types on Android 10+ (zero storage permissions!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mimeType.startsWith("image/")) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/CollageX")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    resolver.openOutputStream(uri)?.use { stream ->
                        writer(stream)
                    } ?: throw IllegalStateException("Failed to write to MediaStore.")
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    val size = resolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                    return@withContext uri to size
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    throw e
                }
            }
        }

        // App-private FileProvider fallback for PDF or Android 7-9
        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { stream ->
            writer(stream)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        uri to file.length()
    }
}
