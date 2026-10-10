package com.zevira.app.ui.screens.auth

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.local.db.ZeviraDatabase
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import com.zevira.app.ui.components.CartoonButton
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import com.zevira.app.ui.components.StarSky
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextLightPrimary
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
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
    val db = remember { ZeviraDatabase.getInstance(context) }

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
            .verticalScroll(scrollState)
    ) {
        // 1. Sleek StarSky Hero Header with Mascot
        StarSky(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotJejak(size = 85.dp, state = MascotState.IDLE)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Selamat Datang Kembali! 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextLightPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Masuk untuk melanjutkan dan menyinkronkan jejakmu",
                    fontSize = 13.sp,
                    color = Color(0xFFA8BCE3)
                )
            }
        }

        // 2. Unified Modern Form Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            CartoonCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Masuk ke Akun",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Gunakan username dan kata sandi yang telah kamu daftarkan",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Username Field
                    Text(
                        text = "Username",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Masukkan username kamu...",
                                color = TextSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                        },
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color(0xFFF7FAFE),
                            unfocusedContainerColor = Color(0xFFF7FAFE),
                            cursorColor = ElectricBlue,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0xFFD6E2F2)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text(
                        text = "Kata Sandi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Masukkan kata sandi...",
                                color = TextSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
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
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color(0xFFF7FAFE),
                            unfocusedContainerColor = Color(0xFFF7FAFE),
                            cursorColor = ElectricBlue,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0xFFD6E2F2)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Error Alert Banner
                    if (lockoutSeconds > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CoralRed.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .border(1.dp, CoralRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = CoralRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.padding(start = 8.dp))
                                Text(
                                    text = stringResource(R.string.error_rate_limited, lockoutSeconds),
                                    color = CoralRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CoralRed.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .border(1.dp, CoralRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = CoralRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.padding(start = 8.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = CoralRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Login Button
                    CartoonButton(
                        onClick = {
                            if (lockoutSeconds > 0) return@CartoonButton

                            val trimmedUser = username.lowercase().trim()
                            if (trimmedUser.length >= 4 && password.length >= 6) {
                                coroutineScope.launch {
                                    val previousDemoUid = prefs.userIdFlow.first()
                                    val memberUid = "member_$trimmedUser"

                                    // Migrate any previous demo trips to this member
                                    withContext(Dispatchers.IO) {
                                        if (previousDemoUid.isNotEmpty()) {
                                            db.tripDao().migrateDemoTripsToMember(previousDemoUid, memberUid)
                                        }
                                    }

                                    prefs.setUserId(memberUid)
                                    prefs.setUserProfile(displayName = trimmedUser.replaceFirstChar { it.uppercase() }, username = trimmedUser)
                                    prefs.setUserStatus(isGuest = false, isDemo = false)
                                    onLoginSuccess()
                                }
                            } else {
                                failedAttempts++
                                errorMessage = "Username minimal 4 karakter dan kata sandi minimal 6 karakter."
                                if (failedAttempts >= 5) {
                                    lockoutSeconds = 30 * (failedAttempts - 4)
                                }
                            }
                        },
                        text = "Masuk Sekarang",
                        backgroundColor = ElectricBlue,
                        contentColor = SurfaceWhite,
                        enabled = lockoutSeconds == 0,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Login,
                                contentDescription = null,
                                tint = SurfaceWhite
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color(0xFFE5EEFA), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(18.dp))

                    // Guest Mode Button
                    CartoonButton(
                        onClick = {
                            coroutineScope.launch {
                                val guestUid = "guest_" + System.currentTimeMillis()
                                prefs.setUserId(guestUid)
                                prefs.setUserProfile(displayName = "Tamu", username = "tamu")
                                prefs.setUserStatus(isGuest = true, isDemo = false)
                                onLoginSuccess()
                            }
                        },
                        text = "Jelajah sebagai Tamu (Offline)",
                        backgroundColor = Color(0xFFF7FAFE),
                        contentColor = TextPrimary,
                        borderColor = Color(0xFFCAD4E0),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch to Register Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Belum punya akun Zevira? ",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Daftar di sini",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricBlue,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}
