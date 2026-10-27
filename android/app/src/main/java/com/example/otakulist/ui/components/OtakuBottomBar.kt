package com.example.otakulist.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.otakulist.ui.theme.OtakuTheme
import kotlin.math.abs
import kotlin.math.roundToInt

enum class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    WATCHLIST("watchlist", "Watchlist", Icons.Default.Bookmarks),
    BROWSE("browse", "Browse", Icons.Default.Explore),
    STATS("stats", "Stats", Icons.Default.BarChart),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

private val BarHeight = 52.dp
private val CircleSize = 44.dp

@Composable
fun OtakuBottomBar(
    pagerPosition: () -> Float,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsLength = NavDestination.entries.size
    val continuousPos = pagerPosition().coerceIn(0f, (itemsLength - 1).toFloat())
    val pos = continuousPos / itemsLength

    val nearestInt = continuousPos.roundToInt().coerceIn(0, itemsLength - 1)
    val pageDistance = abs(continuousPos - nearestInt)
    val rawProgress = (pageDistance * 2f).coerceIn(0f, 1f)
    val buttonHide = rawProgress * rawProgress * (3f - 2f * rawProgress)

    val activeDestination = NavDestination.entries[nearestInt]

    val onTabSelected: (NavDestination) -> Unit = { destination ->
        onNavigate(destination)
    }

    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val totalHeight = BarHeight + navBarBottom

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        val w = maxWidth
        val span = 1.0f / itemsLength
        val s = 0.20f
        val loc = pos + (span - s) / 2.0f

        val buttonCenterX = w * (pos + 0.5f / itemsLength)
        val buttonYOffset = (-16).dp + (34.dp * buttonHide)

        val colors = OtakuTheme.colors

        // 1. Elevated active circular bubble (draws above y=0 directly over main content, sinks behind bar during dive)
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (buttonCenterX - CircleSize / 2).roundToPx(),
                        y = buttonYOffset.roundToPx()
                    )
                }
                .size(CircleSize)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = if (colors.isDark) 0.5f else 0.2f),
                    ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.3f else 0.12f)
                )
                .clip(CircleShape)
                .background(colors.surfaceBase)
                .border(
                    width = 1.dp,
                    color = if (colors.isDark) colors.outline.copy(alpha = 0.5f) else colors.outline.copy(alpha = 0.7f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onTabSelected(activeDestination)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activeDestination.icon,
                contentDescription = activeDestination.title,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        // 2. Full-width curved notch canvas (starts at y=0, drawn in front of active button)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .clipToBounds()
        ) {
            val width = size.width
            val totalH = size.height
            val h = BarHeight.toPx()

            val path = Path().apply {
                reset()
                moveTo(0f, 0f)
                lineTo((loc - 0.10f) * width, 0f)
                cubicTo(
                    x1 = (loc + s * 0.20f) * width, y1 = h * 0.05f,
                    x2 = loc * width, y2 = h * 0.60f,
                    x3 = (loc + s * 0.50f) * width, y3 = h * 0.60f
                )
                cubicTo(
                    x1 = (loc + s) * width, y1 = h * 0.60f,
                    x2 = (loc + s - s * 0.20f) * width, y2 = h * 0.05f,
                    x3 = (loc + s + 0.10f) * width, y3 = 0f
                )
                lineTo(width, 0f)
                lineTo(width, totalH)
                lineTo(0f, totalH)
                close()
            }

            drawPath(
                path = path,
                color = colors.surfaceBase
            )

            // Subtle top edge outline
            val topStroke = Path().apply {
                reset()
                moveTo(0f, 0f)
                lineTo((loc - 0.10f) * width, 0f)
                cubicTo(
                    x1 = (loc + s * 0.20f) * width, y1 = h * 0.05f,
                    x2 = loc * width, y2 = h * 0.60f,
                    x3 = (loc + s * 0.50f) * width, y3 = h * 0.60f
                )
                cubicTo(
                    x1 = (loc + s) * width, y1 = h * 0.60f,
                    x2 = (loc + s - s * 0.20f) * width, y2 = h * 0.05f,
                    x3 = (loc + s + 0.10f) * width, y3 = 0f
                )
                lineTo(width, 0f)
            }
            drawPath(
                path = topStroke,
                color = colors.outline.copy(alpha = if (colors.isDark) 0.35f else 0.6f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 3. Row of inactive icons (compact 18dp size, centered at BarHeight)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavDestination.entries.forEachIndexed { index, destination ->
                val desiredPosition = index.toFloat() / itemsLength
                val difference = abs(pos - desiredPosition)
                val verticalAlignment = (1.0f - itemsLength * difference).coerceIn(0f, 1f)
                val opacity = (itemsLength * difference).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(destination)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title,
                        tint = if (colors.isDark) Color(0xFFC5C5D6) else Color(0xFF6E7385),
                        modifier = Modifier
                            .size(18.dp)
                            .offset(y = (verticalAlignment * 22).dp)
                            .graphicsLayer { alpha = opacity }
                    )
                }
            }
        }
    }
}

@Composable
fun OtakuBottomBar(
    currentRoute: String,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val targetIndex = NavDestination.entries.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    OtakuBottomBar(
        pagerPosition = { targetIndex.toFloat() },
        onNavigate = onNavigate,
        modifier = modifier
    )
}
