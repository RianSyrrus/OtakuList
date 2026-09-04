package com.example.otakulist.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),       // rounded-sm
    medium = RoundedCornerShape(8.dp),      // rounded default
    large = RoundedCornerShape(12.dp),      // rounded-md (cards, poster)
    extraLarge = RoundedCornerShape(16.dp)  // rounded-lg
)

val PillShape = CircleShape
val CardShape = RoundedCornerShape(12.dp)
val PosterShape = RoundedCornerShape(12.dp)
val CompactButtonShape = RoundedCornerShape(8.dp)
