package com.zevira.app.ui.screens.returntostart

import android.location.Location
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.location.RouteSimplifier
import com.zevira.app.data.location.TrackingService
import com.zevira.app.data.sensor.CompassSensorManager
import com.zevira.app.ui.components.CartoonButton
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.NightNavy
import com.zevira.app.ui.theme.PillShape
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ReturnToStartScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val compassSensorManager = remember { CompassSensorManager(context) }
    val compassData by compassSensorManager.compassData.collectAsState()
    val trackingState by TrackingService.trackingState.collectAsState()

    DisposableEffect(Unit) {
        compassSensorManager.startListening()
        onDispose {
            compassSensorManager.stopListening()
        }
    }

    val currentLat = trackingState.currentLat
    val currentLng = trackingState.currentLng
    val startLat = trackingState.startLat
    val startLng = trackingState.startLng

    // Calculate straight line distance and bearing to start
    val straightDistanceMeters: Double
    val bearingToStart: Float
    if (currentLat != null && currentLng != null && startLat != null && startLng != null) {
        val results = FloatArray(2)
        Location.distanceBetween(currentLat, currentLng, startLat, startLng, results)
        straightDistanceMeters = results[0].toDouble()
        bearingToStart = (results[1] + 360f) % 360f
    } else {
        straightDistanceMeters = 0.0
        bearingToStart = 0f
    }

    // Relative angle: Bearing to start minus current device heading
    val relativeArrowAngle = (bearingToStart - compassData.azimuthDegrees + 360f) % 360f
    val animatedArrowAngle by animateFloatAsState(targetValue = relativeArrowAngle, label = "arrowAngle")

    // Estimated walk time (assumes average walking speed of 4.5 km/h = 75 m/min)
    val etaMinutes = (straightDistanceMeters / 75.0).roundToInt().coerceAtLeast(1)

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
                text = stringResource(R.string.return_start_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        // Big Compass Arrow Dial pointing to Start
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(SurfaceWhite, CircleShape)
                .border(4.dp, StickerBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Background compass ring ticks
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                drawCircle(
                    color = SurfaceTint,
                    radius = this.size.width / 2f
                )
            }

            // Big Giant Direction Arrow
            Icon(
                imageVector = Icons.Rounded.Navigation,
                contentDescription = "Arah Titik Start",
                tint = StarYellow,
                modifier = Modifier
                    .size(140.dp)
                    .rotate(animatedArrowAngle)
            )

            // Inner center dot
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(ElectricBlue, CircleShape)
                    .border(2.dp, StickerBorder, CircleShape)
            )
        }

        // Distance & ETA Cards
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CartoonCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceWhite
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.return_distance_label),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f km", straightDistanceMeters / 1000.0),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }

                CartoonCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceWhite
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.return_eta_label),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "~$etaMinutes menit",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            CartoonButton(
                onClick = onNavigateBack,
                text = stringResource(R.string.btn_highlight_route),
                backgroundColor = ElectricBlue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
