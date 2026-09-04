package com.example.otakulist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.ui.theme.CanvasBackground
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.StatusDropped
import com.example.otakulist.ui.theme.StatusPlanToWatch
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceContainer
import com.example.otakulist.ui.theme.Tertiary
import com.example.otakulist.ui.theme.Typography

@Composable
fun AnimePosterGridItem(
    anime: AnimeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    watchlistStatus: WatchlistStatus? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
    ) {
        // 1. Elevated 2:3 Poster Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(CardShape)
                .background(SurfaceContainer)
        ) {
            AnimeCoverImage(
                coverFile = anime.coverFile,
                contentDescription = anime.title,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle dark bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CanvasBackground.copy(alpha = 0.65f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Score Pill Badge (Top-Left)
            if (!anime.score.isNullOrBlank() && anime.score != "0" && anime.score != "?") {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CanvasBackground.copy(alpha = 0.85f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Score",
                        tint = Tertiary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = anime.score,
                        style = Typography.labelSmall.copy(fontSize = 10.sp),
                        color = OnBackground
                    )
                }
            }

            // Watchlist Status Dot (Bottom-Right)
            if (watchlistStatus != null) {
                val dotColor = if (watchlistStatus == WatchlistStatus.SUDAH) StatusCompleted else StatusWatching
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        // 2. Title & Subtitle below poster
        Text(
            text = anime.title,
            style = Typography.titleMedium.copy(fontSize = 13.sp, lineHeight = 17.sp),
            color = OnSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        val typeText = anime.type ?: "TV"
        val epText = anime.episodes?.let { "$it ep" } ?: "1 ep"
        Text(
            text = "$typeText · $epText",
            style = Typography.bodySmall.copy(fontSize = 11.sp),
            color = OnSurfaceMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}