package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Genuine high-quality image encoders for CollageX 5.5 Elite:
 * - Uncompressed 24-bit RGB / 32-bit BGRA Studio BMP encoder (API 24+, zero loss, DPI metadata)
 * - Native ISO/IEC 23008-12 HEIC (HEIF/HEVC) still-image encoder using Android MediaCodec + MediaMuxer (API 28+)
 */
object HighQualityImageEncoders {

    /**
     * Writes a genuine uncompressed Windows BMP (24-bit BGR or 32-bit BGRA) with physical DPI metadata.
     */
    fun writeBmp(
        bitmap: Bitmap,
        includeAlpha: Boolean,
        dpi: Int,
        outputStream: OutputStream,
    ) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        writeBmpFromPixels(
            width = width,
            height = height,
            argbPixels = pixels,
            includeAlpha = includeAlpha,
            dpi = dpi,
            outputStream = outputStream,
        )
    }

    /**
     * Pure-Kotlin BMP encoder testable on JVM without Android Bitmap dependencies.
     */
    fun writeBmpFromPixels(
        width: Int,
        height: Int,
        argbPixels: IntArray,
        includeAlpha: Boolean,
        dpi: Int,
        outputStream: OutputStream,
    ) {
        require(width > 0 && height > 0) { "BMP dimensions must be positive" }
        require(argbPixels.size >= width * height) { "Pixel buffer smaller than width * height" }

        val bytesPerPixel = if (includeAlpha) 4 else 3
        val unpaddedRowBytes = width * bytesPerPixel
        val rowPadding = (4 - (unpaddedRowBytes % 4)) % 4
        val rowStride = unpaddedRowBytes + rowPadding
        val pixelDataSize = rowStride.toLong() * height.toLong()
        val headerSize = 54
        val fileSize = (headerSize + pixelDataSize).toInt()
        val pixelsPerMeter = (dpi.coerceIn(36, 1200) * 39.3701).roundToInt()

        val header = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN)
        // BITMAPFILEHEADER (14 bytes)
        header.put('B'.code.toByte())
        header.put('M'.code.toByte())
        header.putInt(fileSize)
        header.putShort(0)
        header.putShort(0)
        header.putInt(headerSize)

        // BITMAPINFOHEADER (40 bytes)
        header.putInt(40)
        header.putInt(width)
        header.putInt(height) // Positive height = standard bottom-up DIB
        header.putShort(1) // Planes
        header.putShort((bytesPerPixel * 8).toShort())
        header.putInt(0) // BI_RGB uncompressed
        header.putInt(pixelDataSize.toInt())
        header.putInt(pixelsPerMeter)
        header.putInt(pixelsPerMeter)
        header.putInt(0) // Colors used
        header.putInt(0) // Important colors

        val out = BufferedOutputStream(outputStream, 65536)
        out.write(header.array())

        val rowBuffer = ByteArray(rowStride)
        for (y in height - 1 downTo 0) {
            val rowOffset = y * width
            var bi = 0
            for (x in 0 until width) {
                val px = argbPixels[rowOffset + x]
                rowBuffer[bi++] = (px and 0xFF).toByte() // Blue
                rowBuffer[bi++] = ((px ushr 8) and 0xFF).toByte() // Green
                rowBuffer[bi++] = ((px ushr 16) and 0xFF).toByte() // Red
                if (includeAlpha) {
                    rowBuffer[bi++] = ((px ushr 24) and 0xFF).toByte() // Alpha
                }
            }
            for (p in 0 until rowPadding) {
                rowBuffer[bi++] = 0
            }
            out.write(rowBuffer)
        }
        out.flush()
    }

    /**
     * Encodes a Bitmap as a genuine ISO/IEC 23008-12 HEIC (.heic) image using Android's
     * native HEVC MediaCodec and HEIF MediaMuxer (API 28+).
     */
    fun writeHeic(
        context: Context,
        bitmap: Bitmap,
        quality: Int,
        outputStream: OutputStream,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            throw IllegalStateException(
                "HEIC encoding requires Android 9 (API 28) or newer. Please choose WebP Lossless, PNG, or JPEG on this device.",
            )
        }
        encodeHeicApi28(context, bitmap, quality.coerceIn(50, 100), outputStream)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun encodeHeicApi28(
        context: Context,
        sourceBitmap: Bitmap,
        quality: Int,
        outputStream: OutputStream,
    ) {
        // HEVC requires even dimensions (multiple of 2; align to 2px cleanly without distorting aspect)
        val encW = ((sourceBitmap.width + 1) and 1.inv()).coerceAtLeast(64)
        val encH = ((sourceBitmap.height + 1) and 1.inv()).coerceAtLeast(64)

        val evenBitmap = if (encW == sourceBitmap.width && encH == sourceBitmap.height) {
            sourceBitmap
        } else {
            Bitmap.createBitmap(encW, encH, Bitmap.Config.ARGB_8888).also { padded ->
                val c = Canvas(padded)
                c.drawColor(Color.WHITE)
                c.drawBitmap(sourceBitmap, 0f, 0f, null)
            }
        }

        val tempFile = File.createTempFile("collagex_heic_", ".heic", context.cacheDir)
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var muxerStarted = false

        try {
            val encoderInfo = findHevcEncoder(encW, encH)
                ?: throw IllegalStateException(
                    "No HEVC/HEIC hardware or software encoder found for ${encW}×${encH}px on this device. Try 4K/2K or PNG/WebP.",
                )

            val caps = encoderInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            val colorFormat = selectYuvColorFormat(caps.colorFormats)

            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, encW, encH).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
                setInteger(MediaFormat.KEY_FRAME_RATE, 1)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0)
                val encCaps = caps.encoderCapabilities
                if (encCaps != null && encCaps.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)) {
                    setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)
                    val qRange = encCaps.qualityRange
                    val scaledQ = qRange.lower + ((qRange.upper - qRange.lower) * (quality / 100.0)).roundToInt()
                    setInteger(MediaFormat.KEY_QUALITY, scaledQ.coerceIn(qRange.lower, qRange.upper))
                } else {
                    val pixels = encW.toLong() * encH.toLong()
                    val bitsPerPixel = 1.6 + (quality / 100.0) * 3.4
                    val targetBitrate = (pixels * bitsPerPixel).toLong().coerceIn(2_000_000L, 40_000_000L).toInt()
                    setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
                }
            }

            codec = MediaCodec.createByCodecName(encoderInfo.name)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_HEIF)

            val argb = IntArray(encW * encH)
            evenBitmap.getPixels(argb, 0, encW, 0, 0, encW, encH)

            var inputQueued = false
            var outputDone = false
            var trackIndex = -1
            val bufferInfo = MediaCodec.BufferInfo()
            var idleSpins = 0

            while (!outputDone && idleSpins < 200) {
                if (!inputQueued) {
                    val inIndex = codec.dequeueInputBuffer(20_000L)
                    if (inIndex >= 0) {
                        val inputImage = runCatching { codec.getInputImage(inIndex) }.getOrNull()
                        if (inputImage != null) {
                            fillImageYuv420(inputImage, argb, encW, encH)
                            val size = (encW * encH * 3) / 2
                            codec.queueInputBuffer(inIndex, 0, size, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        } else {
                            val inBuf = codec.getInputBuffer(inIndex)
                                ?: throw IllegalStateException("HEIC encoder input buffer unavailable")
                            val written = fillByteBufferYuv420(inBuf, argb, encW, encH, colorFormat)
                            codec.queueInputBuffer(inIndex, 0, written, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        }
                        inputQueued = true
                    }
                }

                val outIndex = codec.dequeueOutputBuffer(bufferInfo, 25_000L)
                when {
                    outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        idleSpins++
                    }
                    outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outFormat = codec.outputFormat
                        // Mark image grid metadata for HEIF muxer compatibility
                        outFormat.setInteger(MediaFormat.KEY_WIDTH, encW)
                        outFormat.setInteger(MediaFormat.KEY_HEIGHT, encH)
                        trackIndex = muxer.addTrack(outFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                    outIndex >= 0 -> {
                        idleSpins = 0
                        val outBuf = codec.getOutputBuffer(outIndex)
                        if (outBuf != null && bufferInfo.size > 0 &&
                            (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0
                        ) {
                            if (!muxerStarted) {
                                val outFormat = codec.outputFormat
                                outFormat.setInteger(MediaFormat.KEY_WIDTH, encW)
                                outFormat.setInteger(MediaFormat.KEY_HEIGHT, encH)
                                trackIndex = muxer.addTrack(outFormat)
                                muxer.start()
                                muxerStarted = true
                            }
                            outBuf.position(bufferInfo.offset)
                            outBuf.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(trackIndex, outBuf, bufferInfo)
                        }
                        codec.releaseOutputBuffer(outIndex, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            outputDone = true
                        }
                    }
                }
            }

            if (!muxerStarted) {
                throw IllegalStateException("HEIC encoder produced no image frames at ${encW}×${encH}px.")
            }

            muxer.stop()
            muxer.release()
            muxer = null
            muxerStarted = false

            codec.stop()
            codec.release()
            codec = null

            if (!tempFile.exists() || tempFile.length() < 64L) {
                throw IllegalStateException("HEIC output file was empty. Try a lower resolution or PNG/WebP.")
            }

            FileInputStream(tempFile).use { input ->
                val buf = ByteArray(65536)
                while (true) {
                    val read = input.read(buf)
                    if (read <= 0) break
                    outputStream.write(buf, 0, read)
                }
            }
            outputStream.flush()
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            if (muxerStarted) {
                runCatching { muxer?.stop() }
            }
            runCatching { muxer?.release() }
            if (evenBitmap !== sourceBitmap && !evenBitmap.isRecycled) {
                evenBitmap.recycle()
            }
            runCatching { tempFile.delete() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun findHevcEncoder(width: Int, height: Int): MediaCodecInfo? {
        val list = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
        val candidates = list.filter { info ->
            info.isEncoder && info.supportedTypes.any { it.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true) }
        }
        // Prefer an encoder whose video capabilities explicitly support the target dimensions
        val matchingSize = candidates.filter { info ->
            runCatching {
                val vCaps = info.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_HEVC).videoCapabilities
                vCaps != null && vCaps.isSizeSupported(width, height)
            }.getOrDefault(false)
        }
        val pool = if (matchingSize.isNotEmpty()) matchingSize else candidates
        // Prefer Android's predictable software/hardware codecs that support YUV420Flexible
        return pool.firstOrNull { info ->
            runCatching {
                val fmts = info.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_HEVC).colorFormats
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible in fmts
            }.getOrDefault(false)
        } ?: pool.firstOrNull()
    }

    @Suppress("DEPRECATION")
    private fun selectYuvColorFormat(supported: IntArray): Int {
        return when {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible in supported ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar in supported ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar in supported ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
            else -> MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        }
    }

    /**
     * Converts ARGB pixels to BT.601 YUV420 directly into an Android MediaCodec input Image,
     * respecting each plane's exact rowStride and pixelStride.
     */
    private fun fillImageYuv420(image: Image, argb: IntArray, width: Int, height: Int) {
        val planes = image.planes
        val yBuf = planes[0].buffer
        val uBuf = planes[1].buffer
        val vBuf = planes[2].buffer
        val yRowStride = planes[0].rowStride
        val yPixelStride = planes[0].pixelStride
        val uRowStride = planes[1].rowStride
        val uPixelStride = planes[1].pixelStride
        val vRowStride = planes[2].rowStride
        val vPixelStride = planes[2].pixelStride

        for (y in 0 until height) {
            val rowStart = y * width
            val yPlaneRow = y * yRowStride
            val uvRow = (y shr 1)
            val uPlaneRow = uvRow * uRowStride
            val vPlaneRow = uvRow * vRowStride
            for (x in 0 until width) {
                val px = argb[rowStart + x]
                val r = (px ushr 16) and 0xFF
                val g = (px ushr 8) and 0xFF
                val b = px and 0xFF

                val yVal = (((66 * r + 129 * g + 25 * b + 128) shr 8) + 16).coerceIn(0, 255)
                val yPos = yPlaneRow + x * yPixelStride
                if (yPos < yBuf.limit()) yBuf.put(yPos, yVal.toByte())

                if ((y and 1) == 0 && (x and 1) == 0) {
                    val uVal = (((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128).coerceIn(0, 255)
                    val vVal = (((112 * r - 94 * g - 18 * b + 128) shr 8) + 128).coerceIn(0, 255)
                    val uvCol = x shr 1
                    val uPos = uPlaneRow + uvCol * uPixelStride
                    val vPos = vPlaneRow + uvCol * vPixelStride
                    if (uPos < uBuf.limit()) uBuf.put(uPos, uVal.toByte())
                    if (vPos < vBuf.limit()) vBuf.put(vPos, vVal.toByte())
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun fillByteBufferYuv420(
        buffer: ByteBuffer,
        argb: IntArray,
        width: Int,
        height: Int,
        colorFormat: Int,
    ): Int {
        buffer.clear()
        val frameSize = width * height
        val semiPlanar = colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        val out = ByteArray((frameSize * 3) / 2)
        var yIdx = 0
        var uvIdx = frameSize
        var uIdx = frameSize
        var vIdx = frameSize + frameSize / 4

        for (y in 0 until height) {
            val rowStart = y * width
            for (x in 0 until width) {
                val px = argb[rowStart + x]
                val r = (px ushr 16) and 0xFF
                val g = (px ushr 8) and 0xFF
                val b = px and 0xFF

                out[yIdx++] = (((66 * r + 129 * g + 25 * b + 128) shr 8) + 16).coerceIn(0, 255).toByte()
                if ((y and 1) == 0 && (x and 1) == 0) {
                    val u = (((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128).coerceIn(0, 255).toByte()
                    val v = (((112 * r - 94 * g - 18 * b + 128) shr 8) + 128).coerceIn(0, 255).toByte()
                    if (semiPlanar) {
                        out[uvIdx++] = u
                        out[uvIdx++] = v
                    } else {
                        out[uIdx++] = u
                        out[vIdx++] = v
                    }
                }
            }
        }
        val toWrite = max(0, minOf(out.size, buffer.remaining()))
        buffer.put(out, 0, toWrite)
        return toWrite
    }
}
