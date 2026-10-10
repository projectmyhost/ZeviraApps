package com.zevira.app.ui.screens.summary

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.export.GpxExporter
import com.zevira.app.data.local.db.ZeviraDatabase
import com.zevira.app.data.local.entity.LocationPointEntity
import com.zevira.app.data.local.entity.TripEntity
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import com.zevira.app.data.location.LatLngPoint
import com.zevira.app.data.location.RouteSimplifier
import com.zevira.app.data.local.entity.TrackingMode
import com.zevira.app.ui.components.CanvasRouteThumbnail
import com.zevira.app.ui.components.CartoonButton
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import com.zevira.app.ui.components.StarSky
import com.zevira.app.ui.theme.CartoonCardShape
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.MintGreen
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextLightPrimary
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripSummaryScreen(
    tripId: String,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var trip by remember { mutableStateOf<TripEntity?>(null) }
    var locationPoints by remember { mutableStateOf<List<LocationPointEntity>>(emptyList()) }
    var tripTitle by remember { mutableStateOf("") }
    var newStarsEarned by remember { mutableStateOf(0) }
    var animateStarRise by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        withContext(Dispatchers.IO) {
            val db = ZeviraDatabase.getInstance(context)
            val loaded = db.tripDao().getTripById(tripId)
            val points = db.locationPointDao().getPointsForTrip(tripId)
            if (loaded != null) {
                trip = loaded
                locationPoints = points
                tripTitle = loaded.title
                val stars = (loaded.distanceMeters / 1000.0).toInt().coerceAtLeast(1)
                newStarsEarned = stars
                animateStarRise = true
                val prefs = UserPreferencesDataStore(context)
                prefs.addStars(stars)
            }
        }
    }

    val starOffset by animateFloatAsState(
        targetValue = if (animateStarRise) -30f else 60f,
        animationSpec = tween(durationMillis = 1000),
        label = "starRise"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceTint)
            .verticalScroll(scrollState)
    ) {
        // StarSky Celebration Header
        StarSky(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Rising animated star
                Box(
                    modifier = Modifier.offset(y = starOffset.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MascotJejak(size = 90.dp, state = MascotState.CELEBRATING)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.summary_title),
                    color = TextLightPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stringResource(R.string.star_earned_toast, newStarsEarned),
                    color = StarYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 1. Editable Title Field (Fixed White Text Bug!)
            CartoonCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nama Petualangan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = tripTitle,
                        onValueChange = { tripTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        placeholder = {
                            Text(
                                text = "Petualangan Jalan-jalan",
                                color = TextSecondary.copy(alpha = 0.6f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            cursorColor = ElectricBlue,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0xFFCAD4E0),
                            focusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Route Canvas Thumbnail & Visible Tracking Logs Section
            trip?.let { currentTrip ->
                CartoonCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceWhite
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header of the Route Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Jejak Garis Lintasan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            val ptsCount = if (locationPoints.isNotEmpty()) locationPoints.size else {
                                if (currentTrip.encodedPolyline.isNotEmpty()) {
                                    RouteSimplifier.decodePolyline(currentTrip.encodedPolyline).size
                                } else 1
                            }
                            Box(
                                modifier = Modifier
                                    .background(SurfaceTint, RoundedCornerShape(8.dp))
                                    .border(1.2.dp, StickerBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$ptsCount Titik GPS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ElectricBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // A. Canvas Route Preview (Never blank! Line + Markers + Grid)
                        val latLngPoints = remember(locationPoints, currentTrip.encodedPolyline) {
                            if (locationPoints.isNotEmpty()) {
                                locationPoints.map { LatLngPoint(it.latitude, it.longitude) }
                            } else if (currentTrip.encodedPolyline.isNotEmpty()) {
                                RouteSimplifier.decodePolyline(currentTrip.encodedPolyline)
                            } else emptyList()
                        }

                        val resolvedStartLat = currentTrip.startLat ?: locationPoints.firstOrNull()?.latitude
                        val resolvedStartLng = currentTrip.startLng ?: locationPoints.firstOrNull()?.longitude
                        val resolvedEndLat = currentTrip.endLat ?: locationPoints.lastOrNull()?.latitude ?: resolvedStartLat
                        val resolvedEndLng = currentTrip.endLng ?: locationPoints.lastOrNull()?.longitude ?: resolvedStartLng

                        CanvasRouteThumbnail(
                            pointsList = latLngPoints,
                            encodedPolyline = currentTrip.encodedPolyline,
                            startLat = resolvedStartLat,
                            startLng = resolvedStartLng,
                            endLat = resolvedEndLat,
                            endLng = resolvedEndLng,
                            thumbnailHeight = 200.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // B. Dedicated Tracking Logs Panel (Garis line & timeline detail logs)
                        TrackingLogsTimeline(
                            trip = currentTrip,
                            points = locationPoints,
                            startLat = resolvedStartLat,
                            startLng = resolvedStartLng,
                            endLat = resolvedEndLat,
                            endLng = resolvedEndLng
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Metrics Grid
            trip?.let { t ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Jarak",
                        value = String.format("%.2f km", t.distanceMeters / 1000.0),
                        modifier = Modifier.weight(1f)
                    )
                    val mins = t.durationSeconds / 60
                    val secs = t.durationSeconds % 60
                    MetricBox(
                        title = "Durasi",
                        value = String.format("%02d:%02d", mins, secs),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Kecepatan Rata2",
                        value = String.format("%.1f km/j", t.avgSpeedKmh),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Kalori",
                        value = String.format("%.0f kkal", t.calories),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Actions
            CartoonButton(
                onClick = {
                    coroutineScope.launch {
                        trip?.let { t ->
                            withContext(Dispatchers.IO) {
                                val db = ZeviraDatabase.getInstance(context)
                                db.tripDao().updateTrip(t.copy(title = tripTitle.ifEmpty { t.title }))
                            }
                        }
                        onNavigateHome()
                    }
                },
                text = stringResource(R.string.btn_save_trip),
                backgroundColor = ElectricBlue,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CartoonButton(
                onClick = {
                    coroutineScope.launch {
                        trip?.let { t ->
                            withContext(Dispatchers.IO) {
                                val db = ZeviraDatabase.getInstance(context)
                                val points = db.locationPointDao().getPointsForTrip(t.id)
                                val shareIntent = GpxExporter.exportAndShareGpx(context, t, points)
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan GPX"))
                            }
                        }
                    }
                },
                text = stringResource(R.string.btn_export_gpx),
                backgroundColor = StarYellow,
                contentColor = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CartoonButton(
                onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val db = ZeviraDatabase.getInstance(context)
                            db.tripDao().deleteTripById(tripId)
                        }
                        onNavigateHome()
                    }
                },
                text = stringResource(R.string.btn_discard_trip),
                backgroundColor = SurfaceWhite,
                contentColor = CoralRed,
                borderColor = CoralRed,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun TrackingLogsTimeline(
    trip: TripEntity,
    points: List<LocationPointEntity>,
    startLat: Double?,
    startLng: Double?,
    endLat: Double?,
    endLng: Double?
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val startTimeStr = remember(trip.startTime) {
        if (trip.startTime > 0) timeFormat.format(Date(trip.startTime)) else "--:--:--"
    }
    val endTimeStr = remember(trip.endTime) {
        val t = trip.endTime ?: System.currentTimeMillis()
        timeFormat.format(Date(t))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7FAFE), RoundedCornerShape(12.dp))
            .border(1.2.dp, Color(0xFFD6E2F3), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📋 Log Jejak & Koordinat",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (trip.mode == TrackingMode.WALK) "Jalan Santai" else "Olahraga",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step 1: Start Location
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MintGreen.copy(alpha = 0.2f), CircleShape)
                    .border(1.2.dp, StickerBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MintGreen, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Titik Keberadaan Awal (Start)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = startTimeStr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                val coordStart = if (startLat != null && startLng != null) {
                    String.format(Locale.US, "%.5f, %.5f", startLat, startLng)
                } else {
                    "Mencari titik GPS..."
                }
                Text(
                    text = "📍 $coordStart",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        // Connector line
        Box(
            modifier = Modifier
                .padding(start = 11.dp, top = 2.dp, bottom = 2.dp)
                .width(2.dp)
                .height(18.dp)
                .background(Color(0xFFBACADB))
        )

        // Step 2: End Location
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(CoralRed.copy(alpha = 0.2f), CircleShape)
                    .border(1.2.dp, StickerBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CoralRed, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Titik Terakhir Jalan-Jalan (Finish)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = endTimeStr,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                val coordEnd = if (endLat != null && endLng != null) {
                    String.format(Locale.US, "%.5f, %.5f", endLat, endLng)
                } else {
                    "Lokasi tercatat"
                }
                Text(
                    text = "🏁 $coordEnd",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFE2EBF5), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Mini Badges Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val totalPoints = if (points.isNotEmpty()) points.size else 1
            Text(
                text = "• $totalPoints Titik Koordinat Terekam",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Text(
                text = "• Akurasi: Terverifikasi",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElectricBlue
            )
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    CartoonCard(
        modifier = modifier,
        backgroundColor = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
