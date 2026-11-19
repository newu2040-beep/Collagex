package com.example.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CollageXThemeTokens

/**
 * Reusable strict-token UI component library for CollageX.
 * Never restyle inline; customize here for cross-screen consistency.
 */

@Composable
fun CollageXLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    val colors = CollageXThemeTokens.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(CxRadius.Card))
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.heroStart, colors.heroEnd),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val w = this.size.width
            val h = this.size.height
            val gap = w * 0.10f
            val colW = (w - gap) / 2f
            val r = CornerRadius(w * 0.14f, w * 0.14f)
            // Top-left square
            drawRoundRect(Color.White, Offset(0f, 0f), Size(colW, h * 0.44f), r)
            // Top-right tall
            drawRoundRect(Color.White, Offset(colW + gap, 0f), Size(colW, h * 0.62f), r)
            // Bottom-left tall
            drawRoundRect(Color.White, Offset(0f, h * 0.44f + gap), Size(colW, h * 0.56f - gap), r)
            // Bottom-right short
            drawRoundRect(Color.White, Offset(colW + gap, h * 0.62f + gap), Size(colW, h * 0.38f - gap), r)
            // Sparkle star
            val sx = w * 0.88f
            val sy = h * 0.12f
            val sr = w * 0.15f
            val sparkle = Path().apply {
                moveTo(sx, sy - sr)
                lineTo(sx + sr * 0.28f, sy - sr * 0.28f)
                lineTo(sx + sr, sy)
                lineTo(sx + sr * 0.28f, sy + sr * 0.28f)
                lineTo(sx, sy + sr)
                lineTo(sx - sr * 0.28f, sy + sr * 0.28f)
                lineTo(sx - sr, sy)
                lineTo(sx - sr * 0.28f, sy - sr * 0.28f)
                close()
            }
            drawPath(sparkle, Color(0xFFFFE8EE))
        }
    }
}

@Composable
fun CxPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    testTag: String = "primary_button",
) {
    val colors = CollageXThemeTokens.colors
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(CxRadius.Pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.border,
            disabledContentColor = colors.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = CxSpace.S16),
        modifier = modifier
            .fillMaxWidth()
            .height(CxSizing.ButtonHeight)
            .testTag(testTag),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(CxSizing.ChipIconSize),
                color = colors.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(CxSizing.ChipIconSize),
                )
                Spacer(Modifier.width(CxSpace.S8))
            }
            Text(
                text = text,
                style = CxType.BodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun CxSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String = "secondary_button",
) {
    val colors = CollageXThemeTokens.colors
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(CxRadius.Pill),
        border = BorderStroke(1.dp, colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.surface,
            contentColor = colors.text,
        ),
        contentPadding = PaddingValues(horizontal = CxSpace.S16),
        modifier = modifier
            .fillMaxWidth()
            .height(CxSizing.ButtonHeight)
            .testTag(testTag),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(CxSizing.ChipIconSize),
                tint = colors.text,
            )
            Spacer(Modifier.width(CxSpace.S8))
        }
        Text(
            text = text,
            style = CxType.BodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CxIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = CollageXThemeTokens.colors.text,
    enabled: Boolean = true,
    testTag: String = "icon_button",
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(CxSizing.TouchTarget)
            .testTag(testTag),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else CollageXThemeTokens.colors.textMuted.copy(alpha = 0.45f),
            modifier = Modifier.size(CxSizing.IconSize),
        )
    }
}

@Composable
fun CxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = CollageXThemeTokens.colors.surface,
    borderColor: Color = CollageXThemeTokens.colors.border,
    contentPadding: PaddingValues = PaddingValues(CxSpace.S16),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(CxRadius.Card)
    Surface(
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(shape)
                        .clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            content = content,
        )
    }
}

@Composable
fun CxTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    centerTitle: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = CollageXThemeTokens.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(CxSizing.TopBarHeight)
            .padding(horizontal = CxSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            CxIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                onClick = onBack,
                testTag = "top_bar_back",
            )
        } else if (leadingIcon != null) {
            Box(
                modifier = Modifier.size(CxSizing.TouchTarget),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = colors.text,
                    modifier = Modifier.size(CxSizing.IconSize),
                )
            }
        } else {
            Spacer(Modifier.width(CxSpace.S8))
        }

        Text(
            text = title,
            style = CxType.Heading,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (centerTitle && onBack != null) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.weight(1f),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CxSpace.S4),
            content = actions,
        )
    }
}

@Composable
fun CxChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    testTag: String = "chip_$label",
) {
    val colors = CollageXThemeTokens.colors
    val bgColor by animateColorAsState(
        targetValue = if (selected) colors.primarySoft else colors.surface,
        animationSpec = tween(180),
        label = "chip_bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.border,
        animationSpec = tween(180),
        label = "chip_border",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.text,
        animationSpec = tween(180),
        label = "chip_text",
    )
    val shape = RoundedCornerShape(CxRadius.Pill)

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CxSpace.S12, vertical = CxSpace.S8)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(CxSizing.ChipIconSize),
            )
            Spacer(Modifier.width(CxSpace.S4))
        } else if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(CxSpace.S4))
        }
        Text(
            text = label,
            style = CxType.CaptionMedium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CxTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    trailingText: String? = null,
    testTag: String = "input_$label",
) {
    val colors = CollageXThemeTokens.colors
    val borderColor = if (errorText != null) colors.danger else colors.border
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = CxType.CaptionMedium,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CxSizing.InputHeight)
                .clip(RoundedCornerShape(CxRadius.Small))
                .background(colors.surface)
                .border(1.dp, borderColor, RoundedCornerShape(CxRadius.Small))
                .padding(horizontal = CxSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = CxType.Input,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = CxType.Input.copy(color = colors.text),
                    singleLine = singleLine,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag),
                )
            }
            if (trailingText != null) {
                Spacer(Modifier.width(CxSpace.S8))
                Text(
                    text = trailingText,
                    style = CxType.CaptionMedium,
                    color = colors.textMuted,
                )
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = CxType.Caption,
                color = colors.danger,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun CxListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingTint: Color = CollageXThemeTokens.colors.primary,
    leadingContainerColor: Color = CollageXThemeTokens.colors.surface2,
    trailingValue: String? = null,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    testTag: String = "list_item_$title",
) {
    val colors = CollageXThemeTokens.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = CxSizing.TouchTarget)
            .clip(RoundedCornerShape(CxRadius.Small))
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier,
            )
            .padding(vertical = CxSpace.S8, horizontal = CxSpace.S4)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CxSpace.S12),
    ) {
        if (leadingIcon != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(leadingContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = leadingTint,
                    modifier = Modifier.size(CxSizing.ChipIconSize),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CxSpace.S4),
        ) {
            Text(
                text = title,
                style = CxType.BodyMedium,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = CxType.Caption,
                    color = colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        } else {
            if (!trailingValue.isNullOrBlank()) {
                Text(
                    text = trailingValue,
                    style = CxType.CaptionMedium,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (showChevron && onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(CxSizing.ChipIconSize),
                )
            }
        }
    }
}

@Composable
fun CxSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingTint: Color = CollageXThemeTokens.colors.primary,
    leadingBg: Color = CollageXThemeTokens.colors.surface2,
    testTag: String = "switch_$title",
) {
    val colors = CollageXThemeTokens.colors
    CxListItem(
        title = title,
        subtitle = subtitle,
        leadingIcon = leadingIcon,
        leadingTint = leadingTint,
        leadingContainerColor = leadingBg,
        showChevron = false,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        testTag = testTag,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onPrimary,
                    checkedTrackColor = colors.primary,
                    uncheckedThumbColor = colors.surface,
                    uncheckedTrackColor = colors.border,
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        },
    )
}

enum class MainTab(val index: Int, val labelRes: Int) {
    HOME(0, R.string.nav_home),
    PROJECTS(1, R.string.nav_projects),
    EXPORTS(3, R.string.nav_exports),
    SETTINGS(4, R.string.nav_settings),
}

/**
 * Compact rounded capsule bottom navigation bar matching the CollageX mockup:
 * fixed destinations with a single animated sliding pill indicator + stationary central circular '+' action.
 */
@Composable
fun CxBottomNav(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CollageXThemeTokens.colors
    Surface(
        color = colors.surface,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(topStart = CxRadius.Card, topEnd = CxRadius.Card),
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(CxSizing.BottomNavHeight + CxSpace.S8)
                .padding(horizontal = CxSpace.S8, vertical = CxSpace.S4),
        ) {
            val slotWidth = maxWidth / 5f
            val indicatorOffset by animateDpAsState(
                targetValue = slotWidth * selectedTab.index,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "nav_pill_offset",
            )

            // Single sliding active pill indicator behind the selected destination
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset + CxSpace.S4)
                    .width(slotWidth - CxSpace.S8)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(CxRadius.Pill))
                    .background(colors.primarySoft),
            )

            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavSlot(
                    selected = selectedTab == MainTab.HOME,
                    activeIcon = Icons.Rounded.Home,
                    inactiveIcon = Icons.Outlined.Home,
                    label = stringResource(R.string.nav_home),
                    onClick = { onSelectTab(MainTab.HOME) },
                    modifier = Modifier.weight(1f),
                    testTag = "nav_home",
                )
                NavSlot(
                    selected = selectedTab == MainTab.PROJECTS,
                    activeIcon = Icons.Rounded.GridView,
                    inactiveIcon = Icons.Outlined.GridView,
                    label = stringResource(R.string.nav_projects),
                    onClick = { onSelectTab(MainTab.PROJECTS) },
                    modifier = Modifier.weight(1f),
                    testTag = "nav_projects",
                )
                // Stationary central circular '+' create action
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(CxSizing.TouchTarget)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(colors.heroStart, colors.primary),
                                ),
                            )
                            .clickable(
                                role = Role.Button,
                                onClickLabel = stringResource(R.string.nav_create),
                                onClick = onCreateClick,
                            )
                            .testTag("nav_create_fab"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.nav_create),
                            tint = colors.onPrimary,
                            modifier = Modifier.size(CxSizing.IconSize),
                        )
                    }
                }
                NavSlot(
                    selected = selectedTab == MainTab.EXPORTS,
                    activeIcon = Icons.Rounded.BarChart,
                    inactiveIcon = Icons.Outlined.BarChart,
                    label = stringResource(R.string.nav_exports),
                    onClick = { onSelectTab(MainTab.EXPORTS) },
                    modifier = Modifier.weight(1f),
                    testTag = "nav_exports",
                )
                NavSlot(
                    selected = selectedTab == MainTab.SETTINGS,
                    activeIcon = Icons.Rounded.Settings,
                    inactiveIcon = Icons.Outlined.Settings,
                    label = stringResource(R.string.nav_settings),
                    onClick = { onSelectTab(MainTab.SETTINGS) },
                    modifier = Modifier.weight(1f),
                    testTag = "nav_settings",
                )
            }
        }
    }
}

@Composable
private fun NavSlot(
    selected: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    val colors = CollageXThemeTokens.colors
    val tint by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.textMuted,
        animationSpec = tween(180),
        label = "nav_tint",
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(CxRadius.Pill))
            .clickable(role = Role.Tab, onClick = onClick)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(CxSizing.IconSize),
        )
        Text(
            text = label,
            style = CxType.Caption,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CxEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = CollageXThemeTokens.colors
    CxCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = CxSpace.S16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.primarySoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = title,
                style = CxType.Heading,
                color = colors.text,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = CxType.Body,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(CxSpace.S4))
                CxPrimaryButton(
                    text = actionLabel,
                    onClick = onAction,
                    testTag = "empty_state_action",
                )
            }
        }
    }
}

@Composable
fun CxLoadingState(
    message: String = "Loading…",
    modifier: Modifier = Modifier,
) {
    val colors = CollageXThemeTokens.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CxSpace.S32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
    ) {
        CircularProgressIndicator(color = colors.primary)
        Text(text = message, style = CxType.Body, color = colors.textMuted)
    }
}

@Composable
fun CxErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = CollageXThemeTokens.colors
    CxCard(
        modifier = modifier,
        borderColor = colors.danger.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = message,
                style = CxType.Body,
                color = colors.text,
                textAlign = TextAlign.Center,
            )
            if (onRetry != null) {
                CxSecondaryButton(
                    text = "Try Again",
                    onClick = onRetry,
                    testTag = "error_retry_button",
                )
            }
        }
    }
}

@Composable
fun CxToastBanner(
    message: String?,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = CollageXThemeTokens.colors
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = fadeIn(tween(180)) + slideInVertically(tween(200)) { -it },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it },
        modifier = modifier,
    ) {
        Surface(
            color = if (isError) colors.danger else colors.text,
            contentColor = Color.White,
            shape = RoundedCornerShape(CxRadius.Pill),
            shadowElevation = 6.dp,
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S8),
        ) {
            Text(
                text = message.orEmpty(),
                style = CxType.CaptionMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = CxSpace.S16, vertical = CxSpace.S12),
            )
        }
    }
}
