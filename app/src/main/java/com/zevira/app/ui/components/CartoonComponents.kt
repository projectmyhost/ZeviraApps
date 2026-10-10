package com.zevira.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.ui.theme.CartoonBadgeShape
import com.zevira.app.ui.theme.CartoonCardShape
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.PillShape
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary

@Composable
fun CartoonCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceWhite,
    borderColor: Color = StickerBorder,
    shadowColor: Color = StickerBorder,
    shape: Shape = CartoonCardShape,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.padding(bottom = shadowOffset),
        propagateMinConstraints = true
    ) {
        // Solid offset shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 0.dp, y = shadowOffset)
                .background(shadowColor, shape)
        )
        // Top Card Surface
        Box(
            modifier = Modifier
                .background(backgroundColor, shape)
                .border(borderWidth, borderColor, shape)
                .clip(shape)
        ) {
            content()
        }
    }
}

@Composable
fun CartoonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "",
    backgroundColor: Color = ElectricBlue,
    contentColor: Color = SurfaceWhite,
    borderColor: Color = StickerBorder,
    shadowColor: Color = StickerBorder,
    shape: Shape = PillShape,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentOffset by animateFloatAsState(
        targetValue = if (isPressed) shadowOffset.value else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "buttonPress"
    )

    Box(
        modifier = modifier
            .padding(bottom = shadowOffset)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        propagateMinConstraints = true
    ) {
        // Solid bottom shadow
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 0.dp, y = shadowOffset)
                    .background(shadowColor, shape)
            )
        }

        // Top button element
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .offset(y = currentOffset.dp)
                .background(if (enabled) backgroundColor else Color.LightGray, shape)
                .border(borderWidth, if (enabled) borderColor else Color.Gray, shape)
                .clip(shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (content != null) {
                content()
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = text,
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CartoonHoldButton(
    onHoldComplete: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = CoralRed,
    contentColor: Color = SurfaceWhite,
    borderColor: Color = StickerBorder,
    shadowColor: Color = StickerBorder,
    shape: Shape = PillShape,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    holdDurationMs: Long = 1200L
) {
    val view = LocalView.current
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            while (isHolding) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed.toFloat() / holdDurationMs).coerceIn(0f, 1f)
                if (elapsed >= holdDurationMs) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    isHolding = false
                    holdProgress = 0f
                    onHoldComplete()
                    break
                }
                delay(16L)
            }
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier = modifier
            .padding(bottom = shadowOffset)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        tryAwaitRelease()
                        isHolding = false
                    }
                )
            },
        propagateMinConstraints = true
    ) {
        // Bottom Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 0.dp, y = shadowOffset)
                .background(shadowColor, shape)
        )

        // Top Button Surface
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .offset(y = if (isHolding) shadowOffset else 0.dp)
                .background(backgroundColor, shape)
                .border(borderWidth, borderColor, shape)
                .clip(shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Visual hold progress indicator
            if (isHolding) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.35f))
                )
            }

            Text(
                text = if (isHolding) "Tahan... (${(holdProgress * 100).toInt()}%)" else text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CartoonChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceWhite,
    textColor: Color = TextPrimary,
    borderColor: Color = StickerBorder,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .background(backgroundColor, CartoonBadgeShape)
            .border(1.5.dp, borderColor, CartoonBadgeShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
