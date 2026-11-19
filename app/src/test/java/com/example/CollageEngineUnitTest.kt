package com.example

import com.example.core.model.CanvasMath
import com.example.core.model.CanvasOrientation
import com.example.core.model.CanvasSpec
import com.example.core.model.CollageLayer
import com.example.core.model.DimensionUnit
import com.example.core.model.ExportFormat
import com.example.core.model.ExportJobConfig
import com.example.core.model.ExportResolutionPreset
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import com.example.core.model.ResizeMode
import com.example.engine.LayoutEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun exportResolutionPresetsPreserveAspectRatio() {
        val spec = CanvasSpec(3000.0, 2000.0, DimensionUnit.PX, 300)
        val dims4k = CanvasMath.computeExportDimensions(
            spec,
            ExportJobConfig(format = ExportFormat.PNG, resolutionPreset = ExportResolutionPreset.UHD_4K),
        )
        assertEquals(3840, dims4k.widthPx)
        assertEquals(2560, dims4k.heightPx)
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
}
