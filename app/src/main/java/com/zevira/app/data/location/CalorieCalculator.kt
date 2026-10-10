package com.zevira.app.data.location

import com.zevira.app.data.local.entity.SportActivity

object CalorieCalculator {

    /**
     * Determines MET value dynamically based on sub-activity and speed in km/h.
     */
    fun getMet(activity: SportActivity, speedKmh: Double): Double {
        return when (activity) {
            SportActivity.WALK, SportActivity.NONE -> {
                if (speedKmh < 4.8) 3.0 else 3.8
            }
            SportActivity.RUN -> {
                when {
                    speedKmh < 8.8 -> 8.3
                    speedKmh < 10.5 -> 9.8
                    else -> 11.0
                }
            }
            SportActivity.CYCLE -> {
                when {
                    speedKmh < 16.0 -> 6.0
                    speedKmh <= 19.0 -> 8.0
                    else -> 10.0
                }
            }
        }
    }

    /**
     * Calculates calories burned for a duration in seconds:
     * calories = MET * weightKg * (movingDurationHours)
     */
    fun calculateCalories(
        activity: SportActivity,
        speedKmh: Double,
        weightKg: Float,
        durationSeconds: Long
    ): Double {
        val met = getMet(activity, speedKmh)
        val hours = durationSeconds / 3600.0
        return met * weightKg * hours
    }

    /**
     * Calculates pace in minutes per kilometer.
     * Returns 0.0 if speed is negligible.
     */
    fun calculatePaceMinutesPerKm(speedKmh: Double): Double {
        if (speedKmh <= 0.1) return 0.0
        return 60.0 / speedKmh
    }
}
