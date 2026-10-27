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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.local.entity.WatchlistWithAnime
import com.example.otakulist.data.local.entity.getPrimaryStudio
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.CompactButtonShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.Outline
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceContainerLowest
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.Tertiary
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
    val isCompleted = (totalEp != null && currentEp >= totalEp) || watchlist.status == "SUDAH" || watchlist.status == "COMPLETED"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceBase)
            .border(1.dp, Outline.copy(alpha = 0.35f), CardShape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster with Type & Score Badges
        Box(
            modifier = Modifier
                .width(76.dp)
                .height(110.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(0.8.dp, Outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        ) {
            AnimeCoverImage(
                coverFile = anime.coverFile,
                contentDescription = anime.title,
                modifier = Modifier.matchParentSize(),
                isAdult = anime.isAdult
            )

            // Format badge (Bottom-End)
            if (!anime.type.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceContainerLowest.copy(alpha = 0.88f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = anime.type,
                        style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                        color = OnSurface
                    )
                }
            }

            // Anime MAL Score badge (Top-Start)
            if (!anime.score.isNullOrBlank() && anime.score != "0" && anime.score != "?") {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceContainerLowest.copy(alpha = 0.88f))
                        .padding(horizontal = 4.dp, vertical = 1.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Tertiary,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = anime.score,
                        style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = OnBackground
                    )
                }
            }
        }

        // Info and Progress Column
        Column(
            modifier = Modifier
                .weight(1f)
                .height(110.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title & Subtitle + Badges Row
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = anime.title,
                        style = Typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        ),
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Personal Rating Badge if set
                    if (watchlist.personalScore > 0) {
                        Row(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Tertiary.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Tertiary,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${watchlist.personalScore}/10",
                                style = Typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Tertiary
                            )
                        }
                    }
                }

                val seasonStudio = listOfNotNull(
                    anime.season?.takeIf { it.isNotBlank() },
                    anime.getPrimaryStudio()
                ).joinToString(" · ")

                if (seasonStudio.isNotBlank()) {
                    Text(
                        text = seasonStudio,
                        style = Typography.bodySmall.copy(fontSize = 11.sp),
                        color = OnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Episode Progress & Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = OnSurface
                        )
                        Text(
                            text = if (totalEp != null) "/ $totalEp" else "/ ?",
                            style = Typography.labelSmall,
                            color = OnSurfaceMuted
                        )
                    }

                    Text(
                        text = if (isCompleted) "Selesai" else "$percentInt%",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isCompleted) StatusCompleted else StatusWatching
                    )
                }

                CustomLinearProgressIndicator(
                    progress = progressFraction,
                    progressColor = if (isCompleted) StatusCompleted else StatusWatching
                )
            }

            // Tracker Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            if (isCompleted) StatusCompleted.copy(alpha = 0.12f)
                            else StatusWatching.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isCompleted) "Tamat" else "Menonton",
                        style = Typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isCompleted) StatusCompleted else StatusWatching
                    )
                }

                // Quick +1 Ep Button or Completed Check
                if (isCompleted) {
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(CompactButtonShape)
                            .background(StatusCompleted.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = StatusCompleted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Selesai",
                            style = Typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = StatusCompleted
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(CompactButtonShape)
                            .background(SurfaceVariant)
                            .border(0.8.dp, Outline.copy(alpha = 0.5f), CompactButtonShape)
                            .clickable(enabled = totalEp == null || currentEp < totalEp) {
                                onIncrementEpisode()
                            }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increment episode",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "+1 Ep",
                            style = Typography.labelMedium.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = OnBackground
                        )
                    }
                }
            }
        }
    }
}

