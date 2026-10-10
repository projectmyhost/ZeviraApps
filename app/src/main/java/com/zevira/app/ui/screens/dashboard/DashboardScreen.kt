package com.zevira.app.ui.screens.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zevira.app.R
import com.zevira.app.data.local.entity.TrackingMode
import com.zevira.app.ui.components.CartoonButton
import com.zevira.app.ui.components.CartoonCard
import com.zevira.app.ui.components.CartoonChip
import com.zevira.app.ui.components.MascotJejak
import com.zevira.app.ui.components.MascotState
import com.zevira.app.ui.components.StarSky
import com.zevira.app.ui.theme.CartoonCardShape
import com.zevira.app.ui.theme.CoralRed
import com.zevira.app.ui.theme.ElectricBlue
import com.zevira.app.ui.theme.MintGreen
import com.zevira.app.ui.theme.PillShape
import com.zevira.app.ui.theme.StarYellow
import com.zevira.app.ui.theme.StickerBorder
import com.zevira.app.ui.theme.SurfaceTint
import com.zevira.app.ui.theme.SurfaceWhite
import com.zevira.app.ui.theme.TextLightPrimary
import com.zevira.app.ui.theme.TextPrimary
import com.zevira.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToLiveTracking: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Gentle pulse animation for GO button
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "goScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceTint)
            .verticalScroll(scrollState)
    ) {
        // 1. StarSky Header with Greeting & Mascot
        StarSky(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top row with stars counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ZEVIRA",
                        fontWeight = FontWeight.ExtraBold,
                        color = StarYellow,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp
                    )

                    // Bintang Perjalanan badge
                    Row(
                        modifier = Modifier
                            .background(Color(0x33000000), PillShape)
                            .border(1.dp, StarYellow, PillShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = StarYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.totalStars} Bintang",
                            color = SurfaceWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Greeting & Mascot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.greeting_day),
                            color = TextLightPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.greeting_sub),
                            color = Color(0xFFA8BCE3),
                            fontSize = 13.sp
                        )
                    }

                    MascotJejak(
                        size = 72.dp,
                        state = MascotState.IDLE
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Unfinished Trip Recovery Alert
            if (state.activeUnfinishedTrip != null) {
                CartoonCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = Color(0xFFFFF9E6),
                    borderColor = StarYellow
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(R.string.restored_trip_banner),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CartoonButton(
                                onClick = onNavigateToLiveTracking,
                                text = stringResource(R.string.btn_restore_yes),
                                backgroundColor = MintGreen,
                                contentColor = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            CartoonButton(
                                onClick = {
                                    state.activeUnfinishedTrip?.let {
                                        viewModel.discardUnfinishedTrip(it.id)
                                    }
                                },
                                text = stringResource(R.string.btn_restore_discard),
                                backgroundColor = CoralRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // User Status Banners (Guest vs Demo)
            if (state.isGuest) {
                CartoonCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = SurfaceWhite
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.banner_guest_title),
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.banner_guest_desc),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CartoonButton(
                                    onClick = onNavigateToLogin,
                                    text = "Login / Daftar",
                                    backgroundColor = ElectricBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                CartoonButton(
                                    onClick = { viewModel.activateDemoSession() },
                                    text = "Mode Demo",
                                    backgroundColor = StarYellow,
                                    contentColor = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            } else if (state.isDemo) {
                CartoonCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = Color(0xFFFFF0F0),
                    borderColor = CoralRed
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.demo_mode_banner),
                                color = CoralRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        CartoonButton(
                            onClick = onNavigateToLogin,
                            text = stringResource(R.string.btn_upgrade_account),
                            backgroundColor = ElectricBlue,
                            contentColor = SurfaceWhite
                        )
                    }
                }
            }

            // Mode Selector Toggle (Jalan-jalan vs Olahraga)
            CartoonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                backgroundColor = SurfaceWhite
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isWalk = state.selectedMode == TrackingMode.WALK
                    val isSport = state.selectedMode == TrackingMode.SPORT

                    // Walk Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isWalk) ElectricBlue else Color.Transparent,
                                PillShape
                            )
                            .border(
                                if (isWalk) 2.dp else 1.dp,
                                if (isWalk) StickerBorder else Color.LightGray,
                                PillShape
                            )
                            .clickable { viewModel.setMode(TrackingMode.WALK) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsWalk,
                                contentDescription = null,
                                tint = if (isWalk) SurfaceWhite else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.mode_walk),
                                fontWeight = FontWeight.Bold,
                                color = if (isWalk) SurfaceWhite else TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Sport Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSport) StarYellow else Color.Transparent,
                                PillShape
                            )
                            .border(
                                if (isSport) 2.dp else 1.dp,
                                if (isSport) StickerBorder else Color.LightGray,
                                PillShape
                            )
                            .clickable { viewModel.setMode(TrackingMode.SPORT) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsRun,
                                contentDescription = null,
                                tint = if (isSport) TextPrimary else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.mode_sport),
                                fontWeight = FontWeight.Bold,
                                color = if (isSport) TextPrimary else TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Big Pulsing GO Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse halo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .background(ElectricBlue.copy(alpha = 0.15f), CircleShape)
                )

                // GO Button Sticker
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .offset(y = 4.dp)
                        .background(StickerBorder, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .background(ElectricBlue, CircleShape)
                        .border(3.dp, StickerBorder, CircleShape)
                        .clickable {
                            if (state.isGuest) {
                                viewModel.setShowGatewaySheet(true)
                            } else {
                                onNavigateToLiveTracking()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.btn_start_tracking),
                        fontWeight = FontWeight.ExtraBold,
                        color = SurfaceWhite,
                        fontSize = 42.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CartoonCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(R.string.stat_distance),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "0.00 km",
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    }
                }

                CartoonCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(R.string.stat_duration),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "00:00",
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Compass Card
            CartoonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCompass() },
                backgroundColor = SurfaceWhite
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SurfaceTint, CircleShape)
                                .border(1.5.dp, StickerBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Explore,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.quick_compass),
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Buka kompas realtime",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "→",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }

    // Login Gateway Bottom Sheet
    if (state.showGatewaySheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setShowGatewaySheet(false) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MascotJejak(size = 90.dp, state = MascotState.IDLE)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.gateway_sheet_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.gateway_sheet_desc),
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                CartoonButton(
                    onClick = {
                        viewModel.setShowGatewaySheet(false)
                        onNavigateToLogin()
                    },
                    text = stringResource(R.string.btn_login_register),
                    backgroundColor = ElectricBlue,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                CartoonButton(
                    onClick = {
                        viewModel.activateDemoSession()
                        onNavigateToLiveTracking()
                    },
                    text = stringResource(R.string.btn_try_demo),
                    backgroundColor = StarYellow,
                    contentColor = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.gateway_demo_warning),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
