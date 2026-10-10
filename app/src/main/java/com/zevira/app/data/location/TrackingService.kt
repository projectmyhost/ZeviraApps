package com.zevira.app.data.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.zevira.app.MainActivity
import com.zevira.app.ZeviraApp
import com.zevira.app.R
import com.zevira.app.data.local.db.LocationPointDao
import com.zevira.app.data.local.db.TripDao
import com.zevira.app.data.local.entity.LocationPointEntity
import com.zevira.app.data.local.entity.SportActivity
import com.zevira.app.data.local.entity.TrackingMode
import com.zevira.app.data.local.entity.TripEntity
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {

    @Inject
    lateinit var tripDao: TripDao

    @Inject
    lateinit var locationPointDao: LocationPointDao

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var batchSaveJob: Job? = null

    private val kalmanFilter = KalmanLocationFilter()
    private val pendingPoints = mutableListOf<LocationPointEntity>()
    private var lastAcceptedLocation: Location? = null
    private var lastAcceptedTimestampMs: Long = 0L

    private var tripId: String = ""
    private var ownerUid: String = ""
    private var isDemoTrip: Boolean = false
    private var trackingMode: TrackingMode = TrackingMode.WALK
    private var sportActivity: SportActivity = SportActivity.NONE
    private var userWeightKg: Float = 60f

    private var durationSeconds: Long = 0L
    private var movingDurationSeconds: Long = 0L
    private var distanceMeters: Double = 0.0
    private var maxSpeedKmh: Double = 0.0
    private var startLat: Double? = null
    private var startLng: Double? = null
    private var isPaused: Boolean = false
    private var pointsCount: Int = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (isPaused) return
            for (location in result.locations) {
                processNewLocation(location)
            }
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
        const val EXTRA_MODE = "EXTRA_MODE"
        const val EXTRA_ACTIVITY = "EXTRA_ACTIVITY"

        private val _trackingState = MutableStateFlow(TrackingState())
        val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundWithNotification()
        when (intent?.action) {
            ACTION_START -> {
                val modeStr = intent.getStringExtra(EXTRA_MODE) ?: TrackingMode.WALK.name
                val activityStr = intent.getStringExtra(EXTRA_ACTIVITY) ?: SportActivity.NONE.name
                val explicitTripId = intent.getStringExtra(EXTRA_TRIP_ID)
                startTracking(
                    mode = TrackingMode.valueOf(modeStr),
                    activity = SportActivity.valueOf(activityStr),
                    restoredTripId = explicitTripId
                )
            }
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_NOT_STICKY
    }

    private fun startTracking(
        mode: TrackingMode,
        activity: SportActivity,
        restoredTripId: String? = null
    ) {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            Timber.e("startTracking aborted: Location permission ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION not granted!")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        this.trackingMode = mode
        this.sportActivity = activity
        this.tripId = restoredTripId ?: UUID.randomUUID().toString()
        this.isPaused = false

        serviceScope.launch {
            ownerUid = preferencesDataStore.userIdFlow.first().ifEmpty { "guest" }
            isDemoTrip = preferencesDataStore.isDemoFlow.first()
            userWeightKg = preferencesDataStore.userWeightFlow.first()

            // Initialize or restore Trip in Room
            if (restoredTripId == null) {
                val newTrip = TripEntity(
                    id = tripId,
                    ownerUid = ownerUid,
                    title = "Petualangan ${if (mode == TrackingMode.WALK) "Jalan-jalan" else "Olahraga"}",
                    mode = mode,
                    subActivity = activity,
                    startTime = System.currentTimeMillis(),
                    isDemo = isDemoTrip
                )
                tripDao.insertTrip(newTrip)
            } else {
                val existing = tripDao.getTripById(tripId)
                if (existing != null) {
                    distanceMeters = existing.distanceMeters
                    durationSeconds = existing.durationSeconds
                    movingDurationSeconds = existing.movingDurationSeconds
                    startLat = existing.startLat
                    startLng = existing.startLng
                }
            }

            _trackingState.value = TrackingState(
                tripId = tripId,
                isTracking = true,
                isPaused = false,
                isSearchingGps = true,
                mode = mode,
                subActivity = activity,
                distanceMeters = distanceMeters,
                durationSeconds = durationSeconds,
                startLat = startLat,
                startLng = startLng
            )

            requestLocationUpdates()
            fetchInitialLocation()
            startTimer()
            startBatchSaver()
        }
    }

    private fun pauseTracking() {
        isPaused = true
        _trackingState.value = _trackingState.value.copy(isPaused = true)
        updateNotification()
    }

    private fun resumeTracking() {
        isPaused = false
        _trackingState.value = _trackingState.value.copy(isPaused = false)
        updateNotification()
    }

    private fun stopTracking() {
        isPaused = false
        stopLocationUpdates()
        timerJob?.cancel()
        batchSaveJob?.cancel()

        serviceScope.launch {
            flushPendingPoints()

            // Finalize Trip in Room
            val currentTrip = tripDao.getTripById(tripId)
            if (currentTrip != null) {
                val allPoints = locationPointDao.getPointsForTrip(tripId)
                val latLngs = allPoints.map { LatLngPoint(it.latitude, it.longitude) }
                val simplified = RouteSimplifier.simplify(latLngs, epsilonMeters = 3.0)
                val encoded = RouteSimplifier.encodePolyline(simplified)

                val avgSpeed = if (durationSeconds > 0) {
                    (distanceMeters / 1000.0) / (durationSeconds / 3600.0)
                } else 0.0

                val finalTrip = currentTrip.copy(
                    endTime = System.currentTimeMillis(),
                    distanceMeters = distanceMeters,
                    durationSeconds = durationSeconds,
                    movingDurationSeconds = movingDurationSeconds,
                    calories = _trackingState.value.calories,
                    avgSpeedKmh = avgSpeed,
                    maxSpeedKmh = maxSpeedKmh,
                    endLat = _trackingState.value.currentLat,
                    endLng = _trackingState.value.currentLng,
                    encodedPolyline = encoded,
                    isCompleted = true
                )
                tripDao.updateTrip(finalTrip)
            }

            _trackingState.value = _trackingState.value.copy(
                isTracking = false,
                isPaused = false
            )

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun processNewLocation(location: Location) {
        val accuracy = location.accuracy
        val speedMps = if (location.hasSpeed()) location.speed else 0f
        val timestamp = location.time

        // 1. Extreme outlier check: ignore if accuracy > 100m or non-positive
        if (accuracy > 100f || accuracy <= 0f) {
            _trackingState.value = _trackingState.value.copy(isGpsWeak = true)
            return
        }

        // 2. Filter out unrealistic speeds (teleport spikes)
        val maxAllowedSpeedMps = when (trackingMode) {
            TrackingMode.WALK -> 60f
            TrackingMode.SPORT -> when (sportActivity) {
                SportActivity.WALK -> 6f
                SportActivity.RUN -> 12f
                SportActivity.CYCLE -> 25f
                SportActivity.NONE -> 35f
            }
        }
        if (speedMps > maxAllowedSpeedMps) {
            return
        }

        // 3. Smooth with Kalman Filter
        val (smoothedLat, smoothedLng) = kalmanFilter.filter(
            measuredLat = location.latitude,
            measuredLng = location.longitude,
            accuracyMeters = accuracy,
            timestampMs = timestamp
        )

        val isGpsWeak = accuracy > 45f

        // Set Start Location if not set yet (using first reasonably accurate fix <= 50m)
        if ((startLat == null || startLng == null) && accuracy <= 50f) {
            startLat = smoothedLat
            startLng = smoothedLng
            serviceScope.launch {
                val current = tripDao.getTripById(tripId)
                if (current != null) {
                    tripDao.updateTrip(current.copy(startLat = startLat, startLng = startLng))
                }
            }
        }

        // Only accumulate distance & save polyline points if fix is not weak
        if (!isGpsWeak) {
            // 4. Segment Gap Check: if time gap > 30s, start new segment without adding fake line/distance
            val isSegmentGap = lastAcceptedTimestampMs > 0 && (timestamp - lastAcceptedTimestampMs > 30000L)

            // 5. Distance Accumulation
            val lastLoc = lastAcceptedLocation
            if (lastLoc != null && !isSegmentGap) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    lastLoc.latitude, lastLoc.longitude,
                    smoothedLat, smoothedLng,
                    results
                )
                val d = results[0].toDouble()
                // Only add distance if moving (speed >= 0.3 m/s or displacement > 3m)
                if (speedMps >= 0.3f || d > 3.0) {
                    distanceMeters += d
                }
            }

            val speedKmh = speedMps * 3.6
            if (speedKmh > maxSpeedKmh) {
                maxSpeedKmh = speedKmh
            }

            lastAcceptedLocation = Location("kalman").apply {
                this.latitude = smoothedLat
                this.longitude = smoothedLng
                this.accuracy = accuracy
            }
            lastAcceptedTimestampMs = timestamp

            pointsCount++

            val pointEntity = LocationPointEntity(
                tripId = tripId,
                latitude = smoothedLat,
                longitude = smoothedLng,
                altitude = location.altitude,
                accuracy = accuracy,
                speed = speedMps,
                bearing = if (location.hasBearing()) location.bearing else 0f,
                timestamp = timestamp,
                isSegmentStart = isSegmentGap
            )

            synchronized(pendingPoints) {
                pendingPoints.add(pointEntity)
            }
        }

        val speedKmh = speedMps * 3.6

        // Dynamic Calories
        val calories = CalorieCalculator.calculateCalories(
            activity = sportActivity,
            speedKmh = speedKmh,
            weightKg = userWeightKg,
            durationSeconds = movingDurationSeconds
        )

        // Always update trackingState coordinates so map blue dot tracks accurately!
        _trackingState.value = _trackingState.value.copy(
            isSearchingGps = false,
            isGpsWeak = isGpsWeak,
            currentLat = smoothedLat,
            currentLng = smoothedLng,
            currentAccuracy = accuracy,
            currentBearing = if (location.hasBearing()) location.bearing else 0f,
            currentSpeedKmh = speedKmh,
            maxSpeedKmh = maxSpeedKmh,
            distanceMeters = distanceMeters,
            calories = calories,
            startLat = startLat,
            startLng = startLng,
            pointsCount = pointsCount
        )

        updateNotification()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                if (!isPaused) {
                    durationSeconds++
                    // Moving duration increases if speed >= 0.5 m/s
                    if (_trackingState.value.currentSpeedKmh >= 1.8) {
                        movingDurationSeconds++
                    }
                    _trackingState.value = _trackingState.value.copy(
                        durationSeconds = durationSeconds,
                        movingDurationSeconds = movingDurationSeconds
                    )
                }
            }
        }
    }

    private fun startBatchSaver() {
        batchSaveJob?.cancel()
        batchSaveJob = serviceScope.launch {
            while (isActive) {
                delay(10000L) // every 10 seconds
                flushPendingPoints()
            }
        }
    }

    private suspend fun flushPendingPoints() {
        val pointsToSave: List<LocationPointEntity>
        synchronized(pendingPoints) {
            if (pendingPoints.isEmpty()) return
            pointsToSave = ArrayList(pendingPoints)
            pendingPoints.clear()
        }

        val existing = tripDao.getTripById(tripId)
        if (existing == null) {
            Timber.w("flushPendingPoints: Trip $tripId not yet in DB, deferring points flush")
            synchronized(pendingPoints) {
                pendingPoints.addAll(0, pointsToSave)
            }
            return
        }

        try {
            locationPointDao.insertPoints(pointsToSave)
            tripDao.updateTrip(
                existing.copy(
                    distanceMeters = distanceMeters,
                    durationSeconds = durationSeconds,
                    movingDurationSeconds = movingDurationSeconds,
                    calories = _trackingState.value.calories,
                    maxSpeedKmh = maxSpeedKmh
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error saving batch location points")
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchInitialLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && _trackingState.value.currentLat == null) {
                    processNewLocation(loc)
                }
            }
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        processNewLocation(loc)
                    }
                }
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException fetching initial location")
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val intervalMs = if (trackingMode == TrackingMode.WALK) 2000L else 1000L
        val minIntervalMs = 1000L
        val minDistanceM = 0f

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(minIntervalMs)
            .setMinUpdateDistanceMeters(minDistanceM)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Missing location permissions for FusedLocationProviderClient")
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startForegroundWithNotification() {
        try {
            val notification = buildTrackingNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    ZeviraApp.NOTIFICATION_TRACKING_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(ZeviraApp.NOTIFICATION_TRACKING_ID, notification)
            }
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException while calling startForeground with location type")
            stopSelf()
        } catch (e: Exception) {
            Timber.e(e, "Exception while calling startForeground")
            stopSelf()
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ZeviraApp.NOTIFICATION_TRACKING_ID, buildTrackingNotification())
    }

    private fun buildTrackingNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = Intent(this, TrackingService::class.java).apply { action = ACTION_RESUME }
            val pendingResume = PendingIntent.getService(this, 1, resumeIntent, PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action.Builder(0, getString(R.string.btn_resume), pendingResume).build()
        } else {
            val pauseIntent = Intent(this, TrackingService::class.java).apply { action = ACTION_PAUSE }
            val pendingPause = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action.Builder(0, getString(R.string.btn_pause), pendingPause).build()
        }

        val stopIntent = Intent(this, TrackingService::class.java).apply { action = ACTION_STOP }
        val pendingStop = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val stopAction = NotificationCompat.Action.Builder(0, "Stop", pendingStop).build()

        val distanceKm = distanceMeters / 1000.0
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        val contentText = String.format("%.2f km  |  %s", distanceKm, timeString)

        return NotificationCompat.Builder(this, ZeviraApp.CHANNEL_TRACKING_ID)
            .setContentTitle(if (isPaused) getString(R.string.tracking_paused) else getString(R.string.tracking_active))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_zevira_star)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        timerJob?.cancel()
        batchSaveJob?.cancel()
    }
}
