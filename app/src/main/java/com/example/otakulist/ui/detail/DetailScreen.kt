package com.example.otakulist.ui.detail

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.local.entity.RelatedAnimeItem
import com.example.otakulist.data.local.entity.UserWatchlistEntity
import com.example.otakulist.data.local.entity.getParsedRelatedAnime
import com.example.otakulist.data.local.entity.getPrimaryStudio
import com.example.otakulist.data.local.entity.getProducers
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.ui.components.AnimeCoverImage
import com.example.otakulist.ui.components.OtakuDetailTopBar
import com.example.otakulist.ui.theme.CanvasBackground
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.CompactButtonShape
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.Error
import com.example.otakulist.ui.theme.OnBackground
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.OnSurfaceVariant
import com.example.otakulist.ui.theme.Outline
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.StatusDropped
import com.example.otakulist.ui.theme.StatusPlanToWatch
import com.example.otakulist.ui.theme.StatusWatching
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.SurfaceBright
import com.example.otakulist.ui.theme.SurfaceContainer
import com.example.otakulist.ui.theme.SurfaceContainerHigh
import com.example.otakulist.ui.theme.SurfaceContainerLow
import com.example.otakulist.ui.theme.SurfaceContainerLowest
import com.example.otakulist.ui.theme.SurfaceVariant
import com.example.otakulist.ui.theme.Tertiary
import com.example.otakulist.ui.theme.OtakuTheme
import com.example.otakulist.ui.theme.Typography

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onAnimeClick: (Int, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val anime by viewModel.anime.collectAsState()
    val watchlistEntry by viewModel.watchlistEntry.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val context = LocalContext.current
    val colors = OtakuTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Bar
            OtakuDetailTopBar(
                title = "Detail Anime",
                onBackClick = onBackClick,
                onShareClick = {
                    anime?.let { item ->
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Lihat ${item.title} di OtakuList - Offline Anime Tracker!")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan anime"))
                    }
                }
            )

            anime?.let { item ->
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .navigationBarsPadding()
                        .padding(bottom = Dimens.spacing2Xl)
                ) {
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))

                    // 2. Hero Section (Poster + Title + Metadata)
                    HeroSection(anime = item)

                    Spacer(modifier = Modifier.height(Dimens.spacingMd))

                // 4. Action Area (Unsaved vs Saved State)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.marginScreen)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                ) {
                    if (!isSaved) {
                        UnsavedActionSection(
                            onAddToWatchlist = {
                                viewModel.addToWatchlist(WatchlistStatus.BELUM)
                                Toast.makeText(context, "Ditambahkan ke Watchlist", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        watchlistEntry?.let { entry ->
                            SavedActionSection(
                                entry = entry,
                                onToggleStatus = {
                                    viewModel.toggleStatus()
                                    val isSudah = entry.status == WatchlistStatus.SUDAH.value || entry.status == "COMPLETED"
                                    val msg = if (isSudah) "Diubah ke Belum Ditonton" else "Ditandai Sudah Ditonton"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                onRemove = {
                                    viewModel.removeFromWatchlist()
                                    Toast.makeText(context, "Dihapus dari Watchlist", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingLg))

                // 5. Genre Tags
                val genres = item.genres?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                if (genres.isNotEmpty()) {
                    GenreChipsSection(genres = genres)
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                }

                // 6. Synopsis Card
                if (!item.synopsis.isNullOrBlank()) {
                    SynopsisSection(synopsis = item.synopsis)
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                }

                // 6.5. Anime Terkait (Related Anime)
                val relatedList = item.getParsedRelatedAnime()
                if (relatedList.isNotEmpty()) {
                    RelatedAnimeSection(
                        currentAnimeId = item.id,
                        relatedList = relatedList,
                        onAnimeClick = { targetId -> onAnimeClick(targetId, item.isAdult) }
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                }

                // 7. Information Grid (2-Column)
                InformationGridSection(anime = item)
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Memuat data anime...",
                    style = Typography.bodyMedium,
                    color = OnSurfaceMuted
                )
            }
        }
    }

    // Left edge swipe detector for back navigation
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(36.dp)
            .align(Alignment.CenterStart)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (dragAmount > 20f) {
                        change.consume()
                        onBackClick()
                    }
                }
            }
    )
}
}

@Composable
private fun HeroSection(
    anime: AnimeEntity
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.marginScreen, vertical = Dimens.spacingSm),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        // Poster Frame 2:3 with Score Badge at Bottom-Left
        Box(
            modifier = Modifier
                .width(130.dp)
                .aspectRatio(2f / 3f)
                .clip(CardShape)
                .background(SurfaceContainerLow)
                .border(1.dp, Outline.copy(alpha = 0.5f), CardShape)
        ) {
            AnimeCoverImage(
                coverFile = anime.coverFile,
                contentDescription = anime.title,
                modifier = Modifier.fillMaxSize(),
                isAdult = anime.isAdult
            )

            if (!anime.score.isNullOrBlank() && anime.score != "0" && anime.score != "?") {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerLowest.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Score",
                        tint = Tertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = anime.score,
                        style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }
            }
        }

        // Title and Quick Metadata Column
        Column(
            modifier = Modifier
                .weight(1f)
                .height(195.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (anime.isAdult) {
                    Box(
                        modifier = Modifier
                            .clip(CompactButtonShape)
                            .background(Color(0xFFE53935).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFE53935), CompactButtonShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "18+ Dewasa",
                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF5252)
                        )
                    }
                } else if (!anime.score.isNullOrBlank() && anime.score != "0" && anime.score != "?") {
                    Box(
                        modifier = Modifier
                            .clip(CompactButtonShape)
                            .background(SurfaceContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Koleksi Favorit",
                            style = Typography.labelSmall.copy(fontSize = 10.sp),
                            color = Tertiary
                        )
                    }
                }

                Text(
                    text = anime.title,
                    style = Typography.headlineLarge.copy(fontSize = 20.sp, lineHeight = 26.sp),
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val secondaryTitle = anime.japaneseTitle ?: anime.englishTitle
                if (!secondaryTitle.isNullOrBlank()) {
                    Text(
                        text = secondaryTitle,
                        style = Typography.labelMedium.copy(fontStyle = FontStyle.Italic, fontSize = 12.sp),
                        color = OnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Info Rows with Icons
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val type = anime.type ?: "TV Series"
                val ep = anime.episodes?.let { "$it Ep" } ?: "? Ep"
                MetaIconRow(
                    icon = Icons.Default.Tv,
                    iconTint = StatusWatching,
                    text = "$type · $ep"
                )

                val statusText = anime.status ?: "Selesai Tayang"
                MetaIconRow(
                    icon = Icons.Default.Verified,
                    iconTint = StatusCompleted,
                    text = statusText
                )

                val durationText = anime.duration?.takeIf { it.isNotBlank() } ?: "24 Menit / episode"
                MetaIconRow(
                    icon = Icons.Default.Schedule,
                    iconTint = OnSurfaceVariant,
                    text = durationText
                )
            }
        }
    }
}

@Composable
private fun MetaIconRow(icon: ImageVector, iconTint: Color, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            style = Typography.labelSmall.copy(fontSize = 11.sp),
            color = OnSurfaceMuted
        )
    }
}

@Composable
private fun UnsavedActionSection(
    onAddToWatchlist: () -> Unit
) {
    Button(
        onClick = onAddToWatchlist,
        colors = ButtonDefaults.buttonColors(
            containerColor = StatusWatching,
            contentColor = OnBackground
        ),
        shape = CardShape,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PlaylistAdd,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Tambah ke Watchlist",
            style = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun SavedActionSection(
    entry: UserWatchlistEntity,
    onToggleStatus: () -> Unit,
    onRemove: () -> Unit
) {
    val isSudah = entry.status == WatchlistStatus.SUDAH.value || entry.status == "COMPLETED"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceBase)
            .border(1.dp, Outline.copy(alpha = 0.35f), CardShape)
            .padding(Dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        // 1. Header: Status Watchlist & Toggle Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "STATUS TONTONAN",
                    style = Typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                    color = OnSurfaceMuted
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSudah) StatusCompleted else StatusWatching)
                    )
                    Text(
                        text = if (isSudah) "Sudah Ditonton" else "Belum Ditonton",
                        style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSudah) StatusCompleted else OnBackground
                    )
                }
            }

            Button(
                onClick = onToggleStatus,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSudah) SurfaceContainerHigh else StatusCompleted,
                    contentColor = if (isSudah) OnSurface else OnBackground
                ),
                shape = CompactButtonShape,
                modifier = Modifier.height(36.dp)
            ) {
                if (isSudah) {
                    Text(
                        text = "Ubah ke Belum",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tandai Sudah",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        HorizontalDivider(color = Outline.copy(alpha = 0.3f), thickness = 0.8.dp)

        // 2. Remove Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CompactButtonShape)
                .clickable(onClick = onRemove)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Error,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Hapus dari Watchlist",
                style = Typography.labelSmall.copy(color = Error, fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun GenreChipsSection(genres: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.marginScreen),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
        ) {
            genres.forEach { genre ->
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(SurfaceBase)
                        .border(1.dp, Outline.copy(alpha = 0.5f), PillShape)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = genre,
                        style = Typography.labelSmall.copy(fontSize = 11.sp),
                        color = OnSurfaceMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SynopsisSection(synopsis: String) {
    var expanded by remember(synopsis) { mutableStateOf(false) }
    var canExpand by remember(synopsis) { mutableStateOf(false) }
    val formattedSynopsis = remember(synopsis) { cleanSynopsisText(synopsis) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.marginScreen)
            .clip(CardShape)
            .background(SurfaceBase)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing
                )
            )
            .padding(Dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sinopsis",
                style = Typography.titleMedium,
                color = OnSurface
            )
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = OnSurfaceMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = formattedSynopsis,
            style = Typography.bodyMedium.copy(lineHeight = 22.sp),
            color = OnSurface,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!expanded) {
                    canExpand = textLayoutResult.hasVisualOverflow || textLayoutResult.lineCount > 4
                }
            },
            modifier = if (canExpand) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { expanded = !expanded }
            } else Modifier
        )

        if (canExpand) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier
                        .clip(CompactButtonShape)
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (expanded) "Lebih Sedikit" else "Selengkapnya",
                        style = Typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = StatusWatching
                        )
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = StatusWatching,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun cleanSynopsisText(raw: String?): String {
    if (raw.isNullOrBlank()) return "Tidak ada sinopsis tersedia."
    var t = raw.replace("\r\n", "\n").replace("\r", "\n")
    t = Regex("\"\\s*\\n\\s*").replace(t, "\"")
    t = Regex("\\s*\\n\\s*\"").replace(t, "\"")
    t = Regex("(?<!\\n)\\n(?!\\n)").replace(t, " ")
    t = Regex("\\s+([.,!?:;])").replace(t, "$1")
    t = Regex("([a-zA-Z0-9_.,!?:;]\")([a-zA-Z0-9_])").replace(t, "$1 $2")
    t = Regex("([a-zA-Z0-9_])\"([a-zA-Z0-9_])").replace(t, "$1 \"$2")
    return Regex("[ \\t]{2,}").replace(t, " ").trim()
}

@Composable
private fun InformationGridSection(anime: AnimeEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.marginScreen),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Informasi Detail",
                style = Typography.titleMedium,
                color = OnSurface
            )
            Text(
                text = "Arsip OtakuList",
                style = Typography.labelSmall,
                color = OnSurfaceMuted
            )
        }

        // 2-Column Grid (Studio, Total Episode, Durasi, Rating Usia, Musim Rilis, Sumber Asal)
        val studioDisplay = anime.getPrimaryStudio() ?: "-"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            InfoCard(
                icon = Icons.Default.Apartment,
                label = "Studio",
                value = studioDisplay,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            InfoCard(
                icon = Icons.Default.Tag,
                label = "Total Episode",
                value = anime.episodes?.takeIf { it.isNotBlank() }?.let { "$it Episode" } ?: "-",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            InfoCard(
                icon = Icons.Default.Schedule,
                label = "Durasi",
                value = anime.duration?.takeIf { it.isNotBlank() } ?: "-",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            InfoCard(
                icon = Icons.Default.Shield,
                label = "Rating Usia",
                value = anime.rating?.takeIf { it.isNotBlank() } ?: "-",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            InfoCard(
                icon = Icons.Default.CalendarMonth,
                label = "Musim Rilis",
                value = anime.season?.takeIf { it.isNotBlank() } ?: "-",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            InfoCard(
                icon = Icons.Default.AutoStories,
                label = "Sumber Asal",
                value = "Katalog Resmi",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        InfoCard(
            icon = Icons.Default.AutoAwesomeMotion,
            label = "Produser",
            value = anime.getProducers() ?: "-",
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2
) {
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(SurfaceBase)
            .padding(Dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceMuted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = Typography.labelSmall.copy(fontSize = 11.sp),
                color = OnSurfaceMuted
            )
        }
        Text(
            text = value,
            style = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = OnSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RelatedAnimeSection(
    currentAnimeId: Int,
    relatedList: List<RelatedAnimeItem>,
    onAnimeClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.marginScreen)
            .clip(CardShape)
            .background(SurfaceBase)
            .padding(Dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CompactButtonShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesomeMotion,
                    contentDescription = null,
                    tint = StatusWatching,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Anime Terkait",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )
            Text(
                text = "(${relatedList.size})",
                style = Typography.labelSmall.copy(fontSize = 11.sp),
                color = OnSurfaceMuted
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            relatedList.forEach { related ->
                val isCurrent = related.id == currentAnimeId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCurrent) StatusWatching.copy(alpha = 0.15f) else SurfaceContainer)
                        .border(
                            width = 1.dp,
                            color = if (isCurrent) StatusWatching else Outline.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !isCurrent) { onAnimeClick(related.id) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!related.type.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isCurrent) StatusWatching else StatusWatching.copy(alpha = 0.18f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = related.type,
                                    style = Typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isCurrent) OnBackground else StatusWatching
                                )
                            }
                        }
                        Text(
                            text = related.title,
                            style = Typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isCurrent) StatusWatching else OnSurface
                        )
                    }
                }
            }
        }
    }
}