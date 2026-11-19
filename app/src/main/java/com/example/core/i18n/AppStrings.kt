package com.example.core.i18n

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.core.model.DarkModeOption
import com.example.core.model.ExportFormat
import com.example.core.model.ExportResolutionPreset
import com.example.core.model.LayerType
import com.example.core.model.LayoutType
import com.example.core.model.ResizeMode
import com.example.engine.AutoFitPreset

data class AppLanguageOption(
    val code: String,
    val nativeLabel: String,
    val englishName: String,
)

val SupportedAppLanguages = listOf(
    AppLanguageOption("en-GB", "English (UK)", "English (UK)"),
    AppLanguageOption("en", "English (US)", "English (US)"),
    AppLanguageOption("es", "Español", "Spanish"),
    AppLanguageOption("pt", "Português", "Portuguese"),
    AppLanguageOption("id", "Bahasa Indonesia", "Indonesian"),
    AppLanguageOption("zh-TW", "繁體中文 (台灣)", "Taiwanese / Traditional Chinese"),
    AppLanguageOption("fil", "Filipino", "Filipino"),
    AppLanguageOption("fr", "Français", "French"),
    AppLanguageOption("de", "Deutsch", "German"),
    AppLanguageOption("ko", "한국어", "Korean"),
    AppLanguageOption("ar", "العربية", "Arabic"),
    AppLanguageOption("ru", "Русский", "Russian"),
    AppLanguageOption("vi", "Tiếng Việt", "Vietnamese"),
    AppLanguageOption("th", "ไทย", "Thai"),
    AppLanguageOption("hi", "हिन्दी", "Hindi"),
    AppLanguageOption("ne", "नेपाली", "Nepali"),
    AppLanguageOption("ja", "日本語", "Japanese"),
    AppLanguageOption("ms", "Bahasa Melayu", "Malaysian"),
    AppLanguageOption("tr", "Türkçe", "Turkish"),
    AppLanguageOption("zh", "简体中文", "Simplified Chinese"),
)

@Immutable
data class AppStringsPart1(
    val languageCode: String,
    // Common
    val versionWord: String,
    val closeLabel: String,
    val cancelLabel: String,
    val saveLabel: String,
    val deleteLabel: String,
    val keepLabel: String,
    val resetLabel: String,
    val selectedLabel: String,
    val activeLabel: String,
    val allowLabel: String,
    val allowedLabel: String,
    val chooseLabel: String,
    val grantedLabel: String,
    val continueLabel: String,
    val tryAgainLabel: String,
    val loadingLabel: String,
    val languageUpdatedPrefix: String,
    val manualLabel: String,
    val autoFitLabel: String,
    val savedStatus: String,
    val savingStatus: String,
    val syncedStatus: String,
    // Welcome
    val welcomeTagline: String,
    val welcomeGoogleCta: String,
    val welcomeAboutCta: String,
    val openSourceBadge: String,
    val welcomeAboutBody1: String,
    val welcomeAboutBody2: String,
    // Bottom Nav
    val navHome: String,
    val navProjects: String,
    val navCreate: String,
    val navExports: String,
    val navSettings: String,
    // Home & Create Chooser
    val homeGreetingPrefix: String,
    val homeSubtitle: String,
    val homeNewCollage: String,
    val homeStartCreating: String,
    val homeQuickTools: String,
    val homeRecentCollages: String,
    val actionSeeAll: String,
    val toolTemplates: String,
    val toolGallery: String,
    val toolLayout: String,
    val toolTools: String,
    val noCollagesTitle: String,
    val noCollagesMsg: String,
    val tryStarterCollage: String,
    val loadingCollages: String,
    val starterTemplatesTitle: String,
    val starterTemplatesSubtitle: String,
    val createWithLayoutTitle: String,
    val collageToolsTitle: String,
    val createNewCollageTitle: String,
    val createNewCollageSubtitle: String,
    val targetRatioAndSize: String,
    val autoFitOrganization: String,
    val pickPhotosFromGallery: String,
    val photoWallMosaicTitle: String,
    val photoWallMosaicSubtitle: String,
    val starterPackTitle: String,
    val starterPackSubtitle: String,
    val printReadyA4Title: String,
    // Projects & Exports
    val projectsTitle: String,
    val searchCollagesPlaceholder: String,
    val filterAll: String,
    val filterFavorites: String,
    val filterArchived: String,
    val sortRecent: String,
    val sortName: String,
    val sortPhotos: String,
    val noMatchingProjectsTitle: String,
    val noMatchingProjectsMsg: String,
    val loadingProjects: String,
    val menuResizeCanvas: String,
    val menuRename: String,
    val menuDuplicate: String,
    val menuFavorite: String,
    val menuUnfavorite: String,
    val menuArchive: String,
    val menuUnarchive: String,
    val renameCollageTitle: String,
    val projectTitleLabel: String,
    val deleteProjectMsg: String,
    val removeLocalOnly: String,
    val deleteCloudOnly: String,
    val deleteLocalAndCloud: String,
    val photosWord: String,
    val exportsTitle: String,
    val exportsSubtitle: String,
    val noExportsTitle: String,
    val noExportsMsg: String,
    val loadingExports: String,
    // Editor & Delete / Don't Delete
    val editorTitle: String,
    val editorExport: String,
    val editorTabLayout: String,
    val editorTabBorder: String,
    val editorTabBackground: String,
    val editorToolText: String,
    val editorToolSticker: String,
    val editorToolFilter: String,
    val editorToolBackground: String,
    val editorToolMore: String,
    val noActiveCollageTitle: String,
    val noActiveCollageMsg: String,
    val goHomeLabel: String,
    val layerPhoto: String,
    val layerText: String,
    val layerSticker: String,
    val editPhoto: String,
    val editText: String,
    val editSticker: String,
    val dontDeleteLabel: String,
    val dontDeleteOnLabel: String,
    val protectedLabel: String,
    val unlockAndDelete: String,
    val markDontDelete: String,
    val deleteConfirmMsg: String,
    val protectedConfirmMsg: String,
    val borderRadius: String,
    val borderSpacing: String,
    val borderPadding: String,
    val borderColor: String,
    val softCellShadow: String,
    val solidPalette: String,
    val gradientsAndImage: String,
    val pickImage: String,
    val paperTexture: String,
    // Editor Sheets
    val addTextLayer: String,
    val editTextLayer: String,
    val writeCaptionPlaceholder: String,
    val inbuiltSystemFonts: String,
    val boldLabel: String,
    val pillBackground: String,
    val sizeLabel: String,
    val textColor: String,
    val addToCollage: String,
    val updateText: String,
    val openSourceStickers: String,
    val onCanvasKeepOrDelete: String,
    val addNewStickerTint: String,
    val editableLabel: String,
    val filterAndAdjustments: String,
    val filtersLabel: String,
    val detailAndFocus: String,
    val sharpenLabel: String,
    val vignetteLabel: String,
    val lightAndTone: String,
    val brightnessLabel: String,
    val exposureLabel: String,
    val contrastLabel: String,
    val highlightsLabel: String,
    val shadowsLabel: String,
    val colorAndFinish: String,
    val saturationLabel: String,
    val vibranceLabel: String,
    val warmthLabel: String,
    val tintLabel: String,
    val fadeMatteLabel: String,
    val selectedLayerTitle: String,
    val replaceLabel: String,
    val swapLabel: String,
    val fitWholeLabel: String,
    val fillCellLabel: String,
    val rotate90Label: String,
    val flipHLabel: String,
    val flipVLabel: String,
    val zoomScaleLabel: String,
    val opacityLabel: String,
    val perPhotoRadius: String,
    val moreToolsAndLayers: String,
    val canvasSizeAndRatio: String,
    val canvasSizeAndRatioSub: String,
    val addPhotosTitle: String,
    val addPhotosSub: String,
    val addSamplePhotosTitle: String,
    val addSamplePhotosSub: String,
    val showGuidesTitle: String,
    val hideGuidesTitle: String,
    val guidesSub: String,
    val stylePresetsLabel: String,
    val saveStylePlaceholder: String,
    val layersLabel: String,
    val autoFitStudioTitle: String,
    val autoFitStudioSubtitle: String,
    val autoFitStyleLabel: String,
    val currentLabel: String,
    val autoFitOnNewPhotos: String,
    val autoFitOnNewPhotosSub: String,
    val autoFitManualNote: String,
    val applyAutoFitLabel: String,
)

@Immutable
data class AppStringsPart2(
    // Custom Size
    val customSizeTitle: String,
    val liveResizePreview: String,
    val invalidSize: String,
    val customSizePresets: String,
    val customSizeDimensions: String,
    val customSizeWidth: String,
    val customSizeHeight: String,
    val customSizeDpi: String,
    val customDpiOption: String,
    val customSizeOrientation: String,
    val customSizePortrait: String,
    val customSizeLandscape: String,
    val contentResizeMode: String,
    val customSizeMaintainRatio: String,
    val autoFitLayoutCorners: String,
    val autoFitLayoutCornersSub: String,
    val customSizeExtendCanvas: String,
    val extendCanvasSub: String,
    val outputLabel: String,
    val customSizeApply: String,
    // Export
    val exportTitle: String,
    val exportFormat: String,
    val exportResolution: String,
    val exportQuality: String,
    val qualitySharePrivacy: String,
    val exportTransparentBg: String,
    val exportTransparentSub: String,
    val exportNoAlphaNote: String,
    val exportStripExif: String,
    val exportStripExifSub: String,
    val exportAutoShare: String,
    val exportAutoShareSub: String,
    val exportSaveGallery: String,
    val exportingLabel: String,
    val exportSaveAndShare: String,
    val exportSaveAs: String,
    val exportCancel: String,
    val exportDirectShare: String,
    val exportOpen: String,
    val exportCompleteTitle: String,
    val zeroWatermarksFullQuality: String,
    val export8kNote: String,
    val formatDescPng: String,
    val formatDescJpeg: String,
    val formatDescHeic: String,
    val formatDescWebp: String,
    val formatDescWebpLossless: String,
    val formatDescBmp: String,
    val formatDescTiff: String,
    val formatDescPdf: String,
    val resCanvasSize: String,
    // Settings & Info
    val settingsTitle: String,
    val settingsThemes: String,
    val palettesWord: String,
    val settingsAppFont: String,
    val settingsAppFontSub: String,
    val settingsDarkMode: String,
    val modePrefix: String,
    val darkModeSystem: String,
    val darkModeLight: String,
    val darkModeDark: String,
    val settingsLanguage: String,
    val settingsNotifications: String,
    val notificationsReadySub: String,
    val notificationsTapSub: String,
    val settingsPermissions: String,
    val settingsPermissionsSub: String,
    val settingsCloudSync: String,
    val settingsCloudSyncSub: String,
    val settingsStripExif: String,
    val settingsStripExifSub: String,
    val settingsDefaultDpi: String,
    val settingsTipsAndTricks: String,
    val settingsTipsSub: String,
    val settingsAbout: String,
    val settingsHelp: String,
    val settingsPrivacy: String,
    val settingsOpenSource: String,
    val settingsSignOut: String,
    val settingsDeleteData: String,
    val settingsDeleteDataSub: String,
    val accountAndSyncTitle: String,
    val openSourceBannerTitle: String,
    val madeWithLoveByRahul: String,
    val signedInWithGoogle: String,
    val permissionsSheetDesc: String,
    val galleryPickerTitle: String,
    val galleryPickerSub: String,
    val filesFolderTitle: String,
    val filesFolderSub: String,
    val openSystemSettings: String,
    val allowNotificationsBtn: String,
    val tipsSheetSubtitle: String,
    val defaultDpiTitle: String,
    val deleteAllTitle: String,
    val deleteAllMsg: String,
    val deleteEverythingBtn: String,
    val aboutBody1: String,
    val helpBody1: String,
    val helpBody2: String,
    val privacyBody1: String,
    val privacyBody2: String,
    val openSourceBody1: String,
    val openSourceBody2: String,
    // Enums
    val layoutGrid: String,
    val layoutHero: String,
    val layoutMasonry: String,
    val layoutBento: String,
    val layoutStrip: String,
    val layoutMosaic: String,
    val layoutFreeform: String,
    val resizeFit: String,
    val resizeFill: String,
    val resizeCrop: String,
    val resizeExtend: String,
    val autoFitBalanced: String,
    val autoFitSeamless: String,
    val autoFitGallery: String,
    val autoFitSoftCards: String,
    val autoFitWholePhoto: String,
)

@Immutable
data class AppStrings(
    val p1: AppStringsPart1,
    val p2: AppStringsPart2,
) {
    val languageCode: String get() = p1.languageCode
    val versionWord: String get() = p1.versionWord
    val closeLabel: String get() = p1.closeLabel
    val cancelLabel: String get() = p1.cancelLabel
    val saveLabel: String get() = p1.saveLabel
    val deleteLabel: String get() = p1.deleteLabel
    val keepLabel: String get() = p1.keepLabel
    val resetLabel: String get() = p1.resetLabel
    val selectedLabel: String get() = p1.selectedLabel
    val activeLabel: String get() = p1.activeLabel
    val allowLabel: String get() = p1.allowLabel
    val allowedLabel: String get() = p1.allowedLabel
    val chooseLabel: String get() = p1.chooseLabel
    val grantedLabel: String get() = p1.grantedLabel
    val continueLabel: String get() = p1.continueLabel
    val tryAgainLabel: String get() = p1.tryAgainLabel
    val loadingLabel: String get() = p1.loadingLabel
    val languageUpdatedPrefix: String get() = p1.languageUpdatedPrefix
    val manualLabel: String get() = p1.manualLabel
    val autoFitLabel: String get() = p1.autoFitLabel
    val savedStatus: String get() = p1.savedStatus
    val savingStatus: String get() = p1.savingStatus
    val syncedStatus: String get() = p1.syncedStatus
    val welcomeTagline: String get() = p1.welcomeTagline
    val welcomeGoogleCta: String get() = p1.welcomeGoogleCta
    val welcomeAboutCta: String get() = p1.welcomeAboutCta
    val openSourceBadge: String get() = p1.openSourceBadge
    val welcomeAboutBody1: String get() = p1.welcomeAboutBody1
    val welcomeAboutBody2: String get() = p1.welcomeAboutBody2
    val navHome: String get() = p1.navHome
    val navProjects: String get() = p1.navProjects
    val navCreate: String get() = p1.navCreate
    val navExports: String get() = p1.navExports
    val navSettings: String get() = p1.navSettings
    val homeGreetingPrefix: String get() = p1.homeGreetingPrefix
    val homeSubtitle: String get() = p1.homeSubtitle
    val homeNewCollage: String get() = p1.homeNewCollage
    val homeStartCreating: String get() = p1.homeStartCreating
    val homeQuickTools: String get() = p1.homeQuickTools
    val homeRecentCollages: String get() = p1.homeRecentCollages
    val actionSeeAll: String get() = p1.actionSeeAll
    val toolTemplates: String get() = p1.toolTemplates
    val toolGallery: String get() = p1.toolGallery
    val toolLayout: String get() = p1.toolLayout
    val toolTools: String get() = p1.toolTools
    val noCollagesTitle: String get() = p1.noCollagesTitle
    val noCollagesMsg: String get() = p1.noCollagesMsg
    val tryStarterCollage: String get() = p1.tryStarterCollage
    val loadingCollages: String get() = p1.loadingCollages
    val starterTemplatesTitle: String get() = p1.starterTemplatesTitle
    val starterTemplatesSubtitle: String get() = p1.starterTemplatesSubtitle
    val createWithLayoutTitle: String get() = p1.createWithLayoutTitle
    val collageToolsTitle: String get() = p1.collageToolsTitle
    val createNewCollageTitle: String get() = p1.createNewCollageTitle
    val createNewCollageSubtitle: String get() = p1.createNewCollageSubtitle
    val targetRatioAndSize: String get() = p1.targetRatioAndSize
    val autoFitOrganization: String get() = p1.autoFitOrganization
    val pickPhotosFromGallery: String get() = p1.pickPhotosFromGallery
    val photoWallMosaicTitle: String get() = p1.photoWallMosaicTitle
    val photoWallMosaicSubtitle: String get() = p1.photoWallMosaicSubtitle
    val starterPackTitle: String get() = p1.starterPackTitle
    val starterPackSubtitle: String get() = p1.starterPackSubtitle
    val printReadyA4Title: String get() = p1.printReadyA4Title
    val projectsTitle: String get() = p1.projectsTitle
    val searchCollagesPlaceholder: String get() = p1.searchCollagesPlaceholder
    val filterAll: String get() = p1.filterAll
    val filterFavorites: String get() = p1.filterFavorites
    val filterArchived: String get() = p1.filterArchived
    val sortRecent: String get() = p1.sortRecent
    val sortName: String get() = p1.sortName
    val sortPhotos: String get() = p1.sortPhotos
    val noMatchingProjectsTitle: String get() = p1.noMatchingProjectsTitle
    val noMatchingProjectsMsg: String get() = p1.noMatchingProjectsMsg
    val loadingProjects: String get() = p1.loadingProjects
    val menuResizeCanvas: String get() = p1.menuResizeCanvas
    val menuRename: String get() = p1.menuRename
    val menuDuplicate: String get() = p1.menuDuplicate
    val menuFavorite: String get() = p1.menuFavorite
    val menuUnfavorite: String get() = p1.menuUnfavorite
    val menuArchive: String get() = p1.menuArchive
    val menuUnarchive: String get() = p1.menuUnarchive
    val renameCollageTitle: String get() = p1.renameCollageTitle
    val projectTitleLabel: String get() = p1.projectTitleLabel
    val deleteProjectMsg: String get() = p1.deleteProjectMsg
    val removeLocalOnly: String get() = p1.removeLocalOnly
    val deleteCloudOnly: String get() = p1.deleteCloudOnly
    val deleteLocalAndCloud: String get() = p1.deleteLocalAndCloud
    val photosWord: String get() = p1.photosWord
    val exportsTitle: String get() = p1.exportsTitle
    val exportsSubtitle: String get() = p1.exportsSubtitle
    val noExportsTitle: String get() = p1.noExportsTitle
    val noExportsMsg: String get() = p1.noExportsMsg
    val loadingExports: String get() = p1.loadingExports
    val editorTitle: String get() = p1.editorTitle
    val editorExport: String get() = p1.editorExport
    val editorTabLayout: String get() = p1.editorTabLayout
    val editorTabBorder: String get() = p1.editorTabBorder
    val editorTabBackground: String get() = p1.editorTabBackground
    val editorToolText: String get() = p1.editorToolText
    val editorToolSticker: String get() = p1.editorToolSticker
    val editorToolFilter: String get() = p1.editorToolFilter
    val editorToolBackground: String get() = p1.editorToolBackground
    val editorToolMore: String get() = p1.editorToolMore
    val noActiveCollageTitle: String get() = p1.noActiveCollageTitle
    val noActiveCollageMsg: String get() = p1.noActiveCollageMsg
    val goHomeLabel: String get() = p1.goHomeLabel
    val layerPhoto: String get() = p1.layerPhoto
    val layerText: String get() = p1.layerText
    val layerSticker: String get() = p1.layerSticker
    val editPhoto: String get() = p1.editPhoto
    val editText: String get() = p1.editText
    val editSticker: String get() = p1.editSticker
    val dontDeleteLabel: String get() = p1.dontDeleteLabel
    val dontDeleteOnLabel: String get() = p1.dontDeleteOnLabel
    val protectedLabel: String get() = p1.protectedLabel
    val unlockAndDelete: String get() = p1.unlockAndDelete
    val markDontDelete: String get() = p1.markDontDelete
    val deleteConfirmMsg: String get() = p1.deleteConfirmMsg
    val protectedConfirmMsg: String get() = p1.protectedConfirmMsg
    val borderRadius: String get() = p1.borderRadius
    val borderSpacing: String get() = p1.borderSpacing
    val borderPadding: String get() = p1.borderPadding
    val borderColor: String get() = p1.borderColor
    val softCellShadow: String get() = p1.softCellShadow
    val solidPalette: String get() = p1.solidPalette
    val gradientsAndImage: String get() = p1.gradientsAndImage
    val pickImage: String get() = p1.pickImage
    val paperTexture: String get() = p1.paperTexture
    val addTextLayer: String get() = p1.addTextLayer
    val editTextLayer: String get() = p1.editTextLayer
    val writeCaptionPlaceholder: String get() = p1.writeCaptionPlaceholder
    val inbuiltSystemFonts: String get() = p1.inbuiltSystemFonts
    val boldLabel: String get() = p1.boldLabel
    val pillBackground: String get() = p1.pillBackground
    val sizeLabel: String get() = p1.sizeLabel
    val textColor: String get() = p1.textColor
    val addToCollage: String get() = p1.addToCollage
    val updateText: String get() = p1.updateText
    val openSourceStickers: String get() = p1.openSourceStickers
    val onCanvasKeepOrDelete: String get() = p1.onCanvasKeepOrDelete
    val addNewStickerTint: String get() = p1.addNewStickerTint
    val editableLabel: String get() = p1.editableLabel
    val filterAndAdjustments: String get() = p1.filterAndAdjustments
    val filtersLabel: String get() = p1.filtersLabel
    val detailAndFocus: String get() = p1.detailAndFocus
    val sharpenLabel: String get() = p1.sharpenLabel
    val vignetteLabel: String get() = p1.vignetteLabel
    val lightAndTone: String get() = p1.lightAndTone
    val brightnessLabel: String get() = p1.brightnessLabel
    val exposureLabel: String get() = p1.exposureLabel
    val contrastLabel: String get() = p1.contrastLabel
    val highlightsLabel: String get() = p1.highlightsLabel
    val shadowsLabel: String get() = p1.shadowsLabel
    val colorAndFinish: String get() = p1.colorAndFinish
    val saturationLabel: String get() = p1.saturationLabel
    val vibranceLabel: String get() = p1.vibranceLabel
    val warmthLabel: String get() = p1.warmthLabel
    val tintLabel: String get() = p1.tintLabel
    val fadeMatteLabel: String get() = p1.fadeMatteLabel
    val selectedLayerTitle: String get() = p1.selectedLayerTitle
    val replaceLabel: String get() = p1.replaceLabel
    val swapLabel: String get() = p1.swapLabel
    val fitWholeLabel: String get() = p1.fitWholeLabel
    val fillCellLabel: String get() = p1.fillCellLabel
    val rotate90Label: String get() = p1.rotate90Label
    val flipHLabel: String get() = p1.flipHLabel
    val flipVLabel: String get() = p1.flipVLabel
    val zoomScaleLabel: String get() = p1.zoomScaleLabel
    val opacityLabel: String get() = p1.opacityLabel
    val perPhotoRadius: String get() = p1.perPhotoRadius
    val moreToolsAndLayers: String get() = p1.moreToolsAndLayers
    val canvasSizeAndRatio: String get() = p1.canvasSizeAndRatio
    val canvasSizeAndRatioSub: String get() = p1.canvasSizeAndRatioSub
    val addPhotosTitle: String get() = p1.addPhotosTitle
    val addPhotosSub: String get() = p1.addPhotosSub
    val addSamplePhotosTitle: String get() = p1.addSamplePhotosTitle
    val addSamplePhotosSub: String get() = p1.addSamplePhotosSub
    val showGuidesTitle: String get() = p1.showGuidesTitle
    val hideGuidesTitle: String get() = p1.hideGuidesTitle
    val guidesSub: String get() = p1.guidesSub
    val stylePresetsLabel: String get() = p1.stylePresetsLabel
    val saveStylePlaceholder: String get() = p1.saveStylePlaceholder
    val layersLabel: String get() = p1.layersLabel
    val autoFitStudioTitle: String get() = p1.autoFitStudioTitle
    val autoFitStudioSubtitle: String get() = p1.autoFitStudioSubtitle
    val autoFitStyleLabel: String get() = p1.autoFitStyleLabel
    val currentLabel: String get() = p1.currentLabel
    val autoFitOnNewPhotos: String get() = p1.autoFitOnNewPhotos
    val autoFitOnNewPhotosSub: String get() = p1.autoFitOnNewPhotosSub
    val autoFitManualNote: String get() = p1.autoFitManualNote
    val applyAutoFitLabel: String get() = p1.applyAutoFitLabel

    val customSizeTitle: String get() = p2.customSizeTitle
    val liveResizePreview: String get() = p2.liveResizePreview
    val invalidSize: String get() = p2.invalidSize
    val customSizePresets: String get() = p2.customSizePresets
    val customSizeDimensions: String get() = p2.customSizeDimensions
    val customSizeWidth: String get() = p2.customSizeWidth
    val customSizeHeight: String get() = p2.customSizeHeight
    val customSizeDpi: String get() = p2.customSizeDpi
    val customDpiOption: String get() = p2.customDpiOption
    val customSizeOrientation: String get() = p2.customSizeOrientation
    val customSizePortrait: String get() = p2.customSizePortrait
    val customSizeLandscape: String get() = p2.customSizeLandscape
    val contentResizeMode: String get() = p2.contentResizeMode
    val customSizeMaintainRatio: String get() = p2.customSizeMaintainRatio
    val autoFitLayoutCorners: String get() = p2.autoFitLayoutCorners
    val autoFitLayoutCornersSub: String get() = p2.autoFitLayoutCornersSub
    val customSizeExtendCanvas: String get() = p2.customSizeExtendCanvas
    val extendCanvasSub: String get() = p2.extendCanvasSub
    val outputLabel: String get() = p2.outputLabel
    val customSizeApply: String get() = p2.customSizeApply
    val exportTitle: String get() = p2.exportTitle
    val exportFormat: String get() = p2.exportFormat
    val exportResolution: String get() = p2.exportResolution
    val exportQuality: String get() = p2.exportQuality
    val qualitySharePrivacy: String get() = p2.qualitySharePrivacy
    val exportTransparentBg: String get() = p2.exportTransparentBg
    val exportTransparentSub: String get() = p2.exportTransparentSub
    val exportNoAlphaNote: String get() = p2.exportNoAlphaNote
    val exportStripExif: String get() = p2.exportStripExif
    val exportStripExifSub: String get() = p2.exportStripExifSub
    val exportAutoShare: String get() = p2.exportAutoShare
    val exportAutoShareSub: String get() = p2.exportAutoShareSub
    val exportSaveGallery: String get() = p2.exportSaveGallery
    val exportingLabel: String get() = p2.exportingLabel
    val exportSaveAndShare: String get() = p2.exportSaveAndShare
    val exportSaveAs: String get() = p2.exportSaveAs
    val exportCancel: String get() = p2.exportCancel
    val exportDirectShare: String get() = p2.exportDirectShare
    val exportOpen: String get() = p2.exportOpen
    val exportCompleteTitle: String get() = p2.exportCompleteTitle
    val zeroWatermarksFullQuality: String get() = p2.zeroWatermarksFullQuality
    val export8kNote: String get() = p2.export8kNote
    val formatDescPng: String get() = p2.formatDescPng
    val formatDescJpeg: String get() = p2.formatDescJpeg
    val formatDescHeic: String get() = p2.formatDescHeic
    val formatDescWebp: String get() = p2.formatDescWebp
    val formatDescWebpLossless: String get() = p2.formatDescWebpLossless
    val formatDescBmp: String get() = p2.formatDescBmp
    val formatDescTiff: String get() = p2.formatDescTiff
    val formatDescPdf: String get() = p2.formatDescPdf
    val resCanvasSize: String get() = p2.resCanvasSize
    val settingsTitle: String get() = p2.settingsTitle
    val settingsThemes: String get() = p2.settingsThemes
    val palettesWord: String get() = p2.palettesWord
    val settingsAppFont: String get() = p2.settingsAppFont
    val settingsAppFontSub: String get() = p2.settingsAppFontSub
    val settingsDarkMode: String get() = p2.settingsDarkMode
    val modePrefix: String get() = p2.modePrefix
    val darkModeSystem: String get() = p2.darkModeSystem
    val darkModeLight: String get() = p2.darkModeLight
    val darkModeDark: String get() = p2.darkModeDark
    val settingsLanguage: String get() = p2.settingsLanguage
    val settingsNotifications: String get() = p2.settingsNotifications
    val notificationsReadySub: String get() = p2.notificationsReadySub
    val notificationsTapSub: String get() = p2.notificationsTapSub
    val settingsPermissions: String get() = p2.settingsPermissions
    val settingsPermissionsSub: String get() = p2.settingsPermissionsSub
    val settingsCloudSync: String get() = p2.settingsCloudSync
    val settingsCloudSyncSub: String get() = p2.settingsCloudSyncSub
    val settingsStripExif: String get() = p2.settingsStripExif
    val settingsStripExifSub: String get() = p2.settingsStripExifSub
    val settingsDefaultDpi: String get() = p2.settingsDefaultDpi
    val settingsTipsAndTricks: String get() = p2.settingsTipsAndTricks
    val settingsTipsSub: String get() = p2.settingsTipsSub
    val settingsAbout: String get() = p2.settingsAbout
    val settingsHelp: String get() = p2.settingsHelp
    val settingsPrivacy: String get() = p2.settingsPrivacy
    val settingsOpenSource: String get() = p2.settingsOpenSource
    val settingsSignOut: String get() = p2.settingsSignOut
    val settingsDeleteData: String get() = p2.settingsDeleteData
    val settingsDeleteDataSub: String get() = p2.settingsDeleteDataSub
    val accountAndSyncTitle: String get() = p2.accountAndSyncTitle
    val openSourceBannerTitle: String get() = p2.openSourceBannerTitle
    val madeWithLoveByRahul: String get() = p2.madeWithLoveByRahul
    val signedInWithGoogle: String get() = p2.signedInWithGoogle
    val permissionsSheetDesc: String get() = p2.permissionsSheetDesc
    val galleryPickerTitle: String get() = p2.galleryPickerTitle
    val galleryPickerSub: String get() = p2.galleryPickerSub
    val filesFolderTitle: String get() = p2.filesFolderTitle
    val filesFolderSub: String get() = p2.filesFolderSub
    val openSystemSettings: String get() = p2.openSystemSettings
    val allowNotificationsBtn: String get() = p2.allowNotificationsBtn
    val tipsSheetSubtitle: String get() = p2.tipsSheetSubtitle
    val defaultDpiTitle: String get() = p2.defaultDpiTitle
    val deleteAllTitle: String get() = p2.deleteAllTitle
    val deleteAllMsg: String get() = p2.deleteAllMsg
    val deleteEverythingBtn: String get() = p2.deleteEverythingBtn
    val aboutBody1: String get() = p2.aboutBody1
    val helpBody1: String get() = p2.helpBody1
    val helpBody2: String get() = p2.helpBody2
    val privacyBody1: String get() = p2.privacyBody1
    val privacyBody2: String get() = p2.privacyBody2
    val openSourceBody1: String get() = p2.openSourceBody1
    val openSourceBody2: String get() = p2.openSourceBody2
    val layoutGrid: String get() = p2.layoutGrid
    val layoutHero: String get() = p2.layoutHero
    val layoutMasonry: String get() = p2.layoutMasonry
    val layoutBento: String get() = p2.layoutBento
    val layoutStrip: String get() = p2.layoutStrip
    val layoutMosaic: String get() = p2.layoutMosaic
    val layoutFreeform: String get() = p2.layoutFreeform
    val resizeFit: String get() = p2.resizeFit
    val resizeFill: String get() = p2.resizeFill
    val resizeCrop: String get() = p2.resizeCrop
    val resizeExtend: String get() = p2.resizeExtend
    val autoFitBalanced: String get() = p2.autoFitBalanced
    val autoFitSeamless: String get() = p2.autoFitSeamless
    val autoFitGallery: String get() = p2.autoFitGallery
    val autoFitSoftCards: String get() = p2.autoFitSoftCards
    val autoFitWholePhoto: String get() = p2.autoFitWholePhoto

    fun layoutLabel(type: LayoutType): String = when (type) {
        LayoutType.GRID -> layoutGrid
        LayoutType.ASYMMETRIC -> layoutHero
        LayoutType.MASONRY -> layoutMasonry
        LayoutType.BENTO -> layoutBento
        LayoutType.STRIP -> layoutStrip
        LayoutType.MOSAIC -> layoutMosaic
        LayoutType.FREEFORM -> layoutFreeform
    }

    fun resizeModeLabel(mode: ResizeMode): String = when (mode) {
        ResizeMode.FIT -> resizeFit
        ResizeMode.FILL -> resizeFill
        ResizeMode.CROP -> resizeCrop
        ResizeMode.EXTEND -> resizeExtend
    }

    fun darkModeLabel(opt: DarkModeOption): String = when (opt) {
        DarkModeOption.SYSTEM -> darkModeSystem
        DarkModeOption.LIGHT -> darkModeLight
        DarkModeOption.DARK -> darkModeDark
    }

    fun layerKindLabel(type: LayerType): String = when (type) {
        LayerType.PHOTO -> layerPhoto
        LayerType.TEXT -> layerText
        LayerType.STICKER -> layerSticker
    }

    fun autoFitPresetLabel(preset: AutoFitPreset): String = when (preset) {
        AutoFitPreset.BALANCED -> autoFitBalanced
        AutoFitPreset.SEAMLESS -> autoFitSeamless
        AutoFitPreset.GALLERY -> autoFitGallery
        AutoFitPreset.SOFT_CARDS -> autoFitSoftCards
        AutoFitPreset.WHOLE_PHOTO -> autoFitWholePhoto
    }

    fun resolutionPresetLabel(preset: ExportResolutionPreset): String = when (preset) {
        ExportResolutionPreset.ORIGINAL -> resCanvasSize
        else -> preset.label
    }

    fun formatDescription(format: ExportFormat): String = when (format) {
        ExportFormat.PNG -> formatDescPng
        ExportFormat.JPEG -> formatDescJpeg
        ExportFormat.HEIC -> formatDescHeic
        ExportFormat.WEBP -> formatDescWebp
        ExportFormat.WEBP_LOSSLESS -> formatDescWebpLossless
        ExportFormat.BMP -> formatDescBmp
        ExportFormat.TIFF -> formatDescTiff
        ExportFormat.PDF -> formatDescPdf
    }

    fun translateSaveStatus(raw: String): String = when (raw) {
        "Saving…" -> savingStatus
        "Synced" -> syncedStatus
        else -> savedStatus
    }
}

val LocalAppStrings = staticCompositionLocalOf { stringsForLanguage("en-GB") }

fun languageDisplayName(code: String): String {
    return SupportedAppLanguages.firstOrNull { it.code == code }?.let {
        if (it.nativeLabel == it.englishName) it.nativeLabel else "${it.nativeLabel} (${it.englishName})"
    } ?: "English (UK)"
}

fun stringsForLanguage(code: String): AppStrings = when (code) {
    "en-GB" -> englishUkStrings()
    "es" -> spanishStrings()
    "pt" -> portugueseStrings()
    "id" -> indonesianStrings()
    "zh-TW" -> taiwaneseStrings()
    "fil" -> filipinoStrings()
    "fr" -> frenchStrings()
    "de" -> germanStrings()
    "ko" -> koreanStrings()
    "ar" -> arabicStrings()
    "ru" -> russianStrings()
    "vi" -> vietnameseStrings()
    "th" -> thaiStrings()
    "hi" -> hindiStrings()
    "ne" -> nepaliStrings()
    "ja" -> japaneseStrings()
    "ms" -> malayStrings()
    "tr" -> turkishStrings()
    "zh" -> chineseStrings()
    else -> englishUsStrings()
}

internal fun englishUsStrings() = AppStrings(
    p1 = AppStringsPart1(
    languageCode = "en",
    versionWord = "Version",
    closeLabel = "Close",
    cancelLabel = "Cancel",
    saveLabel = "Save",
    deleteLabel = "Delete",
    keepLabel = "Keep",
    resetLabel = "Reset",
    selectedLabel = "Selected",
    activeLabel = "Active",
    allowLabel = "Allow",
    allowedLabel = "Allowed",
    chooseLabel = "Choose",
    grantedLabel = "Granted",
    continueLabel = "Continue",
    tryAgainLabel = "Try Again",
    loadingLabel = "Loading…",
    languageUpdatedPrefix = "Language changed to",
    manualLabel = "Manual",
    autoFitLabel = "Auto-Fit",
    savedStatus = "Saved",
    savingStatus = "Saving…",
    syncedStatus = "Synced",
    welcomeTagline = "Create Stunning Collages with Your Memories",
    welcomeGoogleCta = "Continue with Google",
    welcomeAboutCta = "About CollageX & Privacy",
    openSourceBadge = "Open Source · Free Forever",
    welcomeAboutBody1 = "CollageX is completely free and open source with zero paywalls, zero watermarks, and zero AI gimmicks. Every layout, 50+ photo grid, Auto-Fit Studio, custom canvas sizing, and HEIC/PNG/JPEG/WebP Lossless/BMP/TIFF/PDF export is unlocked for everyone.",
    welcomeAboutBody2 = "Sign in with Google to securely isolate and sync your collage projects, custom canvas settings, and export history across devices via Firebase Firestore. Your photos remain private on your device.",
    navHome = "Home",
    navProjects = "Projects",
    navCreate = "Create Collage",
    navExports = "Exports",
    navSettings = "Settings",
    homeGreetingPrefix = "Hello,",
    homeSubtitle = "Turn your photos into beautiful collages",
    homeNewCollage = "New Collage",
    homeStartCreating = "Start creating now",
    homeQuickTools = "Quick Tools",
    homeRecentCollages = "Recent Collages",
    actionSeeAll = "See all",
    toolTemplates = "Templates",
    toolGallery = "Gallery",
    toolLayout = "Layout",
    toolTools = "Tools",
    noCollagesTitle = "No collages yet",
    noCollagesMsg = "Pick 2 to 50+ photos from your gallery, or launch a starter collage to explore the editor right away.",
    tryStarterCollage = "Try Starter Collage",
    loadingCollages = "Loading your collages…",
    starterTemplatesTitle = "Starter Templates",
    starterTemplatesSubtitle = "Choose a layout preset and start with your photos or sample scenes.",
    createWithLayoutTitle = "Create with Layout",
    collageToolsTitle = "Collage Tools",
    createNewCollageTitle = "Create New Collage",
    createNewCollageSubtitle = "Pick your ratio & Auto-Fit style — spacing, borders & corners organize automatically when you select photos (editable anytime).",
    targetRatioAndSize = "Target Ratio & Size",
    autoFitOrganization = "Auto-Fit Organization",
    pickPhotosFromGallery = "Pick Photos from Gallery (2–50+)",
    photoWallMosaicTitle = "50+ Photo Wall Mosaic",
    photoWallMosaicSubtitle = "Adaptive dense grid auto-fitted for large collections",
    starterPackTitle = "Start with 6-Photo Starter Pack",
    starterPackSubtitle = "Auto-fitted sample collage ready to customize",
    printReadyA4Title = "Print-Ready A4 Collage (300 DPI)",
    projectsTitle = "Projects & Layouts",
    searchCollagesPlaceholder = "Search collages…",
    filterAll = "All",
    filterFavorites = "Favorites",
    filterArchived = "Archived",
    sortRecent = "Sort: Recent",
    sortName = "Sort: A–Z",
    sortPhotos = "Sort: Photos",
    noMatchingProjectsTitle = "No matching projects",
    noMatchingProjectsMsg = "Create a new collage or adjust your search filter.",
    loadingProjects = "Loading projects…",
    menuResizeCanvas = "Resize Canvas",
    menuRename = "Rename",
    menuDuplicate = "Duplicate",
    menuFavorite = "Favorite",
    menuUnfavorite = "Unfavorite",
    menuArchive = "Archive",
    menuUnarchive = "Unarchive",
    renameCollageTitle = "Rename Collage",
    projectTitleLabel = "Project title",
    deleteProjectMsg = "Choose whether to remove only the local copy on this device or also delete its cloud backup.",
    removeLocalOnly = "Remove Local Copy Only",
    deleteCloudOnly = "Delete Cloud Backup Only",
    deleteLocalAndCloud = "Delete Local & Cloud",
    photosWord = "photos",
    exportsTitle = "Activity & Exports",
    exportsSubtitle = "High-resolution exports with zero watermarks.",
    noExportsTitle = "No exports yet",
    noExportsMsg = "Open any collage in the editor and tap Export to save HEIC, PNG, JPEG, WebP Lossless, BMP, TIFF, or PDF.",
    loadingExports = "Loading export history…",
    editorTitle = "Edit Collage",
    editorExport = "Export",
    editorTabLayout = "Layout",
    editorTabBorder = "Border",
    editorTabBackground = "Background",
    editorToolText = "Text",
    editorToolSticker = "Sticker",
    editorToolFilter = "Filter",
    editorToolBackground = "Background",
    editorToolMore = "More",
    noActiveCollageTitle = "No active collage",
    noActiveCollageMsg = "Return home to pick photos or open an existing project.",
    goHomeLabel = "Go Home",
    layerPhoto = "Photo",
    layerText = "Text",
    layerSticker = "Sticker",
    editPhoto = "Edit Photo",
    editText = "Edit Text",
    editSticker = "Edit Sticker",
    dontDeleteLabel = "Don't Delete",
    dontDeleteOnLabel = "Don't Delete: ON",
    protectedLabel = "Protected",
    unlockAndDelete = "Unlock & Delete",
    markDontDelete = "Mark Don't Delete (Protect)",
    deleteConfirmMsg = "Do you want to delete this item from your collage, or keep it? (You can undo anytime.)",
    protectedConfirmMsg = "This item is marked Don't Delete (Protected). You can keep it safe or unlock and delete it now.",
    borderRadius = "Corner Radius",
    borderSpacing = "Cell Spacing",
    borderPadding = "Outer Margin",
    borderColor = "Border Color",
    softCellShadow = "Soft Cell Shadow",
    solidPalette = "Solid Pastel Palette",
    gradientsAndImage = "Pastel Gradients & Image",
    pickImage = "Pick Image",
    paperTexture = "Paper Texture",
    addTextLayer = "Add Text Layer",
    editTextLayer = "Edit Text Layer",
    writeCaptionPlaceholder = "Write a caption…",
    inbuiltSystemFonts = "Inbuilt & System Fonts",
    boldLabel = "Bold",
    pillBackground = "Pill Background",
    sizeLabel = "Size",
    textColor = "Text Color",
    addToCollage = "Add to Collage",
    updateText = "Update Text",
    openSourceStickers = "Open-Source Stickers",
    onCanvasKeepOrDelete = "On Canvas — Keep or Delete",
    addNewStickerTint = "Add New Sticker · Tint",
    editableLabel = "Editable",
    filterAndAdjustments = "Filter & Adjustments",
    filtersLabel = "Filters",
    detailAndFocus = "Detail & Focus",
    sharpenLabel = "Sharpen",
    vignetteLabel = "Vignette",
    lightAndTone = "Light & Tone",
    brightnessLabel = "Brightness",
    exposureLabel = "Exposure",
    contrastLabel = "Contrast",
    highlightsLabel = "Highlights",
    shadowsLabel = "Shadows",
    colorAndFinish = "Color & Finish",
    saturationLabel = "Saturation",
    vibranceLabel = "Vibrance",
    warmthLabel = "Warmth",
    tintLabel = "Tint",
    fadeMatteLabel = "Fade (Matte)",
    selectedLayerTitle = "Selected Layer",
    replaceLabel = "Replace",
    swapLabel = "Swap",
    fitWholeLabel = "Fit Whole",
    fillCellLabel = "Fill Cell",
    rotate90Label = "Rotate 90°",
    flipHLabel = "Flip H",
    flipVLabel = "Flip V",
    zoomScaleLabel = "Zoom / Scale",
    opacityLabel = "Opacity",
    perPhotoRadius = "Per-Photo Radius",
    moreToolsAndLayers = "More Tools & Layers",
    canvasSizeAndRatio = "Canvas Size & Aspect Ratio",
    canvasSizeAndRatioSub = "Resize this existing collage in px, in, cm, mm or pt",
    addPhotosTitle = "Add Photos (2–50+)",
    addPhotosSub = "Import additional photos from your gallery",
    addSamplePhotosTitle = "Add 10 Sample Photos (Stress Test)",
    addSamplePhotosSub = "Quickly test 25–50+ multi-photo layouts",
    showGuidesTitle = "Show Snap & Center Guides",
    hideGuidesTitle = "Hide Snap & Center Guides",
    guidesSub = "Alignment crosshair and safe margin frame",
    stylePresetsLabel = "Style Presets",
    saveStylePlaceholder = "Save current border & bg as…",
    layersLabel = "Layers",
    autoFitStudioTitle = "Auto-Fit Studio",
    autoFitStudioSubtitle = "Automatically organizes layout, spacing, borders & corners for your photos.",
    autoFitStyleLabel = "Auto-Fit Style",
    currentLabel = "Current",
    autoFitOnNewPhotos = "Auto-Fit on New Photos",
    autoFitOnNewPhotosSub = "Re-organize spacing, borders & corners automatically when adding photos",
    autoFitManualNote = "Manual edits always stay unlocked — tweak any layout, border, spacing, corner radius, or photo crop anytime.",
    applyAutoFitLabel = "Apply Auto-Fit",
    ),
    p2 = AppStringsPart2(
    customSizeTitle = "Custom Size",
    liveResizePreview = "Live Resize Preview",
    invalidSize = "Invalid size",
    customSizePresets = "Aspect Ratio Presets",
    customSizeDimensions = "Custom Dimensions",
    customSizeWidth = "Width",
    customSizeHeight = "Height",
    customSizeDpi = "Resolution (DPI)",
    customDpiOption = "Custom DPI…",
    customSizeOrientation = "Orientation",
    customSizePortrait = "Portrait",
    customSizeLandscape = "Landscape",
    contentResizeMode = "Content Resize Mode",
    customSizeMaintainRatio = "Maintain Aspect Ratio",
    autoFitLayoutCorners = "Auto-Fit Layout, Spacing & Corners",
    autoFitLayoutCornersSub = "Automatically organize borders and corners to match this ratio & size",
    customSizeExtendCanvas = "Extend Canvas",
    extendCanvasSub = "Fill new area with background instead of scaling",
    outputLabel = "Output",
    customSizeApply = "Apply Changes",
    exportTitle = "Export Collage",
    exportFormat = "Format",
    exportResolution = "Output Resolution",
    exportQuality = "Compression Quality",
    qualitySharePrivacy = "Quality, Share & Privacy",
    exportTransparentBg = "Transparent Background",
    exportTransparentSub = "Export without solid canvas backdrop",
    exportNoAlphaNote = "Switch to PNG, WebP, WebP Lossless, BMP, or TIFF for a transparent background.",
    exportStripExif = "Strip EXIF Metadata",
    exportStripExifSub = "Remove GPS location and camera metadata from exported files",
    exportAutoShare = "Direct Share after export",
    exportAutoShareSub = "Automatically open Android Share sheet as soon as export finishes",
    exportSaveGallery = "Save to Gallery",
    exportingLabel = "Exporting…",
    exportSaveAndShare = "Save & Direct Share",
    exportSaveAs = "Save to Folder…",
    exportCancel = "Cancel Export",
    exportDirectShare = "Direct Share",
    exportOpen = "Open",
    exportCompleteTitle = "Export Complete",
    zeroWatermarksFullQuality = "Zero watermarks · Full quality",
    export8kNote = "Ultra-high 6K/8K output uses memory-safe rendering. TIFF streams strip-by-strip.",
    formatDescPng = "Lossless PNG with optional transparent background.",
    formatDescJpeg = "Compact photographic JPEG with adjustable compression quality.",
    formatDescHeic = "Next-gen HEIC (HEIF/HEVC) high-efficiency photo format — crisp detail at half the file size.",
    formatDescWebp = "Modern high-efficiency WebP supporting alpha and quality control.",
    formatDescWebpLossless = "Zero-loss WebP Lossless with crisp edges, alpha transparency & smaller files than PNG.",
    formatDescBmp = "Uncompressed 24/32-bit Studio BMP with zero compression artifacts and DPI metadata.",
    formatDescTiff = "Archival print TIFF written with memory-safe strip streaming (supports up to 8K).",
    formatDescPdf = "Print-ready vector-framed PDF sized accurately to canvas DPI.",
    resCanvasSize = "Canvas Size",
    settingsTitle = "Settings",
    settingsThemes = "Themes",
    palettesWord = "palettes",
    settingsAppFont = "App Font",
    settingsAppFontSub = "Inbuilt Google Fonts & Android System Set",
    settingsDarkMode = "Dark Mode",
    modePrefix = "Mode",
    darkModeSystem = "System",
    darkModeLight = "Light",
    darkModeDark = "Dark",
    settingsLanguage = "Language",
    settingsNotifications = "Push Notifications",
    notificationsReadySub = "Export completion alerts ready",
    notificationsTapSub = "Tap to allow push notifications",
    settingsPermissions = "Permissions & Access",
    settingsPermissionsSub = "Notifications, Gallery Photo Picker & Folder access",
    settingsCloudSync = "Cloud Auto-Sync",
    settingsCloudSyncSub = "Back up collage metadata and preferences to Firestore",
    settingsStripExif = "Strip EXIF Metadata",
    settingsStripExifSub = "Protect location privacy on every exported collage",
    settingsDefaultDpi = "Default Print DPI",
    settingsTipsAndTricks = "Beginner's Tips & Tricks",
    settingsTipsSub = "6 quick beginner-friendly guides for stunning collages",
    settingsAbout = "About CollageX",
    settingsHelp = "Help & Support",
    settingsPrivacy = "Privacy Policy",
    settingsOpenSource = "Open Source",
    settingsSignOut = "Sign Out",
    settingsDeleteData = "Delete Account Data",
    settingsDeleteDataSub = "Permanently delete local projects, exports, and Firestore backup",
    accountAndSyncTitle = "Account & Sync",
    openSourceBannerTitle = "100% Free & Open Source",
    madeWithLoveByRahul = "Made with ❤️ by Rahul Shah",
    signedInWithGoogle = "Signed in with Google",
    permissionsSheetDesc = "Manage notifications, gallery access, and folder export permissions cleanly without intrusive background storage access.",
    galleryPickerTitle = "Gallery Access (Photo Picker)",
    galleryPickerSub = "Active via Android Photo Picker — pick 2 to 50+ photos safely with zero broad storage permissions.",
    filesFolderTitle = "Files & Export Folder Access",
    filesFolderSub = "Grant persistent access to any folder on your device via Android Storage Access Framework.",
    openSystemSettings = "Open Android App Settings",
    allowNotificationsBtn = "Allow Notifications",
    tipsSheetSubtitle = "Master CollageX in minutes with these simple workflows.",
    defaultDpiTitle = "Default Canvas DPI",
    deleteAllTitle = "Delete all account data?",
    deleteAllMsg = "This removes all local projects, export records, and Firestore cloud backups for your account. Exported images already saved to your device gallery remain untouched.",
    deleteEverythingBtn = "Delete Everything",
    aboutBody1 = "CollageX 5.5 Elite is a native Kotlin & Jetpack Compose collage studio built for creators who want complete control over 2 to 50+ photo layouts, Auto-Fit Studio, Delete & Don't Delete layer protection, 12 filters with Sharpen & Vignette grading, 14 inbuilt & system fonts, 12 themes, 20 full-UI languages, and HEIC / PNG / JPEG / WebP Lossless / BMP / 8K TIFF / PDF export.",
    helpBody1 = "• Tap any photo, text, or sticker on the canvas to edit, protect (Don't Delete), or delete cleanly.\n• Tap Auto-Fit or the aspect ratio pill above the canvas to organize spacing, borders, and corners automatically or resize in px, in, cm, mm, or pt.\n• Use Undo/Redo freely — every edit is non-destructive.",
    helpBody2 = "• Check out Beginner's Tips & Tricks in Settings for a complete 6-step guide. Export supports HEIC, PNG, JPEG, WebP, WebP Lossless, BMP, strip-streamed 8K TIFF, and print-ready PDF with Direct Share.",
    privacyBody1 = "Your photos are processed locally on your device and never uploaded to third-party servers. Photo selection uses the zero-permission Android Photo Picker, and folder saving uses MediaStore & Storage Access Framework.",
    privacyBody2 = "When Cloud Auto-Sync is enabled, only your project layout structure, canvas dimensions, preferences, and export metadata are stored securely under your private Firebase account. You can delete all cloud and local data anytime.",
    openSourceBody1 = "CollageX 5.5 Elite is built with open Android technologies: Kotlin, Jetpack Compose, Material 3, Room, DataStore, Coil, and 10 bundled Google Fonts plus Android system fonts.",
    openSourceBody2 = "Every single feature — 50+ photo grids, Auto-Fit Studio, custom print DPI sizing, HEIC/BMP/WebP Lossless/8K TIFF/PDF export, all 12 themes, and all 20 languages — is free forever for everyone.",
    layoutGrid = "Grid",
    layoutHero = "Hero",
    layoutMasonry = "Masonry",
    layoutBento = "Bento",
    layoutStrip = "Strip",
    layoutMosaic = "Mosaic",
    layoutFreeform = "Freeform",
    resizeFit = "Fit",
    resizeFill = "Fill",
    resizeCrop = "Crop",
    resizeExtend = "Extend",
    autoFitBalanced = "Balanced",
    autoFitSeamless = "Seamless",
    autoFitGallery = "Gallery Frame",
    autoFitSoftCards = "Soft Cards",
    autoFitWholePhoto = "Whole Photo",
    ),
)

internal fun englishUkStrings(): AppStrings {
    val us = englishUsStrings()
    return AppStrings(
        p1 = us.p1.copy(
            languageCode = "en-GB",
            homeSubtitle = "Turn your photos into lovely collages",
            borderColor = "Border Colour",
            textColor = "Text Colour",
            colorAndFinish = "Colour & Finish",
            filterFavorites = "Favourites",
            menuFavorite = "Favourite",
            menuUnfavorite = "Unfavourite",
            autoFitOrganization = "Auto-Fit Organisation",
        ),
        p2 = us.p2.copy(
            customSizeTitle = "Customise Size",
            settingsThemes = "Colour Themes",
        ),
    )
}
