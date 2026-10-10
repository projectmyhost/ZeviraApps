package com.zevira.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.data.location.LatLngPoint
import com.zevira.app.data.location.RouteSimplifier
import com.zevira.app.ui.theme.CartoonCardShape
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.MintGreen
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min

@Composable
fun CanvasRouteThumbnail(
    modifier: Modifier = Modifier,
    encodedPolyline: String = "",
    pointsList: List<LatLngPoint> = emptyList(),
    startLat: Double? = null,
    startLng: Double? = null,
    endLat: Double? = null,
    endLng: Double? = null,
    thumbnailSize: Dp? = null,
    thumbnailHeight: Dp = 190.dp,
    routeColor: Color = ElectricBlue,
    backgroundColor: Color = Color(0xFFF3F7FD)
) {
    // Resolve effective points
    val points = remember(encodedPolyline, pointsList, startLat, startLng, endLat, endLng) {
        when {
            pointsList.isNotEmpty() -> pointsList
            encodedPolyline.isNotEmpty() -> RouteSimplifier.decodePolyline(encodedPolyline)
            startLat != null && startLng != null && endLat != null && endLng != null -> {
                if (startLat == endLat && startLng == endLng) {
                    listOf(LatLngPoint(startLat, startLng))
                } else {
                    listOf(LatLngPoint(startLat, startLng), LatLngPoint(endLat, endLng))
                }
            }
            startLat != null && startLng != null -> listOf(LatLngPoint(startLat, startLng))
            endLat != null && endLng != null -> listOf(LatLngPoint(endLat, endLng))
            else -> emptyList()
        }
    }

    val boxModifier = if (thumbnailSize != null) {
        modifier.size(thumbnailSize)
    } else {
        modifier.fillMaxWidth().height(thumbnailHeight)
    }

    val isCompact = thumbnailSize != null && thumbnailSize < 120.dp

    Box(
        modifier = boxModifier
            .background(backgroundColor, CartoonCardShape)
            .border(2.dp, StickerBorder, CartoonCardShape)
            .clip(CartoonCardShape)
    ) {
        // 1. Canvas Layer (Grid lines + GPS Route Polyline + Markers)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // A. Draw Subtle Cartography Grid
            val gridStep = 28.dp.toPx()
            val gridColor = Color(0xFFDEE7F6)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

            var gx = gridStep
            while (gx < width) {
                drawLine(
                    color = gridColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
                gx += gridStep
            }

            var gy = gridStep
            while (gy < height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
                gy += gridStep
            }

            // B. Draw Route Line or Stationary Beacon
            val padding = 28.dp.toPx()
            val drawWidth = width - 2 * padding
            val drawHeight = height - 2 * padding

            if (points.size >= 2) {
                var minLat = Double.MAX_VALUE
                var maxLat = -Double.MAX_VALUE
                var minLng = Double.MAX_VALUE
                var maxLng = -Double.MAX_VALUE

                for (p in points) {
                    if (p.latitude < minLat) minLat = p.latitude
                    if (p.latitude > maxLat) maxLat = p.latitude
                    if (p.longitude < minLng) minLng = p.longitude
                    if (p.longitude > maxLng) maxLng = p.longitude
                }

                val latSpan = maxLat - minLat
                val lngSpan = maxLng - minLng

                // Check if user was mostly stationary (span < ~10m)
                if (latSpan < 0.00008 && lngSpan < 0.00008) {
                    // Draw stationary center beacon with radar pulse
                    val centerX = width / 2f
                    val centerY = height / 2f

                    drawCircle(
                        color = ElectricBlue.copy(alpha = 0.12f),
                        radius = 42.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = ElectricBlue.copy(alpha = 0.25f),
                        radius = 24.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = MintGreen,
                        radius = 8.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = StickerBorder,
                        radius = 8.dp.toPx(),
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                } else {
                    // Normalize coordinates while preserving aspect ratio
                    val latRange = latSpan.coerceAtLeast(0.00001)
                    val lngRange = lngSpan.coerceAtLeast(0.00001)

                    val scaleX = drawWidth / lngRange
                    val scaleY = drawHeight / latRange
                    val scale = min(scaleX, scaleY)

                    val scaledWidth = (lngRange * scale).toFloat()
                    val scaledHeight = (latRange * scale).toFloat()

                    val offsetX = padding + (drawWidth - scaledWidth) / 2f
                    val offsetY = padding + (drawHeight - scaledHeight) / 2f

                    val path = Path()
                    var startPixel = Offset.Zero
                    var endPixel = Offset.Zero

                    for (i in points.indices) {
                        val p = points[i]
                        val x = offsetX + ((p.longitude - minLng) * scale).toFloat()
                        val y = offsetY + ((maxLat - p.latitude) * scale).toFloat()

                        if (i == 0) {
                            path.moveTo(x, y)
                            startPixel = Offset(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                        if (i == points.lastIndex) {
                            endPixel = Offset(x, y)
                        }
                    }

                    // 1. Outer Dark Stroke (Sticker Border effect)
                    drawPath(
                        path = path,
                        color = StickerBorder,
                        style = Stroke(
                            width = 6.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 2. Vibrant Route Line
                    drawPath(
                        path = path,
                        color = routeColor,
                        style = Stroke(
                            width = 3.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 3. Start Point Marker (Mint Green with halo)
                    drawCircle(
                        color = MintGreen.copy(alpha = 0.35f),
                        radius = 12.dp.toPx(),
                        center = startPixel
                    )
                    drawCircle(
                        color = MintGreen,
                        radius = 6.5.dp.toPx(),
                        center = startPixel
                    )
                    drawCircle(
                        color = StickerBorder,
                        radius = 6.5.dp.toPx(),
                        center = startPixel,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = startPixel
                    )

                    // 4. End Point Marker (Coral Red with halo)
                    drawCircle(
                        color = CoralRed.copy(alpha = 0.35f),
                        radius = 12.dp.toPx(),
                        center = endPixel
                    )
                    drawCircle(
                        color = CoralRed,
                        radius = 6.5.dp.toPx(),
                        center = endPixel
                    )
                    drawCircle(
                        color = StickerBorder,
                        radius = 6.5.dp.toPx(),
                        center = endPixel,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = endPixel
                    )
                }
            } else {
                // points.size is 0 or 1: Show single position beacon
                val centerX = width / 2f
                val centerY = height / 2f

                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.12f),
                    radius = 42.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = ElectricBlue.copy(alpha = 0.25f),
                    radius = 24.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = MintGreen,
                    radius = 8.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = StickerBorder,
                    radius = 8.dp.toPx(),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
            }
        }

        if (!isCompact) {
            // 2. Top-Left Badge: GPS Points Count
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .background(SurfaceWhite, RoundedCornerShape(8.dp))
                    .border(1.2.dp, StickerBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val labelText = if (points.size >= 2) {
                    "📍 ${points.size} Titik Jejak"
                } else if (points.size == 1) {
                    "📍 1 Titik Terdeteksi"
                } else {
                    "📍 Jejak Lokasi Aktif"
                }
                Text(
                    text = labelText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }

            // 3. Top-Right Badge: Compass indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(SurfaceWhite, RoundedCornerShape(8.dp))
                    .border(1.2.dp, StickerBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🧭 Radar GPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            // 4. Bottom Legend: Start & Finish indicators
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .background(SurfaceWhite.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                    .border(1.2.dp, StickerBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MintGreen, CircleShape)
                        .border(1.dp, StickerBorder, CircleShape)
                )
                Text(
                    text = "Mulai",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CoralRed, CircleShape)
                        .border(1.dp, StickerBorder, CircleShape)
                )
                Text(
                    text = "Selesai",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}
