package com.zevira.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.local.db.ZeviraDatabase
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import com.zevira.app.ui.components.CartoonButton
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val prefs = remember { UserPreferencesDataStore(context) }
    val userId by prefs.userIdFlow.collectAsState(initial = "")
    val isGuest by prefs.isGuestFlow.collectAsState(initial = true)
    val isDemo by prefs.isDemoFlow.collectAsState(initial = false)
    val weightKg by prefs.userWeightFlow.collectAsState(initial = 60f)
    val mapStyle by prefs.mapStyleFlow.collectAsState(initial = "real_life")
    val isPowerSaver by prefs.isPowerSaverFlow.collectAsState(initial = false)
    val totalStars by prefs.totalStarsFlow.collectAsState(initial = 0)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf(weightKg.toString()) }

    val db = remember {
        androidx.room.Room.databaseBuilder(
            context,
            ZeviraDatabase::class.java,
            "zevira_database.db"
        ).build()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceTint)
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Avatar Card
        CartoonCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SurfaceWhite
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MascotJejak(size = 75.dp, state = MascotState.IDLE)
                Spacer(modifier = Modifier.padding(8.dp))
                Column {
                    val statusText = when {
                        isGuest -> "Pengguna Tamu"
                        isDemo -> "Akun Demo"
                        else -> "Member Zevira"
                    }
                    Text(
                        text = statusText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = StarYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(
                            text = "$totalStars Bintang Perjalanan",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight Setting for Calories
        CartoonCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SurfaceWhite
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Berat Badan (kg)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Digunakan untuk estimasi kalori mode Olahraga",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            cursorColor = ElectricBlue,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0xFFCAD4E0)
                        )
                    )
                    CartoonButton(
                        onClick = {
                            val parsed = weightInput.toFloatOrNull() ?: 60f
                            coroutineScope.launch { prefs.setUserWeight(parsed) }
                        },
                        text = "Simpan",
                        backgroundColor = ElectricBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Power Saver Mode Toggle
        CartoonCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SurfaceWhite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.setting_power_saver),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.setting_power_saver_desc),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isPowerSaver,
                    onCheckedChange = {
                        coroutineScope.launch { prefs.setPowerSaver(it) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SurfaceWhite,
                        checkedTrackColor = ElectricBlue
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Default Map Style Toggle
        CartoonCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SurfaceWhite
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.setting_map_style),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CartoonButton(
                        onClick = {
                            coroutineScope.launch { prefs.setMapStyle("real_life") }
                        },
                        text = "Real Life (Satelit)",
                        backgroundColor = if (mapStyle == "real_life") ElectricBlue else SurfaceWhite,
                        contentColor = if (mapStyle == "real_life") SurfaceWhite else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    CartoonButton(
                        onClick = {
                            coroutineScope.launch { prefs.setMapStyle("clean_white") }
                        },
                        text = "Putih Polos",
                        backgroundColor = if (mapStyle == "clean_white") ElectricBlue else SurfaceWhite,
                        contentColor = if (mapStyle == "clean_white") SurfaceWhite else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Session Actions (Login/Logout, Delete Account)
        if (isGuest || isDemo) {
            CartoonButton(
                onClick = onNavigateToLogin,
                text = stringResource(R.string.btn_login_register),
                backgroundColor = ElectricBlue,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            CartoonButton(
                onClick = {
                    coroutineScope.launch { prefs.clearUserSession() }
                },
                text = stringResource(R.string.btn_logout),
                backgroundColor = SurfaceWhite,
                contentColor = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Delete Account & All Data
        CartoonButton(
            onClick = { showDeleteConfirmDialog = true },
            text = stringResource(R.string.btn_delete_account),
            backgroundColor = SurfaceWhite,
            contentColor = CoralRed,
            borderColor = CoralRed,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.btn_delete_account),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(text = stringResource(R.string.delete_account_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                db.tripDao().deleteAllTripsForUser(userId)
                            }
                            prefs.clearUserSession()
                            showDeleteConfirmDialog = false
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.btn_confirm_delete), color = CoralRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}
