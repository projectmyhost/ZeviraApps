package com.navira.app.ui.screens.summary

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.navira.app.R
import com.navira.app.data.export.GpxExporter
import com.navira.app.data.local.db.LocationPointDao
import com.navira.app.data.local.db.TripDao
import com.navira.app.data.local.entity.TripEntity
import com.navira.app.data.local.preferences.UserPreferencesDataStore
import com.navira.app.ui.components.CanvasRouteThumbnail
import com.navira.app.ui.components.CartoonButton
import com.navira.app.ui.components.CartoonCard
import com.navira.app.ui.components.MascotJejak
import com.navira.app.ui.components.MascotState
import com.navira.app.ui.components.StarSky
import com.navira.app.ui.theme.CartoonCardShape
import com.navira.app.ui.theme.CoralRed
import com.navira.app.ui.theme.ElectricBlue
import com.navira.app.ui.theme.MintGreen
import com.navira.app.ui.theme.StarYellow
import com.navira.app.ui.theme.StickerBorder
import com.navira.app.ui.theme.SurfaceTint
import com.navira.app.ui.theme.SurfaceWhite
import com.navira.app.ui.theme.TextLightPrimary
import com.navira.app.ui.theme.TextPrimary
import com.navira.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TripSummaryScreen(
    tripId: String,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var trip by remember { mutableStateOf<TripEntity?>(null) }
    var tripTitle by remember { mutableStateOf("") }
    var newStarsEarned by remember { mutableStateOf(0) }
    var animateStarRise by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        withContext(Dispatchers.IO) {
            val db = androidx.room.Room.databaseBuilder(
                context,
                com.navira.app.data.local.db.NaviraDatabase::class.java,
                "navira_database.db"
            ).build()
            val loaded = db.tripDao().getTripById(tripId)
            if (loaded != null) {
                trip = loaded
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
                .height(230.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
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
            // Editable Title Field
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Route Canvas Thumbnail
            trip?.let { currentTrip ->
                CartoonCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceWhite
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CanvasRouteThumbnail(
                            encodedPolyline = currentTrip.encodedPolyline,
                            thumbnailSize = 180.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Grid
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

            // Actions
            CartoonButton(
                onClick = {
                    coroutineScope.launch {
                        trip?.let { t ->
                            withContext(Dispatchers.IO) {
                                val db = androidx.room.Room.databaseBuilder(
                                    context,
                                    com.navira.app.data.local.db.NaviraDatabase::class.java,
                                    "navira_database.db"
                                ).build()
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
                                val db = androidx.room.Room.databaseBuilder(
                                    context,
                                    com.navira.app.data.local.db.NaviraDatabase::class.java,
                                    "navira_database.db"
                                ).build()
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
                            val db = androidx.room.Room.databaseBuilder(
                                context,
                                com.navira.app.data.local.db.NaviraDatabase::class.java,
                                "navira_database.db"
                            ).build()
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

            Spacer(modifier = Modifier.height(24.dp))
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
