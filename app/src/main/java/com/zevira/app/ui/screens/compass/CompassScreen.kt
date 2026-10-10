package com.zevira.app.ui.screens.compass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.data.sensor.CompassSensorManager
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun CompassScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { CompassSensorManager(context) }
    val compassData by sensorManager.compassData.collectAsState()

    var useTrueNorth by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    val displayAzimuth = if (useTrueNorth) {
        (compassData.azimuthDegrees + compassData.trueNorthOffset + 360f) % 360f
    } else {
        compassData.azimuthDegrees
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceTint)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(SurfaceWhite, CircleShape)
                    .border(2.dp, StickerBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Kompas Realtime",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        // Cartoon Calibration Warning if sensor is unreliable
        AnimatedVisibility(visible = compassData.isAccuracyLow) {
            CartoonCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFFFF9E6),
                borderColor = StarYellow
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MascotJejak(size = 45.dp, state = MascotState.IDLE)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Akurasi kompas rendah! Putar perangkat membentuk angka 8 untuk kalibrasi.",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Main Compass Dial
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(SurfaceWhite, CircleShape)
                .border(4.dp, StickerBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Dial with Compass Ticks and Cardinal markers
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .rotate(-displayAzimuth)
            ) {
                val radius = size.width / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Outer border ring
                drawCircle(
                    color = StickerBorder,
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 8 main ticks
                for (i in 0 until 8) {
                    val angle = Math.toRadians(i * 45.0)
                    val tickLen = if (i % 2 == 0) 18.dp.toPx() else 10.dp.toPx()
                    val startX = center.x + (radius - tickLen) * kotlin.math.sin(angle).toFloat()
                    val startY = center.y - (radius - tickLen) * kotlin.math.cos(angle).toFloat()
                    val endX = center.x + radius * kotlin.math.sin(angle).toFloat()
                    val endY = center.y - radius * kotlin.math.cos(angle).toFloat()

                    drawLine(
                        color = if (i == 0) CoralRed else StickerBorder,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (i % 2 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Fixed Needle (North pointing up in Red, South in Blue)
            Canvas(modifier = Modifier.size(160.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)

                // Top Red Needle (North)
                val northPath = Path().apply {
                    moveTo(center.x, center.y - 70.dp.toPx())
                    lineTo(center.x + 12.dp.toPx(), center.y)
                    lineTo(center.x - 12.dp.toPx(), center.y)
                    close()
                }
                drawPath(path = northPath, color = CoralRed)

                // Bottom Blue Needle (South)
                val southPath = Path().apply {
                    moveTo(center.x, center.y + 70.dp.toPx())
                    lineTo(center.x + 12.dp.toPx(), center.y)
                    lineTo(center.x - 12.dp.toPx(), center.y)
                    close()
                }
                drawPath(path = southPath, color = ElectricBlue)

                // Needle Center Cap
                drawCircle(
                    color = SurfaceWhite,
                    radius = 14.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = StickerBorder,
                    radius = 14.dp.toPx(),
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // Degree and Cardinal Text Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${displayAzimuth.roundToInt()}° ${compassData.directionText}",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // True North Toggle
            CartoonCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Utara Sejati (True North)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Koreksi deklinasi magnetik",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = useTrueNorth,
                        onCheckedChange = { useTrueNorth = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = ElectricBlue
                        )
                    )
                }
            }
        }
    }
}
