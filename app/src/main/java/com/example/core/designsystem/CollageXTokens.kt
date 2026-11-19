package com.example.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.ThemePalette
import com.example.ui.theme.LocalCxUiFont

/**
 * Strict Design Tokens for CollageX.
 * All UI spacing, radii, typography, and colors reference this single source of truth.
 */
object CxSpace {
    val S4 = 4.dp
    val S8 = 8.dp
    val S12 = 12.dp
    val S16 = 16.dp
    val S24 = 24.dp
    val S32 = 32.dp
}

object CxRadius {
    val Small = 8.dp
    val Card = 16.dp
    val Pill = 999.dp
}

object CxSizing {
    val TouchTarget = 48.dp
    val ButtonHeight = 48.dp
    val InputHeight = 48.dp
    val TopBarHeight = 56.dp
    val BottomNavHeight = 56.dp
    val IconSize = 24.dp
    val ChipIconSize = 20.dp
}

/** Bundled Plus Jakarta Sans default UI font family. */
val CxUiFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
)

data class FontChoice(
    val id: String,
    val label: String,
    val category: String,
    val family: FontFamily,
)

/**
 * Full inbuilt Google Fonts + Android System Font Families for both Canvas Text Layers & App UI Font selection.
 */
object CanvasFonts {
    val Sans = CxUiFontFamily
    val Poppins = FontFamily(
        Font(R.font.poppins, FontWeight.Normal),
        Font(R.font.poppins, FontWeight.Medium),
        Font(R.font.poppins, FontWeight.Bold),
    )
    val Montserrat = FontFamily(
        Font(R.font.montserrat, FontWeight.Normal),
        Font(R.font.montserrat, FontWeight.Medium),
        Font(R.font.montserrat, FontWeight.Bold),
    )
    val Geometric = FontFamily(Font(R.font.space_grotesk, FontWeight.Normal))
    val Serif = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
    val Lora = FontFamily(Font(R.font.lora, FontWeight.Normal))
    val Script = FontFamily(Font(R.font.pacifico, FontWeight.Normal))
    val DancingScript = FontFamily(Font(R.font.dancing_script, FontWeight.Normal))
    val Display = FontFamily(Font(R.font.bebas_neue, FontWeight.Normal))
    val Mono = FontFamily(Font(R.font.jetbrains_mono, FontWeight.Normal))

    // Android System Font Set
    val SystemDefault = FontFamily.Default
    val SystemSans = FontFamily.SansSerif
    val SystemSerif = FontFamily.Serif
    val SystemMonospace = FontFamily.Monospace
    val SystemCursive = FontFamily.Cursive

    val AllCanvasFonts: List<FontChoice> = listOf(
        FontChoice("SANS", "Jakarta", "Inbuilt", Sans),
        FontChoice("POPPINS", "Poppins", "Inbuilt", Poppins),
        FontChoice("MONTSERRAT", "Montserrat", "Inbuilt", Montserrat),
        FontChoice("SERIF", "Playfair", "Inbuilt", Serif),
        FontChoice("LORA", "Lora", "Inbuilt", Lora),
        FontChoice("SCRIPT", "Pacifico", "Inbuilt", Script),
        FontChoice("DANCING", "Dancing", "Inbuilt", DancingScript),
        FontChoice("GEOMETRIC", "Grotesk", "Inbuilt", Geometric),
        FontChoice("DISPLAY", "Bebas Neue", "Inbuilt", Display),
        FontChoice("MONO", "JetBrains", "Inbuilt", Mono),
        FontChoice("SYS_SANS", "System Sans", "System", SystemSans),
        FontChoice("SYS_SERIF", "System Serif", "System", SystemSerif),
        FontChoice("SYS_MONO", "System Mono", "System", SystemMonospace),
        FontChoice("SYS_CURSIVE", "System Cursive", "System", SystemCursive),
    )

    val AppUiFontChoices: List<FontChoice> = listOf(
        FontChoice("PLUS_JAKARTA", "Plus Jakarta Sans", "Inbuilt", Sans),
        FontChoice("POPPINS", "Poppins", "Inbuilt", Poppins),
        FontChoice("MONTSERRAT", "Montserrat", "Inbuilt", Montserrat),
        FontChoice("LORA", "Lora Serif", "Inbuilt", Lora),
        FontChoice("GEOMETRIC", "Space Grotesk", "Inbuilt", Geometric),
        FontChoice("MONO", "JetBrains Mono", "Inbuilt", Mono),
        FontChoice("SYS_DEFAULT", "System Default", "System", SystemDefault),
        FontChoice("SYS_SANS", "System Sans", "System", SystemSans),
        FontChoice("SYS_SERIF", "System Serif", "System", SystemSerif),
        FontChoice("SYS_MONO", "System Monospace", "System", SystemMonospace),
    )

    fun byId(id: String): FontFamily = when (id) {
        "PLUS_JAKARTA", "SANS" -> Sans
        "POPPINS" -> Poppins
        "MONTSERRAT" -> Montserrat
        "SERIF" -> Serif
        "LORA" -> Lora
        "SCRIPT" -> Script
        "DANCING" -> DancingScript
        "GEOMETRIC" -> Geometric
        "DISPLAY" -> Display
        "MONO" -> Mono
        "SYS_DEFAULT" -> SystemDefault
        "SYS_SANS" -> SystemSans
        "SYS_SERIF" -> SystemSerif
        "SYS_MONO" -> SystemMonospace
        "SYS_CURSIVE" -> SystemCursive
        else -> Sans
    }

    fun uiFontLabel(id: String): String =
        AppUiFontChoices.firstOrNull { it.id == id }?.label ?: "Plus Jakarta Sans"
}

/** Strict Type Scale (Title 22/28, Heading 18/24, Body 15/22, Input 16/22, Caption 12/16) respecting user's chosen UI font. */
object CxType {
    val Title: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = LocalCxUiFont.current,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        )

    val Heading: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = LocalCxUiFont.current,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )

    val Body: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = LocalCxUiFont.current,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )

    val BodyMedium: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = LocalCxUiFont.current,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )

    val Input: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = LocalCxUiFont.current,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        )

    val Caption: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = LocalCxUiFont.current,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

    val CaptionMedium: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = TextStyle(
            fontFamily = LocalCxUiFont.current,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
}

@Immutable
data class CxColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val primary: Color,
    val primarySoft: Color,
    val onPrimary: Color,
    val text: Color,
    val textMuted: Color,
    val border: Color,
    val danger: Color,
    val success: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val toolPink: Color,
    val toolBlue: Color,
    val toolPeach: Color,
    val toolLavender: Color,
)

fun cxColorsFor(palette: ThemePalette, dark: Boolean): CxColors {
    if (dark) {
        val (primary, primarySoft, heroStart, heroEnd) = when (palette) {
            ThemePalette.PASTEL_PINK -> Quad(Color(0xFFFF4F7E), Color(0xFF3E1C28), Color(0xFFFF6D75), Color(0xFFE52A65))
            ThemePalette.LAVENDER -> Quad(Color(0xFFA78BFA), Color(0xFF2B2244), Color(0xFF9F7AEA), Color(0xFF6D28D9))
            ThemePalette.MINT -> Quad(Color(0xFF2DD4BF), Color(0xFF173634), Color(0xFF2DD4BF), Color(0xFF0F766E))
            ThemePalette.PEACH -> Quad(Color(0xFFFB923C), Color(0xFF3D2518), Color(0xFFFB923C), Color(0xFFEA580C))
            ThemePalette.SKY_BLUE -> Quad(Color(0xFF38BDF8), Color(0xFF183042), Color(0xFF38BDF8), Color(0xFF0284C7))
            ThemePalette.NEUTRAL -> Quad(Color(0xFFCBD5E1), Color(0xFF272E3A), Color(0xFF64748B), Color(0xFF334155))
            ThemePalette.SAKURA_ROSE -> Quad(Color(0xFFF43F5E), Color(0xFF3D1924), Color(0xFFFB7185), Color(0xFFBE123C))
            ThemePalette.SUNSET_CORAL -> Quad(Color(0xFFFF6B57), Color(0xFF3E201C), Color(0xFFFF8F6B), Color(0xFFE11D48))
            ThemePalette.FOREST_SAGE -> Quad(Color(0xFF4ADE80), Color(0xFF163524), Color(0xFF34D399), Color(0xFF15803D))
            ThemePalette.OCEAN_TEAL -> Quad(Color(0xFF22D3EE), Color(0xFF14333D), Color(0xFF06B6D4), Color(0xFF0369A1))
            ThemePalette.GOLDEN_HONEY -> Quad(Color(0xFFFBBF24), Color(0xFF3C2D12), Color(0xFFF59E0B), Color(0xFFD97706))
            ThemePalette.MIDNIGHT_INDIGO -> Quad(Color(0xFF818CF8), Color(0xFF1E2248), Color(0xFF6366F1), Color(0xFF4338CA))
        }
        return CxColors(
            bg = Color(0xFF141016),
            surface = Color(0xFF1F1922),
            surface2 = Color(0xFF2A212E),
            primary = primary,
            primarySoft = primarySoft,
            onPrimary = if (palette == ThemePalette.NEUTRAL || palette == ThemePalette.GOLDEN_HONEY) Color(0xFF111827) else Color(0xFFFFFFFF),
            text = Color(0xFFFAF5F7),
            textMuted = Color(0xFFBBAEC2),
            border = Color(0xFF382C3E),
            danger = Color(0xFFFF5C75),
            success = Color(0xFF34D399),
            heroStart = heroStart,
            heroEnd = heroEnd,
            toolPink = Color(0xFF3D2030),
            toolBlue = Color(0xFF1E2F44),
            toolPeach = Color(0xFF3D2A1F),
            toolLavender = Color(0xFF2C2342),
        )
    }

    return when (palette) {
        ThemePalette.PASTEL_PINK -> CxColors(
            bg = Color(0xFFFFF5F7),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFFDE6EC),
            primary = Color(0xFFEA2858),
            primarySoft = Color(0xFFFFE4EC),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF1D1724),
            textMuted = Color(0xFF665A6E),
            border = Color(0xFFF3D8E1),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFFFF8072),
            heroEnd = Color(0xFFE8255A),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.LAVENDER -> CxColors(
            bg = Color(0xFFF8F5FF),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFEDE6FF),
            primary = Color(0xFF7C3AED),
            primarySoft = Color(0xFFEDE9FE),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF1A1526),
            textMuted = Color(0xFF5F5672),
            border = Color(0xFFE2D8F7),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFFA78BFA),
            heroEnd = Color(0xFF6D28D9),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.MINT -> CxColors(
            bg = Color(0xFFF2FBF9),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFDEF7F2),
            primary = Color(0xFF0F766E),
            primarySoft = Color(0xFFCCFBF1),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF132220),
            textMuted = Color(0xFF4C6460),
            border = Color(0xFFCDEBE5),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFF2DD4BF),
            heroEnd = Color(0xFF0F766E),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.PEACH -> CxColors(
            bg = Color(0xFFFFF7F2),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFFFE8DA),
            primary = Color(0xFFEA580C),
            primarySoft = Color(0xFFFFEDD5),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF241812),
            textMuted = Color(0xFF6B574C),
            border = Color(0xFFF5DDD0),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFFFB923C),
            heroEnd = Color(0xFFEA580C),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.SKY_BLUE -> CxColors(
            bg = Color(0xFFF2F9FF),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFE0F1FF),
            primary = Color(0xFF0284C7),
            primarySoft = Color(0xFFE0F2FE),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF121D29),
            textMuted = Color(0xFF4D6073),
            border = Color(0xFFD0E5F7),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFF38BDF8),
            heroEnd = Color(0xFF0284C7),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.NEUTRAL -> CxColors(
            bg = Color(0xFFF8FAFC),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFEFF3F8),
            primary = Color(0xFF1E293B),
            primarySoft = Color(0xFFE2E8F0),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF0F172A),
            textMuted = Color(0xFF475569),
            border = Color(0xFFDCE3EC),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFF475569),
            heroEnd = Color(0xFF1E293B),
            toolPink = Color(0xFFF1F5F9),
            toolBlue = Color(0xFFE2E8F0),
            toolPeach = Color(0xFFF1F5F9),
            toolLavender = Color(0xFFE2E8F0),
        )
        ThemePalette.SAKURA_ROSE -> CxColors(
            bg = Color(0xFFFFF1F4),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFFFDCE5),
            primary = Color(0xFFBE123C),
            primarySoft = Color(0xFFFFE4E8),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF231117),
            textMuted = Color(0xFF6E4B55),
            border = Color(0xFFF9C8D5),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFFFB7185),
            heroEnd = Color(0xFFBE123C),
            toolPink = Color(0xFFFFDFE8),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.SUNSET_CORAL -> CxColors(
            bg = Color(0xFFFFF5F2),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFFFE4DC),
            primary = Color(0xFFE11D48),
            primarySoft = Color(0xFFFFE4E6),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF261414),
            textMuted = Color(0xFF6E4F4C),
            border = Color(0xFFF7CFC6),
            danger = Color(0xFFDC2626),
            success = Color(0xFF15803D),
            heroStart = Color(0xFFFF8C61),
            heroEnd = Color(0xFFE11D48),
            toolPink = Color(0xFFFFE4E6),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFFE8D6),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.FOREST_SAGE -> CxColors(
            bg = Color(0xFFF3FAF5),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFE1F5E7),
            primary = Color(0xFF15803D),
            primarySoft = Color(0xFFDCFCE7),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF122419),
            textMuted = Color(0xFF476352),
            border = Color(0xFFC8E9D2),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFF34D399),
            heroEnd = Color(0xFF15803D),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.OCEAN_TEAL -> CxColors(
            bg = Color(0xFFF0FAFC),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFD9F3F8),
            primary = Color(0xFF0E7490),
            primarySoft = Color(0xFFCFFAFE),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF0E2229),
            textMuted = Color(0xFF46646E),
            border = Color(0xFFC2E7EF),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFF22D3EE),
            heroEnd = Color(0xFF0369A1),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFDDF4FA),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.GOLDEN_HONEY -> CxColors(
            bg = Color(0xFFFFFBF0),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFFEF0C7),
            primary = Color(0xFFB45309),
            primarySoft = Color(0xFFFEF3C7),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF261A0C),
            textMuted = Color(0xFF6B5942),
            border = Color(0xFFF5DFB0),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFFF59E0B),
            heroEnd = Color(0xFFB45309),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFE4F0FF),
            toolPeach = Color(0xFFFEF3C7),
            toolLavender = Color(0xFFEEE8FF),
        )
        ThemePalette.MIDNIGHT_INDIGO -> CxColors(
            bg = Color(0xFFF4F5FF),
            surface = Color(0xFFFFFFFF),
            surface2 = Color(0xFFE5E8FF),
            primary = Color(0xFF4338CA),
            primarySoft = Color(0xFFE0E7FF),
            onPrimary = Color(0xFFFFFFFF),
            text = Color(0xFF151733),
            textMuted = Color(0xFF52567A),
            border = Color(0xFFD4D9FA),
            danger = Color(0xFFD92D4B),
            success = Color(0xFF15803D),
            heroStart = Color(0xFF6366F1),
            heroEnd = Color(0xFF3730A3),
            toolPink = Color(0xFFFFE5F0),
            toolBlue = Color(0xFFE0E7FF),
            toolPeach = Color(0xFFFFEFE0),
            toolLavender = Color(0xFFEDE9FE),
        )
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
