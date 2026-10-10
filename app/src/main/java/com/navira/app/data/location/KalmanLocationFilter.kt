package com.navira.app.data.location

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

        val dtSeconds = (timestampMs - lastTimestampMs).coerceAtLeast(0L) / 1000.0
        lastTimestampMs = timestampMs

        // Predict next state variance
        if (dtSeconds > 0) {
            variance += dtSeconds * processNoiseQ * processNoiseQ
        }

        // Kalman Gain K = P / (P + R)
        val kalmanGain = variance / (variance + measurementNoiseR)

        // Update state estimate
        lat += kalmanGain * (measuredLat - lat)
        lng += kalmanGain * (measuredLng - lng)

        // Update estimate error variance P = (1 - K) * P
        variance = (1.0 - kalmanGain) * variance

        return Pair(lat, lng)
    }
}
