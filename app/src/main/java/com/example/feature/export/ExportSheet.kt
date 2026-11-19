package com.example.feature.export

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.core.designsystem.CxCard
import com.example.core.designsystem.CxChip
import com.example.core.designsystem.CxErrorState
import com.example.core.designsystem.CxPrimaryButton
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSecondaryButton
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxSwitchRow
import com.example.core.designsystem.CxTopBar
import com.example.core.designsystem.CxType
import com.example.core.model.CanvasMath
import com.example.core.model.CollageProject
import com.example.core.model.ExportFormat
import com.example.core.model.ExportJobConfig
import com.example.core.model.ExportResolutionPreset
import com.example.feature.ExportUiState
import com.example.feature.editor.CollageCanvasPreview
import com.example.ui.theme.CollageXThemeTokens
import java.util.Locale
import kotlin.math.roundToInt

private const val EXPORT_NOTIFICATION_CHANNEL_ID = "collagex_exports"

/**
 * Full-featured Export Screen supporting JPEG, PNG, WebP, strip-streamed TIFF, and PDF,
 * 1080p / 2K / 4K / 8K Tiled / Canvas Size output, transparent alpha toggle, EXIF stripping,
 * MediaStore saving + SAF "Save As…", Direct Share after export, and completion push notifications.
 */
@Composable
fun ExportScreen(
    project: CollageProject,
    defaultStripExif: Boolean,
    notificationsEnabled: Boolean,
    autoShareDefault: Boolean,
    exportState: ExportUiState,
    onBack: () -> Unit,
    onUpdateAutoShare: (Boolean) -> Unit,
    onStartExport: (ExportJobConfig, Uri?) -> Unit,
    onCancelExport: () -> Unit,
    onResetExportState: () -> Unit,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    val context = LocalContext.current

    var format by remember { mutableStateOf(ExportFormat.PNG) }
    var resolutionPreset by remember { mutableStateOf(ExportResolutionPreset.ORIGINAL) }
    var quality by remember { mutableFloatStateOf(92f) }
    var transparentBg by remember { mutableStateOf(false) }
    var stripExif by remember(defaultStripExif) { mutableStateOf(defaultStripExif) }
    var autoShare by remember(autoShareDefault) { mutableStateOf(autoShareDefault) }
    var pendingDirectShare by remember { mutableStateOf(false) }
    var lastHandledUri by remember { mutableStateOf<String?>(null) }

    val activeConfig = remember(format, resolutionPreset, quality, transparentBg, stripExif) {
        ExportJobConfig(
            format = format,
            resolutionPreset = resolutionPreset,
            quality = quality.roundToInt(),
            transparentBg = transparentBg,
            stripExif = stripExif,
        )
    }

    val targetDims = remember(project.canvas, activeConfig) {
        CanvasMath.computeExportDimensions(project.canvas, activeConfig)
    }

    val estimatedSizeLabel = remember(targetDims, format, quality) {
        val rawMb = targetDims.rawRgbaBytes / (1024.0 * 1024.0)
        val factor = when (format) {
            ExportFormat.JPEG -> 0.08 + (quality / 100.0) * 0.14
            ExportFormat.HEIC -> 0.04 + (quality / 100.0) * 0.08
            ExportFormat.WEBP -> 0.06 + (quality / 100.0) * 0.12
            ExportFormat.WEBP_LOSSLESS -> 0.30
            ExportFormat.PNG -> 0.42
            ExportFormat.BMP -> 0.76
            ExportFormat.TIFF -> 0.78
            ExportFormat.PDF -> 0.28
        }
        val mb = (rawMb * factor).coerceAtLeast(0.08)
        if (mb >= 1.0) String.format(Locale.US, "~%.1f MB", mb)
        else String.format(Locale.US, "~%d KB", (mb * 1024).roundToInt())
    }

    // SAF "Save As…" launcher dynamically matched to the currently selected MIME type
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
    ) { uri ->
        if (uri != null) {
            onStartExport(activeConfig, uri)
        }
    }

    fun launchDirectShare(uriString: String, mimeType: String, displayName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, Uri.parse(uriString))
            putExtra(Intent.EXTRA_SUBJECT, displayName)
            putExtra(Intent.EXTRA_TEXT, "Created with CollageX")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(Intent.createChooser(shareIntent, strings.exportDirectShare))
        }
    }

    // Post real Android export notification & trigger Direct Share when requested
    LaunchedEffect(exportState) {
        val success = exportState as? ExportUiState.Success ?: return@LaunchedEffect
        val out = success.output
        if (lastHandledUri == out.uriString) return@LaunchedEffect
        lastHandledUri = out.uriString

        if (notificationsEnabled) {
            postExportCompleteNotification(
                context = context,
                fileName = out.displayName,
                dimensions = "${out.widthPx}×${out.heightPx}px · ${formatFileBytes(out.fileSizeBytes)}",
            )
        }
        if (pendingDirectShare || autoShare) {
            pendingDirectShare = false
            launchDirectShare(out.uriString, out.mimeType, out.displayName)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (exportState is ExportUiState.Success || exportState is ExportUiState.Error) {
                onResetExportState()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        CxTopBar(
            title = strings.exportTitle,
            onBack = {
                if (exportState is ExportUiState.Running) {
                    onCancelExport()
                }
                onBack()
            },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = CxSpace.S16,
                    end = CxSpace.S16,
                    top = CxSpace.S12,
                    bottom = CxSpace.S24,
                ),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S24),
            ) {
                // 1. Live Preview Card with output summary badge
                item {
                    CxCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(CxRadius.Small))
                                .background(colors.surface2),
                            contentAlignment = Alignment.Center,
                        ) {
                            CollageCanvasPreview(
                                project = project,
                                interactive = false,
                                proxyMaxEdgePx = 640,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(CxSpace.S8),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
                            ) {
                                Text(
                                    text = project.title,
                                    style = CxType.BodyMedium,
                                    color = colors.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${targetDims.widthPx} × ${targetDims.heightPx} px · ${project.canvas.dpi} DPI · $estimatedSizeLabel",
                                    style = CxType.Caption,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(Modifier.width(CxSpace.S8))
                            Surface(
                                shape = RoundedCornerShape(CxRadius.Pill),
                                color = colors.primarySoft,
                            ) {
                                Text(
                                    text = format.label,
                                    style = CxType.CaptionMedium,
                                    color = colors.primary,
                                    modifier = Modifier.padding(horizontal = CxSpace.S12, vertical = CxSpace.S4),
                                )
                            }
                        }
                    }
                }

                // 2. Active Export State Banner (Running / Success / Error)
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = exportState !is ExportUiState.Idle,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        when (val state = exportState) {
                            is ExportUiState.Idle -> Unit
                            is ExportUiState.Running -> {
                                CxCard(borderColor = colors.primary.copy(alpha = 0.4f)) {
                                    Text(
                                        text = state.statusMessage,
                                        style = CxType.BodyMedium,
                                        color = colors.text,
                                    )
                                    LinearProgressIndicator(
                                        progress = { state.progress.coerceIn(0f, 1f) },
                                        color = colors.primary,
                                        trackColor = colors.surface2,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(CxRadius.Pill)),
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "${(state.progress * 100).roundToInt()}%",
                                            style = CxType.CaptionMedium,
                                            color = colors.primary,
                                        )
                                        Text(
                                            text = strings.zeroWatermarksFullQuality,
                                            style = CxType.Caption,
                                            color = colors.textMuted,
                                        )
                                    }
                                    CxSecondaryButton(
                                        text = strings.exportCancel,
                                        onClick = {
                                            pendingDirectShare = false
                                            onCancelExport()
                                        },
                                        testTag = "export_cancel_button",
                                    )
                                }
                            }
                            is ExportUiState.Success -> {
                                val out = state.output
                                CxCard(borderColor = colors.success.copy(alpha = 0.5f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(CxSpace.S12),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(colors.success.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = colors.success,
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
                                        ) {
                                            Text(
                                                text = strings.exportCompleteTitle,
                                                style = CxType.Heading,
                                                color = colors.text,
                                            )
                                            Text(
                                                text = "${out.displayName} · ${out.widthPx}×${out.heightPx}px · ${formatFileBytes(out.fileSizeBytes)}",
                                                style = CxType.Caption,
                                                color = colors.textMuted,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                                    ) {
                                        CxSecondaryButton(
                                            text = strings.exportOpen,
                                            leadingIcon = Icons.Rounded.OpenInNew,
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(Uri.parse(out.uriString), out.mimeType)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                runCatching { context.startActivity(intent) }
                                            },
                                            modifier = Modifier.weight(1f),
                                            testTag = "export_open_button",
                                        )
                                        CxPrimaryButton(
                                            text = strings.exportDirectShare,
                                            leadingIcon = Icons.Rounded.Share,
                                            onClick = {
                                                launchDirectShare(out.uriString, out.mimeType, out.displayName)
                                            },
                                            modifier = Modifier.weight(1f),
                                            testTag = "export_share_button",
                                        )
                                    }
                                }
                            }
                            is ExportUiState.Error -> {
                                CxErrorState(
                                    message = state.message,
                                    onRetry = { onStartExport(activeConfig, null) },
                                )
                            }
                        }
                    }
                }

                // 3. File Format Selector
                item {
                    CxCard {
                        Text(
                            text = strings.exportFormat,
                            style = CxType.Heading,
                            color = colors.text,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                            items(ExportFormat.entries) { fmt ->
                                CxChip(
                                    label = fmt.label,
                                    selected = format == fmt,
                                    onClick = { format = fmt },
                                    testTag = "export_format_${fmt.name}",
                                )
                            }
                        }
                        Text(
                            text = strings.formatDescription(format),
                            style = CxType.Caption,
                            color = colors.textMuted,
                        )
                    }
                }

                // 4. Resolution Preset Selector
                item {
                    CxCard {
                        Text(
                            text = strings.exportResolution,
                            style = CxType.Heading,
                            color = colors.text,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(CxSpace.S8)) {
                            items(ExportResolutionPreset.entries) { preset ->
                                CxChip(
                                    label = strings.resolutionPresetLabel(preset),
                                    selected = resolutionPreset == preset,
                                    onClick = { resolutionPreset = preset },
                                    testTag = "export_res_${preset.name}",
                                )
                            }
                        }
                        if (resolutionPreset == ExportResolutionPreset.UHD_8K || resolutionPreset == ExportResolutionPreset.UHD_6K) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = strings.export8kNote,
                                    style = CxType.Caption,
                                    color = colors.textMuted,
                                )
                            }
                        }
                    }
                }

                // 5. Quality, Transparency, Direct Share & Privacy Options
                item {
                    CxCard {
                        Text(
                            text = strings.qualitySharePrivacy,
                            style = CxType.Heading,
                            color = colors.text,
                        )

                        if (format.supportsQuality) {
                            Column(verticalArrangement = Arrangement.spacedBy(CxSpace.S4)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = strings.exportQuality,
                                        style = CxType.BodyMedium,
                                        color = colors.text,
                                    )
                                    Text(
                                        text = "${quality.roundToInt()}%",
                                        style = CxType.CaptionMedium,
                                        color = colors.primary,
                                    )
                                }
                                Slider(
                                    value = quality,
                                    onValueChange = { quality = it },
                                    valueRange = 50f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = colors.primary,
                                        activeTrackColor = colors.primary,
                                        inactiveTrackColor = colors.border,
                                    ),
                                )
                            }
                        }

                        CxSwitchRow(
                            title = strings.exportAutoShare,
                            subtitle = strings.exportAutoShareSub,
                            checked = autoShare,
                            onCheckedChange = { enabled ->
                                autoShare = enabled
                                onUpdateAutoShare(enabled)
                            },
                            testTag = "export_auto_share_switch",
                        )

                        CxSwitchRow(
                            title = strings.exportTransparentBg,
                            subtitle = if (format.supportsAlpha) {
                                strings.exportTransparentSub
                            } else {
                                strings.exportNoAlphaNote
                            },
                            checked = transparentBg && format.supportsAlpha,
                            onCheckedChange = { enabled ->
                                transparentBg = enabled
                            },
                            testTag = "export_transparent_switch",
                        )

                        if (transparentBg && !format.supportsAlpha) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(CxRadius.Small))
                                    .background(colors.primarySoft)
                                    .padding(CxSpace.S12),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = strings.exportNoAlphaNote,
                                    style = CxType.Caption,
                                    color = colors.text,
                                )
                            }
                        }

                        CxSwitchRow(
                            title = strings.exportStripExif,
                            subtitle = strings.exportStripExifSub,
                            checked = stripExif,
                            onCheckedChange = { stripExif = it },
                            testTag = "export_strip_exif_switch",
                        )
                    }
                }
            }
        }

        // Bottom Sticky Action Bar (1 Primary Save + Save & Direct Share / Save As)
        Surface(
            color = colors.surface,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                val isRunning = exportState is ExportUiState.Running
                CxPrimaryButton(
                    text = if (isRunning) strings.exportingLabel else strings.exportSaveGallery,
                    leadingIcon = Icons.Rounded.Download,
                    loading = isRunning,
                    enabled = !isRunning,
                    onClick = {
                        pendingDirectShare = false
                        onStartExport(activeConfig, null)
                    },
                    testTag = "export_save_gallery_button",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    CxSecondaryButton(
                        text = strings.exportSaveAndShare,
                        leadingIcon = Icons.Rounded.Share,
                        enabled = !isRunning,
                        onClick = {
                            pendingDirectShare = true
                            onStartExport(activeConfig, null)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "export_save_and_share_button",
                    )
                    CxSecondaryButton(
                        text = strings.exportSaveAs,
                        leadingIcon = Icons.Rounded.FolderOpen,
                        enabled = !isRunning,
                        onClick = {
                            pendingDirectShare = false
                            val safeName = project.title
                                .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                                .ifBlank { "CollageX" }
                                .take(40)
                            createDocLauncher.launch("${safeName}_${targetDims.widthPx}x${targetDims.heightPx}.${format.extension}")
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "export_save_as_button",
                    )
                }
            }
        }
    }
}

private fun postExportCompleteNotification(
    context: Context,
    fileName: String,
    dimensions: String,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return
    }
    runCatching {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EXPORT_NOTIFICATION_CHANNEL_ID,
                "CollageX Exports",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Alerts when high-resolution collages finish exporting"
            }
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, EXPORT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Collage Exported")
            .setContentText("$fileName ($dimensions)")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(fileName.hashCode(), notification)
    }
}

private fun formatFileBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 KB"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.0f KB", kb)
    return String.format(Locale.US, "%.1f MB", kb / 1024.0)
}
