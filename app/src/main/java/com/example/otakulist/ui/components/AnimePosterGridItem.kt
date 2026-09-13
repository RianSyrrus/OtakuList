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

import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.font.FontWeight
import com.example.otakulist.ui.theme.Outline

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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(bottom = Dimens.spacingXs),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Elevated 2:3 Poster Frame with subtle border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(CardShape)
                .background(SurfaceContainer)
                .border(1.dp, Outline.copy(alpha = 0.35f), CardShape)
        ) {
            AnimeCoverImage(
                coverFile = anime.coverFile,
                contentDescription = anime.title,
                modifier = Modifier.fillMaxSize(),
                isAdult = anime.isAdult
            )

            // Subtle dark bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            ),
                            startY = 120f
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
                        .background(CanvasBackground.copy(alpha = 0.88f))
                        .border(0.6.dp, Outline.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
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
                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = OnBackground
                    )
                }
            }

            // Format Type Pill (Top-Right)
            if (!anime.type.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(CanvasBackground.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = anime.type,
                        style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                        color = OnSurfaceMuted
                    )
                }
            }

            // Watchlist Status Pill (Bottom-Right)
            if (watchlistStatus != null) {
                val isDone = watchlistStatus == WatchlistStatus.SUDAH
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CanvasBackground.copy(alpha = 0.92f))
                        .border(
                            0.7.dp,
                            if (isDone) StatusCompleted.copy(alpha = 0.7f) else StatusWatching.copy(alpha = 0.7f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.Check else Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = if (isDone) StatusCompleted else StatusWatching,
                        modifier = Modifier.size(9.dp)
                    )
                    Text(
                        text = if (isDone) "Selesai" else "Tonton",
                        style = Typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isDone) StatusCompleted else StatusWatching
                    )
                }
            }
        }

        // 2. Title & Metadata below poster
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = anime.title,
                style = Typography.titleMedium.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 16.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val epText = anime.episodes?.let { "$it ep" } ?: "1 ep"
            val seasonText = anime.season?.takeIf { it.isNotBlank() }
            val meta = if (seasonText != null) "$epText · $seasonText" else epText
            Text(
                text = meta,
                style = Typography.bodySmall.copy(fontSize = 10.5.sp),
                color = OnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}