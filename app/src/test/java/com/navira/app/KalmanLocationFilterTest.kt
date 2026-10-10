package com.navira.app

import com.navira.app.data.location.KalmanLocationFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KalmanLocationFilterTest {

    @Test
    fun testInitialMeasurementReturnsSameCoordinates() {
        val filter = KalmanLocationFilter()
        val (lat, lng) = filter.filter(-6.2088, 106.8456, 10f, 1000L)
        assertEquals(-6.2088, lat, 0.00001)
        assertEquals(106.8456, lng, 0.00001)
        assertTrue(filter.isInitialized())
    }

    @Test
    fun testNoisySpikesAreSmoothed() {
        val filter = KalmanLocationFilter(processNoiseQ = 1.0)
        filter.filter(-6.2088, 106.8456, 5f, 1000L)
        // Sudden noisy reading with high inaccuracy (accuracy = 50m)
        val (lat, lng) = filter.filter(-6.2150, 106.8500, 50f, 2000L)
        // Smoothed lat should be significantly pulled towards original lat, not jump straight to spike
        assertTrue(lat > -6.2150)
        assertTrue(lat < -6.2088)
    }

    @Test
    fun testResetClearsState() {
        val filter = KalmanLocationFilter()
        filter.filter(-6.2088, 106.8456, 10f, 1000L)
        filter.reset()
        val isInit = filter.isInitialized()
        assertEquals(false, isInit)
    }
}
