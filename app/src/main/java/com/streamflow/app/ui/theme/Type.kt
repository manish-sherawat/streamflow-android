package com.streamflow.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** System variable font family fallback */
val RobotoFlex = FontFamily.Default

/**
 * StreamFlow — Expressive type scale.
 * Mirrors design.md §2. Emphasized display/headline/label steps carry the heavier
 * weight the Expressive scale defines for hero titles and primary CTAs; everything
 * else stays on standard weights for scan-ability in dense rails.
 */
val StreamFlowTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle( // section headers: "Actors", "More Like This"
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle( // card titles / cast names
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle( // pill text: IMDb 7.1, 4K HDR
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    bodyLarge = TextStyle( // synopsis
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    labelLarge = TextStyle( // button labels
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle( // captions: "S1:E1", durations
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

// Material3 bridge and component compatibility object
val StreamFlowMaterialType = StreamFlowTypography

object StreamFlowType {
    val displayTitle = StreamFlowTypography.displayLarge
    val sectionHeader = StreamFlowTypography.headlineMedium
    val cardTitle = StreamFlowTypography.titleLarge
    val body = StreamFlowTypography.bodyLarge
    val pillLabel = StreamFlowTypography.titleMedium
    val caption = StreamFlowTypography.labelSmall
    val buttonLabel = StreamFlowTypography.labelLarge
    val brandTitle = StreamFlowTypography.displayLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)

    val heroTitle = displayTitle
    val sheetHeader = sectionHeader
    val titleHeader = sectionHeader
}
