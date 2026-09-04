package com.example.otakulist.ui.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.local.entity.WatchlistWithAnime
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.ui.components.AnimeCoverImage
import com.example.otakulist.ui.components.KuroTrackTopBar
import com.example.otakulist.ui.theme.CanvasBackground
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.CompactButtonShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.OnSurfaceVariant
import com.example.otakulist.ui.theme.Outline
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceContainer
import com.example.otakulist.ui.theme.SurfaceContainerHigh
import com.example.otakulist.ui.theme.SurfaceContainerLow
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.Typography

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onAnimeClick: (Int) -> Unit,
    onNavigateToBrowse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val availableSeasons by viewModel.availableSeasons.collectAsState()
    val groupedWatchlist by viewModel.groupedWatchlist.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val sudahCount by viewModel.sudahCount.collectAsState()
    val belumCount by viewModel.belumCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val expandedSeasons by viewModel.expandedSeasons.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
    ) {
        // 1. Header Bar
        KuroTrackTopBar(subtitle = "Watchlist")

        // 2. Title & Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.marginScreen, vertical = Dimens.spacingSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Tontonan",
                        style = Typography.headlineLarge,
                        color = OnSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(SurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$totalCount Judul",
                            style = Typography.labelSmall.copy(fontSize = 11.sp),
                            color = OnSurfaceMuted
                        )
                    }
                }
                Text(
                    text = "Dikelompokkan per musim tayang",
                    style = Typography.bodySmall,
                    color = OnSurfaceMuted
                )
            }
        }

        // 2.5 Quick Search Bar
        if (totalCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginScreen, vertical = 4.dp)
                    .height(44.dp)
                    .clip(CardShape)
                    .background(SurfaceContainer)
                    .padding(horizontal = Dimens.spacingSm),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari di Watchlist",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Cari judul anime di watchlist...",
                                style = Typography.bodyMedium,
                                color = OnSurfaceMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            textStyle = Typography.bodyMedium.copy(color = OnSurface),
                            cursorBrush = SolidColor(PrimaryAccent),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus pencarian",
                                tint = OnSurfaceMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Status Filter Tabs (Semua, Belum, Sudah)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.marginScreen, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPillTab(
                label = "Semua",
                count = totalCount,
                isSelected = statusFilter == "Semua",
                dotColor = StatusWatching,
                onClick = { viewModel.selectStatusFilter("Semua") },
                modifier = Modifier.weight(1f)
            )
            StatusPillTab(
                label = "Belum",
                count = belumCount,
                isSelected = statusFilter == "BELUM",
                dotColor = OnSurfaceMuted,
                onClick = { viewModel.selectStatusFilter("BELUM") },
                modifier = Modifier.weight(1f)
            )
            StatusPillTab(
                label = "Sudah",
                count = sudahCount,
                isSelected = statusFilter == "SUDAH",
                dotColor = StatusCompleted,
                onClick = { viewModel.selectStatusFilter("SUDAH") },
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Season Filter Row (Horizontal Chips if >= 2 seasons)
        if (availableSeasons.size > 1) {
            val seasonScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(seasonScrollState)
                    .padding(horizontal = Dimens.marginScreen, vertical = Dimens.spacingXs),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Toggle All Collapse Button
                val allCurrentSeasons = groupedWatchlist.map { it.season }.toSet()
                val isAllExpanded = allCurrentSeasons.isNotEmpty() && expandedSeasons.containsAll(allCurrentSeasons)
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(SurfaceContainerHigh)
                        .clickable { viewModel.toggleAllCollapse() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAllExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = null,
                            tint = OnSurfaceMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isAllExpanded) "Ciutkan" else "Buka Semua",
                            style = Typography.labelSmall.copy(fontSize = 11.sp),
                            color = OnSurfaceMuted
                        )
                    }
                }

                availableSeasons.forEach { season ->
                    val isSelected = selectedSeason == season
                    val bg = if (isSelected) StatusWatching.copy(alpha = 0.2f) else SurfaceContainer
                    val borderModifier = if (isSelected) Modifier.border(1.dp, StatusWatching, PillShape) else Modifier

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .then(borderModifier)
                            .background(bg)
                            .clickable { viewModel.selectSeason(season) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (season == "Semua") "Semua Musim" else season,
                            style = Typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) StatusWatching else OnSurfaceMuted
                        )
                    }
                }
            }
        }

        // 5. Main Content: Season-Grouped List
        if (groupedWatchlist.isEmpty()) {
            WatchlistEmptyState(
                hasAny = totalCount > 0,
                searchQuery = searchQuery,
                onClearSearch = { viewModel.onSearchQueryChange("") },
                onNavigateToBrowse = onNavigateToBrowse
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.marginScreen,
                    end = Dimens.marginScreen,
                    top = Dimens.spacingSm,
                    bottom = Dimens.spacing2Xl
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
            ) {
                groupedWatchlist.forEach { group ->
                    val isExpanded = if (searchQuery.isNotBlank()) true else expandedSeasons.contains(group.season)

                    item(key = "header_${group.season}") {
                        SeasonSectionHeader(
                            season = group.season,
                            count = group.items.size,
                            isCollapsed = !isExpanded,
                            onToggleCollapse = { viewModel.toggleSeasonExpand(group.season) }
                        )
                    }

                    if (isExpanded) {
                        itemsIndexed(
                            items = group.items,
                            key = { _, item -> item.watchlist.animeId }
                        ) { index, item ->
                            SeasonWatchlistItemCard(
                                number = index + 1,
                                item = item,
                                onClick = { onAnimeClick(item.watchlist.animeId) },
                                onToggleStatus = { viewModel.toggleWatchlistStatus(item.watchlist.animeId) }
                            )
                        }

                        item(key = "spacer_${group.season}") {
                            Spacer(modifier = Modifier.height(Dimens.spacingSm))
                        }
                    }
                }

                item(key = "footer_offline") {
                    OfflineCacheFooterCard()
                }
            }
        }
    }
}

@Composable
private fun StatusPillTab(
    label: String,
    count: Int,
    isSelected: Boolean,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) StatusWatching.copy(alpha = 0.15f) else SurfaceContainer
    val borderModifier = if (isSelected) Modifier.border(1.dp, StatusWatching.copy(alpha = 0.6f), CardShape) else Modifier
    val textColor = if (isSelected) OnSurface else OnSurfaceMuted

    Row(
        modifier = modifier
            .clip(CardShape)
            .then(borderModifier)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacingSm, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = Typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "($count)",
            style = Typography.labelSmall.copy(fontSize = 11.sp),
            color = OnSurfaceMuted
        )
    }
}

@Composable
private fun SeasonSectionHeader(
    season: String,
    count: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable(onClick = onToggleCollapse)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CompactButtonShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = StatusWatching,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = season,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnBackground
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$count judul",
                style = Typography.labelSmall,
                color = OnSurfaceMuted
            )
            Icon(
                imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = if (isCollapsed) "Buka musim $season" else "Ciutkan musim $season",
                tint = OnSurfaceMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SeasonWatchlistItemCard(
    number: Int,
    item: WatchlistWithAnime,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val isSudah = item.watchlist.status == WatchlistStatus.SUDAH.value || item.watchlist.status == "COMPLETED"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceBase)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Number prefix
        Text(
            text = "$number.",
            style = Typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = OnSurfaceMuted
            ),
            modifier = Modifier.width(24.dp)
        )

        // 2. Poster thumbnail
        Box(
            modifier = Modifier
                .width(46.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceContainer)
        ) {
            AnimeCoverImage(
                coverFile = item.anime.coverFile,
                contentDescription = item.anime.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Title & Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.anime.title,
                style = Typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = OnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val typeInfo = buildString {
                append(item.anime.type ?: "TV")
                if (!item.anime.episodes.isNullOrBlank() && item.anime.episodes != "?") {
                    append(" • ${item.anime.episodes} Ep")
                }
            }
            Text(
                text = typeInfo,
                style = Typography.labelSmall.copy(fontSize = 11.sp),
                color = OnSurfaceMuted
            )

            val genresText = item.anime.genres
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.joinToString(", ")

            if (!genresText.isNullOrBlank()) {
                Text(
                    text = genresText,
                    style = Typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        color = StatusWatching
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 4. One-Tap Toggle Button: Sudah vs Belum
        Box(
            modifier = Modifier
                .clip(PillShape)
                .background(if (isSudah) StatusCompleted.copy(alpha = 0.15f) else SurfaceContainerHigh)
                .border(
                    width = 1.dp,
                    color = if (isSudah) StatusCompleted.copy(alpha = 0.7f) else Outline.copy(alpha = 0.5f),
                    shape = PillShape
                )
                .clickable(onClick = onToggleStatus)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isSudah) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sudah ditonton",
                        tint = StatusCompleted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Sudah",
                        style = Typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = StatusCompleted
                    )
                } else {
                    Text(
                        text = "Belum",
                        style = Typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = OnSurfaceMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchlistEmptyState(
    hasAny: Boolean,
    searchQuery: String = "",
    onClearSearch: () -> Unit = {},
    onNavigateToBrowse: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.marginScreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd),
            modifier = Modifier.padding(horizontal = Dimens.spacingLg)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (searchQuery.isNotBlank()) Icons.Default.Search else Icons.Default.PlaylistAddCheck,
                    contentDescription = null,
                    tint = StatusWatching,
                    modifier = Modifier.size(32.dp)
                )
            }

            val titleText = when {
                searchQuery.isNotBlank() -> "Judul Tidak Ditemukan"
                hasAny -> "Tidak Ada Judul Pada Filter Ini"
                else -> "Watchlist Masih Kosong"
            }

            val subtitleText = when {
                searchQuery.isNotBlank() -> "Tidak ada anime di watchlist yang cocok dengan \"$searchQuery\"."
                hasAny -> "Coba ganti filter status atau filter musim di atas untuk melihat anime lainnya."
                else -> "Pilih anime favorit dari Browse untuk ditambahkan ke daftar musim Anda. Hanya judul yang Anda pilih yang akan masuk ke sini."
            }

            Text(
                text = titleText,
                style = Typography.titleLarge,
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitleText,
                style = Typography.bodyMedium,
                color = OnSurfaceMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            if (searchQuery.isNotBlank()) {
                Button(
                    onClick = onClearSearch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainerHigh,
                        contentColor = OnSurface
                    ),
                    shape = CompactButtonShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacingXs))
                    Text(
                        text = "Hapus Pencarian",
                        style = Typography.labelLarge
                    )
                }
            } else {
                Button(
                    onClick = onNavigateToBrowse,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusWatching,
                        contentColor = OnBackground
                    ),
                    shape = CompactButtonShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacingXs))
                    Text(
                        text = "Jelajahi Katalog Anime",
                        style = Typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineCacheFooterCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceContainerLow)
            .padding(Dimens.spacingSm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.OfflinePin,
            contentDescription = null,
            tint = StatusCompleted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.spacingXs))
        Text(
            text = "Daftar tontonan tersimpan aman di perangkat lokal",
            style = Typography.labelSmall.copy(fontSize = 11.sp),
            color = OnSurfaceMuted
        )
    }
}