package com.example.otakulist.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.repository.StatsModel
import com.example.otakulist.ui.components.KuroTrackTopBar
import com.example.otakulist.ui.theme.CanvasBackground
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.CompactButtonShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.OnSurfaceVariant
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.StatusDropped
import com.example.otakulist.ui.theme.StatusPlanToWatch
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceBright
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.Typography
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
    ) {
        KuroTrackTopBar(subtitle = "Stats")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.marginScreen)
                .padding(top = Dimens.spacingSm, bottom = Dimens.spacing2Xl + 32.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingXl)
        ) {
            // Header: Title & Time Filter Pill
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingXs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Statistik Tontonan",
                        style = Typography.headlineLarge,
                        color = OnBackground
                    )

                    // Filter pill
                    Row(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(SurfaceBase)
                            .padding(horizontal = Dimens.spacingMd, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = StatusWatching,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Semua Waktu",
                            style = Typography.labelMedium,
                            color = OnSurface
                        )
                    }
                }

                Text(
                    text = "Ringkasan arsip tontonan personal Anda dari database lokal.",
                    style = Typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }

            // 2 Main Metric Cards + 1 Full Width Watchlist Card
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
                ) {
                    MetricSummaryCard(
                        title = "Sudah Ditonton",
                        value = stats.sudahCount.toString(),
                        subtitle = "${stats.sudahCount} anime diselesaikan",
                        icon = Icons.Default.CheckCircle,
                        iconTint = StatusCompleted,
                        modifier = Modifier.weight(1f)
                    )

                    MetricSummaryCard(
                        title = "Belum Ditonton",
                        value = stats.belumCount.toString(),
                        subtitle = "Menunggu untuk ditonton",
                        icon = Icons.Default.PlayCircle,
                        iconTint = StatusWatching,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Total Watchlist Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .background(SurfaceBase)
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = StatusWatching,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Total Koleksi Watchlist",
                                style = Typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = stats.totalWatchlistCount.toString(),
                                    style = Typography.headlineMedium,
                                    color = OnBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "judul tersimpan",
                                    style = Typography.bodySmall,
                                    color = OnSurfaceMuted,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }

                    if (stats.totalHoursWatched > 0) {
                        Row(
                            modifier = Modifier
                                .clip(CompactButtonShape)
                                .background(SurfaceVariant)
                                .padding(horizontal = Dimens.spacingSm, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "± ${stats.totalHoursWatched} jam",
                                style = Typography.labelSmall.copy(color = StatusWatching)
                            )
                        }
                    }
                }
            }

            // Collection Distribution
            CollectionDistributionCard(stats = stats)

            // Top Genres
            TopGenresCard(topGenres = stats.topGenres)

            // Studio & Busiest Year Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CardShape)
                        .background(SurfaceBase)
                        .padding(Dimens.spacingMd)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Studio Favorit",
                                style = Typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CompactButtonShape)
                                    .background(SurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = StatusWatching,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stats.favoriteStudio,
                                style = Typography.titleMedium,
                                color = OnBackground,
                                maxLines = 1
                            )
                            Text(
                                text = if (stats.favoriteStudioCount > 0) "${stats.favoriteStudioCount} judul tercatat" else "Belum ada data",
                                style = Typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CardShape)
                        .background(SurfaceBase)
                        .padding(Dimens.spacingMd)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tahun Tersibuk",
                                style = Typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CompactButtonShape)
                                    .background(SurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = StatusWatching,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stats.busiestYear,
                                style = Typography.titleMedium,
                                color = OnBackground,
                                maxLines = 1
                            )
                            Text(
                                text = if (stats.busiestYearCount > 0) "${stats.busiestYearCount} judul tercatat" else "Belum ada data",
                                style = Typography.labelSmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                }
            }

            // Offline Local Notice Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(SurfaceVariant.copy(alpha = 0.6f))
                    .padding(Dimens.spacingMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CompactButtonShape)
                        .background(SurfaceBase),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = StatusCompleted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Data statistik dihitung otomatis dari log riwayat lokal tanpa internet.",
                    style = Typography.bodySmall,
                    color = OnSurfaceMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(SurfaceBase)
            .padding(Dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = Typography.labelSmall,
                color = OnSurfaceMuted
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CompactButtonShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column {
            Text(
                text = value,
                style = Typography.displayLarge.copy(fontSize = 32.sp, lineHeight = 36.sp),
                color = OnBackground
            )
            Text(
                text = subtitle,
                style = Typography.labelSmall,
                color = OnSurfaceMuted
            )
        }
    }
}

@Composable
private fun CollectionDistributionCard(stats: StatsModel) {
    val total = (stats.sudahCount + stats.belumCount).coerceAtLeast(1)
    val hasData = (stats.sudahCount + stats.belumCount) > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceBase)
            .padding(Dimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Distribusi Status Tontonan",
                style = Typography.titleMedium,
                color = OnBackground
            )
            Text(
                text = "${if (hasData) stats.totalWatchlistCount else 0} Total Judul",
                style = Typography.labelSmall,
                color = OnSurfaceMuted
            )
        }

        // Multi-colored segmented bar: Green (Sudah) vs Blue (Belum)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(PillShape)
                .background(SurfaceVariant)
        ) {
            if (stats.sudahCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(stats.sudahCount.toFloat() / total)
                        .background(StatusCompleted)
                )
            }
            if (stats.belumCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(stats.belumCount.toFloat() / total)
                        .background(StatusWatching)
                )
            }
            if (!hasData) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceVariant)
                )
            }
        }

        // Breakdown Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            DistributionPillItem(
                label = "Sudah Ditonton",
                count = "${stats.sudahCount} judul",
                dotColor = StatusCompleted,
                modifier = Modifier.weight(1f)
            )
            DistributionPillItem(
                label = "Belum Ditonton",
                count = "${stats.belumCount} judul",
                dotColor = StatusWatching,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DistributionPillItem(
    label: String,
    count: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = Dimens.spacingSm, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                style = Typography.labelSmall,
                color = OnSurfaceVariant
            )
        }
        Text(
            text = count,
            style = Typography.labelMedium,
            color = OnBackground
        )
    }
}

@Composable
private fun TopGenresCard(topGenres: List<Pair<String, Int>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceBase)
            .padding(Dimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Genre Terbanyak Ditonton",
                    style = Typography.titleMedium,
                    color = OnBackground
                )
                Text(
                    text = "Berdasarkan judul yang diselesaikan",
                    style = Typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }

            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = OnSurfaceMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        if (topGenres.isEmpty()) {
            Text(
                text = "Belum ada data genre tercatat.",
                style = Typography.bodySmall,
                color = OnSurfaceMuted,
                modifier = Modifier.padding(vertical = Dimens.spacingMd)
            )
        } else {
            val maxCount = (topGenres.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
            val opacities = listOf(1f, 0.80f, 0.60f, 0.45f, 0.30f)

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                topGenres.take(5).forEachIndexed { index, (genre, count) ->
                    val fraction = (count.toFloat() / maxCount).coerceIn(0.05f, 1f)
                    val alpha = opacities.getOrElse(index) { 0.3f }

                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingXs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = genre,
                                style = Typography.bodyMedium.copy(color = OnSurface)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(SurfaceVariant)
                                    .padding(horizontal = Dimens.spacingSm, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$count judul",
                                    style = Typography.labelSmall,
                                    color = OnBackground
                                )
                            }
                        }

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(PillShape)
                                .background(SurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction)
                                    .clip(PillShape)
                                    .background(StatusWatching.copy(alpha = alpha))
                            )
                        }
                    }
                }
            }
        }
    }
}
