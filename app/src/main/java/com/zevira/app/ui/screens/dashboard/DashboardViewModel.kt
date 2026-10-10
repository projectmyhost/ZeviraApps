package com.zevira.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zevira.app.data.local.db.TripDao
import com.zevira.app.data.local.entity.TrackingMode
import com.zevira.app.data.local.entity.TripEntity
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DashboardUiState(
    val isGuest: Boolean = true,
    val isDemo: Boolean = false,
    val userId: String = "",
    val userName: String = "",
    val displayName: String = "",
    val selectedMode: TrackingMode = TrackingMode.WALK,
    val recentTrip: TripEntity? = null,
    val activeUnfinishedTrip: TripEntity? = null,
    val totalStars: Int = 0,
    val todayDistanceKm: Double = 0.0,
    val todayDurationMinutes: Long = 0L,
    val showGatewaySheet: Boolean = false
)

private data class UserAuthInfo(
    val isGuest: Boolean,
    val isDemo: Boolean,
    val userId: String,
    val username: String,
    val displayName: String
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore,
    private val tripDao: TripDao
) : ViewModel() {

    private val _selectedMode = MutableStateFlow(TrackingMode.WALK)
    private val _showGatewaySheet = MutableStateFlow(false)

    private val authInfoFlow = combine(
        preferencesDataStore.isGuestFlow,
        preferencesDataStore.isDemoFlow,
        preferencesDataStore.userIdFlow,
        preferencesDataStore.usernameFlow,
        preferencesDataStore.displayNameFlow
    ) { isGuest, isDemo, userId, username, displayName ->
        UserAuthInfo(isGuest, isDemo, userId, username, displayName)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        authInfoFlow,
        _selectedMode,
        _showGatewaySheet,
        tripDao.observeActiveUnfinishedTrip()
    ) { auth, mode, showSheet, unfinishedTrip ->
        DashboardUiState(
            isGuest = auth.isGuest,
            isDemo = auth.isDemo,
            userId = auth.userId,
            userName = auth.username,
            displayName = auth.displayName,
            selectedMode = mode,
            showGatewaySheet = showSheet,
            activeUnfinishedTrip = unfinishedTrip
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun setMode(mode: TrackingMode) {
        _selectedMode.value = mode
    }

    fun setShowGatewaySheet(show: Boolean) {
        _showGatewaySheet.value = show
    }

    fun activateDemoSession() {
        viewModelScope.launch {
            val demoUid = "demo_" + UUID.randomUUID().toString().substring(0, 8)
            preferencesDataStore.setUserId(demoUid)
            preferencesDataStore.setUserStatus(isGuest = false, isDemo = true)
            _showGatewaySheet.value = false
        }
    }

    fun discardUnfinishedTrip(tripId: String) {
        viewModelScope.launch {
            tripDao.deleteTripById(tripId)
        }
    }
}
