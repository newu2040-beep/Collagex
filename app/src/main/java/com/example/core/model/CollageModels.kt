package com.example.core.model

import androidx.compose.runtime.Immutable

enum class ThemePalette(val displayName: String) {
    PASTEL_PINK("Pastel Pink"),
    LAVENDER("Lavender"),
    MINT("Mint"),
    PEACH("Peach"),
    SKY_BLUE("Sky Blue"),
    NEUTRAL("Neutral"),
    ;

    companion object {
        fun fromRaw(raw: String?): ThemePalette =
            entries.firstOrNull { it.name == raw } ?: PASTEL_PINK
    }
}

enum class DarkModeOption(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
    ;

    companion object {
        fun fromRaw(raw: String?): DarkModeOption =
            entries.firstOrNull { it.name == raw } ?: SYSTEM
    }
}

enum class DimensionUnit(val shortLabel: String, val chipLabel: String) {
    PX("px", "Pixels (px)"),
    IN("in", "Inches (in)"),
    CM("cm", "Cm (cm)"),
    MM("mm", "Mm (mm)"),
    PT("pt", "Pt (pt)"),
    ;

    companion object {
        fun fromRaw(raw: String?): DimensionUnit =
            entries.firstOrNull { it.name == raw } ?: PX
    }
}

enum class ResizeMode(val label: String, val description: String) {
    FIT("Fit", "Scale collage inside new canvas without cropping"),
    FILL("Fill", "Cover full canvas; edges may crop cleanly"),
    CROP("Crop", "Keep scale and frame center of existing layout"),
    EXTEND("Extend", "Expand canvas area with background; no stretching"),
    ;

    companion object {
        fun fromRaw(raw: String?): ResizeMode =
            entries.firstOrNull { it.name == raw } ?: FIT
    }
}

enum class CanvasOrientation(val label: String) {
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape"),
    ;

    companion object {
        fun fromRaw(raw: String?): CanvasOrientation =
            entries.firstOrNull { it.name == raw } ?: PORTRAIT
    }
}

enum class LayoutType(val label: String) {
    GRID("Grid"),
    ASYMMETRIC("Hero"),
    MASONRY("Masonry"),
    BENTO("Bento"),
    STRIP("Strip"),
    MOSAIC("Mosaic"),
    FREEFORM("Freeform"),
    ;

    companion object {
        fun fromRaw(raw: String?): LayoutType =
            entries.firstOrNull { it.name == raw } ?: BENTO
    }
}

enum class BackgroundType(val label: String) {
    SOLID("Solid"),
    GRADIENT("Gradient"),
    IMAGE("Image"),
    ;

    companion object {
        fun fromRaw(raw: String?): BackgroundType =
            entries.firstOrNull { it.name == raw } ?: SOLID
    }
}

enum class LayerType {
    PHOTO,
    TEXT,
    STICKER,
    ;

    companion object {
        fun fromRaw(raw: String?): LayerType =
            entries.firstOrNull { it.name == raw } ?: PHOTO
    }
}

enum class PhotoFitMode(val label: String) {
    COVER("Fill Cell"),
    CONTAIN("Fit Whole"),
    ;

    companion object {
        fun fromRaw(raw: String?): PhotoFitMode =
            entries.firstOrNull { it.name == raw } ?: COVER
    }
}

enum class PhotoFilter(val label: String) {
    ORIGINAL("Original"),
    BLOSSOM("Blossom"),
    WARM("Warm"),
    CRISP("Crisp"),
    FILM("Film"),
    MONO("Mono"),
    ;

    companion object {
        fun fromRaw(raw: String?): PhotoFilter =
            entries.firstOrNull { it.name == raw } ?: ORIGINAL
    }
}

enum class StickerKind(val label: String, val glyph: String) {
    BLOSSOM("Blossom", "✿"),
    SPARKLE("Sparkle", "✦"),
    HEART("Heart", "♥"),
    STAR("Star", "★"),
    SUN("Sun", "☀"),
    LEAF("Leaf", "❀"),
    RIBBON("Ribbon", "❖"),
    NOTE("Music", "♪"),
    ;

    companion object {
        fun fromRaw(raw: String?): StickerKind =
            entries.firstOrNull { it.name == raw } ?: BLOSSOM
    }
}

enum class ExportFormat(
    val label: String,
    val extension: String,
    val mimeType: String,
    val supportsAlpha: Boolean,
    val supportsQuality: Boolean,
) {
    JPEG("JPEG", "jpg", "image/jpeg", supportsAlpha = false, supportsQuality = true),
    PNG("PNG", "png", "image/png", supportsAlpha = true, supportsQuality = false),
    WEBP("WebP", "webp", "image/webp", supportsAlpha = true, supportsQuality = true),
    TIFF("TIFF", "tif", "image/tiff", supportsAlpha = true, supportsQuality = false),
    PDF("PDF", "pdf", "application/pdf", supportsAlpha = false, supportsQuality = false),
    ;

    companion object {
        fun fromRaw(raw: String?): ExportFormat =
            entries.firstOrNull { it.name == raw } ?: PNG
    }
}

enum class ExportResolutionPreset(val label: String, val maxLongEdgePx: Int?) {
    ORIGINAL("Canvas Size", null),
    HD_1080("1080p", 1920),
    QHD_2K("2K", 2560),
    UHD_4K("4K", 3840),
    UHD_8K("8K Tiled", 7680),
}

enum class SyncState {
    LOCAL_ONLY,
    PENDING,
    SYNCED,
    ERROR,
}

@Immutable
data class CanvasSpec(
    val width: Double = 3000.0,
    val height: Double = 2000.0,
    val unit: DimensionUnit = DimensionUnit.PX,
    val dpi: Int = 300,
    val aspectLocked: Boolean = true,
    val resizeMode: ResizeMode = ResizeMode.FIT,
    val orientation: CanvasOrientation = CanvasOrientation.LANDSCAPE,
)

@Immutable
data class BackgroundSpec(
    val type: BackgroundType = BackgroundType.SOLID,
    val primaryColor: Int = 0xFFFFFFFF.toInt(),
    val secondaryColor: Int = 0xFFFDE6EC.toInt(),
    val textureIntensity: Float = 0f,
    val imageUri: String? = null,
)

@Immutable
data class BorderSpec(
    val cornerRadius: Float = 16f,
    val spacing: Float = 8f,
    val padding: Float = 12f,
    val color: Int = 0xFFFFFFFF.toInt(),
    val shadowEnabled: Boolean = false,
)

@Immutable
data class PhotoAdjustments(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val warmth: Float = 0f,
    val exposure: Float = 0f,
    val filter: PhotoFilter = PhotoFilter.ORIGINAL,
)

@Immutable
data class TextStyleSpec(
    val text: String = "Memories",
    val fontId: String = "SANS",
    val fontSizeSp: Float = 20f,
    val bold: Boolean = true,
    val color: Int = 0xFF1D1724.toInt(),
    val chipColor: Int? = 0xE6FFFFFF.toInt(),
    val outlineColor: Int? = null,
    val alignment: Int = 0, // -1 left, 0 center, 1 right
)

@Immutable
data class CollageLayer(
    val id: String,
    val type: LayerType,
    val zIndex: Int = 0,
    val visible: Boolean = true,
    val locked: Boolean = false,
    // Normalized freeform / overlay position & size (0..1 of canvas)
    val normX: Float = 0.5f,
    val normY: Float = 0.5f,
    val normWidth: Float = 0.42f,
    val normHeight: Float = 0.42f,
    val scale: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val rotationDeg: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val cornerRadiusOverride: Float? = null,
    val opacity: Float = 1f,
    val fitMode: PhotoFitMode = PhotoFitMode.COVER,
    val localUri: String? = null,
    val sampleArtKey: String? = null,
    val label: String = "",
    val adjustments: PhotoAdjustments = PhotoAdjustments(),
    val textStyle: TextStyleSpec? = null,
    val stickerKind: StickerKind? = null,
    val stickerColor: Int = 0xFFEA2858.toInt(),
)

@Immutable
data class CollageProject(
    val id: String,
    val ownerUid: String = "",
    val title: String,
    val schemaVersion: Int = 1,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val layoutType: LayoutType = LayoutType.BENTO,
    val canvas: CanvasSpec = CanvasSpec(),
    val background: BackgroundSpec = BackgroundSpec(),
    val border: BorderSpec = BorderSpec(),
    val layers: List<CollageLayer> = emptyList(),
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val revision: Int = 1,
    val syncState: SyncState = SyncState.LOCAL_ONLY,
    val cloudBackedUp: Boolean = false,
) {
    val photoCount: Int get() = layers.count { it.type == LayerType.PHOTO }
}

@Immutable
data class ExportJobConfig(
    val format: ExportFormat = ExportFormat.PNG,
    val resolutionPreset: ExportResolutionPreset = ExportResolutionPreset.ORIGINAL,
    val customWidthPx: Int? = null,
    val customHeightPx: Int? = null,
    val quality: Int = 92,
    val transparentBg: Boolean = false,
    val stripExif: Boolean = true,
)

@Immutable
data class ExportRecord(
    val id: String,
    val projectId: String,
    val projectTitle: String,
    val format: ExportFormat,
    val widthPx: Int,
    val heightPx: Int,
    val dpi: Int,
    val fileSizeBytes: Long,
    val transparentBg: Boolean,
    val status: String,
    val localUri: String? = null,
    val createdAtMs: Long,
)

@Immutable
data class UserPreferences(
    val themePalette: ThemePalette = ThemePalette.PASTEL_PINK,
    val darkMode: DarkModeOption = DarkModeOption.SYSTEM,
    val languageCode: String = "en",
    val notificationsEnabled: Boolean = false,
    val stripExifDefault: Boolean = true,
    val defaultDpi: Int = 300,
    val defaultUnit: DimensionUnit = DimensionUnit.PX,
    val cloudAutoSync: Boolean = true,
)

@Immutable
data class StylePreset(
    val id: String,
    val name: String,
    val border: BorderSpec,
    val background: BackgroundSpec,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
