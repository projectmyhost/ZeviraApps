package com.navira.app

import com.navira.app.data.local.entity.SportActivity
import com.navira.app.data.location.CalorieCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun testMetValuesPerActivity() {
        // Walk slow (< 4.8 km/h) -> MET 3.0
        assertEquals(3.0, CalorieCalculator.getMet(SportActivity.WALK, 3.5), 0.01)

        // Walk brisk (>= 4.8 km/h) -> MET 3.8
        assertEquals(3.8, CalorieCalculator.getMet(SportActivity.WALK, 5.2), 0.01)

        // Run fast (> 10.5 km/h) -> MET 11.0
        assertEquals(11.0, CalorieCalculator.getMet(SportActivity.RUN, 11.5), 0.01)

        // Cycle fast (> 19 km/h) -> MET 10.0
        assertEquals(10.0, CalorieCalculator.getMet(SportActivity.CYCLE, 22.0), 0.01)
    }

    @Test
    fun testCalorieCalculation() {
        // 60 kg weight, running 10 km/h (MET 9.8) for 1 hour (3600 seconds)
        // calories = 9.8 * 60 * 1.0 = 588.0 kkal
        val calories = CalorieCalculator.calculateCalories(
            activity = SportActivity.RUN,
            speedKmh = 10.0,
            weightKg = 60f,
            durationSeconds = 3600L
        )
        assertEquals(588.0, calories, 1.0)
    }

    @Test
    fun testPaceCalculation() {
        // 10 km/h -> 6 min/km pace
        val pace = CalorieCalculator.calculatePaceMinutesPerKm(10.0)
        assertEquals(6.0, pace, 0.01)
    }
}
