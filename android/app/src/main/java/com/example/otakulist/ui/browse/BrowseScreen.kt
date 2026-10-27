package com.example.otakulist.ui.browse

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import com.example.otakulist.ui.theme.Outline
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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.model.AnimeSortOrder
import com.example.otakulist.ui.components.AnimePosterGridItem
import com.example.otakulist.ui.components.OtakuTopBar
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
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceContainer
import com.example.otakulist.ui.theme.SurfaceContainerHigh
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.OtakuTheme
import com.example.otakulist.ui.theme.Typography

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel,
    onAnimeClick: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatalogType by viewModel.selectedCatalogType.collectAsState()
    val isAdultContentEnabled by viewModel.isAdultContentEnabled.collectAsState()
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
    val context = LocalContext.current

    val colors = OtakuTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBackground)
    ) {
        // 1. Top Brand Header
        OtakuTopBar(subtitle = "Browse")

        // 1.5. Catalog Switcher Tabs (Only shown when adult content is enabled in settings)
        if (isAdultContentEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginScreen, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainer)
                    .border(0.8.dp, Outline.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isRegularSelected = selectedCatalogType == CatalogType.REGULAR
                val regularBg by animateColorAsState(
                    targetValue = if (isRegularSelected) SurfaceVariant else Color.Transparent,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "browseRegularTabBg"
                )
                val regularBorder by animateColorAsState(
                    targetValue = if (isRegularSelected) PrimaryAccent.copy(alpha = 0.45f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "browseRegularTabBorder"
                )
                val regularIconTint by animateColorAsState(
                    targetValue = if (isRegularSelected) PrimaryAccent else OnSurfaceMuted,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "browseRegularTabIconTint"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(regularBg)
                        .border(
                            0.8.dp,
                            regularBorder,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { viewModel.onCatalogTypeSelect(CatalogType.REGULAR) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = regularIconTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Anime Reguler",
                            style = Typography.labelMedium.copy(
                                fontWeight = if (isRegularSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isRegularSelected) OnBackground else OnSurfaceMuted
                        )
                    }
                }

                val isAdultSelected = selectedCatalogType == CatalogType.ADULT
                val adultBg by animateColorAsState(
                    targetValue = if (isAdultSelected) (if (OtakuTheme.colors.isDark) Color(0xFF381A22) else Color(0xFFFFECEE)) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "browseAdultTabBg"
                )
                val adultBorder by animateColorAsState(
                    targetValue = if (isAdultSelected) (if (OtakuTheme.colors.isDark) Color(0xFFFF5252).copy(alpha = 0.55f) else Color(0xFFE53935).copy(alpha = 0.55f)) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "browseAdultTabBorder"
                )
                val adultIconTint by animateColorAsState(
                    targetValue = if (isAdultSelected) (if (OtakuTheme.colors.isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)) else OnSurfaceMuted,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "browseAdultTabIconTint"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(adultBg)
                        .border(
                            0.8.dp,
                            adultBorder,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { viewModel.onCatalogTypeSelect(CatalogType.ADULT) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = adultIconTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Anime Dewasa (18+)",
                            style = Typography.labelMedium.copy(
                                fontWeight = if (isAdultSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isAdultSelected) (if (OtakuTheme.colors.isDark) Color(0xFFFF6B6B) else Color(0xFFD32F2F)) else OnSurfaceMuted
                        )
                    }
                }
            }
        }

        // 2. Sticky Search Bar Container (Height 48dp, SurfaceContainer, rounded 12dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CanvasBackground)
                .padding(top = if (isAdultContentEnabled) Dimens.spacingXs else Dimens.spacingSm, bottom = Dimens.spacingXs)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginScreen)
                    .height(48.dp)
                    .clip(CardShape)
                    .background(SurfaceContainer)
                    .border(1.dp, Outline.copy(alpha = 0.45f), CardShape)
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

            // 3. Fixed 2x2 Filter Matrix (Genre, Format, Musim, Status)
            val hasActiveFilters = selectedGenre != "All" || selectedType != "All" || selectedStatus != "All" || selectedSeason != "All" || selectedSort != AnimeSortOrder.TITLE_ASC

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginScreen, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Genre & Format
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Genre Filter
                    Box(modifier = Modifier.weight(1f)) {
                        val isGenreActive = selectedGenre != "All"
                        FixedFilterPill(
                            label = "Genre",
                            selectedValue = selectedGenre,
                            isActive = isGenreActive,
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
                                val isSelected = selectedGenre == genre
                                val displayText = if (genre == "All") "Semua Genre" else genre
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            displayText,
                                            style = Typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) StatusWatching else OnSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.onGenreSelect(genre)
                                        showGenreMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Format Filter
                    Box(modifier = Modifier.weight(1f)) {
                        val isTypeActive = selectedType != "All"
                        FixedFilterPill(
                            label = "Format",
                            selectedValue = selectedType,
                            isActive = isTypeActive,
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
                                val isSelected = selectedType == type
                                val displayText = if (type == "All") "Semua Format" else type
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            displayText,
                                            style = Typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) StatusWatching else OnSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.onTypeSelect(type)
                                        showTypeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Row 2: Musim & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Season Filter
                    Box(modifier = Modifier.weight(1f)) {
                        val isSeasonActive = selectedSeason != "All"
                        FixedFilterPill(
                            label = "Musim",
                            selectedValue = selectedSeason,
                            isActive = isSeasonActive,
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
                                val isSelected = selectedSeason == season
                                val displayText = if (season == "All") "Semua Musim" else season
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            displayText,
                                            style = Typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) StatusWatching else OnSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.onSeasonSelect(season)
                                        showSeasonMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Status Filter
                    Box(modifier = Modifier.weight(1f)) {
                        val isStatusActive = selectedStatus != "All"
                        FixedFilterPill(
                            label = "Status",
                            selectedValue = selectedStatus,
                            isActive = isStatusActive,
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
                                val isSelected = selectedStatus == status
                                val displayText = if (status == "All") "Semua Status" else status
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            displayText,
                                            style = Typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) StatusWatching else OnSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.onStatusSelect(status)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Result Count, Reset & Sort Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.marginScreen, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Count + Reset Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formattedCount = String.format(java.util.Locale("id", "ID"), "%,d", animeList.size)
                    Text(
                        text = "$formattedCount anime",
                        style = Typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = OnSurfaceMuted
                    )

                    if (hasActiveFilters) {
                        Row(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(StatusWatching.copy(alpha = 0.15f))
                                .border(0.8.dp, StatusWatching.copy(alpha = 0.6f), PillShape)
                                .clickable { viewModel.clearFilters() }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Reset Filter",
                                tint = StatusWatching,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Reset",
                                style = Typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = StatusWatching
                            )
                        }
                    }
                }

                // Right: Sort Dropdown Button
                Box {
                    val isCustomSort = selectedSort != AnimeSortOrder.TITLE_ASC
                    Row(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (isCustomSort) StatusWatching.copy(alpha = 0.15f) else SurfaceContainer)
                            .border(
                                0.8.dp,
                                if (isCustomSort) StatusWatching.copy(alpha = 0.5f) else Outline.copy(alpha = 0.35f),
                                PillShape
                            )
                            .clickable { showSortMenu = true }
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Urutkan",
                            tint = if (isCustomSort) StatusWatching else OnSurfaceMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = selectedSort.displayName,
                            style = Typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isCustomSort) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isCustomSort) StatusWatching else OnSurface
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (isCustomSort) StatusWatching else OnSurfaceMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(SurfaceContainerHigh)
                    ) {
                        AnimeSortOrder.entries.forEach { sort ->
                            val isSelected = selectedSort == sort
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        sort.displayName,
                                        style = Typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) StatusWatching else OnSurface
                                    )
                                },
                                onClick = {
                                    viewModel.onSortSelect(sort)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
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
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.gutterGrid),
                verticalArrangement = Arrangement.spacedBy(Dimens.gutterGrid)
            ) {
                items(
                    items = animeList,
                    key = { "${it.id}_${it.isAdult}" }
                ) { anime ->
                    AnimePosterGridItem(
                        anime = anime,
                        onClick = { onAnimeClick(anime.id, selectedCatalogType == CatalogType.ADULT) },
                        watchlistStatus = savedWatchlistMap[anime.id],
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                            fadeOutSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            placementSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FixedFilterPill(
    label: String,
    selectedValue: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) StatusWatching.copy(alpha = 0.15f) else SurfaceContainer
    val borderColor = if (isActive) StatusWatching.copy(alpha = 0.6f) else Outline.copy(alpha = 0.35f)
    val contentColor = if (isActive) StatusWatching else OnSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.8.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isActive) selectedValue else label,
            style = Typography.labelMedium.copy(
                fontSize = 11.5.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = if (isActive) StatusWatching else OnSurfaceMuted,
            modifier = Modifier.size(15.dp)
        )
    }
}