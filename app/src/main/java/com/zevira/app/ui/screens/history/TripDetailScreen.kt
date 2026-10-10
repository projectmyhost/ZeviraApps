package com.zevira.app.ui.screens.history

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.export.GpxExporter
import com.zevira.app.data.local.db.ZeviraDatabase
import com.zevira.app.data.local.entity.LocationPointEntity
import com.zevira.app.data.local.entity.TripEntity
import com.zevira.app.data.location.LatLngPoint
import com.zevira.app.data.location.RouteSimplifier
import com.zevira.app.ui.components.CanvasRouteThumbnail
import com.zevira.app.ui.components.CartoonButton
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.screens.map.LeafletMapView
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripDetailScreen(
    tripId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var trip by remember { mutableStateOf<TripEntity?>(null) }
    var points by remember { mutableStateOf<List<LocationPointEntity>>(emptyList()) }
    var replayProgress by remember { mutableFloatStateOf(1f) }

    val db = remember {
        androidx.room.Room.databaseBuilder(
            context,
            ZeviraDatabase::class.java,
            "zevira_database.db"
        ).build()
    }

    LaunchedEffect(tripId) {
        withContext(Dispatchers.IO) {
            trip = db.tripDao().getTripById(tripId)
            points = db.locationPointDao().getPointsForTrip(tripId)
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID")) }

    // Replay point coordinates
    val currentReplayIndex = if (points.isNotEmpty()) {
        ((points.size - 1) * replayProgress).toInt().coerceIn(0, points.size - 1)
    } else 0
    val activePoint = points.getOrNull(currentReplayIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceTint)
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp),
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
                text = trip?.title ?: "Detail Petualangan",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Map Preview Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 16.dp)
                .background(SurfaceWhite, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                .border(2.dp, StickerBorder, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
        ) {
            LeafletMapView(
                modifier = Modifier.fillMaxSize(),
                currentLat = activePoint?.latitude ?: trip?.endLat,
                currentLng = activePoint?.longitude ?: trip?.endLng,
                currentAccuracy = activePoint?.accuracy ?: 10f,
                currentBearing = activePoint?.bearing ?: 0f,
                startLat = trip?.startLat,
                startLng = trip?.startLng,
                mapStyle = "real_life"
            )
        }

        // Replay Slider Box
        if (points.size > 2) {
            CartoonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                backgroundColor = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.replay_route),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${(replayProgress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElectricBlue
                        )
                    }
                    Slider(
                        value = replayProgress,
                        onValueChange = { replayProgress = it },
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricBlue,
                            activeTrackColor = ElectricBlue,
                            inactiveTrackColor = Color.LightGray
                        )
                    )
                }
            }
        }

        // Metrics Grid
        trip?.let { t ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = dateFormatter.format(Date(t.startTime)),
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailMetricCard(
                        title = "Jarak Total",
                        value = String.format("%.2f km", t.distanceMeters / 1000.0),
                        modifier = Modifier.weight(1f)
                    )
                    val mins = t.durationSeconds / 60
                    val secs = t.durationSeconds % 60
                    DetailMetricCard(
                        title = "Durasi Total",
                        value = String.format("%02d:%02d", mins, secs),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailMetricCard(
                        title = "Kecepatan Rata2",
                        value = String.format("%.1f km/j", t.avgSpeedKmh),
                        modifier = Modifier.weight(1f)
                    )
                    DetailMetricCard(
                        title = "Kecepatan Maks",
                        value = String.format("%.1f km/j", t.maxSpeedKmh),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (t.calories > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailMetricCard(
                        title = "Estimasi Kalori",
                        value = String.format("%.0f kkal", t.calories),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // GPX Export Button
                CartoonButton(
                    onClick = {
                        coroutineScope.launch {
                            val shareIntent = GpxExporter.exportAndShareGpx(context, t, points)
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan GPX"))
                        }
                    },
                    text = stringResource(R.string.btn_export_gpx),
                    backgroundColor = StarYellow,
                    contentColor = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun DetailMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    CartoonCard(
        modifier = modifier,
        backgroundColor = SurfaceWhite
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
    }
}
