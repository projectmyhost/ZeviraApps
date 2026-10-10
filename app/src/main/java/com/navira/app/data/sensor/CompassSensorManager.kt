package com.navira.app.data.sensor

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class CompassData(
    val azimuthDegrees: Float = 0f,
    val directionText: String = "N",
    val isAccuracyLow: Boolean = false,
    val trueNorthOffset: Float = 0f
)

@Singleton
class CompassSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassData = MutableStateFlow(CompassData())
    val compassData: StateFlow<CompassData> = _compassData.asStateFlow()

    private var lastAzimuth = 0f
    private val alpha = 0.15f // Low pass filter factor
    private var lastUpdateMs = 0L

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var isAccelerometerSet = false
    private var isMagnetometerSet = false

    fun startListening() {
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun updateGeomagneticField(lat: Double, lng: Double, altM: Double) {
        val field = GeomagneticField(
            lat.toFloat(), lng.toFloat(), altM.toFloat(), System.currentTimeMillis()
        )
        _compassData.value = _compassData.value.copy(trueNorthOffset = field.declination)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val now = System.currentTimeMillis()
        // Throttle to ~30 fps (33ms)
        if (now - lastUpdateMs < 33L) return
        lastUpdateMs = now

        var azimuth = 0f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            isAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            isMagnetometerSet = true
        }

        if (isAccelerometerSet && isMagnetometerSet) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            }
        }

        // Normalize to 0-360
        azimuth = (azimuth + 360f) % 360f

        // Low-pass filter to smooth jitter
        val smoothed = smoothDegrees(lastAzimuth, azimuth, alpha)
        lastAzimuth = smoothed

        val dirText = getCardinalDirection(smoothed)
        val isLow = event.accuracy <= SensorManager.SENSOR_STATUS_UNRELIABLE

        _compassData.value = _compassData.value.copy(
            azimuthDegrees = smoothed,
            directionText = dirText,
            isAccuracyLow = isLow
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val isLow = accuracy <= SensorManager.SENSOR_STATUS_UNRELIABLE
        _compassData.value = _compassData.value.copy(isAccuracyLow = isLow)
    }

    private fun smoothDegrees(prev: Float, target: Float, factor: Float): Float {
        var diff = target - prev
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        return (prev + diff * factor + 360f) % 360f
    }

    private fun getCardinalDirection(deg: Float): String {
        return when {
            deg >= 337.5 || deg < 22.5 -> "U" // Utara (N)
            deg in 22.5..67.5 -> "TL" // Timur Laut (NE)
            deg in 67.5..112.5 -> "T" // Timur (E)
            deg in 112.5..157.5 -> "TG" // Tenggara (SE)
            deg in 157.5..202.5 -> "S" // Selatan (S)
            deg in 202.5..247.5 -> "BD" // Barat Daya (SW)
            deg in 247.5..292.5 -> "B" // Barat (W)
            deg in 292.5..337.5 -> "BL" // Barat Laut (NW)
            else -> "U"
        }
    }
}
