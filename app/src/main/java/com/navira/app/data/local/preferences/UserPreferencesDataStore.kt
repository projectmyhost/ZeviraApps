package com.navira.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "navira_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_IS_GUEST = booleanPreferencesKey("is_guest")
        val KEY_IS_DEMO = booleanPreferencesKey("is_demo")
        val KEY_USER_WEIGHT_KG = floatPreferencesKey("user_weight_kg")
        val KEY_MAP_STYLE = stringPreferencesKey("map_style")
        val KEY_IS_POWER_SAVER = booleanPreferencesKey("is_power_saver")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_PERMISSION_PRIMER_SHOWN = booleanPreferencesKey("permission_primer_shown")
        val KEY_TOTAL_STARS = intPreferencesKey("total_stars")
    }

    val userIdFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: ""
    }

    val isGuestFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_GUEST] ?: true
    }

    val isDemoFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_DEMO] ?: false
    }

    val userWeightFlow: Flow<Float> = dataStore.data.map { prefs ->
        prefs[KEY_USER_WEIGHT_KG] ?: 60f
    }

    val mapStyleFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_MAP_STYLE] ?: "real_life"
    }

    val isPowerSaverFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_POWER_SAVER] ?: false
    }

    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val totalStarsFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_TOTAL_STARS] ?: 0
    }

    suspend fun setUserId(userId: String) {
        dataStore.edit { it[KEY_USER_ID] = userId }
    }

    suspend fun setUserStatus(isGuest: Boolean, isDemo: Boolean) {
        dataStore.edit {
            it[KEY_IS_GUEST] = isGuest
            it[KEY_IS_DEMO] = isDemo
        }
    }

    suspend fun setUserWeight(weightKg: Float) {
        dataStore.edit { it[KEY_USER_WEIGHT_KG] = weightKg }
    }

    suspend fun setMapStyle(style: String) {
        dataStore.edit { it[KEY_MAP_STYLE] = style }
    }

    suspend fun setPowerSaver(enabled: Boolean) {
        dataStore.edit { it[KEY_IS_POWER_SAVER] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun addStars(newStars: Int) {
        dataStore.edit {
            val current = it[KEY_TOTAL_STARS] ?: 0
            it[KEY_TOTAL_STARS] = current + newStars
        }
    }

    suspend fun clearUserSession() {
        dataStore.edit {
            it[KEY_USER_ID] = ""
            it[KEY_IS_GUEST] = true
            it[KEY_IS_DEMO] = false
        }
    }
}
