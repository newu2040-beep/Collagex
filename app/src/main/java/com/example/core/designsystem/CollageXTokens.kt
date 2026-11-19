package com.example.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.ThemePalette

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

/** Single UI font family (Plus Jakarta Sans) with max 3 weights (400, 500, 700). */
val CxUiFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
)

/** Curated open-licensed fonts for Collage Text Layers on canvas. */
object CanvasFonts {
    val Sans = CxUiFontFamily
    val Serif = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
    val Script = FontFamily(Font(R.font.pacifico, FontWeight.Normal))
    val Geometric = FontFamily(Font(R.font.space_grotesk, FontWeight.Normal))

    fun byId(id: String): FontFamily = when (id) {
        "SERIF" -> Serif
        "SCRIPT" -> Script
        "GEOMETRIC" -> Geometric
        else -> Sans
    }
}

/** Strict Type Scale (Title 22/28, Heading 18/24, Body 15/22, Input 16/22, Caption 12/16). */
object CxType {
    val Title = TextStyle(
        fontFamily = CxUiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    )
    val Heading = TextStyle(
        fontFamily = CxUiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )
    val Body = TextStyle(
        fontFamily = CxUiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
    val BodyMedium = TextStyle(
        fontFamily = CxUiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
    val Input = TextStyle(
        fontFamily = CxUiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    )
    val Caption = TextStyle(
        fontFamily = CxUiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val CaptionMedium = TextStyle(
        fontFamily = CxUiFontFamily,
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
        }
        return CxColors(
            bg = Color(0xFF141016),
            surface = Color(0xFF1F1922),
            surface2 = Color(0xFF2A212E),
            primary = primary,
            primarySoft = primarySoft,
            onPrimary = if (palette == ThemePalette.NEUTRAL) Color(0xFF111827) else Color(0xFFFFFFFF),
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
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
