package com.example.feature.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.core.designsystem.CollageXLogoMark
import com.example.core.designsystem.CxCard
import com.example.core.designsystem.CxPrimaryButton
import com.example.core.designsystem.CxRadius
import com.example.core.designsystem.CxSecondaryButton
import com.example.core.designsystem.CxSpace
import com.example.core.designsystem.CxType
import com.example.core.designsystem.SampleArtwork
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.onGoogleSignInClicked
import com.example.ui.theme.CollageXThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onAuthSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val colors = CollageXThemeTokens.colors

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAboutSheet by remember { mutableStateOf(false) }

    // Attempt silent auto sign-in on cold start using a separate GetGoogleIdOption request
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.surface2, colors.bg, colors.surface2),
                ),
            ),
    ) {
        // Subtle cherry-blossom branch & pastel mountain silhouette backdrop matching mockup
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Top-left blossom clusters
            val blossomCenters = listOf(
                Offset(w * 0.08f, h * 0.09f),
                Offset(w * 0.16f, h * 0.13f),
                Offset(w * 0.04f, h * 0.16f),
                Offset(w * 0.90f, h * 0.07f),
                Offset(w * 0.95f, h * 0.12f),
            )
            blossomCenters.forEach { c ->
                drawCircle(colors.primary.copy(alpha = 0.14f), radius = w * 0.06f, center = c)
                drawCircle(colors.primary.copy(alpha = 0.08f), radius = w * 0.095f, center = c)
            }
            // Soft bottom mountain ridge
            val ridge = Path().apply {
                moveTo(0f, h * 0.90f)
                quadraticTo(w * 0.28f, h * 0.83f, w * 0.56f, h * 0.90f)
                quadraticTo(w * 0.80f, h * 0.95f, w, h * 0.86f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(ridge, colors.primary.copy(alpha = 0.13f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CxSpace.S16, vertical = CxSpace.S24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                Spacer(Modifier.height(CxSpace.S16))
                CollageXLogoMark(size = 80.dp)

                // Wordmark "CollageX" with accent X matching mockup
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.text)) { append("Collage") }
                        withStyle(SpanStyle(color = colors.primary)) { append("X") }
                    },
                    style = CxType.Title,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(R.string.welcome_tagline),
                    style = CxType.Body,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(CxSpace.S8))

                // Decorative fanned 3-photo collage preview (static preview, real editor lives inside app)
                DecorativeCollageFan(modifier = Modifier.fillMaxWidth().height(190.dp))
            }

            Spacer(Modifier.height(CxSpace.S24))

            Column(
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CxSpace.S12),
            ) {
                AnimatedVisibility(visible = errorMessage != null, enter = fadeIn()) {
                    CxCard(
                        borderColor = colors.danger.copy(alpha = 0.45f),
                        containerColor = colors.surface,
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = CxType.Caption,
                            color = colors.danger,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                CxPrimaryButton(
                    text = stringResource(R.string.welcome_google_cta),
                    leadingIcon = Icons.Rounded.Login,
                    loading = isLoading,
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            },
                        )
                    },
                    testTag = "google_sign_in_button",
                )

                CxSecondaryButton(
                    text = stringResource(R.string.welcome_about_cta),
                    leadingIcon = Icons.Rounded.Info,
                    onClick = { showAboutSheet = true },
                    testTag = "welcome_about_button",
                )

                Spacer(Modifier.height(CxSpace.S4))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    Text(
                        text = stringResource(R.string.open_source_badge),
                        style = CxType.CaptionMedium,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }

    if (showAboutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAboutSheet = false },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CxSpace.S8),
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        tint = colors.primary,
                    )
                    Text(
                        text = stringResource(R.string.open_source_banner_title),
                        style = CxType.Heading,
                        color = colors.text,
                    )
                }
                Text(
                    text = "CollageX is completely free and open source with zero paywalls, zero watermarks, and zero AI gimmicks. Every layout, 50+ photo grid, post-creation custom canvas sizing, and 4K/8K JPEG/PNG/WebP/TIFF/PDF export is available to everyone.",
                    style = CxType.Body,
                    color = colors.textMuted,
                )
                Text(
                    text = "Sign in with Google to securely isolate and sync your collage projects, custom canvas settings, and export history across devices via Firebase Firestore. Your photos remain private on your device.",
                    style = CxType.Body,
                    color = colors.textMuted,
                )
                CxPrimaryButton(
                    text = "Got it",
                    onClick = { showAboutSheet = false },
                    testTag = "about_sheet_close",
                )
                Spacer(Modifier.height(CxSpace.S8))
            }
        }
    }
}

@Composable
private fun DecorativeCollageFan(modifier: Modifier = Modifier) {
    val colors = CollageXThemeTokens.colors
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Left tilted card (Pagoda)
        Surface(
            shape = RoundedCornerShape(CxRadius.Card),
            border = BorderStroke(3.dp, Color.White),
            shadowElevation = 6.dp,
            modifier = Modifier
                .offset(x = (-66).dp, y = (-6).dp)
                .size(width = 122.dp, height = 148.dp)
                .rotate(-9f),
        ) {
            SampleArtwork.ScenicCanvas(SampleArtwork.PAGODA, Modifier.fillMaxSize())
        }
        // Top-right card (Alpine)
        Surface(
            shape = RoundedCornerShape(CxRadius.Card),
            border = BorderStroke(3.dp, Color.White),
            shadowElevation = 5.dp,
            modifier = Modifier
                .offset(x = 64.dp, y = (-30).dp)
                .size(width = 112.dp, height = 90.dp)
                .rotate(8f),
        ) {
            SampleArtwork.ScenicCanvas(SampleArtwork.ALPINE, Modifier.fillMaxSize())
        }
        // Bottom-right overlapping card (Cherry Blossoms)
        Surface(
            shape = RoundedCornerShape(CxRadius.Card),
            border = BorderStroke(3.dp, Color.White),
            shadowElevation = 8.dp,
            modifier = Modifier
                .offset(x = 46.dp, y = 40.dp)
                .size(width = 132.dp, height = 96.dp)
                .rotate(6f),
        ) {
            SampleArtwork.ScenicCanvas(SampleArtwork.BLOSSOMS, Modifier.fillMaxSize())
        }
        // Decorative subtle rose border glow
        Box(
            modifier = Modifier
                .size(8.dp)
                .offset(x = (-120).dp, y = (-48).dp)
                .clip(RoundedCornerShape(CxRadius.Pill))
                .background(colors.primary.copy(alpha = 0.45f)),
        )
    }
}
