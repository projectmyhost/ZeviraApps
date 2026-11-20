package com.zevira.app.ui.screens.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.local.db.ZeviraDatabase
import com.zevira.app.data.local.entity.TrackingMode
import com.zevira.app.data.local.entity.TripEntity
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import com.zevira.app.data.location.LatLngPoint
import com.zevira.app.data.location.RouteSimplifier
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ScreenBg = Color(0xFF0A0B0F)
private val CardSurface = Color(0xFF14151A)
private val OrangeBright = Color(0xFFFA8E1C)
private val OrangeDeep = Color(0xFFE7620F)
private val TextSilver = Color(0xFF9EA1AE)
private val CoralRed = Color(0xFFFF5C5C)

@Composable
fun HistoryScreen(
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val db = remember { ZeviraDatabase.getInstance(context) }
    val prefs = remember { UserPreferencesDataStore(context) }
    val userId by prefs.userIdFlow.collectAsState(initial = "")

    val completedTrips by db.tripDao().observeCompletedTrips(userId.ifEmpty { "guest" })
        .flowOn(Dispatchers.IO)
        .collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf<TrackingMode?>(null) }
    var tripToDelete by remember { mutableStateOf<TripEntity?>(null) }

    val filteredTrips = completedTrips.filter {
        selectedFilter == null || it.mode == selectedFilter
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ScreenBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(paddingValues)
        ) {
            // Background Layer: Bottom Sunset Skyline Silhouette (Image 2 1:1)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_beranda_sunset),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier.fillMaxSize()
                )
                // Scrim to fade cleanly into obsidian at top & bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.00f to ScreenBg,
                                    0.45f to Color(0x990A0B0F),
                                    0.82f to Color(0xD90A0B0F),
                                    1.00f to ScreenBg
                                )
                            )
                        )
                )
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 1. Top Bar: Brand Logo "// ZEVIRA" | History Icon Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandDoubleSlash(modifier = Modifier.size(width = 20.dp, height = 16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ZEVIRA",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    // Circular history icon button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14151B))
                            .border(1.dp, Color(0x28FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Section Title with Vertical Orange Accent Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFF37812))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Riwayat Jejak",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.2).sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lihat perjalanan dan aktivitasmu di sini.",
                    color = TextSilver,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(start = 14.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Filter Chips Row: [ Semua ] [ Jalan-jalan ] [ Olahraga ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Semua
                    HistoryFilterChip(
                        label = "Semua",
                        selected = selectedFilter == null,
                        icon = { Icon(Icons.Rounded.Layers, null, tint = it, modifier = Modifier.size(16.dp)) },
                        onClick = { selectedFilter = null }
                    )

                    // Jalan-jalan
                    HistoryFilterChip(
                        label = "Jalan-jalan",
                        selected = selectedFilter == TrackingMode.WALK,
                        icon = { Icon(Icons.Rounded.CalendarMonth, null, tint = it, modifier = Modifier.size(16.dp)) },
                        onClick = { selectedFilter = TrackingMode.WALK }
                    )

                    // Olahraga
                    HistoryFilterChip(
                        label = "Olahraga",
                        selected = selectedFilter == TrackingMode.SPORT,
                        icon = { Icon(Icons.Rounded.Place, null, tint = it, modifier = Modifier.size(16.dp)) },
                        onClick = { selectedFilter = TrackingMode.SPORT }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Trip Cards List
                if (filteredTrips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MascotJejak(size = 80.dp, state = MascotState.IDLE)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.empty_history),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSilver
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTrips, key = { it.id }) { trip ->
                            TripHistoryCard(
                                trip = trip,
                                dateFormatter = dateFormatter,
                                onClick = { onNavigateToDetail(trip.id) },
                                onDelete = { tripToDelete = trip }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Delete Confirmation Dialog
            tripToDelete?.let { trip ->
                AlertDialog(
                    onDismissRequest = { tripToDelete = null },
                    containerColor = Color(0xFF16171E),
                    title = {
                        Text(
                            text = "Hapus Catatan Trip?",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    },
                    text = {
                        Text(
                            text = "Trip \"${trip.title.ifEmpty { "Petualangan Jalan-jalan" }}\" akan dihapus permanen.",
                            color = TextSilver,
                            fontSize = 13.5.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        db.tripDao().deleteTripById(trip.id)
                                    }
                                }
                                tripToDelete = null
                            }
                        ) {
                            Text("Hapus", color = CoralRed, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { tripToDelete = null }) {
                            Text("Batal", color = TextSilver)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    selected: Boolean,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF8C8F9C),
        animationSpec = tween(180),
        label = "filterContentColor"
    )

    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .then(
                if (selected) {
                    Modifier
                        .background(
                            Brush.horizontalGradient(listOf(OrangeBright, OrangeDeep))
                        )
                        .border(
                            0.8.dp,
                            Brush.verticalGradient(listOf(Color(0x60FFE0A3), Color.Transparent)),
                            RoundedCornerShape(19.dp)
                        )
                } else {
                    Modifier
                        .background(Color(0xFF16171D))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(19.dp))
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon(contentColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TripHistoryCard(
    trip: TripEntity,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF181920), Color(0xFF121317))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x26FFFFFF), Color(0x0CFFFFFF))
                ),
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Dark Circular Radar / Route Thumbnail
            HistoryRadarThumbnail(
                encodedPolyline = trip.encodedPolyline,
                startLat = trip.startLat,
                startLng = trip.startLng,
                endLat = trip.endLat,
                endLng = trip.endLng,
                modifier = Modifier.size(76.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Center details
            Column(modifier = Modifier.weight(1f)) {
                // Trip Title
                Text(
                    text = trip.title.ifEmpty { "Petualangan Jalan-jalan" },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date row: calendar icon + date string
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF8C8F9C),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = dateFormatter.format(Date(trip.startTime)),
                        color = TextSilver,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                // Stats row: Route icon + 0,00 km | Clock icon + 00:07
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Distance with route icon
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.AltRoute,
                        contentDescription = null,
                        tint = Color(0xFFF37812),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val distKm = trip.distanceMeters / 1000.0
                    Text(
                        text = String.format(Locale("id", "ID"), "%.2f km", distKm),
                        color = Color(0xFFF37812),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "|", color = Color(0x33FFFFFF), fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(8.dp))

                    // Duration with clock icon
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF8C8F9C),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val mins = trip.durationSeconds / 60
                    val secs = trip.durationSeconds % 60
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", mins, secs),
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right side: Activity Tag (Top) & Red Delete Trash (Bottom)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(76.dp)
            ) {
                // Tag pill [ Jalan ] or [ Olahraga ]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF20222A))
                        .border(0.8.dp, Color(0x20FFFFFF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 3.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (trip.mode == TrackingMode.WALK) "Jalan" else "Olahraga",
                        color = Color(0xFFB0B3C0),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Red trash can button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Hapus",
                        tint = CoralRed,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRadarThumbnail(
    encodedPolyline: String,
    startLat: Double?,
    startLng: Double?,
    endLat: Double?,
    endLng: Double?,
    modifier: Modifier = Modifier
) {
    val points = remember(encodedPolyline, startLat, startLng, endLat, endLng) {
        when {
            encodedPolyline.isNotEmpty() -> RouteSimplifier.decodePolyline(encodedPolyline)
            startLat != null && startLng != null && endLat != null && endLng != null -> {
                if (startLat == endLat && startLng == endLng) {
                    listOf(LatLngPoint(startLat, startLng))
                } else {
                    listOf(LatLngPoint(startLat, startLng), LatLngPoint(endLat, endLng))
                }
            }
            startLat != null && startLng != null -> listOf(LatLngPoint(startLat, startLng))
            else -> emptyList()
        }
    }

    val thumbShape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(thumbShape)
            .background(Color(0xFF12141A))
            .border(1.dp, Color(0x20FFFFFF), thumbShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            // 1. Concentric radar webs
            val ringColor = Color.White.copy(alpha = 0.045f)
            val stroke = Stroke(width = 1.dp.toPx())
            drawCircle(color = ringColor, radius = w * 0.18f, center = center, style = stroke)
            drawCircle(color = ringColor, radius = w * 0.34f, center = center, style = stroke)
            drawCircle(color = ringColor, radius = w * 0.48f, center = center, style = stroke)

            // Cross lines
            drawLine(ringColor, Offset(center.x, 0f), Offset(center.x, h), strokeWidth = 1.dp.toPx())
            drawLine(ringColor, Offset(0f, center.y), Offset(w, center.y), strokeWidth = 1.dp.toPx())

            if (points.size <= 1) {
                // Glowing green radar dot for stationary / short trip (matching cards 1-3)
                drawCircle(
                    color = Color(0x3300E676),
                    radius = 9.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color(0xFF00E676),
                    radius = 4.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.8.dp.toPx(),
                    center = center
                )
            } else {
                // Bounded route line for trip with movement (matching card 4)
                var minLat = points[0].latitude
                var maxLat = points[0].latitude
                var minLng = points[0].longitude
                var maxLng = points[0].longitude

                points.forEach {
                    if (it.latitude < minLat) minLat = it.latitude
                    if (it.latitude > maxLat) maxLat = it.latitude
                    if (it.longitude < minLng) minLng = it.longitude
                    if (it.longitude > maxLng) maxLng = it.longitude
                }

                val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
                val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)
                val pad = w * 0.22f

                fun toOffset(pt: LatLngPoint): Offset {
                    val nx = ((pt.longitude - minLng) / lngSpan).toFloat()
                    val ny = 1f - ((pt.latitude - minLat) / latSpan).toFloat()
                    return Offset(pad + nx * (w - 2 * pad), pad + ny * (h - 2 * pad))
                }

                val path = Path().apply {
                    val start = toOffset(points.first())
                    moveTo(start.x, start.y)
                    for (i in 1 until points.size) {
                        val pt = toOffset(points[i])
                        lineTo(pt.x, pt.y)
                    }
                }

                // Orange-to-green gradient route
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFA8E1C), Color(0xFF00E676))
                    ),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Start pin (orange)
                val startPt = toOffset(points.first())
                drawCircle(color = Color(0xFFFA8E1C), radius = 5.dp.toPx(), center = startPt)
                drawCircle(color = Color(0xFF14151B), radius = 2.dp.toPx(), center = startPt)

                // End pin (green)
                val endPt = toOffset(points.last())
                drawCircle(color = Color(0xFF00E676), radius = 5.dp.toPx(), center = endPt)
                drawCircle(color = Color(0xFF14151B), radius = 2.dp.toPx(), center = endPt)
            }
        }
    }
}

@Composable
private fun BrandDoubleSlash(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFA9424), Color(0xFFE76110))
        )

        // Left slash
        val p1 = Path().apply {
            moveTo(w * 0.22f, h * 0.04f)
            lineTo(w * 0.48f, h * 0.04f)
            lineTo(w * 0.26f, h * 0.96f)
            lineTo(w * 0.00f, h * 0.96f)
            close()
        }
        // Right slash
        val p2 = Path().apply {
            moveTo(w * 0.74f, h * 0.04f)
            lineTo(w * 1.00f, h * 0.04f)
            lineTo(w * 0.78f, h * 0.96f)
            lineTo(w * 0.52f, h * 0.96f)
            close()
        }
        drawPath(p1, brush)
        drawPath(p2, brush)
    }
}
