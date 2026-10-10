package com.zevira.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.zevira.app.ui.theme.NightNavy
import com.zevira.app.ui.theme.NightNavyEnd
import com.zevira.app.ui.theme.StarYellow
import kotlin.random.Random

data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val size: Float,
    val layer: Int, // 1, 2, or 3 for parallax
    val twinklePhase: Float
)

@Composable
fun StarSky(
    modifier: Modifier = Modifier,
    isPowerSaver: Boolean = false,
    starCount: Int = 40,
    content: (@Composable () -> Unit)? = null
) {
    if (isPowerSaver) {
        // Flat night sky without animations
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(listOf(NightNavy, NightNavyEnd))
            )
        ) {
            content?.invoke()
        }
        return
    }

    val stars = remember(starCount) {
        val random = Random(42)
        (0 until starCount).map {
            Star(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                size = random.nextFloat() * 2.5f + 1.2f,
                layer = random.nextInt(1, 4),
                twinklePhase = random.nextFloat()
            )
        }
    }

    var touchOffset by remember { mutableStateOf<Offset?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    // Shooting star animation every ~12s
    val shootingStarProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shootingStar"
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchOffset = offset
                            tryAwaitRelease()
                            touchOffset = null
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            // Night sky gradient background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(NightNavy, NightNavyEnd)
                )
            )

            // Draw twinkling stars
            for (star in stars) {
                val baseX = star.xRatio * width
                val baseY = star.yRatio * height

                // Subtle attraction towards touch point
                val currentX: Float
                val currentY: Float
                val touch = touchOffset
                if (touch != null) {
                    val dx = touch.x - baseX
                    val dy = touch.y - baseY
                    val dist = kotlin.math.hypot(dx, dy)
                    if (dist < 300f && dist > 1f) {
                        val pullFactor = (1f - dist / 300f) * 15f
                        currentX = baseX + (dx / dist) * pullFactor
                        currentY = baseY + (dy / dist) * pullFactor
                    } else {
                        currentX = baseX
                        currentY = baseY
                    }
                } else {
                    currentX = baseX
                    currentY = baseY
                }

                val alpha = ((kotlin.math.sin((twinkleProgress + star.twinklePhase) * Math.PI.toFloat()) + 1f) / 2f)
                    .coerceIn(0.25f, 1f)

                drawStarShape(
                    Offset(currentX, currentY),
                    star.size * (0.8f + alpha * 0.4f),
                    StarYellow.copy(alpha = alpha)
                )
            }

            // Draw shooting star during the first 1.5 seconds of each 12 second cycle
            val shootWindow = 1.5f / 12f
            if (shootingStarProgress < shootWindow) {
                val progressInWindow = shootingStarProgress / shootWindow
                val shootStartX = width * 0.2f + progressInWindow * width * 0.6f
                val shootStartY = height * 0.1f + progressInWindow * height * 0.4f
                val tailLength = 60f

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, StarYellow),
                        start = Offset(shootStartX - tailLength, shootStartY - tailLength * 0.6f),
                        end = Offset(shootStartX, shootStartY)
                    ),
                    start = Offset(shootStartX - tailLength, shootStartY - tailLength * 0.6f),
                    end = Offset(shootStartX, shootStartY),
                    strokeWidth = 2.5f
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = Offset(shootStartX, shootStartY)
                )
            }
        }

        content?.invoke()
    }
}

private fun DrawScope.drawStarShape(center: Offset, size: Float, color: Color) {
    drawCircle(color = color, radius = size, center = center)
    // Tiny cross sparkle for brighter stars
    if (size > 2.5f) {
        drawLine(
            color = color.copy(alpha = 0.6f),
            start = Offset(center.x - size * 1.8f, center.y),
            end = Offset(center.x + size * 1.8f, center.y),
            strokeWidth = 1f
        )
        drawLine(
            color = color.copy(alpha = 0.6f),
            start = Offset(center.x, center.y - size * 1.8f),
            end = Offset(center.x, center.y + size * 1.8f),
            strokeWidth = 1f
        )
    }
}
