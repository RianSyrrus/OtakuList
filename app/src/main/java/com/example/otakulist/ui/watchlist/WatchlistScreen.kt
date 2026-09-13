package com.example.otakulist.ui.watchlist

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.local.entity.WatchlistWithAnime
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.ui.components.AnimeCoverImage
import com.example.otakulist.ui.components.OtakuTopBar
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
import com.example.otakulist.ui.theme.OtakuTheme
import com.example.otakulist.ui.theme.Typography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onAnimeClick: (Int, Boolean) -> Unit,
    onNavigateToBrowse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCatalogType by viewModel.selectedCatalogType.collectAsState()
    val isAdultContentEnabled by viewModel.isAdultContentEnabled.collectAsState()
    val regularCount by viewModel.regularCount.collectAsState()
    val adultCount by viewModel.adultCount.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val availableSeasons by viewModel.availableSeasons.collectAsState()
    val groupedWatchlist by viewModel.groupedWatchlist.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val sudahCount by viewModel.sudahCount.collectAsState()
    val belumCount by viewModel.belumCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val expandedSeasons by viewModel.expandedSeasons.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val colors = OtakuTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Header Bar
            OtakuTopBar(subtitle = "Watchlist")

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
                val isRegularSelected = selectedCatalogType == WatchlistCatalogType.REGULAR
                val regularBg by animateColorAsState(
                    targetValue = if (isRegularSelected) SurfaceVariant else Color.Transparent,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "regularTabBg"
                )
                val regularBorder by animateColorAsState(
                    targetValue = if (isRegularSelected) PrimaryAccent.copy(alpha = 0.45f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "regularTabBorder"
                )
                val regularIconTint by animateColorAsState(
                    targetValue = if (isRegularSelected) PrimaryAccent else OnSurfaceMuted,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "regularTabIconTint"
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
                        .clickable { viewModel.selectCatalogType(WatchlistCatalogType.REGULAR) }
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
                            text = "Reguler ($regularCount)",
                            style = Typography.labelMedium.copy(
                                fontWeight = if (isRegularSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isRegularSelected) OnBackground else OnSurfaceMuted
                        )
                    }
                }

                val isAdultSelected = selectedCatalogType == WatchlistCatalogType.ADULT
                val adultBg by animateColorAsState(
                    targetValue = if (isAdultSelected) (if (OtakuTheme.colors.isDark) Color(0xFF381A22) else Color(0xFFFFECEE)) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "adultTabBg"
                )
                val adultBorder by animateColorAsState(
                    targetValue = if (isAdultSelected) (if (OtakuTheme.colors.isDark) Color(0xFFFF5252).copy(alpha = 0.55f) else Color(0xFFE53935).copy(alpha = 0.55f)) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "adultTabBorder"
                )
                val adultIconTint by animateColorAsState(
                    targetValue = if (isAdultSelected) (if (OtakuTheme.colors.isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)) else OnSurfaceMuted,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "adultTabIconTint"
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
                        .clickable { viewModel.selectCatalogType(WatchlistCatalogType.ADULT) }
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
                            text = "Dewasa 18+ ($adultCount)",
                            style = Typography.labelMedium.copy(
                                fontWeight = if (isAdultSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isAdultSelected) (if (OtakuTheme.colors.isDark) Color(0xFFFF6B6B) else Color(0xFFD32F2F)) else OnSurfaceMuted
                        )
                    }
                }
            }
        }

        // 2. Compact Control Bar (Unified Search, Season Filter Dropdown, and Collapse Toggle)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.marginScreen, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Search Field + Filter Musim Dropdown + Toggle Collapse All
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(CardShape)
                        .background(SurfaceContainer)
                        .border(1.dp, Outline.copy(alpha = 0.45f), CardShape)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari di Watchlist",
                            tint = OnSurfaceMuted,
                            modifier = Modifier.size(17.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari judul...",
                                    style = Typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = OnSurfaceMuted
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                textStyle = Typography.bodyMedium.copy(color = OnSurface, fontSize = 13.sp),
                                cursorBrush = SolidColor(PrimaryAccent),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus pencarian",
                                    tint = OnSurfaceMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // Season Filter Dropdown Button
                if (availableSeasons.size > 1) {
                    var showSeasonMenu by remember { mutableStateOf(false) }
                    val isFiltered = selectedSeason != "Semua"
                    val buttonBg = if (isFiltered) StatusWatching.copy(alpha = 0.2f) else SurfaceContainer
                    val borderColor = if (isFiltered) StatusWatching else Outline.copy(alpha = 0.45f)

                    Box {
                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .clip(CardShape)
                                .background(buttonBg)
                                .border(1.dp, borderColor, CardShape)
                                .clickable { showSeasonMenu = true }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (isFiltered) StatusWatching else OnSurfaceMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isFiltered) selectedSeason else "Semua Musim",
                                    style = Typography.labelMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (isFiltered) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isFiltered) StatusWatching else OnSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = if (isFiltered) StatusWatching else OnSurfaceMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSeasonMenu,
                            onDismissRequest = { showSeasonMenu = false },
                            modifier = Modifier.background(SurfaceContainerHigh)
                        ) {
                            availableSeasons.forEach { season ->
                                val isSelected = selectedSeason == season
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (season == "Semua") "Semua Musim" else season,
                                            style = Typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) PrimaryAccent else OnSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectSeason(season)
                                        showSeasonMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Toggle Collapse All Button
                val allCurrentSeasons = groupedWatchlist.map { it.season }.toSet()
                val isAllExpanded = allCurrentSeasons.isNotEmpty() && expandedSeasons.containsAll(allCurrentSeasons)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CardShape)
                        .background(SurfaceContainer)
                        .border(1.dp, Outline.copy(alpha = 0.45f), CardShape)
                        .clickable { viewModel.toggleAllCollapse() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAllExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                        contentDescription = if (isAllExpanded) "Ciutkan Semua" else "Buka Semua",
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Row 2: Status Filter Tabs (Semua, Belum, Sudah)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPillTab(
                    label = "Semua",
                    count = totalCount,
                    isSelected = statusFilter == "Semua",
                    dotColor = PrimaryAccent,
                    activeBg = PrimaryAccent.copy(alpha = 0.18f),
                    activeBorder = PrimaryAccent,
                    onClick = { viewModel.selectStatusFilter("Semua") },
                    modifier = Modifier.weight(1f)
                )
                StatusPillTab(
                    label = "Belum",
                    count = belumCount,
                    isSelected = statusFilter == "BELUM",
                    dotColor = Color(0xFFFFB74D),
                    activeBg = if (OtakuTheme.colors.isDark) Color(0xFF382A1A) else Color(0xFFFFF3E0),
                    activeBorder = Color(0xFFFFB74D),
                    onClick = { viewModel.selectStatusFilter("BELUM") },
                    modifier = Modifier.weight(1f)
                )
                StatusPillTab(
                    label = "Sudah",
                    count = sudahCount,
                    isSelected = statusFilter == "SUDAH",
                    dotColor = StatusCompleted,
                    activeBg = StatusCompleted.copy(alpha = 0.18f),
                    activeBorder = StatusCompleted,
                    onClick = { viewModel.selectStatusFilter("SUDAH") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Main Content: Season-Grouped List with Sticky Season Headers
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
                    top = 2.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
            ) {
                groupedWatchlist.forEach { group ->
                    val isExpanded = if (searchQuery.isNotBlank()) true else expandedSeasons.contains(group.season)

                    stickyHeader(key = "header_${group.season}") {
                        SeasonSectionHeader(
                            season = group.season,
                            count = group.items.size,
                            totalInSeason = group.totalInSeason,
                            isCollapsed = !isExpanded,
                            onToggleCollapse = { viewModel.toggleSeasonExpand(group.season) },
                            modifier = Modifier.animateItem(
                                placementSpec = spring(
                                    dampingRatio = 0.82f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        )
                    }

                    if (isExpanded) {
                        itemsIndexed(
                            items = group.items,
                            key = { _, item -> "${item.watchlist.animeId}_${item.anime.isAdult}" }
                        ) { index, item ->
                            SeasonWatchlistItemCard(
                                number = index + 1,
                                item = item,
                                onClick = { onAnimeClick(item.watchlist.animeId, selectedCatalogType == WatchlistCatalogType.ADULT) },
                                onToggleStatus = {
                                    val isSudah = item.watchlist.status == WatchlistStatus.SUDAH.value || item.watchlist.status == "COMPLETED"
                                    val newStatusText = if (isSudah) "Belum Ditonton" else "Sudah Ditonton"
                                    viewModel.toggleWatchlistStatus(item.watchlist.animeId)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val shortTitle = if (item.anime.title.length > 25) {
                                            item.anime.title.take(22) + "..."
                                        } else {
                                            item.anime.title
                                        }
                                        val result = snackbarHostState.showSnackbar(
                                            message = "\"$shortTitle\" diubah ke $newStatusText",
                                            actionLabel = "URUNGKAN",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.toggleWatchlistStatus(item.watchlist.animeId)
                                        }
                                    }
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                                    fadeOutSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                    placementSpec = spring(
                                        dampingRatio = 0.82f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            )
                        }

                        item(key = "spacer_${group.season}") {
                            Spacer(
                                modifier = Modifier
                                    .height(Dimens.spacingXs)
                                    .animateItem(
                                        fadeInSpec = tween(200),
                                        fadeOutSpec = tween(150),
                                        placementSpec = spring(
                                            dampingRatio = 0.82f,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                            )
                        }
                    }
                }

                item(key = "footer_offline") {
                    OfflineCacheFooterCard()
                }
            }
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 88.dp, start = 12.dp, end = 12.dp),
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = SurfaceContainerHigh,
                contentColor = OnSurface,
                actionColor = StatusWatching,
                shape = CardShape
            )
        }
    )
}
}

@Composable
private fun StatusPillTab(
    label: String,
    count: Int,
    isSelected: Boolean,
    dotColor: Color,
    activeBg: Color,
    activeBorder: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) activeBg else SurfaceContainer
    val borderModifier = if (isSelected) {
        Modifier.border(1.2.dp, activeBorder, CardShape)
    } else {
        Modifier.border(0.8.dp, Outline.copy(alpha = 0.4f), CardShape)
    }
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
                .background(if (isSelected) dotColor else dotColor.copy(alpha = 0.6f))
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
            style = Typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) textColor else OnSurfaceMuted
        )
    }
}

@Composable
private fun SeasonSectionHeader(
    season: String,
    count: Int,
    totalInSeason: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "seasonChevronRotation"
    )
    val borderColor by animateColorAsState(
        targetValue = if (!isCollapsed) StatusWatching.copy(alpha = 0.55f) else Outline.copy(alpha = 0.45f),
        animationSpec = tween(durationMillis = 260),
        label = "seasonHeaderBorder"
    )
    val headerBg by animateColorAsState(
        targetValue = if (!isCollapsed) SurfaceContainerHigh else SurfaceContainer,
        animationSpec = tween(durationMillis = 260),
        label = "seasonHeaderBg"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CanvasBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(CardShape)
                .background(headerBg)
                .border(1.dp, borderColor, CardShape)
                .clickable(onClick = onToggleCollapse)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CompactButtonShape)
                        .background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = StatusWatching,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Text(
                    text = season,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = OnBackground
                )

                val countLabel = if (totalInSeason > count) "$count dari $totalInSeason judul" else "$count judul"
                val isFilteredSeason = totalInSeason > count
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(if (isFilteredSeason) StatusWatching.copy(alpha = 0.15f) else SurfaceVariant)
                        .border(
                            width = if (isFilteredSeason) 0.8.dp else 0.dp,
                            color = if (isFilteredSeason) StatusWatching.copy(alpha = 0.6f) else Color.Transparent,
                            shape = PillShape
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = countLabel,
                        style = Typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isFilteredSeason) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isFilteredSeason) StatusWatching else OnSurfaceMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isCollapsed) "Buka musim $season" else "Ciutkan musim $season",
                tint = if (!isCollapsed) StatusWatching else OnSurfaceMuted,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = chevronRotation }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SeasonWatchlistItemCard(
    number: Int,
    item: WatchlistWithAnime,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isSudah = item.watchlist.status == WatchlistStatus.SUDAH.value || item.watchlist.status == "COMPLETED"

    val buttonBgColor by animateColorAsState(
        targetValue = if (isSudah) StatusCompleted.copy(alpha = 0.15f) else SurfaceContainerHigh,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "toggleBg"
    )
    val buttonBorderColor by animateColorAsState(
        targetValue = if (isSudah) StatusCompleted.copy(alpha = 0.7f) else Outline.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "toggleBorder"
    )

    Row(
        modifier = modifier
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
                    modifier = Modifier.fillMaxSize(),
                    isAdult = item.anime.isAdult
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

            // 4. Toggle Button: Sudah vs Belum (Long-press protected against accidental scrolling taps)
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(buttonBgColor)
                    .border(
                        width = 1.dp,
                        color = buttonBorderColor,
                        shape = PillShape
                    )
                    .combinedClickable(
                        onClick = {
                            Toast.makeText(context, "Tahan tombol untuk mengubah status", Toast.LENGTH_SHORT).show()
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleStatus()
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isSudah,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(initialScale = 0.85f, animationSpec = tween(180)))
                            .togetherWith(fadeOut(tween(140)) + scaleOut(targetScale = 0.85f, animationSpec = tween(140)))
                    },
                    label = "statusButtonContent"
                ) { sudah ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (sudah) {
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