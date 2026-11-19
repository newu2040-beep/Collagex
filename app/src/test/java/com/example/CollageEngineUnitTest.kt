package com.example

import com.example.core.i18n.SupportedAppLanguages
import com.example.core.i18n.stringsForLanguage
import com.example.core.model.CanvasMath
import com.example.core.model.CanvasOrientation
import com.example.core.model.CanvasSpec
import com.example.core.model.CollageLayer
import com.example.core.model.CollageProject
import com.example.core.model.DarkModeOption
import com.example.core.model.DimensionUnit
import com.example.core.model.ExportFormat
import com.example.core.model.ExportJobConfig
import com.example.core.model.ExportResolutionPreset
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import com.example.core.model.PhotoFitMode
import com.example.core.model.ResizeMode
import com.example.engine.AutoFitEngine
import com.example.engine.AutoFitPreset
import com.example.engine.HighQualityImageEncoders
import com.example.engine.LayoutEngine
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollageEngineUnitTest {

    @Test
    fun unitAndDpiConversionsAreAccurate() {
        // 10 inches at 300 DPI = 3000 px
        assertEquals(3000L, CanvasMath.toPixels(10.0, DimensionUnit.IN, 300))
        // A4 (210 x 297 mm) at 300 DPI = 2480 x 3508 px
        assertEquals(2480L, CanvasMath.toPixels(210.0, DimensionUnit.MM, 300))
        assertEquals(3508L, CanvasMath.toPixels(297.0, DimensionUnit.MM, 300))
        // Round-trip cm -> px -> cm
        val px = CanvasMath.toPixels(25.4, DimensionUnit.CM, 300)
        assertEquals(3000L, px)
        val backCm = CanvasMath.fromPixels(px.toDouble(), DimensionUnit.CM, 300)
        assertEquals(25.4, backCm, 0.01)
    }

    @Test
    fun canvasValidationEnforcesSafeLimits() {
        val valid = CanvasMath.validate(CanvasSpec(3000.0, 2000.0, DimensionUnit.PX, 300))
        assertTrue(valid.valid)
        assertFalse(valid.requiresTiledRender)

        val tooSmall = CanvasMath.validate(CanvasSpec(32.0, 32.0, DimensionUnit.PX, 72))
        assertFalse(tooSmall.valid)

        val tiled8k = CanvasMath.validate(CanvasSpec(7680.0, 4320.0, DimensionUnit.PX, 300))
        assertTrue(tiled8k.valid)
        assertTrue(tiled8k.requiresTiledRender)

        val tooHuge = CanvasMath.validate(CanvasSpec(20000.0, 20000.0, DimensionUnit.PX, 300))
        assertFalse(tooHuge.valid)
    }

    @Test
    fun exportResolutionPresetsPreserveAspectRatioIncluding6kUltra() {
        val spec = CanvasSpec(3000.0, 2000.0, DimensionUnit.PX, 300)
        val dims4k = CanvasMath.computeExportDimensions(
            spec,
            ExportJobConfig(format = ExportFormat.PNG, resolutionPreset = ExportResolutionPreset.UHD_4K),
        )
        assertEquals(3840, dims4k.widthPx)
        assertEquals(2560, dims4k.heightPx)

        val dims6k = CanvasMath.computeExportDimensions(
            spec,
            ExportJobConfig(format = ExportFormat.HEIC, resolutionPreset = ExportResolutionPreset.UHD_6K),
        )
        assertEquals(6144, dims6k.widthPx)
        assertEquals(4096, dims6k.heightPx)
    }

    @Test
    fun postCreationResizeAdaptsLayersNonDestructively() {
        val oldSpec = CanvasSpec(3000.0, 2000.0, DimensionUnit.PX, 300, orientation = CanvasOrientation.LANDSCAPE)
        val newSpec = CanvasSpec(1800.0, 3200.0, DimensionUnit.PX, 300, resizeMode = ResizeMode.EXTEND, orientation = CanvasOrientation.PORTRAIT)
        val layers = listOf(
            CollageLayer(id = "l1", type = LayerType.PHOTO, normX = 0.3f, normY = 0.4f, normWidth = 0.4f, normHeight = 0.4f),
            CollageLayer(id = "l2", type = LayerType.TEXT, normX = 0.5f, normY = 0.5f, normWidth = 0.3f, normHeight = 0.1f),
        )
        val adapted = CanvasMath.adaptLayersForResize(layers, oldSpec, newSpec, ResizeMode.EXTEND)
        assertEquals(2, adapted.size)
        assertEquals("l1", adapted[0].id)
        assertTrue(adapted.all { it.normX in 0.05f..0.95f && it.normY in 0.05f..0.95f })
    }

    @Test
    fun layoutEngineHandles2To60PhotosWithoutErrors() {
        val counts = listOf(1, 2, 3, 5, 6, 12, 25, 50, 60)
        for (layout in LayoutType.entries) {
            for (count in counts) {
                val layers = List(count) { idx ->
                    CollageLayer(id = "p$idx", type = LayerType.PHOTO, zIndex = idx)
                }
                val cells = LayoutEngine.computePhotoCells(
                    layers = layers,
                    layoutType = layout,
                    canvasAspectRatio = 1.5f,
                    paddingFraction = 0.02f,
                    spacingFraction = 0.01f,
                )
                assertEquals("Failed for $layout with $count photos", count, cells.size)
                assertTrue(
                    "Invalid cell size for $layout ($count)",
                    cells.all { it.rect.width > 0f && it.rect.height > 0f },
                )
            }
        }
    }

    @Test
    fun autoFitEngineOrganizesSpacingBordersAndRespectsLocks() {
        val portraitStory = CanvasSpec(1800.0, 3200.0, DimensionUnit.PX, 300)
        val square = CanvasSpec(2400.0, 2400.0, DimensionUnit.PX, 300)

        val smallSet = AutoFitEngine.compute(4, square, AutoFitPreset.BALANCED)
        val largeSet = AutoFitEngine.compute(36, square, AutoFitPreset.BALANCED)
        assertTrue(smallSet.border.cornerRadius > largeSet.border.cornerRadius)
        assertTrue(smallSet.border.spacing > largeSet.border.spacing)
        assertEquals(LayoutType.MOSAIC, largeSet.layoutType)

        val seamless = AutoFitEngine.compute(6, portraitStory, AutoFitPreset.SEAMLESS)
        assertEquals(0f, seamless.border.padding, 0.001f)
        assertEquals(0f, seamless.border.cornerRadius, 0.001f)

        val wholePhoto = AutoFitEngine.compute(5, square, AutoFitPreset.WHOLE_PHOTO)
        assertEquals(PhotoFitMode.CONTAIN, wholePhoto.photoFitMode)

        val project = CollageProject(
            id = "p_autofit",
            ownerUid = "u1",
            title = "AutoFit Test",
            canvas = square,
            layers = listOf(
                CollageLayer(id = "locked_p", type = LayerType.PHOTO, scale = 1.8f, panX = 0.2f, locked = true),
                CollageLayer(id = "free_p", type = LayerType.PHOTO, scale = 1.8f, panX = 0.2f, locked = false),
            ),
            createdAtMs = 1L,
            updatedAtMs = 1L,
        )
        val fitted = AutoFitEngine.applyToProject(project, AutoFitPreset.GALLERY, targetCanvas = portraitStory)
        assertEquals(portraitStory, fitted.canvas)
        assertEquals(1.8f, fitted.layers[0].scale, 0.001f) // locked preserved
        assertEquals(1.0f, fitted.layers[1].scale, 0.001f) // unlocked cleanly organized
        assertEquals(0f, fitted.layers[1].panX, 0.001f)
        assertTrue(fitted.border.padding >= 16f)
    }

    @Test
    fun all20LanguagesProvideCompleteWholeUiTranslations() {
        assertEquals(20, SupportedAppLanguages.size)
        val codes = SupportedAppLanguages.map { it.code }
        assertEquals(20, codes.toSet().size)

        val requiredCodes = listOf(
            "en-GB", "en", "es", "pt", "id", "zh-TW", "fil",
            "fr", "de", "ko", "ar", "ru", "vi", "th",
            "hi", "ne", "ja", "ms", "tr", "zh",
        )
        assertTrue(codes.containsAll(requiredCodes))

        val enUs = stringsForLanguage("en")
        for (lang in SupportedAppLanguages) {
            val s = stringsForLanguage(lang.code)
            assertEquals("Mismatched languageCode for ${lang.code}", lang.code, s.languageCode)
            assertTrue(s.welcomeTagline.isNotBlank())
            assertTrue(s.navHome.isNotBlank())
            assertTrue(s.navProjects.isNotBlank())
            assertTrue(s.navSettings.isNotBlank())
            assertTrue(s.exportTitle.isNotBlank())
            assertTrue(s.editorExport.isNotBlank())
            assertTrue(s.settingsLanguage.isNotBlank())
            assertTrue(s.autoFitStudioTitle.isNotBlank())
            assertTrue(s.deleteLabel.isNotBlank())
            assertTrue(s.dontDeleteLabel.isNotBlank())
            assertTrue(s.translateSaveStatus("Saved").isNotBlank())
            for (fmt in ExportFormat.entries) {
                assertTrue(s.formatDescription(fmt).isNotBlank())
            }
            for (preset in ExportResolutionPreset.entries) {
                assertTrue(s.resolutionPresetLabel(preset).isNotBlank())
            }
            for (layout in LayoutType.entries) {
                assertTrue(s.layoutLabel(layout).isNotBlank())
            }
            for (mode in DarkModeOption.entries) {
                assertTrue(s.darkModeLabel(mode).isNotBlank())
            }
            for (af in AutoFitPreset.entries) {
                assertTrue(s.autoFitPresetLabel(af).isNotBlank())
            }
        }

        // Verify newly added languages genuinely localize UI text rather than falling back to English US
        for (code in listOf("es", "pt", "id", "zh-TW", "fil", "fr", "de", "ko", "ar", "ru", "vi", "th")) {
            val localized = stringsForLanguage(code)
            assertNotEquals("Expected localized settings title for $code", enUs.settingsTitle, localized.settingsTitle)
            assertNotEquals("Expected localized home CTA for $code", enUs.homeNewCollage, localized.homeNewCollage)
        }
    }

    @Test
    fun exportFormatsIncludeHeicWebpLosslessAndBmp() {
        assertEquals(8, ExportFormat.entries.size)
        for (fmt in ExportFormat.entries) {
            assertEquals(fmt, ExportFormat.fromRaw(fmt.name))
        }
        assertEquals("heic", ExportFormat.HEIC.extension)
        assertEquals("image/heic", ExportFormat.HEIC.mimeType)
        assertEquals("webp", ExportFormat.WEBP_LOSSLESS.extension)
        assertTrue(ExportFormat.WEBP_LOSSLESS.supportsAlpha)
        assertEquals("bmp", ExportFormat.BMP.extension)
        assertTrue(ExportFormat.BMP.supportsAlpha)
    }

    @Test
    fun bmpEncoderWritesValid24BitPaddedAnd32BitAlphaHeaders() {
        // 3x2 24-bit RGB: unpadded row = 9 bytes -> padded to 12 bytes -> 54 + 24 = 78 bytes
        val pixels3x2 = intArrayOf(
            0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt(), // top row (y=0)
            0xFFFFFFFF.toInt(), 0xFF112233.toInt(), 0xFF000000.toInt(), // bottom row (y=1)
        )
        val out24 = ByteArrayOutputStream()
        HighQualityImageEncoders.writeBmpFromPixels(
            width = 3,
            height = 2,
            argbPixels = pixels3x2,
            includeAlpha = false,
            dpi = 300,
            outputStream = out24,
        )
        val bytes24 = out24.toByteArray()
        assertEquals(78, bytes24.size)
        assertEquals('B'.code.toByte(), bytes24[0])
        assertEquals('M'.code.toByte(), bytes24[1])

        val hdr24 = ByteBuffer.wrap(bytes24, 0, 54).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(78, hdr24.getInt(2))
        assertEquals(54, hdr24.getInt(10))
        assertEquals(3, hdr24.getInt(18))
        assertEquals(2, hdr24.getInt(22))
        assertEquals(24.toShort(), hdr24.getShort(28))
        assertEquals(11811, hdr24.getInt(38)) // 300 DPI * 39.3701

        // Bottom-up DIB: first pixel written is y=1, x=0 (0xFFFFFFFF -> B=0xFF, G=0xFF, R=0xFF)
        assertEquals(0xFF.toByte(), bytes24[54])
        assertEquals(0xFF.toByte(), bytes24[55])
        assertEquals(0xFF.toByte(), bytes24[56])
        // Second pixel on bottom row is y=1, x=1 (0xFF112233 -> B=0x33, G=0x22, R=0x11)
        assertEquals(0x33.toByte(), bytes24[57])
        assertEquals(0x22.toByte(), bytes24[58])
        assertEquals(0x11.toByte(), bytes24[59])

        // 2x2 32-bit BGRA with alpha: row = 8 bytes -> 54 + 16 = 70 bytes
        val pixels2x2 = intArrayOf(
            0x80102030.toInt(), 0x40AABBCC.toInt(),
            0x00000000, 0xFF445566.toInt(),
        )
        val out32 = ByteArrayOutputStream()
        HighQualityImageEncoders.writeBmpFromPixels(
            width = 2,
            height = 2,
            argbPixels = pixels2x2,
            includeAlpha = true,
            dpi = 150,
            outputStream = out32,
        )
        val bytes32 = out32.toByteArray()
        assertEquals(70, bytes32.size)
        val hdr32 = ByteBuffer.wrap(bytes32, 0, 54).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(32.toShort(), hdr32.getShort(28))
        // y=0, x=0 is second row in file (offset 54 + 8 = 62): 0x80102030 -> B=0x30, G=0x20, R=0x10, A=0x80
        assertEquals(0x30.toByte(), bytes32[62])
        assertEquals(0x20.toByte(), bytes32[63])
        assertEquals(0x10.toByte(), bytes32[64])
        assertEquals(0x80.toByte(), bytes32[65])
    }
}

