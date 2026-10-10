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
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.zevira.app.ui.theme.MintGreen
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextLightPrimary
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var termsAgreed by remember { mutableStateOf(false) }

    val prefs = remember { UserPreferencesDataStore(context) }
    val db = remember { ZeviraDatabase.getInstance(context) }

    // Validation rules
    val isUsernameValid = username.matches(Regex("^[a-z0-9._]{3,20}$"))
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val isPasswordLengthValid = password.length >= 8
    val isPasswordStrong = hasUpper && hasLower && hasDigit && isPasswordLengthValid
    val isPasswordMatching = password.isNotEmpty() && password == confirmPassword

    val isFormValid = isUsernameValid && isPasswordLengthValid && isPasswordMatching && termsAgreed

    // Password strength score (0f to 1f)
    var strengthScore = 0f
    if (password.length >= 6) strengthScore += 0.25f
    if (password.length >= 8) strengthScore += 0.25f
    if (hasUpper && hasLower) strengthScore += 0.25f
    if (hasDigit) strengthScore += 0.25f

    val strengthColor = when {
        strengthScore >= 0.75f -> MintGreen
        strengthScore >= 0.5f -> StarYellow
        else -> CoralRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceTint)
            .verticalScroll(scrollState)
    ) {
        // 1. StarSky Hero Header
        StarSky(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotJejak(size = 85.dp, state = MascotState.WALKING)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Daftar Akun Penjelajah 🚀",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextLightPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mulai petualanganmu dan simpan jejak perjalananmu di cloud",
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
                        text = "Registrasi Anggota Baru",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Lengkapi data diri kamu untuk mulai petualangan",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Full Name / Display Name
                    Text(
                        text = "Nama Panggilan / Lengkap",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Contoh: Rizky Pratama",
                                color = TextSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Badge,
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Username
                    Text(
                        text = "Username",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.lowercase().trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Contoh: rizky123",
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
                    if (username.isNotEmpty() && !isUsernameValid) {
                        Text(
                            text = "3-20 karakter, huruf kecil, angka, titik, atau underscore",
                            color = CoralRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Email (Opsional)
                    Text(
                        text = "Alamat Email",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Contoh: rizky@email.com",
                                color = TextSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Password
                    Text(
                        text = "Kata Sandi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Minimal 8 karakter...",
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

                    // Strength Meter
                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { strengthScore },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp),
                            color = strengthColor,
                            trackColor = Color(0xFFE2EBF5)
                        )
                        Text(
                            text = if (isPasswordStrong) "Kata sandi kuat ✅" else "Gunakan minimal 8 karakter dengan huruf dan angka",
                            fontSize = 11.sp,
                            color = if (isPasswordStrong) MintGreen else TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Confirm Password
                    Text(
                        text = "Konfirmasi Kata Sandi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Ulangi kata sandi...",
                                color = TextSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
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
                    if (confirmPassword.isNotEmpty() && !isPasswordMatching) {
                        Text(
                            text = "Konfirmasi kata sandi tidak cocok",
                            color = CoralRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terms Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAgreed,
                            onCheckedChange = { termsAgreed = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElectricBlue)
                        )
                        Column {
                            Text(
                                text = "Saya menyetujui ",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Row {
                                Text(
                                    text = "Syarat & Ketentuan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue,
                                    modifier = Modifier.clickable { onNavigateToTerms() }
                                )
                                Text(text = " dan ", fontSize = 12.sp, color = TextPrimary)
                                Text(
                                    text = "Privasi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue,
                                    modifier = Modifier.clickable { onNavigateToPrivacy() }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Register Button
                    CartoonButton(
                        onClick = {
                            if (isFormValid) {
                                coroutineScope.launch {
                                    val previousDemoUid = prefs.userIdFlow.first()
                                    val trimmedUser = username.lowercase().trim()
                                    val newMemberUid = "member_$trimmedUser"
                                    val resolvedDisplayName = fullName.trim().ifEmpty { trimmedUser.replaceFirstChar { it.uppercase() } }

                                    withContext(Dispatchers.IO) {
                                        if (previousDemoUid.isNotEmpty()) {
                                            db.tripDao().migrateDemoTripsToMember(previousDemoUid, newMemberUid)
                                        }
                                    }

                                    prefs.setUserId(newMemberUid)
                                    prefs.setUserProfile(displayName = resolvedDisplayName, username = trimmedUser)
                                    prefs.setUserStatus(isGuest = false, isDemo = false)
                                    onRegisterSuccess()
                                }
                            }
                        },
                        text = "Buat Akun Zevira",
                        backgroundColor = ElectricBlue,
                        contentColor = SurfaceWhite,
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.PersonAdd,
                                contentDescription = null,
                                tint = SurfaceWhite
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to Login Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sudah memiliki akun? ",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Masuk di sini",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricBlue,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}
