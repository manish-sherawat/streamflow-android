package com.streamflow.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Spacing {
    val xxs = 4.dp
    val xs  = 8.dp
    val sm  = 12.dp
    val md  = 16.dp   // standard screen horizontal padding
    val lg  = 24.dp
    val xl  = 32.dp
}

object Radius {
    val posterCard      = RoundedCornerShape(10.dp)
    val pill            = RoundedCornerShape(50)
    val sheet           = RoundedCornerShape(24.dp)
    val secondaryButton = RoundedCornerShape(50)
    val card            = RoundedCornerShape(16.dp)
    val input           = RoundedCornerShape(12.dp)
    val chip            = RoundedCornerShape(8.dp)
}

object Sizes {
    val posterCardWidth     = 120.dp
    val posterCardHeight    = 178.dp
    val castAvatar          = 64.dp
    val bottomNavHeight     = 60.dp
    val primaryButtonHeight = 50.dp
    val secondaryButtonHeight = 44.dp
    val navIconSize         = 24.dp
    val navDotSize          = 4.dp
}
