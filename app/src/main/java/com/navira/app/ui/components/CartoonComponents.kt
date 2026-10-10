package com.navira.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navira.app.ui.theme.CartoonBadgeShape
import com.navira.app.ui.theme.CartoonCardShape
import com.navira.app.ui.theme.ElectricBlue
import com.navira.app.ui.theme.PillShape
import com.navira.app.ui.theme.StickerBorder
import com.navira.app.ui.theme.SurfaceWhite
import com.navira.app.ui.theme.TextPrimary

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
    Box(modifier = modifier) {
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
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
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
                .offset(y = currentOffset.dp)
                .background(if (enabled) backgroundColor else Color.LightGray, shape)
                .border(borderWidth, if (enabled) borderColor else Color.Gray, shape)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (content != null) {
                content()
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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
    backgroundColor: Color = ElectricBlue,
    contentColor: Color = SurfaceWhite,
    borderColor: Color = StickerBorder,
    shadowColor: Color = StickerBorder,
    shape: Shape = PillShape,
    holdDurationMs: Long = 1500L
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        val completed = tryAwaitRelease()
                        // Handle tap vs hold
                    }
                )
            }
    ) {
        // Normal state or pressing state
        CartoonButton(
            onClick = onHoldComplete,
            text = text,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            borderColor = borderColor,
            shadowColor = shadowColor,
            shape = shape
        )
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
            fontSize = 13.sp
        )
    }
}
