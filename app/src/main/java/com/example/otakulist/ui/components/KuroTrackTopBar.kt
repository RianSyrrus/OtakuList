package com.example.otakulist.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otakulist.R
import com.example.otakulist.ui.theme.Dimens
import com.example.otakulist.ui.theme.OnPrimary
import com.example.otakulist.ui.theme.OnSurface
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.OnSurfaceVariant
import com.example.otakulist.ui.theme.PrimaryMaterial
import com.example.otakulist.ui.theme.StatusCompleted
import com.example.otakulist.ui.theme.SurfaceBase
import com.example.otakulist.ui.theme.Typography

@Composable
fun KuroTrackTopBar(
    subtitle: String,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceBase.copy(alpha = 0.85f))
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
                painter = painterResource(id = R.drawable.ic_kurotrack_logo),
                contentDescription = "KuroTrack Logo",
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )

            Column {
                Text(
                    text = "KuroTrack",
                    style = Typography.headlineMedium.copy(fontSize = 18.sp, lineHeight = 22.sp),
                    color = OnSurface
                )
                Text(
                    text = subtitle,
                    style = Typography.labelSmall.copy(fontSize = 10.sp),
                    color = OnSurfaceMuted
                )
            }
        }

        // Right: Sync status + Profile Avatar
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSyncClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Offline Cache Synced",
                    tint = StatusCompleted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PrimaryMaterial),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = OnPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun KuroTrackDetailTopBar(
    title: String = "Anime Details",
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SurfaceBase.copy(alpha = 0.85f))
            .padding(horizontal = Dimens.marginScreen),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_kurotrack_logo),
                contentDescription = "KuroTrack Logo",
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(26.dp)
            )

            Text(
                text = title,
                style = Typography.headlineMedium.copy(fontSize = 18.sp),
                color = OnSurface,
                maxLines = 1
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PrimaryMaterial),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = OnPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}