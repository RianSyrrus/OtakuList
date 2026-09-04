package com.example.otakulist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.Outline
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.Typography

enum class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    WATCHLIST("watchlist", "Watchlist", Icons.Default.Bookmarks),
    BROWSE("browse", "Browse", Icons.Default.Explore),
    STATS("stats", "Stats", Icons.Default.BarChart),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun OtakuBottomBar(
    currentRoute: String,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceBase.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = Outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(vertical = Dimens.spacingXs),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavDestination.entries.forEach { destination ->
            val isSelected = currentRoute == destination.route
            val contentColor = if (isSelected) PrimaryAccent else OnSurfaceMuted

            Column(
                modifier = Modifier
                    .clip(PillShape)
                    .clickable { onNavigate(destination) }
                    .padding(horizontal = Dimens.spacingMd, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 28.dp)
                        .clip(PillShape)
                        .background(
                            if (isSelected) PrimaryAccent.copy(alpha = 0.15f) else SurfaceBase.copy(alpha = 0f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = destination.title,
                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                    color = contentColor
                )
            }
        }
    }
}
