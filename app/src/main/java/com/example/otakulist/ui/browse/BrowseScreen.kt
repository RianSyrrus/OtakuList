package com.example.otakulist.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.model.AnimeSortOrder
import com.example.otakulist.ui.components.AnimePosterGridItem
import com.example.otakulist.ui.components.KuroTrackTopBar
import com.example.otakulist.ui.theme.CanvasBackground
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.CompactButtonShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceContainer
import com.example.otakulist.ui.theme.SurfaceContainerHigh
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.Typography

@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel,
    onAnimeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val availableGenres by viewModel.availableGenres.collectAsState()
    val availableTypes by viewModel.availableTypes.collectAsState()
    val availableStatuses by viewModel.availableStatuses.collectAsState()
    val availableSeasons by viewModel.availableSeasons.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val animeList by viewModel.animeList.collectAsState()
    val savedWatchlistMap by viewModel.savedWatchlistMap.collectAsState()

    var showGenreMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var showSeasonMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
    ) {
        // 1. Top Brand Header
        KuroTrackTopBar(subtitle = "Browse")

        // 2. Sticky Search Bar Container (Height 48dp, SurfaceContainer, rounded 12dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CanvasBackground)
                .padding(top = Dimens.spacingSm, bottom = Dimens.spacingXs)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginScreen)
                    .height(48.dp)
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
                        contentDescription = "Search",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(20.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Cari judul anime, studio, genre...",
                                style = Typography.bodyMedium,
                                color = OnSurfaceMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onQueryChange(it) },
                            textStyle = Typography.bodyMedium.copy(color = OnSurface),
                            cursorBrush = SolidColor(PrimaryAccent),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onQueryChange("") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = OnSurfaceMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 3. Horizontally Scrollable Filter Chips (Genre, Format, Status, Musim, Urutkan)
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = Dimens.marginScreen, vertical = Dimens.spacingSm),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Genre Chip Dropdown
                Box {
                    FilterPill(
                        label = "Genre:",
                        value = selectedGenre,
                        isActive = selectedGenre != "All",
                        hasChevron = true,
                        onClick = { showGenreMenu = true }
                    )
                    DropdownMenu(
                        expanded = showGenreMenu,
                        onDismissRequest = { showGenreMenu = false },
                        modifier = Modifier
                            .background(SurfaceContainerHigh)
                            .heightIn(max = 380.dp)
                    ) {
                        availableGenres.forEach { genre ->
                            DropdownMenuItem(
                                text = { Text(genre, style = Typography.labelMedium, color = if (selectedGenre == genre) StatusWatching else OnSurface) },
                                onClick = {
                                    viewModel.onGenreSelect(genre)
                                    showGenreMenu = false
                                }
                            )
                        }
                    }
                }

                // Format / Type Chip Dropdown
                Box {
                    FilterPill(
                        label = "Format:",
                        value = selectedType,
                        isActive = selectedType != "All",
                        hasChevron = true,
                        onClick = { showTypeMenu = true }
                    )
                    DropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false },
                        modifier = Modifier
                            .background(SurfaceContainerHigh)
                            .heightIn(max = 380.dp)
                    ) {
                        availableTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, style = Typography.labelMedium, color = if (selectedType == type) StatusWatching else OnSurface) },
                                onClick = {
                                    viewModel.onTypeSelect(type)
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }

                // Season / Musim Chip Dropdown
                Box {
                    FilterPill(
                        label = "Musim:",
                        value = selectedSeason,
                        isActive = selectedSeason != "All",
                        hasChevron = true,
                        onClick = { showSeasonMenu = true }
                    )
                    DropdownMenu(
                        expanded = showSeasonMenu,
                        onDismissRequest = { showSeasonMenu = false },
                        modifier = Modifier
                            .background(SurfaceContainerHigh)
                            .heightIn(max = 380.dp)
                    ) {
                        availableSeasons.forEach { season ->
                            DropdownMenuItem(
                                text = { Text(season, style = Typography.labelMedium, color = if (selectedSeason == season) StatusWatching else OnSurface) },
                                onClick = {
                                    viewModel.onSeasonSelect(season)
                                    showSeasonMenu = false
                                }
                            )
                        }
                    }
                }

                // Status Chip Dropdown
                Box {
                    FilterPill(
                        label = "Status:",
                        value = selectedStatus,
                        isActive = selectedStatus != "All",
                        hasChevron = true,
                        onClick = { showStatusMenu = true }
                    )
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                        modifier = Modifier
                            .background(SurfaceContainerHigh)
                            .heightIn(max = 380.dp)
                    ) {
                        availableStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, style = Typography.labelMedium, color = if (selectedStatus == status) StatusWatching else OnSurface) },
                                onClick = {
                                    viewModel.onStatusSelect(status)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }

                // Sort Chip Dropdown
                Box {
                    FilterPill(
                        label = "Urutkan:",
                        value = selectedSort.displayName,
                        isActive = selectedSort != AnimeSortOrder.TITLE_ASC,
                        hasIcon = Icons.Default.SwapVert,
                        onClick = { showSortMenu = true }
                    )
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(SurfaceContainerHigh)
                    ) {
                        AnimeSortOrder.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.displayName, style = Typography.labelMedium, color = if (selectedSort == sort) StatusWatching else OnSurface) },
                                onClick = {
                                    viewModel.onSortSelect(sort)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                // Reset Filter Chip (Only shown when filters or sort are non-default)
                val hasActiveFilters = selectedGenre != "All" || selectedType != "All" || selectedStatus != "All" || selectedSeason != "All" || selectedSort != AnimeSortOrder.TITLE_ASC
                if (hasActiveFilters) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(StatusWatching.copy(alpha = 0.15f))
                            .border(1.dp, StatusWatching.copy(alpha = 0.5f), PillShape)
                            .clickable { viewModel.clearFilters() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Reset Filter",
                                tint = StatusWatching,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Reset Filter",
                                style = Typography.labelSmall.copy(fontSize = 11.sp),
                                color = StatusWatching
                            )
                        }
                    }
                }
            }

            // 4. Result Count & Offline Tag Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginScreen, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MENAMPILKAN",
                        style = Typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.05.sp),
                        color = OnSurfaceMuted
                    )
                    Text(
                        text = "${animeList.size} Hasil",
                        style = Typography.labelSmall.copy(fontSize = 11.sp),
                        color = StatusWatching
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(SurfaceContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusCompleted)
                    )
                    Text(
                        text = "Arsip Offline",
                        style = Typography.labelSmall.copy(fontSize = 10.sp),
                        color = OnSurfaceMuted
                    )
                }
            }
        }

        // 5. 3-Column Dense Poster Grid
        if (animeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.spacing2Xl),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CompactButtonShape)
                            .background(SurfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = OnSurfaceMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Tidak Ada Anime Ditemukan",
                        style = Typography.titleMedium,
                        color = OnSurface
                    )

                    Text(
                        text = "Coba ubah kata kunci pencarian atau sesuaikan filter genre/format.",
                        style = Typography.bodySmall,
                        color = OnSurfaceMuted,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = Dimens.spacingSm)
                            .clip(PillShape)
                            .background(StatusWatching)
                            .clickable { viewModel.clearFilters() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Reset Filter",
                            style = Typography.labelMedium,
                            color = OnBackground
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.marginScreen,
                    end = Dimens.marginScreen,
                    top = Dimens.spacingXs,
                    bottom = Dimens.spacing2Xl
                ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.gutterGrid),
                verticalArrangement = Arrangement.spacedBy(Dimens.gutterGrid)
            ) {
                items(
                    items = animeList,
                    key = { it.id }
                ) { anime ->
                    AnimePosterGridItem(
                        anime = anime,
                        onClick = { onAnimeClick(anime.id) },
                        watchlistStatus = savedWatchlistMap[anime.id]
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit,
    hasChevron: Boolean = false,
    hasIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val bg = if (isActive) StatusWatching.copy(alpha = 0.15f) else SurfaceBase
    val textColor = if (isActive) OnBackground else OnSurface

    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = Typography.labelSmall.copy(fontSize = 11.sp),
            color = if (isActive) StatusWatching else OnSurfaceMuted
        )
        Text(
            text = value,
            style = Typography.labelSmall.copy(fontSize = 11.sp),
            color = textColor
        )
        if (hasChevron) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (isActive) StatusWatching else OnSurfaceMuted,
                modifier = Modifier.size(14.dp)
            )
        }
        if (hasIcon != null) {
            Icon(
                imageVector = hasIcon,
                contentDescription = null,
                tint = if (isActive) StatusWatching else OnSurfaceMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}