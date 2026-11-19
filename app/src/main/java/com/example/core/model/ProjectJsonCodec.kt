package com.example.core.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Full-fidelity local serializer/deserializer for Room project storage using built-in org.json.
 * Keeps project edits versioned, non-destructive, and recoverable after process death.
 */
object ProjectJsonCodec {
    fun encodeProject(project: CollageProject): String {
        val root = JSONObject()
        root.put("schemaVersion", project.schemaVersion)
        root.put("layoutType", project.layoutType.name)

        root.put(
            "canvas",
            JSONObject().apply {
                put("width", project.canvas.width)
                put("height", project.canvas.height)
                put("unit", project.canvas.unit.name)
                put("dpi", project.canvas.dpi)
                put("aspectLocked", project.canvas.aspectLocked)
                put("resizeMode", project.canvas.resizeMode.name)
                put("orientation", project.canvas.orientation.name)
            },
        )

        root.put(
            "background",
            JSONObject().apply {
                put("type", project.background.type.name)
                put("primaryColor", project.background.primaryColor)
                put("secondaryColor", project.background.secondaryColor)
                put("textureIntensity", project.background.textureIntensity.toDouble())
                if (project.background.imageUri != null) {
                    put("imageUri", project.background.imageUri)
                }
            },
        )

        root.put(
            "border",
            JSONObject().apply {
                put("cornerRadius", project.border.cornerRadius.toDouble())
                put("spacing", project.border.spacing.toDouble())
                put("padding", project.border.padding.toDouble())
                put("color", project.border.color)
                put("shadowEnabled", project.border.shadowEnabled)
            },
        )

        val layersArr = JSONArray()
        project.layers.forEach { l ->
            layersArr.put(
                JSONObject().apply {
                    put("id", l.id)
                    put("type", l.type.name)
                    put("zIndex", l.zIndex)
                    put("visible", l.visible)
                    put("locked", l.locked)
                    put("normX", l.normX.toDouble())
                    put("normY", l.normY.toDouble())
                    put("normWidth", l.normWidth.toDouble())
                    put("normHeight", l.normHeight.toDouble())
                    put("scale", l.scale.toDouble())
                    put("panX", l.panX.toDouble())
                    put("panY", l.panY.toDouble())
                    put("rotationDeg", l.rotationDeg.toDouble())
                    put("flipHorizontal", l.flipHorizontal)
                    put("flipVertical", l.flipVertical)
                    if (l.cornerRadiusOverride != null) put("cornerRadiusOverride", l.cornerRadiusOverride.toDouble())
                    put("opacity", l.opacity.toDouble())
                    put("fitMode", l.fitMode.name)
                    if (l.localUri != null) put("localUri", l.localUri)
                    if (l.sampleArtKey != null) put("sampleArtKey", l.sampleArtKey)
                    put("label", l.label)
                    put(
                        "adjustments",
                        JSONObject().apply {
                            put("brightness", l.adjustments.brightness.toDouble())
                            put("contrast", l.adjustments.contrast.toDouble())
                            put("saturation", l.adjustments.saturation.toDouble())
                            put("warmth", l.adjustments.warmth.toDouble())
                            put("exposure", l.adjustments.exposure.toDouble())
                            put("sharpen", l.adjustments.sharpen.toDouble())
                            put("vignette", l.adjustments.vignette.toDouble())
                            put("fade", l.adjustments.fade.toDouble())
                            put("tint", l.adjustments.tint.toDouble())
                            put("highlights", l.adjustments.highlights.toDouble())
                            put("shadows", l.adjustments.shadows.toDouble())
                            put("vibrance", l.adjustments.vibrance.toDouble())
                            put("filter", l.adjustments.filter.name)
                        },
                    )
                    l.textStyle?.let { ts ->
                        put(
                            "textStyle",
                            JSONObject().apply {
                                put("text", ts.text)
                                put("fontId", ts.fontId)
                                put("fontSizeSp", ts.fontSizeSp.toDouble())
                                put("bold", ts.bold)
                                put("color", ts.color)
                                if (ts.chipColor != null) put("chipColor", ts.chipColor)
                                if (ts.outlineColor != null) put("outlineColor", ts.outlineColor)
                                put("alignment", ts.alignment)
                            },
                        )
                    }
                    l.stickerKind?.let { sk ->
                        put("stickerKind", sk.name)
                        put("stickerColor", l.stickerColor)
                    }
                },
            )
        }
        root.put("layers", layersArr)
        return root.toString()
    }

    fun decodeProject(
        id: String,
        ownerUid: String,
        title: String,
        createdAtMs: Long,
        updatedAtMs: Long,
        favorite: Boolean,
        archived: Boolean,
        revision: Int,
        syncState: SyncState,
        cloudBackedUp: Boolean,
        payloadJson: String,
    ): CollageProject {
        val root = try {
            JSONObject(payloadJson)
        } catch (_: Exception) {
            JSONObject()
        }
        val schemaVersion = root.optInt("schemaVersion", 1)
        val layoutType = LayoutType.fromRaw(root.optString("layoutType", "BENTO"))

        val cObj = root.optJSONObject("canvas")
        val canvas = if (cObj != null) {
            CanvasSpec(
                width = cObj.optDouble("width", 3000.0),
                height = cObj.optDouble("height", 2000.0),
                unit = DimensionUnit.fromRaw(cObj.optString("unit", "PX")),
                dpi = cObj.optInt("dpi", 300),
                aspectLocked = cObj.optBoolean("aspectLocked", true),
                resizeMode = ResizeMode.fromRaw(cObj.optString("resizeMode", "FIT")),
                orientation = CanvasOrientation.fromRaw(cObj.optString("orientation", "LANDSCAPE")),
            )
        } else {
            CanvasSpec()
        }

        val bgObj = root.optJSONObject("background")
        val background = if (bgObj != null) {
            BackgroundSpec(
                type = BackgroundType.fromRaw(bgObj.optString("type", "SOLID")),
                primaryColor = bgObj.optInt("primaryColor", 0xFFFFFFFF.toInt()),
                secondaryColor = bgObj.optInt("secondaryColor", 0xFFFDE6EC.toInt()),
                textureIntensity = bgObj.optDouble("textureIntensity", 0.0).toFloat(),
                imageUri = bgObj.optString("imageUri", "").ifBlank { null },
            )
        } else {
            BackgroundSpec()
        }

        val brObj = root.optJSONObject("border")
        val border = if (brObj != null) {
            BorderSpec(
                cornerRadius = brObj.optDouble("cornerRadius", 16.0).toFloat(),
                spacing = brObj.optDouble("spacing", 8.0).toFloat(),
                padding = brObj.optDouble("padding", 12.0).toFloat(),
                color = brObj.optInt("color", 0xFFFFFFFF.toInt()),
                shadowEnabled = brObj.optBoolean("shadowEnabled", false),
            )
        } else {
            BorderSpec()
        }

        val layers = mutableListOf<CollageLayer>()
        val layersArr = root.optJSONArray("layers")
        if (layersArr != null) {
            for (i in 0 until layersArr.length()) {
                val lo = layersArr.optJSONObject(i) ?: continue
                val adjObj = lo.optJSONObject("adjustments")
                val adjustments = if (adjObj != null) {
                    PhotoAdjustments(
                        brightness = adjObj.optDouble("brightness", 0.0).toFloat(),
                        contrast = adjObj.optDouble("contrast", 1.0).toFloat(),
                        saturation = adjObj.optDouble("saturation", 1.0).toFloat(),
                        warmth = adjObj.optDouble("warmth", 0.0).toFloat(),
                        exposure = adjObj.optDouble("exposure", 0.0).toFloat(),
                        sharpen = adjObj.optDouble("sharpen", 0.0).toFloat(),
                        vignette = adjObj.optDouble("vignette", 0.0).toFloat(),
                        fade = adjObj.optDouble("fade", 0.0).toFloat(),
                        tint = adjObj.optDouble("tint", 0.0).toFloat(),
                        highlights = adjObj.optDouble("highlights", 0.0).toFloat(),
                        shadows = adjObj.optDouble("shadows", 0.0).toFloat(),
                        vibrance = adjObj.optDouble("vibrance", 0.0).toFloat(),
                        filter = PhotoFilter.fromRaw(adjObj.optString("filter", "ORIGINAL")),
                    )
                } else {
                    PhotoAdjustments()
                }
                val tsObj = lo.optJSONObject("textStyle")
                val textStyle = if (tsObj != null) {
                    TextStyleSpec(
                        text = tsObj.optString("text", "Memories"),
                        fontId = tsObj.optString("fontId", "SANS"),
                        fontSizeSp = tsObj.optDouble("fontSizeSp", 20.0).toFloat(),
                        bold = tsObj.optBoolean("bold", true),
                        color = tsObj.optInt("color", 0xFF1D1724.toInt()),
                        chipColor = if (tsObj.has("chipColor")) tsObj.optInt("chipColor") else null,
                        outlineColor = if (tsObj.has("outlineColor")) tsObj.optInt("outlineColor") else null,
                        alignment = tsObj.optInt("alignment", 0),
                    )
                } else {
                    null
                }
                val stickerRaw = lo.optString("stickerKind", "").ifBlank { null }
                layers.add(
                    CollageLayer(
                        id = lo.optString("id", "layer_$i"),
                        type = LayerType.fromRaw(lo.optString("type", "PHOTO")),
                        zIndex = lo.optInt("zIndex", i),
                        visible = lo.optBoolean("visible", true),
                        locked = lo.optBoolean("locked", false),
                        normX = lo.optDouble("normX", 0.5).toFloat(),
                        normY = lo.optDouble("normY", 0.5).toFloat(),
                        normWidth = lo.optDouble("normWidth", 0.42).toFloat(),
                        normHeight = lo.optDouble("normHeight", 0.42).toFloat(),
                        scale = lo.optDouble("scale", 1.0).toFloat(),
                        panX = lo.optDouble("panX", 0.0).toFloat(),
                        panY = lo.optDouble("panY", 0.0).toFloat(),
                        rotationDeg = lo.optDouble("rotationDeg", 0.0).toFloat(),
                        flipHorizontal = lo.optBoolean("flipHorizontal", false),
                        flipVertical = lo.optBoolean("flipVertical", false),
                        cornerRadiusOverride = if (lo.has("cornerRadiusOverride")) {
                            lo.optDouble("cornerRadiusOverride").toFloat()
                        } else {
                            null
                        },
                        opacity = lo.optDouble("opacity", 1.0).toFloat(),
                        fitMode = PhotoFitMode.fromRaw(lo.optString("fitMode", "COVER")),
                        localUri = lo.optString("localUri", "").ifBlank { null },
                        sampleArtKey = lo.optString("sampleArtKey", "").ifBlank { null },
                        label = lo.optString("label", ""),
                        adjustments = adjustments,
                        textStyle = textStyle,
                        stickerKind = stickerRaw?.let { StickerKind.fromRaw(it) },
                        stickerColor = lo.optInt("stickerColor", 0xFFEA2858.toInt()),
                    ),
                )
            }
        }

        return CollageProject(
            id = id,
            ownerUid = ownerUid,
            title = title,
            schemaVersion = schemaVersion,
            createdAtMs = createdAtMs,
            updatedAtMs = updatedAtMs,
            layoutType = layoutType,
            canvas = canvas,
            background = background,
            border = border,
            layers = layers,
            favorite = favorite,
            archived = archived,
            revision = revision,
            syncState = syncState,
            cloudBackedUp = cloudBackedUp,
        )
    }
}
