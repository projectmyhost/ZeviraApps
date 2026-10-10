package com.zevira.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.NightNavy
import com.zevira.app.ui.theme.SkyBlue
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.SurfaceWhite

enum class MascotState {
    IDLE,
    WALKING,
    CELEBRATING,
    SLEEPY
}

@Composable
fun MascotJejak(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    state: MascotState = MascotState.IDLE
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascotAnim")

    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = when (state) {
            MascotState.WALKING -> 8f
            MascotState.CELEBRATING -> 14f
            MascotState.IDLE -> 3f
            MascotState.SLEEPY -> 1f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    MascotState.WALKING -> 350
                    MascotState.CELEBRATING -> 250
                    else -> 900
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Canvas(modifier = modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height
        val centerX = width / 2f
        val centerY = height / 2f + bounceOffset

        val radius = width * 0.38f
        val outlineWidth = 3.dp.toPx()

        // 1. Shadow underneath
        drawOval(
            color = Color(0x330F1B3D),
            topLeft = Offset(centerX - radius * 0.8f, height - 10.dp.toPx()),
            size = Size(radius * 1.6f, 10.dp.toPx())
        )

        // 2. Body (Round Blue Character)
        drawCircle(
            color = ElectricBlue,
            radius = radius,
            center = Offset(centerX, centerY)
        )

        // Soft highlight gradient on body top-left
        drawCircle(
            color = SkyBlue,
            radius = radius * 0.35f,
            center = Offset(centerX - radius * 0.4f, centerY - radius * 0.4f)
        )

        // Body outline
        drawCircle(
            color = NightNavy,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = outlineWidth)
        )

        // 3. Eyes based on MascotState
        when (state) {
            MascotState.SLEEPY -> {
                // Closed curved sleeping eyes
                drawSleepingEye(this, centerX - radius * 0.35f, centerY - radius * 0.1f, radius * 0.2f, outlineWidth)
                drawSleepingEye(this, centerX + radius * 0.35f, centerY - radius * 0.1f, radius * 0.2f, outlineWidth)
            }
            MascotState.CELEBRATING -> {
                // Happy curved eyes ^ ^
                drawHappyEye(this, centerX - radius * 0.35f, centerY - radius * 0.15f, radius * 0.22f, outlineWidth)
                drawHappyEye(this, centerX + radius * 0.35f, centerY - radius * 0.15f, radius * 0.22f, outlineWidth)
            }
            else -> {
                // Cute open cartoon eyes with white shine
                drawOpenEye(this, centerX - radius * 0.35f, centerY - radius * 0.1f, radius * 0.18f, outlineWidth)
                drawOpenEye(this, centerX + radius * 0.35f, centerY - radius * 0.1f, radius * 0.18f, outlineWidth)
            }
        }

        // 4. Smile / Mouth
        val mouthPath = Path().apply {
            if (state == MascotState.CELEBRATING) {
                moveTo(centerX - radius * 0.25f, centerY + radius * 0.2f)
                quadraticBezierTo(
                    centerX, centerY + radius * 0.5f,
                    centerX + radius * 0.25f, centerY + radius * 0.2f
                )
            } else {
                moveTo(centerX - radius * 0.2f, centerY + radius * 0.22f)
                quadraticBezierTo(
                    centerX, centerY + radius * 0.38f,
                    centerX + radius * 0.2f, centerY + radius * 0.22f
                )
            }
        }
        drawPath(
            path = mouthPath,
            color = NightNavy,
            style = Stroke(width = outlineWidth, cap = StrokeCap.Round)
        )

        // 5. Compass Explorer Badge on Chest
        val badgeX = centerX + radius * 0.5f
        val badgeY = centerY + radius * 0.45f
        val badgeR = radius * 0.28f

        drawCircle(
            color = StarYellow,
            radius = badgeR,
            center = Offset(badgeX, badgeY)
        )
        drawCircle(
            color = NightNavy,
            radius = badgeR,
            center = Offset(badgeX, badgeY),
            style = Stroke(width = outlineWidth * 0.8f)
        )

        // Mini red/blue needle in compass badge
        drawLine(
            color = Color(0xFFFF6B6B),
            start = Offset(badgeX, badgeY),
            end = Offset(badgeX - 4.dp.toPx(), badgeY - 5.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = ElectricBlue,
            start = Offset(badgeX, badgeY),
            end = Offset(badgeX + 4.dp.toPx(), badgeY + 5.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun drawOpenEye(drawScope: DrawScope, cx: Float, cy: Float, r: Float, outlineWidth: Float) {
    drawScope.drawCircle(
        color = SurfaceWhite,
        radius = r,
        center = Offset(cx, cy)
    )
    drawScope.drawCircle(
        color = NightNavy,
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = outlineWidth * 0.7f)
    )
    // Pupil
    drawScope.drawCircle(
        color = NightNavy,
        radius = r * 0.55f,
        center = Offset(cx + 1f, cy)
    )
    // Sparkle
    drawScope.drawCircle(
        color = SurfaceWhite,
        radius = r * 0.22f,
        center = Offset(cx - 1.5f, cy - 2f)
    )
}

private fun drawHappyEye(drawScope: DrawScope, cx: Float, cy: Float, r: Float, outlineWidth: Float) {
    val path = Path().apply {
        moveTo(cx - r * 0.8f, cy + r * 0.2f)
        quadraticBezierTo(cx, cy - r * 0.6f, cx + r * 0.8f, cy + r * 0.2f)
    }
    drawScope.drawPath(
        path = path,
        color = NightNavy,
        style = Stroke(width = outlineWidth, cap = StrokeCap.Round)
    )
}

private fun drawSleepingEye(drawScope: DrawScope, cx: Float, cy: Float, r: Float, outlineWidth: Float) {
    val path = Path().apply {
        moveTo(cx - r * 0.8f, cy - r * 0.2f)
        quadraticBezierTo(cx, cy + r * 0.5f, cx + r * 0.8f, cy - r * 0.2f)
    }
    drawScope.drawPath(
        path = path,
        color = NightNavy,
        style = Stroke(width = outlineWidth, cap = StrokeCap.Round)
    )
}
