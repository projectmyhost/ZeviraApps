package com.zevira.app.data.location

import kotlin.math.sqrt

/**
 * 1D Kalman filter for smoothing GPS latitude and longitude coordinates.
 * Uses location accuracy as measurement noise variance (R).
 */
class KalmanLocationFilter(
    private val processNoiseQ: Double = 3.0 // meters per second variance
) {
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var variance: Double = -1.0 // -1 means uninitialized
    private var lastTimestampMs: Long = 0L

    fun isInitialized(): Boolean = variance > 0.0

    fun reset() {
        variance = -1.0
        lat = 0.0
        lng = 0.0
        lastTimestampMs = 0L
    }

    /**
     * Filters coordinate given measured lat, lng, accuracy, and timestamp.
     * Returns smoothed Pair(latitude, longitude).
     */
    fun filter(
        measuredLat: Double,
        measuredLng: Double,
        accuracyMeters: Float,
        timestampMs: Long
    ): Pair<Double, Double> {
        val accuracy = if (accuracyMeters < 1f) 1.0 else accuracyMeters.toDouble()
        val measurementNoiseR = accuracy * accuracy

        if (variance < 0.0) {
            // Initialize filter
            this.lat = measuredLat
            this.lng = measuredLng
            this.variance = measurementNoiseR
            this.lastTimestampMs = timestampMs
            return Pair(lat, lng)
        }

        // Distance check between existing state and new measurement
        val dLat = (measuredLat - this.lat) * 111320.0
        val dLng = (measuredLng - this.lng) * 111320.0 * kotlin.math.cos(Math.toRadians(this.lat))
        val distMeters = sqrt(dLat * dLat + dLng * dLng)

        // If location jumped > 50m and new reading is reasonably accurate (<= 30m), snap immediately
        if (distMeters > 50.0 && accuracyMeters <= 30f) {
            this.lat = measuredLat
            this.lng = measuredLng
            this.variance = measurementNoiseR
            this.lastTimestampMs = timestampMs
            return Pair(lat, lng)
        }

        val dtSeconds = (timestampMs - lastTimestampMs).coerceAtLeast(0L) / 1000.0
        lastTimestampMs = timestampMs

        // Predict next state variance
        if (dtSeconds > 0) {
            variance += dtSeconds * processNoiseQ * processNoiseQ
        }

        // Kalman Gain K = P / (P + R)
        val kalmanGain = (variance / (variance + measurementNoiseR)).coerceIn(0.05, 0.95)

        // Update state estimate
        lat += kalmanGain * (measuredLat - lat)
        lng += kalmanGain * (measuredLng - lng)

        // Update estimate error variance P = (1 - K) * P, clamped to avoid freezing
        variance = ((1.0 - kalmanGain) * variance).coerceAtLeast(4.0)

        return Pair(lat, lng)
    }
}
