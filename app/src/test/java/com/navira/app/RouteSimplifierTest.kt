package com.navira.app

import com.navira.app.data.location.LatLngPoint
import com.navira.app.data.location.RouteSimplifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteSimplifierTest {

    @Test
    fun testRdpSimplificationCollinearPoints() {
        val points = listOf(
            LatLngPoint(0.0, 0.0),
            LatLngPoint(0.0, 0.0001), // almost exactly on straight line
            LatLngPoint(0.0, 0.0002),
            LatLngPoint(0.0, 0.0003),
            LatLngPoint(0.0, 0.0004)
        )
        val simplified = RouteSimplifier.simplify(points, epsilonMeters = 3.0)
        // Redundant collinear points should be reduced to start and end
        assertEquals(2, simplified.size)
        assertEquals(points.first(), simplified.first())
        assertEquals(points.last(), simplified.last())
    }

    @Test
    fun testPolylineEncodeAndDecode() {
        val original = listOf(
            LatLngPoint(-6.2088, 106.8456),
            LatLngPoint(-6.2095, 106.8462),
            LatLngPoint(-6.2100, 106.8470)
        )
        val encoded = RouteSimplifier.encodePolyline(original)
        assertTrue(encoded.isNotEmpty())

        val decoded = RouteSimplifier.decodePolyline(encoded)
        assertEquals(original.size, decoded.size)
        for (i in original.indices) {
            assertEquals(original[i].latitude, decoded[i].latitude, 0.00002)
            assertEquals(original[i].longitude, decoded[i].longitude, 0.00002)
        }
    }
}
