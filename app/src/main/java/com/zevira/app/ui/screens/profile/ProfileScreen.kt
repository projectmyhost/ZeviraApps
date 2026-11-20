package com.zevira.app.ui.screens.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zevira.app.R
import com.zevira.app.data.local.db.ZeviraDatabase
import com.zevira.app.data.local.preferences.UserPreferencesDataStore
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ScreenBg = Color(0xFF0A0B0F)
private val CardSurface = Color(0xFF14151A)
private val CardBorderTop = Color(0x28FFFFFF)
private val CardBorderBottom = Color(0x0EFFFFFF)
private val OrangeBright = Color(0xFFFA8E1C)
private val OrangeMid = Color(0xFFF37812)
private val OrangeDeep = Color(0xFFE7620F)
private val TextSilver = Color(0xFF9EA1AE)
private val TextMuted = Color(0xFF8C8F9C)
private val CoralRed = Color(0xFFFF5C5C)

@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val prefs = remember { UserPreferencesDataStore(context) }
    val userId by prefs.userIdFlow.collectAsState(initial = "")
    val displayName by prefs.displayNameFlow.collectAsState(initial = "")
    val username by prefs.usernameFlow.collectAsState(initial = "")
    val isGuest by prefs.isGuestFlow.collectAsState(initial = true)
    val isDemo by prefs.isDemoFlow.collectAsState(initial = false)
    val weightKg by prefs.userWeightFlow.collectAsState(initial = 60f)
    val mapStyle by prefs.mapStyleFlow.collectAsState(initial = "real_life")
    val isPowerSaver by prefs.isPowerSaverFlow.collectAsState(initial = false)
    val totalStars by prefs.totalStarsFlow.collectAsState(initial = 0)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var weightInput by remember(weightKg) { mutableStateOf(String.format(java.util.Locale.US, "%.1f", weightKg)) }
    var weightSavedFeedback by remember { mutableStateOf(false) }

    val db = remember { ZeviraDatabase.getInstance(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // Subtle ambient orange glow curved streak in upper-right
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val glowPath = Path().apply {
                moveTo(w + 20f, h * 0.12f)
                cubicTo(w * 0.85f, h * 0.15f, w * 0.70f, h * 0.20f, w * 0.60f, h * 0.24f)
            }
            drawPath(
                path = glowPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color(0x60F37812), Color(0x90FA8E1C))
                ),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Bar: Brand Logo "// ZEVIRA" | Settings Gear Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandDoubleSlash(modifier = Modifier.size(width = 20.dp, height = 16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ZEVIRA",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Settings icon button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF14151B))
                        .border(1.dp, Color(0x28FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Title Section with Vertical Orange Accent Pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OrangeMid)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Profil & Pengaturan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.2).sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Atur preferensi dan kelola akun Anda.",
                color = TextSilver,
                fontSize = 12.5.sp,
                modifier = Modifier.padding(start = 14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Card 1: User Profile Card (Image 3 1:1)
            val profileCardShape = RoundedCornerShape(22.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(profileCardShape)
                    .background(CardSurface)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(CardBorderTop, CardBorderBottom)),
                        shape = profileCardShape
                    )
                    .clickable {
                        if (isGuest) {
                            onNavigateToLogin()
                        }
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mascot Avatar inside glowing orange circle
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0x33F37812), Color.Transparent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF14151B))
                                .border(1.5.dp, OrangeMid, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            MascotJejak(size = 50.dp, state = MascotState.IDLE)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val statusText = when {
                            displayName.isNotBlank() -> displayName
                            username.isNotBlank() -> username
                            isGuest -> "Pengguna Tamu"
                            isDemo -> "Akun Demo"
                            else -> "Member Zevira"
                        }
                        Text(
                            text = statusText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFF89B1E),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$totalStars Bintang Perjalanan",
                                color = TextSilver,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFA0A3B0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Card 2: Berat Badan (kg) Card
            val settingCardShape = RoundedCornerShape(22.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(settingCardShape)
                    .background(CardSurface)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(CardBorderTop, CardBorderBottom)),
                        shape = settingCardShape
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF20222A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FitnessCenter,
                                contentDescription = null,
                                tint = Color(0xFFA0A3B0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Berat Badan (kg)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Digunakan untuk estimasi kalori mode Olahraga",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input Box and Orange Save Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dark input field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF101116))
                                .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                cursorBrush = SolidColor(OrangeMid),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Orange Save Button
                        Box(
                            modifier = Modifier
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(OrangeBright, OrangeDeep))
                                )
                                .clickable {
                                    val parsed = weightInput.replace(',', '.').toFloatOrNull()
                                    if (parsed != null && parsed > 0f) {
                                        coroutineScope.launch {
                                            prefs.setUserWeight(parsed)
                                            weightSavedFeedback = true
                                        }
                                    }
                                }
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Save,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (weightSavedFeedback) "Tersimpan" else "Simpan",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Card 3: Mode Hemat Baterai Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(settingCardShape)
                    .background(CardSurface)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(CardBorderTop, CardBorderBottom)),
                        shape = settingCardShape
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF20222A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BatterySaver,
                                contentDescription = null,
                                tint = Color(0xFF3DDC97),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mode Hemat Baterai",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Matikan animasi bintang & minimalkan pelacakan sensor.",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isPowerSaver,
                        onCheckedChange = { checked ->
                            coroutineScope.launch { prefs.setPowerSaver(checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OrangeMid,
                            uncheckedThumbColor = Color(0xFFA0A3B0),
                            uncheckedTrackColor = Color(0xFF1E2028),
                            uncheckedBorderColor = Color(0x33FFFFFF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Card 4: Gaya Peta Default Card (Dual Selection)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(settingCardShape)
                    .background(CardSurface)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(CardBorderTop, CardBorderBottom)),
                        shape = settingCardShape
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF20222A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Place,
                                contentDescription = null,
                                tint = Color(0xFFA0A3B0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gaya Peta Default",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pilih tampilan peta yang Anda inginkan.",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Options: Real Life (Satelit) vs Putih Polos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isSatellite = mapStyle == "real_life" || mapStyle == "satellite_hybrid"

                        // Satelit Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .then(
                                    if (isSatellite) {
                                        Modifier
                                            .background(
                                                Brush.horizontalGradient(listOf(OrangeBright, OrangeDeep))
                                            )
                                            .border(
                                                0.8.dp,
                                                Brush.verticalGradient(listOf(Color(0x60FFE0A3), Color.Transparent)),
                                                RoundedCornerShape(23.dp)
                                            )
                                    } else {
                                        Modifier
                                            .background(Color(0xFF1E2028))
                                            .border(1.dp, Color(0x1CFFFFFF), RoundedCornerShape(23.dp))
                                    }
                                )
                                .clickable {
                                    coroutineScope.launch { prefs.setMapStyle("real_life") }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Map,
                                    contentDescription = null,
                                    tint = if (isSatellite) Color.White else TextMuted,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Real Life (Satelit)",
                                    color = if (isSatellite) Color.White else TextMuted,
                                    fontWeight = if (isSatellite) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                        // Putih Polos Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .then(
                                    if (!isSatellite) {
                                        Modifier
                                            .background(
                                                Brush.horizontalGradient(listOf(OrangeBright, OrangeDeep))
                                            )
                                            .border(
                                                0.8.dp,
                                                Brush.verticalGradient(listOf(Color(0x60FFE0A3), Color.Transparent)),
                                                RoundedCornerShape(23.dp)
                                            )
                                    } else {
                                        Modifier
                                            .background(Color(0xFF1E2028))
                                            .border(1.dp, Color(0x1CFFFFFF), RoundedCornerShape(23.dp))
                                    }
                                )
                                .clickable {
                                    coroutineScope.launch { prefs.setMapStyle("clean_white") }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Layers,
                                    contentDescription = null,
                                    tint = if (!isSatellite) Color.White else TextMuted,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Putih Polos",
                                    color = if (!isSatellite) Color.White else TextMuted,
                                    fontWeight = if (!isSatellite) FontWeight.SemiBold else FontWeight.Medium,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Card 5: Keluar (Logout) Card
            val actionCardShape = RoundedCornerShape(20.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(actionCardShape)
                    .background(CardSurface)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(CardBorderTop, CardBorderBottom)),
                        shape = actionCardShape
                    )
                    .clickable { showLogoutConfirmDialog = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Keluar",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFA0A3B0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8. Card 6: Hapus Akun & Seluruh Data Card (Dark Crimson Outline)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(actionCardShape)
                    .background(Color(0xFF160A0C))
                    .border(1.2.dp, Color(0xFF6E2020), actionCardShape)
                    .clickable { showDeleteConfirmDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = CoralRed,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Akun & Seluruh Data",
                        color = CoralRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Logout Confirmation Dialog
        if (showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                containerColor = Color(0xFF16171E),
                title = {
                    Text(
                        text = "Keluar dari Akun?",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Text(
                        text = "Apakah kamu yakin ingin keluar dari akun ini?",
                        color = TextSilver,
                        fontSize = 13.5.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                prefs.clearUserSession()
                                onNavigateToLogin()
                            }
                            showLogoutConfirmDialog = false
                        }
                    ) {
                        Text("Keluar", color = OrangeBright, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmDialog = false }) {
                        Text("Batal", color = TextSilver)
                    }
                }
            )
        }

        // Delete Account Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                containerColor = Color(0xFF16171E),
                title = {
                    Text(
                        text = "Hapus Akun & Data?",
                        fontWeight = FontWeight.Bold,
                        color = CoralRed,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.delete_account_confirm),
                        color = TextSilver,
                        fontSize = 13.5.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    db.tripDao().deleteAllTripsForUser(userId.ifEmpty { "guest" })
                                }
                                prefs.clearUserSession()
                                onNavigateToLogin()
                            }
                            showDeleteConfirmDialog = false
                        }
                    ) {
                        Text("Hapus Selamanya", color = CoralRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Batal", color = TextSilver)
                    }
                }
            )
        }
    }
}

@Composable
private fun BrandDoubleSlash(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFA9424), Color(0xFFE76110))
        )

        // Left slash
        val p1 = Path().apply {
            moveTo(w * 0.22f, h * 0.04f)
            lineTo(w * 0.48f, h * 0.04f)
            lineTo(w * 0.26f, h * 0.96f)
            lineTo(w * 0.00f, h * 0.96f)
            close()
        }
        // Right slash
        val p2 = Path().apply {
            moveTo(w * 0.74f, h * 0.04f)
            lineTo(w * 1.00f, h * 0.04f)
            lineTo(w * 0.78f, h * 0.96f)
            lineTo(w * 0.52f, h * 0.96f)
            close()
        }
        drawPath(p1, brush)
        drawPath(p2, brush)
    }
}
