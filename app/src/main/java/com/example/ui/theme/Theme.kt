package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.core.designsystem.CxColors
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxType
import com.example.core.designsystem.cxColorsFor
import com.example.core.model.DarkModeOption
import com.example.core.model.ThemePalette

val LocalCxColors = staticCompositionLocalOf {
    cxColorsFor(ThemePalette.PASTEL_PINK, dark = false)
}

object CollageXThemeTokens {
    val colors: CxColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCxColors.current
}

@Composable
fun CollageXTheme(
    palette: ThemePalette = ThemePalette.PASTEL_PINK,
    darkModeOption: DarkModeOption = DarkModeOption.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDark = when (darkModeOption) {
        DarkModeOption.SYSTEM -> isSystemInDarkTheme()
        DarkModeOption.LIGHT -> false
        DarkModeOption.DARK -> true
    }

    val cxColors = cxColorsFor(palette, isDark)

    val m3ColorScheme = if (isDark) {
        darkColorScheme(
            primary = cxColors.primary,
            onPrimary = cxColors.onPrimary,
            primaryContainer = cxColors.primarySoft,
            onPrimaryContainer = cxColors.text,
            background = cxColors.bg,
            onBackground = cxColors.text,
            surface = cxColors.surface,
            onSurface = cxColors.text,
            surfaceVariant = cxColors.surface2,
            onSurfaceVariant = cxColors.textMuted,
            outline = cxColors.border,
            outlineVariant = cxColors.border,
            error = cxColors.danger,
            onError = cxColors.onPrimary,
        )
    } else {
        lightColorScheme(
            primary = cxColors.primary,
            onPrimary = cxColors.onPrimary,
            primaryContainer = cxColors.primarySoft,
            onPrimaryContainer = cxColors.text,
            background = cxColors.bg,
            onBackground = cxColors.text,
            surface = cxColors.surface,
            onSurface = cxColors.text,
            surfaceVariant = cxColors.surface2,
            onSurfaceVariant = cxColors.textMuted,
            outline = cxColors.border,
            outlineVariant = cxColors.border,
            error = cxColors.danger,
            onError = cxColors.onPrimary,
        )
    }

    val m3Typography = Typography(
        headlineMedium = CxType.Title,
        headlineSmall = CxType.Title,
        titleLarge = CxType.Title,
        titleMedium = CxType.Heading,
        titleSmall = CxType.BodyMedium,
        bodyLarge = CxType.Input,
        bodyMedium = CxType.Body,
        bodySmall = CxType.Caption,
        labelLarge = CxType.BodyMedium,
        labelMedium = CxType.CaptionMedium,
        labelSmall = CxType.Caption,
    )

    val m3Shapes = Shapes(
        small = RoundedCornerShape(CxRadius.Small),
        medium = RoundedCornerShape(CxRadius.Card),
        large = RoundedCornerShape(CxRadius.Card),
        extraLarge = RoundedCornerShape(CxRadius.Card),
    )

    CompositionLocalProvider(LocalCxColors provides cxColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = m3Typography,
            shapes = m3Shapes,
            content = content,
        )
    }
}
