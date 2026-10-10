package com.zevira.app.ui.screens.map

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.local.entity.TrackingMode
import com.zevira.app.data.location.TrackingService
import com.zevira.app.data.sensor.CompassSensorManager
import com.zevira.app.ui.components.CartoonButton
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.components.CartoonChip
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.MintGreen
import com.zevira.app.ui.theme.PillShape
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun TrackingLiveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReturnToStart: () -> Unit,
    onNavigateToSummary: (String) -> Unit
) {
    val context = LocalContext.current
    val trackingState by TrackingService.trackingState.collectAsState()

    var mapStyle by remember { mutableStateOf("real_life") }
    var recenterTrigger by remember { mutableLongStateOf(0L) }
    var showRecenterButton by remember { mutableStateOf(false) }

    // Start Tracking Service if not running
    LaunchedEffect(Unit) {
        if (!trackingState.isTracking) {
            val startIntent = Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_START
                putExtra(TrackingService.EXTRA_MODE, TrackingMode.WALK.name)
            }
            context.startForegroundService(startIntent)
        }
    }

    // Navigate to Summary when trip stops
    var wasTracking by remember { mutableStateOf(false) }
    LaunchedEffect(trackingState.isTracking) {
        if (trackingState.isTracking) {
            wasTracking = true
        } else if (wasTracking && trackingState.tripId.isNotEmpty()) {
            onNavigateToSummary(trackingState.tripId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Fullscreen Leaflet Map
        LeafletMapView(
            modifier = Modifier.fillMaxSize(),
            currentLat = trackingState.currentLat,
            currentLng = trackingState.currentLng,
            currentAccuracy = trackingState.currentAccuracy,
            currentBearing = trackingState.currentBearing,
            startLat = trackingState.startLat,
            startLng = trackingState.startLng,
            mapStyle = mapStyle,
            recenterTrigger = recenterTrigger,
            onUserPanned = { showRecenterButton = true }
        )

        // 2. Top Bar Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode & GPS Status Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CartoonChip(
                    text = if (trackingState.mode == TrackingMode.WALK) "Jalan" else "Olahraga",
                    backgroundColor = SurfaceWhite,
                    leadingIcon = {
                        Icon(
                            imageVector = if (trackingState.mode == TrackingMode.WALK) Icons.Rounded.DirectionsWalk else Icons.Rounded.DirectionsRun,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                if (trackingState.isSearchingGps) {
                    CartoonChip(
                        text = stringResource(R.string.searching_gps),
                        backgroundColor = Color(0xFFFFF9E6),
                        textColor = TextPrimary,
                        borderColor = StarYellow
                    )
                } else if (trackingState.isGpsWeak) {
                    CartoonChip(
                        text = stringResource(R.string.gps_low_accuracy),
                        backgroundColor = Color(0xFFFFF0F0),
                        textColor = CoralRed,
                        borderColor = CoralRed
                    )
                } else {
                    CartoonChip(
                        text = "GPS Presisi",
                        backgroundColor = Color(0xFFE8FDF3),
                        textColor = Color(0xFF0F5A38),
                        borderColor = MintGreen
                    )
                }
            }

            // Map Style Switcher (Satelit vs Putih Polos)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SurfaceWhite, CircleShape)
                    .border(2.dp, StickerBorder, CircleShape)
                    .clickable {
                        mapStyle = if (mapStyle == "real_life") "clean_white" else "real_life"
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Layers,
                    contentDescription = "Ganti Gaya Peta",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 3. Floating Recenter & Pulang ke Start Buttons (Middle Right)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Pulang ke Start Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(StarYellow, CircleShape)
                    .border(2.dp, StickerBorder, CircleShape)
                    .clickable { onNavigateToReturnToStart() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Navigation,
                    contentDescription = stringResource(R.string.btn_return_to_start),
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Recenter Button
            if (showRecenterButton) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(SurfaceWhite, CircleShape)
                        .border(2.dp, StickerBorder, CircleShape)
                        .clickable {
                            recenterTrigger = System.currentTimeMillis()
                            showRecenterButton = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = stringResource(R.string.btn_recenter),
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 4. Bottom Control Panel
        CartoonCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = SurfaceWhite
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Metrics Row (Distance, Time, Speed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Distance
                    Column {
                        Text(
                            text = stringResource(R.string.stat_distance),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        val distKm = trackingState.distanceMeters / 1000.0
                        Text(
                            text = String.format("%.2f km", distKm),
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 24.sp
                        )
                    }

                    // Duration
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.stat_duration),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        val mins = trackingState.durationSeconds / 60
                        val secs = trackingState.durationSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 24.sp
                        )
                    }

                    // Speed
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Kecepatan",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format("%.1f km/j", trackingState.currentSpeedKmh),
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 24.sp
                        )
                    }
                }

                // Extra Calories Row for Sport Mode
                if (trackingState.mode == TrackingMode.SPORT) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("Kalori: %.1f kkal", trackingState.calories),
                            fontWeight = FontWeight.Bold,
                            color = StarYellow,
                            fontSize = 13.sp
                        )
                        Text(
                            text = stringResource(R.string.calorie_disclaimer),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions: Pause/Resume + Long Press Stop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pause / Resume
                    CartoonButton(
                        onClick = {
                            val action = if (trackingState.isPaused) TrackingService.ACTION_RESUME else TrackingService.ACTION_PAUSE
                            val intent = Intent(context, TrackingService::class.java).apply { this.action = action }
                            context.startService(intent)
                        },
                        text = if (trackingState.isPaused) stringResource(R.string.btn_resume) else stringResource(R.string.btn_pause),
                        backgroundColor = if (trackingState.isPaused) MintGreen else SurfaceWhite,
                        contentColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    // Long-Press Stop Button (Section 5: "Tombol Stop harus ditahan (long-press) agar tidak tertekan tidak sengaja")
                    var isPressingStop by remember { mutableStateOf(false) }

                    LaunchedEffect(isPressingStop) {
                        if (isPressingStop) {
                            delay(1200L) // Long-press threshold
                            if (isPressingStop) {
                                val stopIntent = Intent(context, TrackingService::class.java).apply {
                                    action = TrackingService.ACTION_STOP
                                }
                                context.startService(stopIntent)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressingStop = true
                                        tryAwaitRelease()
                                        isPressingStop = false
                                    }
                                )
                            }
                    ) {
                        CartoonButton(
                            onClick = { /* handled by long-press */ },
                            text = if (isPressingStop) "Tahan..." else stringResource(R.string.btn_stop_hold),
                            backgroundColor = CoralRed,
                            contentColor = SurfaceWhite,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
