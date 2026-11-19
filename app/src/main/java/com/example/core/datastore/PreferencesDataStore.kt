package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.model.DarkModeOption
import com.example.core.model.DimensionUnit
import com.example.core.model.ThemePalette
import com.example.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.collageXDataStore: DataStore<Preferences> by preferencesDataStore(name = "collagex_prefs")

class PreferencesDataStore(context: Context) {
    private val dataStore = context.applicationContext.collageXDataStore

    private object Keys {
        val THEME_PALETTE = stringPreferencesKey("theme_palette")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language_code")
        val UI_FONT_ID = stringPreferencesKey("ui_font_id")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val STRIP_EXIF = booleanPreferencesKey("strip_exif_default")
        val DEFAULT_DPI = intPreferencesKey("default_dpi")
        val DEFAULT_UNIT = stringPreferencesKey("default_unit")
        val CLOUD_AUTO_SYNC = booleanPreferencesKey("cloud_auto_sync")
        val AUTO_SHARE_EXPORT = booleanPreferencesKey("auto_share_after_export")
        val PERMISSIONS_PROMPT_SEEN = booleanPreferencesKey("permissions_prompt_seen")
    }

    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e
        }
        .map { prefs ->
            UserPreferences(
                themePalette = ThemePalette.fromRaw(prefs[Keys.THEME_PALETTE]),
                darkMode = DarkModeOption.fromRaw(prefs[Keys.DARK_MODE]),
                languageCode = prefs[Keys.LANGUAGE] ?: "en-GB",
                uiFontId = prefs[Keys.UI_FONT_ID] ?: "PLUS_JAKARTA",
                notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: false,
                stripExifDefault = prefs[Keys.STRIP_EXIF] ?: true,
                defaultDpi = prefs[Keys.DEFAULT_DPI] ?: 300,
                defaultUnit = DimensionUnit.fromRaw(prefs[Keys.DEFAULT_UNIT]),
                cloudAutoSync = prefs[Keys.CLOUD_AUTO_SYNC] ?: true,
                autoShareAfterExport = prefs[Keys.AUTO_SHARE_EXPORT] ?: false,
                permissionsPromptSeen = prefs[Keys.PERMISSIONS_PROMPT_SEEN] ?: false,
            )
        }

    suspend fun update(prefs: UserPreferences) {
        dataStore.edit { store ->
            store[Keys.THEME_PALETTE] = prefs.themePalette.name
            store[Keys.DARK_MODE] = prefs.darkMode.name
            store[Keys.LANGUAGE] = prefs.languageCode
            store[Keys.UI_FONT_ID] = prefs.uiFontId
            store[Keys.NOTIFICATIONS] = prefs.notificationsEnabled
            store[Keys.STRIP_EXIF] = prefs.stripExifDefault
            store[Keys.DEFAULT_DPI] = prefs.defaultDpi
            store[Keys.DEFAULT_UNIT] = prefs.defaultUnit.name
            store[Keys.CLOUD_AUTO_SYNC] = prefs.cloudAutoSync
            store[Keys.AUTO_SHARE_EXPORT] = prefs.autoShareAfterExport
            store[Keys.PERMISSIONS_PROMPT_SEEN] = prefs.permissionsPromptSeen
        }
    }
}
