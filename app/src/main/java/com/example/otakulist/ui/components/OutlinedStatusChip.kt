package com.example.otakulist.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.Outline
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.Typography

@Composable
fun OutlinedStatusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dotColor: Color? = null,
    badgeCount: Int? = null
) {
    val borderColor = if (selected) PrimaryAccent else Outline
    val textColor = if (selected) OnBackground else OnSurfaceMuted
    val backgroundColor = if (selected) PrimaryAccent.copy(alpha = 0.15f) else SurfaceBase

    Row(
        modifier = modifier
            .clip(PillShape)
            .border(BorderStroke(1.dp, borderColor), PillShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacingMd, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }

        Text(
            text = label,
            style = Typography.labelMedium,
            color = textColor
        )

        if (badgeCount != null) {
            Text(
                text = badgeCount.toString(),
                style = Typography.labelSmall,
                color = if (selected) OnBackground else OnSurfaceMuted
            )
        }
    }
}
