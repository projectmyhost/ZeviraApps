package com.zevira.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.local.db.ZeviraDatabase
import com.zevira.app.data.local.entity.TrackingMode
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import com.zevira.app.ui.components.CanvasRouteThumbnail
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.components.CartoonChip
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val db = remember {
        androidx.room.Room.databaseBuilder(
            context,
            ZeviraDatabase::class.java,
            "zevira_database.db"
        ).build()
    }

    val prefs = remember { UserPreferencesDataStore(context) }
    val userId by prefs.userIdFlow.collectAsState(initial = "")
    val completedTrips by db.tripDao().observeCompletedTrips(userId.ifEmpty { "guest" })
        .flowOn(Dispatchers.IO)
        .collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf<TrackingMode?>(null) }

    val filteredTrips = completedTrips.filter {
        selectedFilter == null || it.mode == selectedFilter
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceTint
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.history_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CartoonChip(
                    text = stringResource(R.string.filter_all),
                    backgroundColor = if (selectedFilter == null) ElectricBlue else SurfaceWhite,
                    textColor = if (selectedFilter == null) SurfaceWhite else TextPrimary,
                    modifier = Modifier.clickable { selectedFilter = null }
                )
                CartoonChip(
                    text = stringResource(R.string.mode_walk),
                    backgroundColor = if (selectedFilter == TrackingMode.WALK) ElectricBlue else SurfaceWhite,
                    textColor = if (selectedFilter == TrackingMode.WALK) SurfaceWhite else TextPrimary,
                    modifier = Modifier.clickable { selectedFilter = TrackingMode.WALK }
                )
                CartoonChip(
                    text = stringResource(R.string.mode_sport),
                    backgroundColor = if (selectedFilter == TrackingMode.SPORT) StarYellow else SurfaceWhite,
                    textColor = if (selectedFilter == TrackingMode.SPORT) TextPrimary else TextPrimary,
                    modifier = Modifier.clickable { selectedFilter = TrackingMode.SPORT }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTrips.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MascotJejak(size = 100.dp, state = MascotState.IDLE)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.empty_history),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTrips, key = { it.id }) { trip ->
                        CartoonCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDetail(trip.id) },
                            backgroundColor = SurfaceWhite
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CanvasRouteThumbnail(
                                    encodedPolyline = trip.encodedPolyline,
                                    thumbnailSize = 80.dp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = trip.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        CartoonChip(
                                            text = if (trip.mode == TrackingMode.WALK) "Jalan" else "Olahraga",
                                            backgroundColor = if (trip.mode == TrackingMode.WALK) SurfaceTint else StarYellow.copy(alpha = 0.3f),
                                            textColor = TextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = dateFormatter.format(Date(trip.startTime)),
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                text = String.format("%.2f km", trip.distanceMeters / 1000.0),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = ElectricBlue
                                            )
                                            val mins = trip.durationSeconds / 60
                                            val secs = trip.durationSeconds % 60
                                            Text(
                                                text = String.format("%02d:%02d", mins, secs),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = TextPrimary
                                            )
                                        }

                                        // Delete Icon Button
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    withContext(Dispatchers.IO) {
                                                        db.tripDao().deleteTripById(trip.id)
                                                    }
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = context.getString(R.string.trip_deleted),
                                                        actionLabel = context.getString(R.string.action_undo),
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        withContext(Dispatchers.IO) {
                                                            db.tripDao().insertTrip(trip)
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Hapus",
                                                tint = CoralRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
