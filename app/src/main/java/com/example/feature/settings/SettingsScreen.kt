package com.example.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.R
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
import com.example.core.model.DarkModeOption
import com.example.core.model.ThemePalette
import com.example.core.model.UserPreferences
import com.example.data.firebase.signOutGoogle
import com.example.ui.theme.CollageXThemeTokens
import java.util.Locale

private data class LanguageOption(val code: String, val label: String)

private val SupportedLanguages = listOf(
    LanguageOption("en", "English"),
    LanguageOption("es", "Español"),
    LanguageOption("fr", "Français"),
    LanguageOption("de", "Deutsch"),
    LanguageOption("ja", "日本語"),
    LanguageOption("hi", "हिन्दी"),
)

private enum class SettingsInfoSheet {
    ABOUT,
    HELP,
    PRIVACY,
    OPEN_SOURCE,
}

/**
 * Screen 5 — Settings & Preferences.
 * Matches the CollageX mockup: Account card, 6 pastel themes, Dark Mode, Language,
 * Notifications (runtime Android 13+ permission), Export/Privacy controls, Info sheets,
 * and the 100% Free & Open Source footer banner.
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showAccountSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showDarkModeSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showDpiSheet by remember { mutableStateOf(false) }
    var activeInfoSheet by remember { mutableStateOf<SettingsInfoSheet?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            onUpdatePreferences { it.copy(notificationsEnabled = true) }
            onShowToast("Export notifications enabled")
        } else {
            onUpdatePreferences { it.copy(notificationsEnabled = false) }
            onShowToast("Notification permission denied")
        }
    }

    val currentLanguageLabel = remember(preferences.languageCode) {
        SupportedLanguages.firstOrNull { it.code == preferences.languageCode }?.label ?: "English"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        CxTopBar(
            title = stringResource(R.string.settings_title),
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
                // 1. Account Profile Card (matches mockup)
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
                                    text = userEmail.ifBlank { "Signed in with Google" },
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

                // 2. Appearance & App Preferences Card (Themes, Dark Mode, Language, Notifications)
                item {
                    CxCard(contentPadding = PaddingValues(horizontal = CxSpace.S12, vertical = CxSpace.S8)) {
                        CxListItem(
                            title = stringResource(R.string.settings_themes),
                            leadingIcon = Icons.Rounded.Palette,
                            leadingTint = colors.primary,
                            leadingContainerColor = colors.toolPeach,
                            trailingValue = preferences.themePalette.displayName,
                            onClick = { showThemeSheet = true },
                            testTag = "settings_themes_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxSwitchRow(
                            title = stringResource(R.string.settings_dark_mode),
                            subtitle = "Mode: ${preferences.darkMode.displayName}",
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
                                    label = opt.displayName,
                                    selected = preferences.darkMode == opt,
                                    onClick = { onUpdatePreferences { it.copy(darkMode = opt) } },
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_dark_opt_${opt.name}",
                                )
                            }
                        }
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = stringResource(R.string.settings_language),
                            leadingIcon = Icons.Rounded.Language,
                            leadingTint = colors.text,
                            leadingContainerColor = colors.toolBlue,
                            trailingValue = currentLanguageLabel,
                            onClick = { showLanguageSheet = true },
                            testTag = "settings_language_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxSwitchRow(
                            title = stringResource(R.string.settings_notifications),
                            leadingIcon = Icons.Rounded.Notifications,
                            leadingTint = colors.text,
                            leadingBg = colors.surface2,
                            checked = preferences.notificationsEnabled,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) == PackageManager.PERMISSION_GRANTED
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
                    }
                }

                // 3. Cloud Sync & Export Defaults Card
                item {
                    CxCard(contentPadding = PaddingValues(horizontal = CxSpace.S12, vertical = CxSpace.S8)) {
                        CxSwitchRow(
                            title = stringResource(R.string.settings_cloud_sync),
                            subtitle = "Back up collage metadata and preferences to Firestore",
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
                            title = stringResource(R.string.settings_strip_exif),
                            subtitle = "Protect location privacy on every exported collage",
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
                            title = stringResource(R.string.settings_default_dpi),
                            leadingIcon = Icons.Rounded.Print,
                            leadingTint = colors.text,
                            leadingContainerColor = colors.toolLavender,
                            trailingValue = "${preferences.defaultDpi} DPI",
                            onClick = { showDpiSheet = true },
                            testTag = "settings_default_dpi_row",
                        )
                    }
                }

                // 4. About, Help, Privacy, Open Source Card (matches mockup)
                item {
                    CxCard(contentPadding = PaddingValues(horizontal = CxSpace.S12, vertical = CxSpace.S8)) {
                        CxListItem(
                            title = stringResource(R.string.settings_about),
                            leadingIcon = Icons.Rounded.Info,
                            leadingTint = colors.text,
                            leadingContainerColor = colors.toolPeach,
                            onClick = { activeInfoSheet = SettingsInfoSheet.ABOUT },
                            testTag = "settings_about_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = stringResource(R.string.settings_help),
                            leadingIcon = Icons.AutoMirrored.Rounded.HelpOutline,
                            leadingTint = colors.text,
                            leadingContainerColor = colors.toolLavender,
                            onClick = { activeInfoSheet = SettingsInfoSheet.HELP },
                            testTag = "settings_help_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = stringResource(R.string.settings_privacy),
                            leadingIcon = Icons.Rounded.PrivacyTip,
                            leadingTint = colors.success,
                            leadingContainerColor = colors.toolBlue,
                            onClick = { activeInfoSheet = SettingsInfoSheet.PRIVACY },
                            testTag = "settings_privacy_row",
                        )
                        HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                        CxListItem(
                            title = stringResource(R.string.settings_open_source),
                            leadingIcon = Icons.Rounded.Favorite,
                            leadingTint = colors.primary,
                            leadingContainerColor = colors.primarySoft,
                            onClick = { activeInfoSheet = SettingsInfoSheet.OPEN_SOURCE },
                            testTag = "settings_open_source_row",
                        )
                    }
                }

                // 5. Prominent "100% Free & Open Source" Banner matching Screen 5 mockup
                item {
                    Surface(
                        shape = RoundedCornerShape(CxRadius.Card),
                        color = colors.primarySoft,
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CxRadius.Card))
                            .clickable(role = Role.Button) {
                                activeInfoSheet = SettingsInfoSheet.OPEN_SOURCE
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
                                    text = stringResource(R.string.open_source_banner_title),
                                    style = CxType.BodyMedium,
                                    color = colors.text,
                                )
                                Text(
                                    text = stringResource(R.string.open_source_banner_subtitle),
                                    style = CxType.Caption,
                                    color = colors.textMuted,
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
                Text("Account & Sync", style = CxType.Heading, color = colors.text)
                Text(
                    text = "$userDisplayName · $userEmail",
                    style = CxType.Caption,
                    color = colors.textMuted,
                )
                CxSecondaryButton(
                    text = stringResource(R.string.settings_sign_out),
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
                    title = stringResource(R.string.settings_delete_data),
                    subtitle = "Permanently delete local projects, exports, and Firestore backup",
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

    // 6 Pastel Theme Picker Sheet
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
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text("Choose Theme Palette", style = CxType.Heading, color = colors.text)
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

    // Language Picker Sheet
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S8),
            ) {
                Text("Display Language", style = CxType.Heading, color = colors.text)
                SupportedLanguages.forEach { lang ->
                    val selected = preferences.languageCode == lang.code
                    CxListItem(
                        title = lang.label,
                        trailingValue = if (selected) "Selected" else null,
                        showChevron = false,
                        onClick = {
                            onUpdatePreferences { it.copy(languageCode = lang.code) }
                            showLanguageSheet = false
                            onShowToast("Language set to ${lang.label}")
                        },
                    )
                }
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
                Text("Default Canvas DPI", style = CxType.Heading, color = colors.text)
                listOf(
                    72 to "72 DPI — Web & Screen",
                    96 to "96 DPI — Desktop Standard",
                    150 to "150 DPI — Proof Print",
                    300 to "300 DPI — High-Res Print (Recommended)",
                ).forEach { (dpi, desc) ->
                    CxListItem(
                        title = "$dpi DPI",
                        subtitle = desc,
                        trailingValue = if (preferences.defaultDpi == dpi) "Active" else null,
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
                    "About CollageX",
                    "CollageX v1.0 is a native Kotlin & Jetpack Compose collage studio built for creators who want complete control over 2 to 50+ photo layouts, non-destructive post-creation custom canvas sizing, and high-resolution export.",
                    "No AI gimmicks, no locked tools, no subscriptions, and no watermarks—ever.",
                )
                SettingsInfoSheet.HELP -> Triple(
                    "Help & Quick Tips",
                    "• Tap any photo cell in the editor to zoom, pan, rotate, apply color filters, or swap cells.\n• Tap the aspect ratio pill above the canvas anytime to resize in px, in, cm, mm, or pt with Fit, Fill, Crop, or Extend.\n• Use Undo/Redo freely—every edit and resize is non-destructive.",
                    "• Export supports JPEG, PNG (with alpha), WebP, strip-streamed TIFF, and print-ready PDF up to 8K.",
                )
                SettingsInfoSheet.PRIVACY -> Triple(
                    "Privacy Policy",
                    "Your photos are processed locally on your device and never uploaded to third-party servers. Photo selection uses the zero-permission Android Photo Picker.",
                    "When Cloud Auto-Sync is enabled, only your project layout structure, canvas dimensions, preferences, and export metadata are stored securely under your private Firebase account. You can delete all cloud and local data at any time from Account & Sync.",
                )
                SettingsInfoSheet.OPEN_SOURCE -> Triple(
                    "100% Free & Open Source",
                    "CollageX is built with open Android technologies: Kotlin, Jetpack Compose, Material 3, Room, DataStore, Coil, and Google Fonts (Plus Jakarta Sans, Playfair Display, Pacifico, Space Grotesk).",
                    "Every single feature—50+ photo grids, custom print DPI sizing, TIFF/PDF export, and all 6 pastel themes—is free forever for everyone.",
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Text(title, style = CxType.Heading, color = colors.text)
                Text(body1, style = CxType.Body, color = colors.textMuted)
                Text(body2, style = CxType.Body, color = colors.textMuted)
                CxPrimaryButton(
                    text = "Close",
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
            title = { Text("Delete all account data?", style = CxType.Heading, color = colors.text) },
            text = {
                Text(
                    "This removes all local projects, export records, and Firestore cloud backups for your account. Exported images already saved to your device gallery remain untouched.",
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
                    Text("Delete Everything", color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
        )
    }
}
