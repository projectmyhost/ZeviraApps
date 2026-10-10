package com.navira.app.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.navira.app.ui.theme.MintGreen
import com.navira.app.ui.theme.StarYellow
import com.navira.app.ui.theme.SurfaceTint
import com.navira.app.ui.theme.SurfaceWhite
import com.navira.app.ui.theme.TextPrimary
import com.navira.app.ui.theme.TextSecondary
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

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAgreed by remember { mutableStateOf(false) }

    val prefs = remember { UserPreferencesDataStore(context) }
    val db = remember {
        androidx.room.Room.databaseBuilder(
            context,
            NaviraDatabase::class.java,
            "navira_database.db"
        ).build()
    }

    // Validation rules (Section 10.2)
    val isUsernameValid = username.matches(Regex("^[a-z0-9._]{4,20}$"))
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val isPasswordLengthValid = password.length >= 10
    val isPasswordStrong = hasUpper && hasLower && hasDigit && isPasswordLengthValid
    val isPasswordMatching = password.isNotEmpty() && password == confirmPassword

    val isFormValid = isUsernameValid && isPasswordStrong && isPasswordMatching && termsAgreed

    // Password strength score (0f to 1f)
    var strengthScore = 0f
    if (password.length >= 8) strengthScore += 0.25f
    if (password.length >= 10) strengthScore += 0.25f
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
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            MascotJejak(size = 85.dp, state = MascotState.WALKING)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.register_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Username input
            CartoonCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.label_username),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.lowercase().trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(imageVector = Icons.Rounded.Person, contentDescription = null, tint = ElectricBlue) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    if (username.isNotEmpty() && !isUsernameValid) {
                        Text(
                            text = "4-20 karakter, huruf kecil, angka, titik, atau underscore",
                            color = CoralRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password input
            CartoonCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.label_password),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, tint = ElectricBlue) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    // Strength Meter
                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { strengthScore },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = strengthColor,
                            trackColor = Color.LightGray
                        )
                        Text(
                            text = if (isPasswordStrong) "Kata sandi kuat" else "Minimal 10 karakter dengan huruf besar, kecil, dan angka",
                            fontSize = 11.sp,
                            color = if (isPasswordStrong) MintGreen else CoralRed,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password input
            CartoonCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.label_confirm_password),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, tint = ElectricBlue) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    if (confirmPassword.isNotEmpty() && !isPasswordMatching) {
                        Text(
                            text = "Konfirmasi kata sandi tidak cocok",
                            color = CoralRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Terms Agreement Checkbox
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
                            text = "Kebijakan Privasi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            modifier = Modifier.clickable { onNavigateToPrivacy() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Register Button (Disabled until valid & agreed)
            CartoonButton(
                onClick = {
                    if (isFormValid) {
                        coroutineScope.launch {
                            val previousDemoUid = prefs.userIdFlow.first()
                            val newMemberUid = "member_$username"

                            withContext(Dispatchers.IO) {
                                if (previousDemoUid.isNotEmpty()) {
                                    db.tripDao().migrateDemoTripsToMember(previousDemoUid, newMemberUid)
                                }
                            }

                            prefs.setUserId(newMemberUid)
                            prefs.setUserStatus(isGuest = false, isDemo = false)
                            onRegisterSuccess()
                        }
                    }
                },
                text = stringResource(R.string.btn_register),
                backgroundColor = ElectricBlue,
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Back to Login Link
        Row(
            modifier = Modifier.padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sudah punya akun? ",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Text(
                text = "Masuk di sini",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}
