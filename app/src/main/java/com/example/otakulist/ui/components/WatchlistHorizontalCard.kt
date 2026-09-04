package com.example.otakulist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.local.entity.WatchlistWithAnime
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.CompactButtonShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceContainerLowest
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.Typography

@Composable
fun WatchlistHorizontalCard(
    item: WatchlistWithAnime,
    onClick: () -> Unit,
    onIncrementEpisode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val anime = item.anime
    val watchlist = item.watchlist

    val totalEp = anime.episodes?.toIntOrNull()
    val currentEp = watchlist.currentEpisode
    val progressFraction = if (totalEp != null && totalEp > 0) {
        (currentEp.toFloat() / totalEp.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val percentInt = (progressFraction * 100).toInt()
    val isCompleted = (totalEp != null && currentEp >= totalEp) || watchlist.status == "COMPLETED"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceBase)
            .clickable(onClick = onClick)
            .padding(Dimens.spacingMd),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster with Type Badge
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(104.dp)
                .clip(CardShape)
        ) {
            AnimeCoverImage(
                coverFile = anime.coverFile,
                contentDescription = anime.title,
                modifier = Modifier.matchParentSize()
            )

            if (!anime.type.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(CompactButtonShape)
                        .background(SurfaceContainerLowest.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = anime.type,
                        style = Typography.labelSmall.copy(fontSize = 9.sp),
                        color = OnSurface
                    )
                }
            }
        }

        // Info and Progress Column
        Column(
            modifier = Modifier
                .weight(1f)
                .height(104.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title & Subtitle
            Column {
                Text(
                    text = anime.title,
                    style = Typography.titleMedium,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val seasonStudio = listOfNotNull(
                    anime.season?.takeIf { it.isNotBlank() },
                    anime.studios?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")

                if (seasonStudio.isNotBlank()) {
                    Text(
                        text = seasonStudio,
                        style = Typography.bodySmall,
                        color = OnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Episode Progress & Bar
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingXs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Ep $currentEp",
                            style = Typography.labelMedium,
                            color = OnSurface
                        )
                        Text(
                            text = if (totalEp != null) "/ $totalEp" else "/ ?",
                            style = Typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                    }

                    Text(
                        text = "$percentInt%",
                        style = Typography.labelSmall,
                        color = if (isCompleted) StatusCompleted else StatusWatching
                    )
                }

                CustomLinearProgressIndicator(
                    progress = progressFraction,
                    progressColor = if (isCompleted) StatusCompleted else StatusWatching
                )
            }

            // Compact +1 Ep Tracker Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .height(Dimens.trackerButtonHeight)
                        .clip(CompactButtonShape)
                        .background(SurfaceVariant)
                        .clickable(enabled = totalEp == null || currentEp < totalEp) {
                            onIncrementEpisode()
                        }
                        .padding(horizontal = Dimens.spacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increment episode",
                        tint = OnBackground,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "1 Ep",
                        style = Typography.labelMedium,
                        color = OnBackground
                    )
                }
            }
        }
    }
}
