package com.example.feature.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.BuildConfig
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.R
import com.example.core.designsystem.CanvasFonts
import com.example.core.designsystem.CxCard
import com.example.core.designsystem.CxChip
import com.example.core.designsystem.CxListItem
import com.example.core.designsystem.CxPrimaryButton
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSecondaryButton
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxSwitchRow
import com.example.core.designsystem.CxTopBar
import com.example.core.designsystem.CxType
import com.example.core.designsystem.cxColorsFor
import com.example.core.i18n.SupportedAppLanguages
import com.example.core.i18n.languageDisplayName
import com.example.core.model.DarkModeOption
import com.example.core.model.ThemePalette
import com.example.core.model.UserPreferences
import com.example.data.firebase.signOutGoogle
import com.example.ui.theme.CollageXThemeTokens
import java.util.Locale

private enum class SettingsInfoSheet {
    ABOUT,
    HELP,
    PRIVACY,
    OPEN_SOURCE,
}

private data class BeginnerTip(
    val title: String,
    val body: String,
    val icon: ImageVector,
)

private val BeginnerTipsList = listOf(
    BeginnerTip(
        title = "1. Pick 2 to 50+ Photos Effortlessly",
        body = "Tap New Collage or the + button to multi-select up to 100 photos using the zero-permission Android Photo Picker. For 15+ photos, switch to Mosaic or Grid so every shot gets balanced breathing room.",
        icon = Icons.Rounded.GridView,
    ),
    BeginnerTip(
        title = "2. Tap, Drag, Pan & Swap Any Cell",
        body = "Tap a photo on the canvas to select it, then drag to reframe inside its cell. Tap the floating pencil icon (top-right) to zoom, rotate, flip, adjust opacity, or Swap positions with another photo.",
        icon = Icons.Rounded.TouchApp,
    ),
    BeginnerTip(
        title = "3. Resize Anytime After Creating",
        body = "Tap the aspect ratio badge (top-left of the canvas) anytime to switch between 1:1, 4:5, 9:16, 16:9, or print sizes (A4, A3, Letter) in px, in, cm, mm, or pt at 300 DPI. Use Extend to add margin without cropping.",
        icon = Icons.Rounded.AspectRatio,
    ),
    BeginnerTip(
        title = "4. Pro Filter, Sharpen & Vignette Grading",
        body = "Select a photo and tap Filter to explore 12 curated looks (Blossom, Warm, Vivid, Sunset, Matte, Noir and more). Dial in Sharpen (15–35%) for crisp edges and a subtle Vignette (20–30%) to draw focus inward.",
        icon = Icons.Rounded.AutoFixHigh,
    ),
    BeginnerTip(
        title = "5. Custom Inbuilt & System Fonts",
        body = "Tap Text in the editor to pick from 14 inbuilt Google Fonts and Android system fonts (Jakarta, Poppins, Montserrat, Playfair, Pacifico, Dancing Script, Bebas Neue, JetBrains Mono, and System fonts). You can also change the entire app's font right here in Settings!",
        icon = Icons.Rounded.TextFields,
    ),
    BeginnerTip(
        title = "6. Watermark-Free 6K/8K, HEIC & Direct Share",
        body = "Export in HEIC (half-size high efficiency), PNG (lossless / transparent), JPEG, WebP, WebP Lossless, uncompressed Studio BMP, strip-streamed 8K TIFF, or print-ready PDF. Tap Save & Direct Share to send your collage straight to WhatsApp, Instagram, or Messages.",
        icon = Icons.Rounded.Share,
    ),
)

/**
 * Screen 5 — Settings & Preferences.
 * Features: Account card, 12 themes, App UI Font (Inbuilt + System), Dark Mode, 8 live languages,
 * Push Notifications & Permissions Center, Export/Privacy controls, Beginner's Tips & Tricks,
 * About CollageX crediting Rahul Shah, and the 100% Free & Open Source footer banner.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userDisplayName: String,
    userEmail: String,
    userPhotoUrl: String?,
    preferences: UserPreferences,
    onUpdatePreferences: ((UserPreferences) -> UserPreferences) -> Unit,
    onDeleteAllData: (onComplete: () -> Unit) -> Unit,
    onShowToast: (String) -> Unit,
    bottomPadding: PaddingValues,
) {
    val colors = CollageXThemeTokens.colors
    val strings = CollageXThemeTokens.strings
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showAccountSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showFontSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showPermissionsSheet by remember { mutableStateOf(false) }
    var showTipsSheet by remember { mutableStateOf(false) }
    var showDpiSheet by remember { mutableStateOf(false) }
    var activeInfoSheet by remember { mutableStateOf<SettingsInfoSheet?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var grantedFolderLabel by remember { mutableStateOf<String?>(null) }

    var notificationsGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            },
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationsGranted = granted
        if (granted) {
            onUpdatePreferences { it.copy(notificationsEnabled = true) }
            onShowToast("Push notifications enabled")
        } else {
            onUpdatePreferences { it.copy(notificationsEnabled = false) }
            onShowToast("Notification permission denied — enable in Android App Settings")
        }
    }

    // SAF folder access launcher (Play-Policy compliant alternative to broad storage permissions)
    val folderAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
            }
            grantedFolderLabel = uri.lastPathSegment?.substringAfterLast(':') ?: "Selected Folder"
            onShowToast("Folder access granted for exports")
        }
    }

    // Zero-permission Photo Picker test launcher inside Permissions Center
    val galleryTestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            onShowToast("Gallery Photo Picker is ready")
        }
    }

    val currentLanguageLabel = remember(preferences.languageCode) {
        languageDisplayName(preferences.languageCode)
    }
    val currentFontLabel = remember(preferences.uiFontId) {
        CanvasFonts.uiFontLabel(preferences.uiFontId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        CxTopBar(
            title = strings.settingsTitle,
            leadingIcon = Icons.Rounded.Settings,
            centerTitle = false,
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
                    top = CxSpace.S8,
                    bottom = bottomPadding.calculateBottomPadding() + CxSpace.S24,
                ),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S16),
            ) {
                // 1. Account Profile Card
                item {
                    CxCard(
                        onClick = { showAccountSheet = true },
                        modifier = Modifier.testTag("settings_account_card"),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CxSpace.S12),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(colors.primarySoft)
                                    .border(2.dp, colors.border, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (!userPhotoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = userPhotoUrl,
                                        contentDescription = stringResource(R.string.cd_profile),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    Text(
                                        text = userDisplayName.trim().take(1)
                                            .uppercase(Locale.getDefault()).ifBlank { "C" },
                                        style = CxType.Title,
                                        color = colors.primary,
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
                            ) {
                                Text(
                                    text = userDisplayName.ifBlank { "Creator" },
                                    style = CxType.Heading,
                                    color = colors.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = userEmail.ifBlank { strings.signedInWithGoogle },
                                    style = CxType.Caption,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = colors.textMuted,
                            )
                        }
                    }
                }

                // 2. Appearance, Fonts, Languages & Permissions Card
                item {
                    CxCard(contentPadding = PaddingValues(horizontal = CxSpace.S12, vertical = CxSpace.S8)) {
                        CxListItem(
                            title = strings.settingsThemes,
                            subtitle = "${ThemePalette.entries.size} ${strings.palettesWord}",
                            leadingIcon = Icons.Rounded.Palette,
                            leadingTint = colors.primary,
                            leadingContainerColor = colors.toolPeach,
                            trailingValue = preferences.themePalette.displayName,
                            onClick = { showThemeSheet = true },
                            testTag = "settings_themes_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = strings.settingsAppFont,
                            subtitle = strings.settingsAppFontSub,
                            leadingIcon = Icons.Rounded.FontDownload,
                            leadingTint = colors.primary,
                            leadingContainerColor = colors.toolLavender,
                            trailingValue = currentFontLabel,
                            onClick = { showFontSheet = true },
                            testTag = "settings_font_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxSwitchRow(
                            title = strings.settingsDarkMode,
                            subtitle = "${strings.modePrefix}: ${strings.darkModeLabel(preferences.darkMode)}",
                            leadingIcon = Icons.Rounded.DarkMode,
                            leadingTint = colors.text,
                            leadingBg = colors.surface2,
                            checked = preferences.darkMode == DarkModeOption.DARK,
                            onCheckedChange = { isDark ->
                                onUpdatePreferences {
                                    it.copy(darkMode = if (isDark) DarkModeOption.DARK else DarkModeOption.LIGHT)
                                }
                            },
                            testTag = "settings_dark_mode_switch",
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = CxSpace.S12, end = CxSpace.S12, bottom = CxSpace.S4),
                            horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                        ) {
                            DarkModeOption.entries.forEach { opt ->
                                CxChip(
                                    label = strings.darkModeLabel(opt),
                                    selected = preferences.darkMode == opt,
                                    onClick = { onUpdatePreferences { it.copy(darkMode = opt) } },
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_dark_opt_${opt.name}",
                                )
                            }
                        }
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = strings.settingsLanguage,
                            leadingIcon = Icons.Rounded.Language,
                            leadingTint = colors.text,
                            leadingContainerColor = colors.toolBlue,
                            trailingValue = currentLanguageLabel,
                            onClick = { showLanguageSheet = true },
                            testTag = "settings_language_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxSwitchRow(
                            title = strings.settingsNotifications,
                            subtitle = if (notificationsGranted) strings.notificationsReadySub else strings.notificationsTapSub,
                            leadingIcon = Icons.Rounded.Notifications,
                            leadingTint = colors.text,
                            leadingBg = colors.surface2,
                            checked = preferences.notificationsEnabled && notificationsGranted,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) == PackageManager.PERMISSION_GRANTED
                                    notificationsGranted = granted
                                    if (granted) {
                                        onUpdatePreferences { it.copy(notificationsEnabled = true) }
                                    } else {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    onUpdatePreferences { it.copy(notificationsEnabled = wantEnable) }
                                }
                            },
                            testTag = "settings_notifications_switch",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = strings.settingsPermissions,
                            subtitle = strings.settingsPermissionsSub,
                            leadingIcon = Icons.Rounded.AdminPanelSettings,
                            leadingTint = colors.success,
                            leadingContainerColor = colors.surface2,
                            onClick = { showPermissionsSheet = true },
                            testTag = "settings_permissions_row",
                        )
                    }
                }

                // 3. Cloud Sync & Export Defaults Card
                item {
                    CxCard(contentPadding = PaddingValues(horizontal = CxSpace.S12, vertical = CxSpace.S8)) {
                        CxSwitchRow(
                            title = strings.settingsCloudSync,
                            subtitle = strings.settingsCloudSyncSub,
                            leadingIcon = Icons.Rounded.CloudSync,
                            leadingTint = colors.primary,
                            leadingBg = colors.primarySoft,
                            checked = preferences.cloudAutoSync,
                            onCheckedChange = { enabled ->
                                onUpdatePreferences { it.copy(cloudAutoSync = enabled) }
                            },
                            testTag = "settings_cloud_sync_switch",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxSwitchRow(
                            title = strings.exportAutoShare,
                            subtitle = strings.exportAutoShareSub,
                            leadingIcon = Icons.Rounded.Share,
                            leadingTint = colors.primary,
                            leadingBg = colors.toolPink,
                            checked = preferences.autoShareAfterExport,
                            onCheckedChange = { enabled ->
                                onUpdatePreferences { it.copy(autoShareAfterExport = enabled) }
                            },
                            testTag = "settings_auto_share_switch",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxSwitchRow(
                            title = strings.settingsStripExif,
                            subtitle = strings.settingsStripExifSub,
                            leadingIcon = Icons.Rounded.Shield,
                            leadingTint = colors.success,
                            leadingBg = colors.surface2,
                            checked = preferences.stripExifDefault,
                            onCheckedChange = { enabled ->
                                onUpdatePreferences { it.copy(stripExifDefault = enabled) }
                            },
                            testTag = "settings_exif_switch",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = strings.settingsDefaultDpi,
                            leadingIcon = Icons.Rounded.Print,
                            leadingTint = colors.text,
                            leadingContainerColor = colors.toolLavender,
                            trailingValue = "${preferences.defaultDpi} DPI",
                            onClick = { showDpiSheet = true },
                            testTag = "settings_default_dpi_row",
                        )
                    }
                }

                // 4. Beginner's Tips, About, Help, Privacy & Open Source Card
                item {
                    CxCard(contentPadding = PaddingValues(horizontal = CxSpace.S12, vertical = CxSpace.S8)) {
                        CxListItem(
                            title = strings.settingsTipsAndTricks,
                            subtitle = strings.settingsTipsSub,
                            leadingIcon = Icons.Rounded.Lightbulb,
                            leadingTint = colors.primary,
                            leadingContainerColor = colors.toolPeach,
                            onClick = { showTipsSheet = true },
                            testTag = "settings_tips_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = strings.settingsAbout,
                            subtitle = strings.madeWithLoveByRahul,
                            leadingIcon = Icons.Rounded.Info,
                            leadingTint = colors.text,
                            leadingContainerColor = colors.toolPeach,
                            trailingValue = "v${BuildConfig.VERSION_NAME}",
                            onClick = { activeInfoSheet = SettingsInfoSheet.ABOUT },
                            testTag = "settings_about_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = strings.settingsHelp,
                            leadingIcon = Icons.AutoMirrored.Rounded.HelpOutline,
                            leadingTint = colors.text,
                            leadingContainerColor = colors.toolLavender,
                            onClick = { activeInfoSheet = SettingsInfoSheet.HELP },
                            testTag = "settings_help_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = strings.settingsPrivacy,
                            leadingIcon = Icons.Rounded.PrivacyTip,
                            leadingTint = colors.success,
                            leadingContainerColor = colors.toolBlue,
                            onClick = { activeInfoSheet = SettingsInfoSheet.PRIVACY },
                            testTag = "settings_privacy_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = strings.settingsOpenSource,
                            leadingIcon = Icons.Rounded.Favorite,
                            leadingTint = colors.primary,
                            leadingContainerColor = colors.primarySoft,
                            onClick = { activeInfoSheet = SettingsInfoSheet.OPEN_SOURCE },
                            testTag = "settings_open_source_row",
                        )
                    }
                }

                // 5. Prominent "100% Free & Open Source · Made with ❤️ by Rahul Shah" Footer Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(CxRadius.Card),
                        color = colors.primarySoft,
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CxRadius.Card))
                            .clickable(role = Role.Button) {
                                activeInfoSheet = SettingsInfoSheet.ABOUT
                            }
                            .testTag("settings_open_source_banner"),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CxSpace.S16),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CxSpace.S12),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Code,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(28.dp),
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
                            ) {
                                Text(
                                    text = "${strings.openSourceBannerTitle} · v${BuildConfig.VERSION_NAME}",
                                    style = CxType.BodyMedium,
                                    color = colors.text,
                                )
                                Text(
                                    text = strings.madeWithLoveByRahul,
                                    style = CxType.CaptionMedium,
                                    color = colors.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Account Management Bottom Sheet
    if (showAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text(strings.accountAndSyncTitle, style = CxType.Heading, color = colors.text)
                Text(
                    text = "$userDisplayName · $userEmail",
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
                CxSecondaryButton(
                    text = strings.settingsSignOut,
                    leadingIcon = Icons.AutoMirrored.Rounded.Logout,
                    onClick = {
                        showAccountSheet = false
                        signOutGoogle(
                            credentialManager = credentialManager,
                            scope = scope,
                        )
                    },
                    testTag = "settings_sign_out_button",
                )
                CxListItem(
                    title = strings.settingsDeleteData,
                    subtitle = strings.settingsDeleteDataSub,
                    leadingIcon = Icons.Rounded.DeleteForever,
                    leadingTint = colors.danger,
                    leadingContainerColor = colors.danger.copy(alpha = 0.12f),
                    onClick = {
                        showAccountSheet = false
                        showDeleteConfirmDialog = true
                    },
                    testTag = "settings_delete_data_row",
                )
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }

    // 12 Theme Palette Picker Sheet (scrollable)
    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                Text("${strings.settingsThemes} (${ThemePalette.entries.size})", style = CxType.Heading, color = colors.text)
                ThemePalette.entries.forEach { palette ->
                    val previewColors = cxColorsFor(palette, dark = false)
                    val selected = preferences.themePalette == palette
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CxRadius.Small))
                            .background(if (selected) colors.primarySoft else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (selected) colors.primary else colors.border,
                                shape = RoundedCornerShape(CxRadius.Small),
                            )
                            .clickable(role = Role.RadioButton) {
                                onUpdatePreferences { it.copy(themePalette = palette) }
                                showThemeSheet = false
                            }
                            .padding(horizontal = CxSpace.S12, vertical = CxSpace.S12)
                            .testTag("theme_option_${palette.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CxSpace.S12),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(previewColors.primary)
                                .border(2.dp, previewColors.surface2, CircleShape),
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(previewColors.surface2)
                                .border(1.dp, previewColors.border, CircleShape),
                        )
                        Text(
                            text = palette.displayName,
                            style = CxType.BodyMedium,
                            color = colors.text,
                            modifier = Modifier.weight(1f),
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = colors.primary,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }

    // App UI Font Picker Sheet (Inbuilt Google Fonts + System Font Set)
    if (showFontSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFontSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                Text(strings.settingsAppFont, style = CxType.Heading, color = colors.text)
                Text(
                    text = "Choose an inbuilt Google Font or an Android System font for the entire CollageX interface.",
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
                CanvasFonts.AppUiFontChoices.forEach { fontChoice ->
                    val selected = preferences.uiFontId == fontChoice.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CxRadius.Small))
                            .background(if (selected) colors.primarySoft else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (selected) colors.primary else colors.border,
                                shape = RoundedCornerShape(CxRadius.Small),
                            )
                            .clickable(role = Role.RadioButton) {
                                onUpdatePreferences { it.copy(uiFontId = fontChoice.id) }
                                showFontSheet = false
                                onShowToast("Font set to ${fontChoice.label}")
                            }
                            .padding(horizontal = CxSpace.S12, vertical = CxSpace.S12)
                            .testTag("font_option_${fontChoice.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fontChoice.label,
                                fontFamily = fontChoice.family,
                                fontSize = 16.sp,
                                color = colors.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${fontChoice.category} Font · Aa Bb Cc 123",
                                fontFamily = fontChoice.family,
                                fontSize = 12.sp,
                                color = colors.textMuted,
                                maxLines = 1,
                            )
                        }
                        if (selected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = colors.primary,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }

    // 8-Language Live Picker Sheet (instant UI refresh across all screens!)
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                Text("${strings.settingsLanguage} (${SupportedAppLanguages.size})", style = CxType.Heading, color = colors.text)
                SupportedAppLanguages.forEach { lang ->
                    val selected = preferences.languageCode == lang.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CxRadius.Small))
                            .background(if (selected) colors.primarySoft else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (selected) colors.primary else colors.border,
                                shape = RoundedCornerShape(CxRadius.Small),
                            )
                            .clickable(role = Role.RadioButton) {
                                onUpdatePreferences { it.copy(languageCode = lang.code) }
                                showLanguageSheet = false
                                onShowToast("${strings.languageUpdatedPrefix} ${lang.nativeLabel}")
                            }
                            .padding(horizontal = CxSpace.S12, vertical = CxSpace.S12)
                            .testTag("language_option_${lang.code}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lang.nativeLabel,
                                style = CxType.BodyMedium,
                                color = colors.text,
                                maxLines = 1,
                            )
                            if (lang.nativeLabel != lang.englishName) {
                                Text(
                                    text = lang.englishName,
                                    style = CxType.Caption,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                )
                            }
                        }
                        if (selected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = colors.primary,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }

    // Permissions & Access Center Sheet (Google Play Policy Compliant)
    if (showPermissionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPermissionsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text(strings.settingsPermissions, style = CxType.Heading, color = colors.text)
                Text(
                    text = strings.permissionsSheetDesc,
                    style = CxType.Caption,
                    color = colors.textMuted,
                )

                CxListItem(
                    title = strings.settingsNotifications,
                    subtitle = if (notificationsGranted) {
                        strings.notificationsReadySub
                    } else {
                        strings.notificationsTapSub
                    },
                    leadingIcon = Icons.Rounded.Notifications,
                    leadingTint = if (notificationsGranted) colors.success else colors.primary,
                    leadingContainerColor = colors.surface2,
                    trailingValue = if (notificationsGranted) strings.allowedLabel else strings.allowLabel,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onUpdatePreferences { it.copy(notificationsEnabled = true) }
                            onShowToast(strings.notificationsReadySub)
                        }
                    },
                    testTag = "perm_notifications_row",
                )

                CxListItem(
                    title = strings.galleryPickerTitle,
                    subtitle = strings.galleryPickerSub,
                    leadingIcon = Icons.Rounded.PhotoLibrary,
                    leadingTint = colors.success,
                    leadingContainerColor = colors.toolBlue,
                    trailingValue = strings.activeLabel,
                    onClick = {
                        galleryTestLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    testTag = "perm_gallery_row",
                )

                CxListItem(
                    title = strings.filesFolderTitle,
                    subtitle = if (grantedFolderLabel != null) {
                        "${strings.grantedLabel}: $grantedFolderLabel (SAF)"
                    } else {
                        strings.filesFolderSub
                    },
                    leadingIcon = Icons.Rounded.FolderOpen,
                    leadingTint = colors.primary,
                    leadingContainerColor = colors.toolPeach,
                    trailingValue = if (grantedFolderLabel != null) strings.grantedLabel else strings.chooseLabel,
                    onClick = {
                        runCatching { folderAccessLauncher.launch(null) }
                            .onFailure { onShowToast("SAF unavailable") }
                    },
                    testTag = "perm_files_row",
                )

                CxSecondaryButton(
                    text = strings.openSystemSettings,
                    leadingIcon = Icons.Rounded.Settings,
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        runCatching { context.startActivity(intent) }
                    },
                    testTag = "perm_system_settings_btn",
                )

                CxPrimaryButton(
                    text = strings.closeLabel,
                    onClick = { showPermissionsSheet = false },
                    testTag = "perm_sheet_close",
                )
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }

    // Beginner's Tips & Tricks Sheet
    if (showTipsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTipsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text(strings.settingsTipsAndTricks, style = CxType.Heading, color = colors.text)
                Text(
                    text = strings.tipsSheetSubtitle,
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
                BeginnerTipsList.forEach { tip ->
                    CxCard(
                        containerColor = colors.surface2,
                        contentPadding = PaddingValues(CxSpace.S12),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                        ) {
                            Icon(
                                imageVector = tip.icon,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = tip.title,
                                style = CxType.BodyMedium,
                                color = colors.text,
                            )
                        }
                        Text(
                            text = tip.body,
                            style = CxType.Caption,
                            color = colors.textMuted,
                        )
                    }
                }
                CxPrimaryButton(
                    text = strings.closeLabel,
                    onClick = { showTipsSheet = false },
                    testTag = "tips_sheet_close",
                )
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }

    // Default DPI Picker Sheet
    if (showDpiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDpiSheet = false },
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                Text(strings.defaultDpiTitle, style = CxType.Heading, color = colors.text)
                listOf(
                    72 to "72 DPI — Web & Screen",
                    96 to "96 DPI — Desktop Standard",
                    150 to "150 DPI — Proof Print",
                    300 to "300 DPI — High-Res Print",
                ).forEach { (dpi, desc) ->
                    CxListItem(
                        title = "$dpi DPI",
                        subtitle = desc,
                        trailingValue = if (preferences.defaultDpi == dpi) strings.activeLabel else null,
                        showChevron = false,
                        onClick = {
                            onUpdatePreferences { it.copy(defaultDpi = dpi) }
                            showDpiSheet = false
                        },
                    )
                }
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }

    // Info Bottom Sheets (About / Help / Privacy / Open Source)
    activeInfoSheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = { activeInfoSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
        ) {
            val (title, body1, body2) = when (sheet) {
                SettingsInfoSheet.ABOUT -> Triple(
                    "${strings.settingsAbout} · v${BuildConfig.VERSION_NAME}",
                    strings.aboutBody1,
                    "${strings.madeWithLoveByRahul} · ${strings.openSourceBadge}",
                )
                SettingsInfoSheet.HELP -> Triple(
                    strings.settingsHelp,
                    strings.helpBody1,
                    strings.helpBody2,
                )
                SettingsInfoSheet.PRIVACY -> Triple(
                    strings.settingsPrivacy,
                    strings.privacyBody1,
                    strings.privacyBody2,
                )
                SettingsInfoSheet.OPEN_SOURCE -> Triple(
                    strings.openSourceBannerTitle,
                    strings.openSourceBody1,
                    "${strings.madeWithLoveByRahul} · ${strings.openSourceBody2}",
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text(title, style = CxType.Heading, color = colors.text)
                if (sheet == SettingsInfoSheet.ABOUT || sheet == SettingsInfoSheet.OPEN_SOURCE) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                        verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(CxRadius.Pill),
                            color = colors.surface2,
                            border = BorderStroke(1.dp, colors.border),
                        ) {
                            Text(
                                text = "${strings.versionWord} ${BuildConfig.VERSION_NAME}",
                                style = CxType.CaptionMedium,
                                color = colors.text,
                                modifier = Modifier.padding(horizontal = CxSpace.S12, vertical = CxSpace.S4),
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(CxRadius.Pill),
                            color = colors.primarySoft,
                        ) {
                            Text(
                                text = strings.madeWithLoveByRahul,
                                style = CxType.CaptionMedium,
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = CxSpace.S12, vertical = CxSpace.S4),
                            )
                        }
                    }
                }
                Text(body1, style = CxType.Body, color = colors.textMuted)
                Text(body2, style = CxType.Body, color = colors.textMuted)
                CxPrimaryButton(
                    text = strings.closeLabel,
                    onClick = { activeInfoSheet = null },
                    testTag = "settings_info_close",
                )
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = colors.surface,
            title = { Text(strings.deleteAllTitle, style = CxType.Heading, color = colors.text) },
            text = {
                Text(
                    strings.deleteAllMsg,
                    style = CxType.Body,
                    color = colors.textMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAllData {
                            signOutGoogle(credentialManager = credentialManager, scope = scope)
                        }
                    },
                ) {
                    Text(strings.deleteEverythingBtn, color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(strings.cancelLabel, color = colors.textMuted)
                }
            },
        )
    }
}
