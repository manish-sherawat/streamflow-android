package com.streamflow.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// System default stands in for Inter/SF Pro; swap FontFamily.Default for a
// bundled Inter font family when brand assets are finalized.
private val Base = FontFamily.Default

object StreamFlowType {
    val displayTitle = TextStyle(
        fontFamily = Base,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    )
    val sectionHeader = TextStyle(
        fontFamily = Base,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = TextPrimary
    )
    val cardTitle = TextStyle(
        fontFamily = Base,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = TextPrimary
    )
    val body = TextStyle(
        fontFamily = Base,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextSecondary
    )
    val pillLabel = TextStyle(
        fontFamily = Base,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = TextPrimary
    )
    val caption = TextStyle(
        fontFamily = Base,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = TextTertiary
    )
    val buttonLabel = TextStyle(
        fontFamily = Base,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 1.em
    )
    val heroTitle = displayTitle
    val sheetHeader = sectionHeader
    val titleHeader = sectionHeader
}

// Material3 typography bridge so default components (TextField, etc.) stay on-brand
val StreamFlowMaterialType = Typography(
    headlineMedium = StreamFlowType.displayTitle,
    titleLarge = StreamFlowType.sectionHeader,
    titleSmall = StreamFlowType.cardTitle,
    bodyMedium = StreamFlowType.body,
    labelMedium = StreamFlowType.pillLabel,
    labelSmall = StreamFlowType.caption,
    labelLarge = StreamFlowType.buttonLabel
)
