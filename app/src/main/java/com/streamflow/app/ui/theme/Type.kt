package com.streamflow.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// System font until bundled Inter is added.
// All text color is set at the call-site; no color baked into styles here.
private val Base = FontFamily.Default

object StreamFlowType {
    // Display & hero titles — large, confident, sentence-case
    val displayTitle = TextStyle(
        fontFamily  = Base,
        fontWeight  = FontWeight.Bold,
        fontSize    = 26.sp,
        lineHeight  = 32.sp,
        letterSpacing = (-0.3).sp
    )

    // Section rail headers
    val sectionHeader = TextStyle(
        fontFamily  = Base,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 16.sp,
        lineHeight  = 22.sp,
        letterSpacing = 0.sp
    )

    // Card thumbnail title
    val cardTitle = TextStyle(
        fontFamily  = Base,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.sp
    )

    // Body copy
    val body = TextStyle(
        fontFamily  = Base,
        fontWeight  = FontWeight.Normal,
        fontSize    = 14.sp,
        lineHeight  = 21.sp,
        letterSpacing = 0.sp
    )

    // Chip / pill label
    val pillLabel = TextStyle(
        fontFamily  = Base,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.sp
    )

    // Caption / metadata
    val caption = TextStyle(
        fontFamily  = Base,
        fontWeight  = FontWeight.Normal,
        fontSize    = 11.sp,
        lineHeight  = 15.sp,
        letterSpacing = 0.sp
    )

    // Button label
    val buttonLabel = TextStyle(
        fontFamily  = Base,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 15.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.sp
    )

    // Wordmark / brand
    val brandTitle = TextStyle(
        fontFamily  = Base,
        fontWeight  = FontWeight.Black,
        fontSize    = 18.sp,
        lineHeight  = 22.sp,
        letterSpacing = 2.sp
    )

    // Aliases for backward compat
    val heroTitle     = displayTitle
    val sheetHeader   = sectionHeader
    val titleHeader   = sectionHeader
}

// Material3 typography bridge
val StreamFlowMaterialType = Typography(
    displayMedium  = StreamFlowType.displayTitle,
    headlineMedium = StreamFlowType.displayTitle,
    titleLarge     = StreamFlowType.sectionHeader,
    titleMedium    = StreamFlowType.sectionHeader,
    titleSmall     = StreamFlowType.cardTitle,
    bodyLarge      = StreamFlowType.body,
    bodyMedium     = StreamFlowType.body,
    labelLarge     = StreamFlowType.buttonLabel,
    labelMedium    = StreamFlowType.pillLabel,
    labelSmall     = StreamFlowType.caption
)
