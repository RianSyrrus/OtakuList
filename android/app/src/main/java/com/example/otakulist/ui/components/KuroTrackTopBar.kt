package com.example.otakulist.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.R
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnPrimary
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.OnSurfaceVariant
import com.example.otakulist.ui.theme.Outline
import com.example.otakulist.ui.theme.PillShape
import com.example.otakulist.ui.theme.PrimaryAccent
import com.example.otakulist.ui.theme.PrimaryMaterial
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.OtakuTheme
import com.example.otakulist.ui.theme.Typography

@Composable
fun OtakuTopBar(
    subtitle: String,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit = {}
) {
    val colors = OtakuTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surfaceBase.copy(alpha = 0.95f),
        border = BorderStroke(0.8.dp, colors.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = Dimens.marginScreen),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Logo + Title + Subtitle
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_otakulist_logo),
                    contentDescription = "OtakuList Logo",
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = "OtakuList",
                        style = Typography.headlineMedium.copy(
                            fontSize = 18.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = Typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 0.4.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.primary
                    )
                }
            }

            // Right: Standalone Offline Badge
            Row(
                modifier = Modifier
                    .clip(PillShape)
                    .background(colors.surfaceContainer)
                    .border(0.8.dp, colors.outline.copy(alpha = 0.45f), PillShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(StatusCompleted)
                )
                Text(
                    text = "OFFLINE",
                    style = Typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = StatusCompleted
                )
            }
        }
    }
}

@Composable
fun OtakuDetailTopBar(
    title: String = "Detail Anime",
    onBackClick: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = OtakuTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surfaceBase.copy(alpha = 0.95f),
        border = BorderStroke(0.8.dp, colors.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = Dimens.marginScreen),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = title,
                    style = Typography.headlineMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Standalone Offline Badge
                Row(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(colors.surfaceContainer)
                        .border(0.8.dp, colors.outline.copy(alpha = 0.45f), PillShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusCompleted)
                    )
                    Text(
                        text = "OFFLINE",
                        style = Typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = StatusCompleted
                    )
                }

                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Deprecated("Gunakan OtakuTopBar", ReplaceWith("OtakuTopBar(subtitle, modifier, onSyncClick)"))
@Composable
fun KuroTrackTopBar(
    subtitle: String,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit = {}
) = OtakuTopBar(subtitle, modifier, onSyncClick)

@Deprecated("Gunakan OtakuDetailTopBar", ReplaceWith("OtakuDetailTopBar(title, onBackClick, onShareClick, modifier)"))
@Composable
fun KuroTrackDetailTopBar(
    title: String = "Detail Anime",
    onBackClick: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) = OtakuDetailTopBar(title, onBackClick, onShareClick, modifier)