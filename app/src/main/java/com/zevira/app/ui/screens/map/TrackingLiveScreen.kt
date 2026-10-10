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
import com.zevira.app.ui.components.CartoonHoldButton
import com.zevira.app.ui.theme.CoralRed
import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableFloatStateOf
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
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

    var mapStyle by remember { mutableStateOf("streets") }
    var recenterTrigger by remember { mutableLongStateOf(0L) }
    var showRecenterButton by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(TrackingMode.WALK) }

    // Check location permission state
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    var hasLocationPermission by remember { mutableStateOf(fineGranted || coarseGranted) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            hasLocationPermission = true
        }
    }

    var initialLat by remember { mutableStateOf<Double?>(null) }
    var initialLng by remember { mutableStateOf<Double?>(null) }
    var previewLat by remember { mutableStateOf<Double?>(null) }
    var previewLng by remember { mutableStateOf<Double?>(null) }
    var previewAccuracy by remember { mutableFloatStateOf(10f) }
    var previewBearing by remember { mutableFloatStateOf(0f) }

    // Live continuous location updates while IDLE / previewing map
    DisposableEffect(hasLocationPermission, trackingState.isTracking) {
        if (hasLocationPermission && !trackingState.isTracking) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(0f)
                .build()
            val idleCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val last = result.lastLocation ?: return
                    previewLat = last.latitude
                    previewLng = last.longitude
                    previewAccuracy = last.accuracy
                    previewBearing = if (last.hasBearing()) last.bearing else 0f
                }
            }
            try {
                fusedClient.requestLocationUpdates(request, idleCallback, Looper.getMainLooper())
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null && previewLat == null) {
                        previewLat = loc.latitude
                        previewLng = loc.longitude
                        previewAccuracy = loc.accuracy
                        previewBearing = if (loc.hasBearing()) loc.bearing else 0f
                    }
                }
            } catch (e: SecurityException) {
                // Handled gracefully
            }
            onDispose {
                fusedClient.removeLocationUpdates(idleCallback)
            }
        } else {
            onDispose { }
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

    val liveLat = if (trackingState.isTracking) trackingState.currentLat else (previewLat ?: initialLat)
    val liveLng = if (trackingState.isTracking) trackingState.currentLng else (previewLng ?: initialLng)
    val liveAccuracy = if (trackingState.isTracking) trackingState.currentAccuracy else previewAccuracy
    val liveBearing = if (trackingState.isTracking) trackingState.currentBearing else previewBearing

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Fullscreen Leaflet Map
        LeafletMapView(
            modifier = Modifier.fillMaxSize(),
            currentLat = liveLat,
            currentLng = liveLng,
            currentAccuracy = liveAccuracy,
            currentBearing = liveBearing,
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
                .statusBarsPadding()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode & GPS Status Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val currentMode = if (trackingState.isTracking) trackingState.mode else selectedMode
                CartoonChip(
                    text = if (currentMode == TrackingMode.WALK) "Jalan" else "Olahraga",
                    backgroundColor = SurfaceWhite,
                    leadingIcon = {
                        Icon(
                            imageVector = if (currentMode == TrackingMode.WALK) Icons.Rounded.DirectionsWalk else Icons.Rounded.DirectionsRun,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                if (trackingState.isTracking) {
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
                } else {
                    CartoonChip(
                        text = if (liveLat != null) "GPS Siap" else "Mencari GPS…",
                        backgroundColor = if (liveLat != null) Color(0xFFE8FDF3) else Color(0xFFFFF9E6),
                        textColor = if (liveLat != null) Color(0xFF0F5A38) else TextPrimary,
                        borderColor = if (liveLat != null) MintGreen else StarYellow
                    )
                }
            }

            // Map Style Switcher Toggle Button (Peta Jalan vs Satelit Hybrid)
            Row(
                modifier = Modifier
                    .background(SurfaceWhite, PillShape)
                    .border(2.dp, StickerBorder, PillShape)
                    .clickable {
                        mapStyle = if (mapStyle == "streets") "satellite_hybrid" else "streets"
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Layers,
                    contentDescription = "Ganti Layer Peta",
                    tint = ElectricBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (mapStyle == "streets") stringResource(R.string.map_style_streets) else stringResource(R.string.map_style_satellite),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
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
            // Pulang ke Start Button (only when tracking is active and start point is known)
            if (trackingState.isTracking && trackingState.startLat != null) {
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
        if (!trackingState.isTracking) {
            // IDLE STATE: User can see map, choose mode, and tap "Mulai"
            CartoonCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                backgroundColor = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mulai Petualangan",
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Titik lokasimu sudah terdeteksi di peta.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        MascotJejak(size = 46.dp, state = MascotState.IDLE)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Toggle (Jalan-jalan vs Olahraga)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CartoonButton(
                            onClick = { selectedMode = TrackingMode.WALK },
                            text = "Jalan-jalan",
                            backgroundColor = if (selectedMode == TrackingMode.WALK) ElectricBlue else SurfaceWhite,
                            contentColor = if (selectedMode == TrackingMode.WALK) SurfaceWhite else TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        CartoonButton(
                            onClick = { selectedMode = TrackingMode.SPORT },
                            text = "Olahraga",
                            backgroundColor = if (selectedMode == TrackingMode.SPORT) StarYellow else SurfaceWhite,
                            contentColor = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Start Button
                    CartoonButton(
                        onClick = {
                            if (!hasLocationPermission) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                val startIntent = Intent(context, TrackingService::class.java).apply {
                                    action = TrackingService.ACTION_START
                                    putExtra(TrackingService.EXTRA_MODE, selectedMode.name)
                                }
                                try {
                                    context.startForegroundService(startIntent)
                                } catch (e: Exception) {
                                    timber.log.Timber.e(e, "Failed to startForegroundService")
                                }
                            }
                        },
                        text = "Mulai Merekam",
                        backgroundColor = MintGreen,
                        contentColor = TextPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = TextPrimary
                            )
                        }
                    )
                }
            }
        } else {
            // ACTIVE TRACKING STATE: Live Metrics, Pause/Resume, and Single-Click Stop
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
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Distance
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.stat_distance),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            val distKm = trackingState.distanceMeters / 1000.0
                            Text(
                                text = String.format("%.2f km", distKm),
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Duration
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.stat_duration),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            val mins = trackingState.durationSeconds / 60
                            val secs = trackingState.durationSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Speed
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Kecepatan",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            Text(
                                text = String.format("%.1f km/j", trackingState.currentSpeedKmh),
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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

                    // Actions: Pause/Resume + Single Click Stop Button
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

                        // Single Click Stop Button (No hold required!)
                        CartoonButton(
                            onClick = {
                                val stopIntent = Intent(context, TrackingService::class.java).apply {
                                    action = TrackingService.ACTION_STOP
                                }
                                context.startService(stopIntent)
                            },
                            text = stringResource(R.string.btn_stop),
                            backgroundColor = CoralRed,
                            contentColor = SurfaceWhite,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Stop,
                                    contentDescription = null,
                                    tint = SurfaceWhite
                                )
                            }
                        )
                    }
                }
            }
        }

        // 5. Friendly Permission Prompt Overlay (If location permission is not yet granted)
        if (!hasLocationPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CartoonCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceWhite
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MascotJejak(size = 90.dp, state = MascotState.IDLE)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Izin Lokasi Diperlukan 📍",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Zevira membutuhkan izin lokasi untuk menampilkan posisimu di peta dan merekam jejak perjalanan secara realtime.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        CartoonButton(
                            onClick = {
                                val perms = mutableListOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                permissionLauncher.launch(perms.toTypedArray())
                            },
                            text = "Aktifkan Izin Lokasi",
                            backgroundColor = ElectricBlue,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
