package com.example.otakulist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.otakulist.ui.theme.CardShape
import com.example.otakulist.ui.theme.OnSurfaceMuted
import com.example.otakulist.ui.theme.SurfaceContainerHigh

@Composable
fun AnimeCoverImage(
    coverFile: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    shape: Shape = CardShape,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val imagePath = if (!coverFile.isNullOrBlank()) {
        "file:///android_asset/covers/$coverFile"
    } else {
        null
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imagePath)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
            .clip(shape)
            .background(SurfaceContainerHigh),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainerHigh)
            )
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = OnSurfaceMuted
                )
            }
        }
    )
}
