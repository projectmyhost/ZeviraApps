package com.zevira.app.data.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double
)

/**
 * Ramer-Douglas-Peucker (RDP) route simplification and polyline encoder.
 */
object RouteSimplifier {

    /**
     * Simplifies a list of LatLngPoints using RDP algorithm with given epsilon (in meters).
     */
    fun simplify(points: List<LatLngPoint>, epsilonMeters: Double = 3.0): List<LatLngPoint> {
        if (points.size < 3) return points

        var maxDistance = 0.0
        var maxIndex = 0
        val end = points.size - 1

        for (i in 1 until end) {
            val dist = perpendicularDistanceMeters(points[i], points[0], points[end])
            if (dist > maxDistance) {
                maxDistance = dist
                maxIndex = i
            }
        }

        return if (maxDistance > epsilonMeters) {
            val left = simplify(points.subList(0, maxIndex + 1), epsilonMeters)
            val right = simplify(points.subList(maxIndex, points.size), epsilonMeters)
            left.dropLast(1) + right
        } else {
            listOf(points.first(), points.last())
        }
    }

    /**
     * Calculates perpendicular distance from point P to line AB in meters (haversine approximation).
     */
    fun perpendicularDistanceMeters(p: LatLngPoint, a: LatLngPoint, b: LatLngPoint): Double {
        val totalDist = distanceBetweenMeters(a.latitude, a.longitude, b.latitude, b.longitude)
        if (totalDist == 0.0) {
            return distanceBetweenMeters(p.latitude, p.longitude, a.latitude, a.longitude)
        }

        // Cross-track distance
        val dAP = distanceBetweenMeters(a.latitude, a.longitude, p.latitude, p.longitude)
        val bearingAB = initialBearing(a, b)
        val bearingAP = initialBearing(a, p)

        val deltaBearing = Math.toRadians(bearingAP - bearingAB)
        val angularDist = dAP / 6371000.0 // Earth radius in meters

        return kotlin.math.abs(kotlin.math.asin(sin(angularDist) * sin(deltaBearing))) * 6371000.0
    }

    fun distanceBetweenMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun initialBearing(a: LatLngPoint, b: LatLngPoint): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    /**
     * Standard Polyline Encoding (Google Polyline format, precision 1e5).
     */
    fun encodePolyline(points: List<LatLngPoint>): String {
        val result = StringBuilder()
        var lastLat = 0
        var lastLng = 0

        for (point in points) {
            val lat = (point.latitude * 1e5).toInt()
            val lng = (point.longitude * 1e5).toInt()

            val dLat = lat - lastLat
            val dLng = lng - lastLng

            encodeValue(dLat, result)
            encodeValue(dLng, result)

            lastLat = lat
            lastLng = lng
        }

        return result.toString()
    }

    private fun encodeValue(value: Int, result: StringBuilder) {
        var v = if (value < 0) (value shl 1).inv() else value shl 1
        while (v >= 0x20) {
            result.append(((0x20 or (v and 0x1f)) + 63).toChar())
            v = v shr 5
        }
        result.append((v + 63).toChar())
    }

    /**
     * Decodes standard Polyline string back to list of LatLngPoints.
     */
    fun decodePolyline(encoded: String): List<LatLngPoint> {
        val points = mutableListOf<LatLngPoint>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lng += dLng

            points.add(LatLngPoint(lat / 1e5, lng / 1e5))
        }

        return points
    }
}
