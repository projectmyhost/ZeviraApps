package com.zevira.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zevira.app.data.location.LatLngPoint
import com.zevira.app.data.location.RouteSimplifier
import com.zevira.app.ui.theme.CartoonCardShape
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.MintGreen
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite

@Composable
fun CanvasRouteThumbnail(
    encodedPolyline: String,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = 100.dp,
    routeColor: Color = ElectricBlue,
    backgroundColor: Color = SurfaceTint
) {
    val points = remember(encodedPolyline) {
        if (encodedPolyline.isNotEmpty()) {
            RouteSimplifier.decodePolyline(encodedPolyline)
        } else {
            emptyList()
        }
    }

    Box(
        modifier = modifier
            .size(thumbnailSize)
            .background(backgroundColor, CartoonCardShape)
            .border(1.5.dp, StickerBorder, CartoonCardShape)
            .clip(CartoonCardShape)
    ) {
        if (points.size >= 2) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val padding = 16.dp.toPx()
                val drawWidth = this.size.width - 2 * padding
                val drawHeight = this.size.height - 2 * padding

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

                val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
                val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

                val path = Path()
                for (i in points.indices) {
                    val p = points[i]
                    // Normalize to canvas coordinates
                    val x = padding + ((p.longitude - minLng) / lngRange * drawWidth).toFloat()
                    val y = padding + ((maxLat - p.latitude) / latRange * drawHeight).toFloat()

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Draw path
                drawPath(
                    path = path,
                    color = routeColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Start dot (Green)
                val startP = points.first()
                val startX = padding + ((startP.longitude - minLng) / lngRange * drawWidth).toFloat()
                val startY = padding + ((maxLat - startP.latitude) / latRange * drawHeight).toFloat()
                drawCircle(
                    color = MintGreen,
                    radius = 4.dp.toPx(),
                    center = Offset(startX, startY)
                )
                drawCircle(
                    color = StickerBorder,
                    radius = 4.dp.toPx(),
                    center = Offset(startX, startY),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // End dot (Blue)
                val endP = points.last()
                val endX = padding + ((endP.longitude - minLng) / lngRange * drawWidth).toFloat()
                val endY = padding + ((maxLat - endP.latitude) / latRange * drawHeight).toFloat()
                drawCircle(
                    color = ElectricBlue,
                    radius = 4.dp.toPx(),
                    center = Offset(endX, endY)
                )
                drawCircle(
                    color = StickerBorder,
                    radius = 4.dp.toPx(),
                    center = Offset(endX, endY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}
