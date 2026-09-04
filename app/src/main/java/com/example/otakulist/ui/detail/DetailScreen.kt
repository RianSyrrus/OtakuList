package com.example.otakulist.ui.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.otakulist.data.local.entity.UserWatchlistEntity
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.ui.components.AnimeCoverImage
import com.example.otakulist.ui.components.KuroTrackDetailTopBar
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
import com.example.otakulist.ui.theme.Typography

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val anime by viewModel.anime.collectAsState()
    val watchlistEntry by viewModel.watchlistEntry.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
    ) {
        // 1. Top Bar
        KuroTrackDetailTopBar(
            onBackClick = onBackClick,
            onMoreClick = {}
        )

        anime?.let { item ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = Dimens.spacing2Xl)
            ) {
                // 2. Sub-Nav Bar (Offline Status + Actions)
                SubNavBar(
                    isSaved = isSaved,
                    onToggleSave = {
                        if (isSaved) {
                            viewModel.removeFromWatchlist()
                        } else {
                            viewModel.addToWatchlist(WatchlistStatus.BELUM)
                        }
                    },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Lihat ${item.title} di KuroTrack Offline Anime Tracker!")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan anime"))
                    }
                )

                // 3. Hero Section (Poster + Title + Metadata)
                HeroSection(anime = item)

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                // 4. Action Area (Unsaved vs Saved State)
                Box(modifier = Modifier.padding(horizontal = Dimens.marginScreen)) {
                    if (!isSaved) {
                        UnsavedActionSection(
                            onAddToWatchlist = { viewModel.addToWatchlist(WatchlistStatus.BELUM) }
                        )
                    } else {
                        watchlistEntry?.let { entry ->
                            SavedActionSection(
                                entry = entry,
                                anime = item,
                                onToggleStatus = { viewModel.toggleStatus() },
                                onRemove = { viewModel.removeFromWatchlist() }
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
}

@Composable
private fun SubNavBar(
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.marginScreen, vertical = Dimens.spacingXs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(PillShape)
                .background(SurfaceContainerHigh)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StatusCompleted)
            )
            Text(
                text = "Tersimpan di Penyimpanan Lokal (Offline)",
                style = Typography.labelSmall.copy(fontSize = 11.sp),
                color = OnSurfaceMuted
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CardShape)
                    .background(SurfaceBase)
                    .clickable(onClick = onToggleSave),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isSaved) StatusWatching else OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CardShape)
                    .background(SurfaceBase)
                    .clickable(onClick = onShare),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroSection(anime: AnimeEntity) {
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
                modifier = Modifier.fillMaxSize()
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
                if (!anime.score.isNullOrBlank() && anime.score != "0" && anime.score != "?") {
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
    anime: AnimeEntity,
    onToggleStatus: () -> Unit,
    onRemove: () -> Unit
) {
    val isSudah = entry.status == WatchlistStatus.SUDAH.value || entry.status == "COMPLETED"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(SurfaceBase)
            .padding(Dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        // 1. Status Watchlist & Toggle Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Status Tontonan",
                    style = Typography.labelSmall,
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

        // 2. Remove Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CompactButtonShape)
                .clickable(onClick = onRemove)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Hapus dari Watchlist",
                style = Typography.labelSmall.copy(color = Error)
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
            }
        )

        if (canExpand) {
            Row(
                modifier = Modifier
                    .clip(CompactButtonShape)
                    .clickable { expanded = !expanded }
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (expanded) "Lebih Sedikit" else "Selengkapnya",
                    style = Typography.labelSmall.copy(fontSize = 11.sp, color = StatusWatching)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = StatusWatching,
                    modifier = Modifier.size(14.dp)
                )
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
                text = "Arsip KuroTrack",
                style = Typography.labelSmall,
                color = OnSurfaceMuted
            )
        }

        // 2-Column Grid (Studio, Total Episode, Durasi, Rating Usia, Musim Rilis, Sumber Asal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            InfoCard(
                icon = Icons.Default.Apartment,
                label = "Studio",
                value = anime.studios ?: "-",
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.Default.Tag,
                label = "Total Episode",
                value = anime.episodes?.let { "$it Episode" } ?: "-",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            InfoCard(
                icon = Icons.Default.Schedule,
                label = "Durasi",
                value = anime.duration ?: "-",
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.Default.Shield,
                label = "Rating Usia",
                value = anime.rating ?: "-",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            InfoCard(
                icon = Icons.Default.CalendarMonth,
                label = "Musim Rilis",
                value = anime.season ?: "-",
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                icon = Icons.Default.AutoStories,
                label = "Sumber Asal",
                value = "Katalog Resmi",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}