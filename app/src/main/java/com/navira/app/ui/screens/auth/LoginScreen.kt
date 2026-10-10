package com.navira.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navira.app.R
import com.navira.app.data.local.db.NaviraDatabase
import com.navira.app.data.local.preferences.UserPreferencesDataStore
import com.navira.app.ui.components.CartoonButton
import com.navira.app.ui.components.CartoonCard
import com.navira.app.ui.components.MascotJejak
import com.navira.app.ui.components.MascotState
import com.navira.app.ui.theme.CoralRed
import com.navira.app.ui.theme.ElectricBlue
import com.navira.app.ui.theme.StarYellow
import com.navira.app.ui.theme.SurfaceTint
import com.navira.app.ui.theme.SurfaceWhite
import com.navira.app.ui.theme.TextPrimary
import com.navira.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var lockoutSeconds by remember { mutableIntStateOf(0) }

    val prefs = remember { UserPreferencesDataStore(context) }
    val db = remember {
        androidx.room.Room.databaseBuilder(
            context,
            NaviraDatabase::class.java,
            "navira_database.db"
        ).build()
    }

    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000L)
            lockoutSeconds--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceTint)
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))
            MascotJejak(size = 90.dp, state = MascotState.IDLE)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.login_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Masuk untuk menyinkronkan jejak petualanganmu",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Username input
            CartoonCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.label_username),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Person, contentDescription = null, tint = ElectricBlue)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Password input
            CartoonCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.label_password),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, tint = ElectricBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            // Error display / Rate limiting
            if (lockoutSeconds > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.error_rate_limited, lockoutSeconds),
                    color = CoralRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = CoralRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            CartoonButton(
                onClick = {
                    if (lockoutSeconds > 0) return@CartoonButton

                    // Validate Form
                    if (username.length >= 4 && password.length >= 6) {
                        coroutineScope.launch {
                            val previousDemoUid = prefs.userIdFlow.first()
                            val memberUid = "member_" + username.lowercase().trim()

                            // Migrate any Demo trips to Member
                            withContext(Dispatchers.IO) {
                                if (previousDemoUid.isNotEmpty()) {
                                    db.tripDao().migrateDemoTripsToMember(previousDemoUid, memberUid)
                                }
                            }

                            prefs.setUserId(memberUid)
                            prefs.setUserStatus(isGuest = false, isDemo = false)
                            onLoginSuccess()
                        }
                    } else {
                        failedAttempts++
                        errorMessage = context.getString(R.string.error_invalid_credentials)
                        if (failedAttempts >= 5) {
                            lockoutSeconds = 30 * (failedAttempts - 4)
                        }
                    }
                },
                text = stringResource(R.string.btn_login),
                backgroundColor = ElectricBlue,
                enabled = lockoutSeconds == 0,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Google Sign-In Button
            CartoonButton(
                onClick = {
                    coroutineScope.launch {
                        val googleUid = "google_user_" + System.currentTimeMillis()
                        val previousDemoUid = prefs.userIdFlow.first()
                        withContext(Dispatchers.IO) {
                            if (previousDemoUid.isNotEmpty()) {
                                db.tripDao().migrateDemoTripsToMember(previousDemoUid, googleUid)
                            }
                        }
                        prefs.setUserId(googleUid)
                        prefs.setUserStatus(isGuest = false, isDemo = false)
                        onLoginSuccess()
                    }
                },
                text = stringResource(R.string.btn_google_signin),
                backgroundColor = SurfaceWhite,
                contentColor = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Register Link
        Row(
            modifier = Modifier.padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Belum punya akun? ",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Text(
                text = "Daftar sekarang",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}
