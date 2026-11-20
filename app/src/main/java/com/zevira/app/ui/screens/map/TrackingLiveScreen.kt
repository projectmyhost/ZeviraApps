package com.zevira.app.ui.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.zevira.app.R
import com.zevira.app.data.local.entity.TrackingMode
import com.zevira.app.data.location.TrackingService
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import timber.log.Timber
import java.util.Locale

private val DarkPillBg = Color(0xCC16171E)
private val DarkPillBorder = Color(0x33FFFFFF)
private val OrangeBright = Color(0xFFFA8E1C)
private val OrangeMid = Color(0xFFF37812)
private val OrangeDeep = Color(0xFFE7620F)
private val CardSurface = Color(0xFF14151B)
private val TextSilver = Color(0xFF9EA1AE)

@SuppressLint("MissingPermission")
@Composable
fun TrackingLiveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReturnToStart: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateToCompass: () -> Unit = {}
) {
    val context = LocalContext.current
    val trackingState by TrackingService.trackingState.collectAsState()

    var mapStyle by remember { mutableStateOf("streets") }
    var recenterTrigger by remember { mutableLongStateOf(0L) }
    var selectedMode by remember { mutableStateOf(TrackingMode.WALK) }
    var showGpsDisabledDialog by remember { mutableStateOf(false) }

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

    var previewLat by remember { mutableStateOf<Double?>(null) }
    var previewLng by remember { mutableStateOf<Double?>(null) }
    var previewAccuracy by remember { mutableFloatStateOf(10f) }
    var previewBearing by remember { mutableFloatStateOf(0f) }

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
                Timber.w(e, "Location access denied in background")
            }
            onDispose {
                fusedClient.removeLocationUpdates(idleCallback)
            }
        } else {
            onDispose { }
        }
    }

    var wasTracking by remember { mutableStateOf(false) }
    LaunchedEffect(trackingState.isTracking) {
        if (trackingState.isTracking) {
            wasTracking = true
        } else if (wasTracking && trackingState.tripId.isNotEmpty()) {
            onNavigateToSummary(trackingState.tripId)
        }
    }

    val liveLat = if (trackingState.isTracking) trackingState.currentLat else previewLat
    val liveLng = if (trackingState.isTracking) trackingState.currentLng else previewLng
    val liveAccuracy = if (trackingState.isTracking) trackingState.currentAccuracy else previewAccuracy
    val liveBearing = if (trackingState.isTracking) trackingState.currentBearing else previewBearing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0F))
    ) {
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
            isTracking = trackingState.isTracking,
            recenterTrigger = recenterTrigger
        )

        // 2. Top Bar: 3 Floating Dark Pills (Jalan | Mencari GPS... ) | Peta Jalan ˅)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill 1: Mode Indicator
            val currentMode = if (trackingState.isTracking) trackingState.mode else selectedMode
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkPillBg)
                    .border(1.dp, DarkPillBorder, RoundedCornerShape(18.dp))
                    .clickable {
                        if (!trackingState.isTracking) {
                            selectedMode = if (selectedMode == TrackingMode.WALK) TrackingMode.SPORT else TrackingMode.WALK
                        }
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentMode == TrackingMode.WALK) {
                            Icons.AutoMirrored.Rounded.DirectionsWalk
                        } else {
                            Icons.AutoMirrored.Rounded.DirectionsRun
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentMode == TrackingMode.WALK) "Jalan" else "Olahraga",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            // Pill 2: GPS Status Indicator with Glowing Arc Accent
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkPillBg)
                    .border(1.dp, DarkPillBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GpsStatusRadarIcon(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(7.dp))
                    val gpsLabel = when {
                        trackingState.isTracking && trackingState.isSearchingGps -> "Mencari GPS…"
                        trackingState.isTracking && trackingState.isGpsWeak -> "GPS Lemah"
                        trackingState.isTracking -> "GPS Presisi"
                        liveLat != null -> "GPS Siap"
                        else -> "Mencari GPS…"
                    }
                    Text(
                        text = gpsLabel,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Orange curved arc accent matching reference: ")"
                    OrangeStatusArc(modifier = Modifier.size(width = 8.dp, height = 16.dp))
                }
            }

            // Pill 3: Map Style Dropdown Pill
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkPillBg)
                    .border(1.dp, DarkPillBorder, RoundedCornerShape(18.dp))
                    .clickable {
                        mapStyle = if (mapStyle == "streets") "satellite_hybrid" else "streets"
                    }
                    .padding(horizontal = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (mapStyle == "streets") "Peta Jalan" else "Satelit",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFFA0A3B0),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 3. Floating Recenter Arrow Button (Right side, above card)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 278.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xCC14151B))
                .border(1.dp, DarkPillBorder, CircleShape)
                .clickable {
                    recenterTrigger = System.currentTimeMillis()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Navigation,
                contentDescription = "Pusatkan",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(45f)
            )
        }

        // 4. Bottom Floating Card ("Mulai Petualangan" or Active Tracking HUD)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(bottom = 12.dp)
        ) {
            if (!trackingState.isTracking) {
                // IDLE STATE (Image 1 1:1)
                val cardShape = RoundedCornerShape(26.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(CardSurface)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color(0x28FFFFFF), Color(0x0EFFFFFF))
                            ),
                            shape = cardShape
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    // Top drag handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x40FFFFFF))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title & Compass Action Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mulai Petualangan",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.2).sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Titik lokasimu sudah terdeteksi di peta.",
                                color = TextSilver,
                                fontSize = 12.sp
                            )
                        }

                        // Orange glowing circular compass needle button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF14151B))
                                .border(1.2.dp, Color(0x60F37812), CircleShape)
                                .clickable { onNavigateToCompass() },
                            contentAlignment = Alignment.Center
                        ) {
                            CompassNeedleIcon(modifier = Modifier.size(26.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Selector Row (Jalan-jalan vs Olahraga)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isWalk = selectedMode == TrackingMode.WALK
                        val isSport = selectedMode == TrackingMode.SPORT

                        // Jalan-jalan button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .then(
                                    if (isWalk) {
                                        Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(OrangeBright, OrangeDeep)
                                                )
                                            )
                                            .border(
                                                0.8.dp,
                                                Brush.verticalGradient(
                                                    listOf(Color(0x60FFE0A3), Color.Transparent)
                                                ),
                                                RoundedCornerShape(23.dp)
                                            )
                                    } else {
                                        Modifier
                                            .background(Color(0xFF1B1C23))
                                            .border(1.dp, Color(0x1CFFFFFF), RoundedCornerShape(23.dp))
                                    }
                                )
                                .clickable { selectedMode = TrackingMode.WALK },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isWalk) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.DirectionsWalk,
                                    contentDescription = null,
                                    tint = if (isWalk) Color.White else Color(0xFF8C8F9C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Jalan-jalan",
                                    color = if (isWalk) Color.White else Color(0xFF8C8F9C),
                                    fontWeight = if (isWalk) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        // Olahraga button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .then(
                                    if (isSport) {
                                        Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(OrangeBright, OrangeDeep)
                                                )
                                            )
                                            .border(
                                                0.8.dp,
                                                Brush.verticalGradient(
                                                    listOf(Color(0x60FFE0A3), Color.Transparent)
                                                ),
                                                RoundedCornerShape(23.dp)
                                            )
                                    } else {
                                        Modifier
                                            .background(Color(0xFF1B1C23))
                                            .border(1.dp, Color(0x1CFFFFFF), RoundedCornerShape(23.dp))
                                    }
                                )
                                .clickable { selectedMode = TrackingMode.SPORT },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSport) Icons.Rounded.Check else Icons.Rounded.Timeline,
                                    contentDescription = null,
                                    tint = if (isSport) Color.White else Color(0xFF8C8F9C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Olahraga",
                                    color = if (isSport) Color.White else Color(0xFF8C8F9C),
                                    fontWeight = if (isSport) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Button: "Mulai Merekam" (Dark bg with neon orange border & orange play icon)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF121318))
                            .border(1.4.dp, OrangeMid, RoundedCornerShape(24.dp))
                            .clickable {
                                if (!hasLocationPermission) {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                    return@clickable
                                }

                                val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                                val isGpsActive = locManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                                    locManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

                                if (!isGpsActive) {
                                    showGpsDisabledDialog = true
                                    return@clickable
                                }

                                val startIntent = Intent(context, TrackingService::class.java).apply {
                                    action = TrackingService.ACTION_START
                                    putExtra(TrackingService.EXTRA_MODE, selectedMode.name)
                                }
                                try {
                                    context.startForegroundService(startIntent)
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to startForegroundService")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = OrangeMid,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mulai Merekam",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else {
                // ACTIVE TRACKING HUD
                val cardShape = RoundedCornerShape(26.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(CardSurface)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color(0x28FFFFFF), Color(0x0EFFFFFF))
                            ),
                            shape = cardShape
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    // Status row with blinking recording indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (trackingState.isPaused) Color(0xFFFFB020) else Color(0xFF00E676))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (trackingState.isPaused) "Pelacakan Dijeda" else "Pelacakan Berjalan",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Pulang ke Start Shortcut
                        if (trackingState.startLat != null) {
                            Text(
                                text = "Pulang ke Start",
                                color = OrangeBright,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { onNavigateToReturnToStart() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Grid (Jarak, Durasi, Kecepatan)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Jarak", color = TextSilver, fontSize = 11.5.sp)
                            val distKm = trackingState.distanceMeters / 1000.0
                            Text(
                                text = String.format(Locale.US, "%.2f km", distKm),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        }
                        Column {
                            Text(text = "Durasi", color = TextSilver, fontSize = 11.5.sp)
                            val mins = trackingState.durationSeconds / 60
                            val secs = trackingState.durationSeconds % 60
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", mins, secs),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        }
                        Column {
                            Text(text = "Kecepatan", color = TextSilver, fontSize = 11.5.sp)
                            Text(
                                text = String.format(Locale.US, "%.1f km/j", trackingState.currentSpeedKmh),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Controls: Pause/Resume + Stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pause / Resume
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(Color(0xFF20222A))
                                .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(23.dp))
                                .clickable {
                                    val action = if (trackingState.isPaused) TrackingService.ACTION_RESUME else TrackingService.ACTION_PAUSE
                                    val intent = Intent(context, TrackingService::class.java).apply { this.action = action }
                                    context.startService(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (trackingState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = null,
                                    tint = OrangeBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (trackingState.isPaused) "Lanjut" else "Jeda",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        // Stop Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(OrangeBright, OrangeDeep)
                                    )
                                )
                                .clickable {
                                    val stopIntent = Intent(context, TrackingService::class.java).apply {
                                        action = TrackingService.ACTION_STOP
                                    }
                                    context.startService(stopIntent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Stop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Berhenti",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Dark-Themed GPS Disabled Dialog
        if (showGpsDisabledDialog) {
            AlertDialog(
                onDismissRequest = { showGpsDisabledDialog = false },
                containerColor = Color(0xFF16171E),
                title = {
                    Text(
                        text = "GPS Belum Aktif 🛰️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Aktifkan GPS lokasi pada perangkat Anda agar pergerakan dan rute perjalanan dapat direkam dengan presisi.",
                        fontSize = 13.5.sp,
                        color = TextSilver,
                        lineHeight = 19.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showGpsDisabledDialog = false
                            try {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } catch (e: Exception) {
                                Timber.e(e, "Cannot open location settings")
                            }
                        }
                    ) {
                        Text("Buka Pengaturan GPS", color = OrangeBright, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGpsDisabledDialog = false }) {
                        Text("Batal", color = TextSilver)
                    }
                }
            )
        }
    }
}

@Composable
private fun GpsStatusRadarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.44f
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = r,
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFF37812),
            radius = r * 0.38f,
            center = center
        )
    }
}

@Composable
private fun OrangeStatusArc(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val h = size.height
        val path = Path().apply {
            moveTo(0f, 2f)
            cubicTo(size.width * 1.2f, h * 0.25f, size.width * 1.2f, h * 0.75f, 0f, h - 2f)
        }
        drawPath(
            path = path,
            color = Color(0xFFF37812),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun CompassNeedleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension * 0.42f
        val needleLen = outerRadius * 0.75f
        val needleW = outerRadius * 0.28f

        rotate(degrees = 45f, pivot = center) {
            val northPath = Path().apply {
                moveTo(center.x, center.y - needleLen)
                lineTo(center.x + needleW, center.y)
                lineTo(center.x - needleW, center.y)
                close()
            }
            drawPath(path = northPath, color = Color(0xFFF37812))

            val southPath = Path().apply {
                moveTo(center.x, center.y + needleLen)
                lineTo(center.x + needleW, center.y)
                lineTo(center.x - needleW, center.y)
                close()
            }
            drawPath(path = southPath, color = Color(0xFF9E4B07))
        }

        drawCircle(
            color = Color.White,
            radius = 1.6.dp.toPx(),
            center = center
        )
    }
}
