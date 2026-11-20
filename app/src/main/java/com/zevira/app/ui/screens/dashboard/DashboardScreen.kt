package com.zevira.app.ui.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zevira.app.R
import com.zevira.app.data.local.entity.TrackingMode
import java.util.Locale

private val BerandaBg = Color(0xFF0A0B0F)
private val BerandaCardTop = Color(0xFF181920)
private val BerandaCardBottom = Color(0xFF131419)
private val BerandaCardBorderTop = Color(0x28FFFFFF)
private val BerandaCardBorderBottom = Color(0x0AFFFFFF)
private val BerandaOrangeBright = Color(0xFFFA9122)
private val BerandaOrangeMid = Color(0xFFF37812)
private val BerandaOrangeDeep = Color(0xFFE35A0E)
private val BerandaTextMuted = Color(0xFF8F92A0)
private val BerandaTextSilver = Color(0xFFC5C7D0)
private val BerandaIconMuted = Color(0xFF7D808D)

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

    val infiniteTransition = rememberInfiniteTransition(label = "goGlowPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "goGlowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BerandaBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            // Section 1: Scenic sunset city skyline header (edge-to-edge behind status bar)
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_beranda_sunset),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.matchParentSize()
                )
                // Top twilight scrim & bottom seamless fade-to-obsidian scrim
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.00f to Color(0xB80B101C),
                                    0.24f to Color(0x400D1220),
                                    0.52f to Color(0x140A0B0F),
                                    0.78f to Color(0xBD0A0B0F),
                                    0.94f to Color(0xF50A0B0F),
                                    1.00f to BerandaBg
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp, bottom = 18.dp)
                ) {
                    // Top Brand Row: "/ ZEVIRA" + "Explore Your Journey" | "★ 0 Bintang >"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ZeviraBrandSlash(
                                    modifier = Modifier.size(width = 22.dp, height = 18.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                color = Color.White,
                                                fontWeight = FontWeight.Black
                                            )
                                        ) {
                                            append("ZEV")
                                        }
                                        withStyle(
                                            SpanStyle(
                                                color = Color(0xFFF37E18),
                                                fontWeight = FontWeight.Black
                                            )
                                        ) {
                                            append("IRA")
                                        }
                                    },
                                    fontSize = 24.sp,
                                    letterSpacing = 0.6.sp,
                                    lineHeight = 26.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Explore Your Journey",
                                color = Color(0xFFB4B7C2),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.2.sp,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        // Star Counter Pill Badge ("★ 0 Bintang >")
                        Row(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x9916171E))
                                .border(
                                    width = 1.dp,
                                    color = Color(0x3DFFFFFF),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable {
                                    if (state.isGuest) {
                                        viewModel.setShowGatewaySheet(true)
                                    }
                                }
                                .padding(start = 11.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFF89B1E),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${state.totalStars} Bintang",
                                color = Color(0xFFF1F2F6),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFA0A3B0),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Open skyline showcase space so golden sun & buildings remain unobstructed
                    Spacer(modifier = Modifier.height(98.dp))

                    // Greeting Section
                    val resolvedName = state.displayName.ifEmpty { state.userName }
                    val greetingText = if (resolvedName.isNotBlank()) {
                        "Halo, $resolvedName!"
                    } else {
                        stringResource(R.string.greeting_day)
                    }
                    Text(
                        text = greetingText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.greeting_sub),
                        color = Color(0xFF9EA1AE),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Section 2: Lower Body with subtle topographic contour backdrop
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                BerandaContourBackdrop(
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Unfinished Trip Recovery Banner (only visible when an active interrupted trip exists)
                    if (state.activeUnfinishedTrip != null) {
                        UnfinishedTripDarkBanner(
                            onResume = onNavigateToLiveTracking,
                            onDiscard = {
                                state.activeUnfinishedTrip?.let {
                                    viewModel.discardUnfinishedTrip(it.id)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 16.dp)
                        )
                    }

                    // Mode Selector Segmented Pill (Jalan-jalan | Olahraga)
                    ModeSelectorPill(
                        selectedMode = state.selectedMode,
                        onSelectMode = { viewModel.setMode(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hero GO Button with outer ring, top-right orange arc & subtle right navigation arrow
                    HeroGoSection(
                        glowAlpha = glowAlpha,
                        onClick = {
                            if (state.isGuest) {
                                viewModel.setShowGatewaySheet(true)
                            } else {
                                onNavigateToLiveTracking()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(216.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick Stats Row (Jarak | Durasi)
                    val formattedDistance = String.format(Locale.US, "%.2f km", state.todayDistanceKm)
                    val totalMins = state.todayDurationMinutes
                    val hours = totalMins / 60
                    val mins = totalMins % 60
                    val formattedDuration = String.format(Locale.US, "%02d:%02d", hours, mins)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatMetricCard(
                            label = stringResource(R.string.stat_distance),
                            value = formattedDistance,
                            valueColor = if (state.todayDistanceKm > 0.0) Color.White else BerandaTextSilver,
                            valueWeight = FontWeight.SemiBold,
                            icon = {
                                HighwayRoadIcon(modifier = Modifier.size(30.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        StatMetricCard(
                            label = stringResource(R.string.stat_duration),
                            value = formattedDuration,
                            valueColor = Color.White,
                            valueWeight = FontWeight.Bold,
                            icon = {
                                StopwatchOutlineIcon(modifier = Modifier.size(30.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Kompas Cepat Action Card
                    QuickCompassCard(
                        onClick = onNavigateToCompass,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }

    // Dark-themed Login Gateway Bottom Sheet for Guests
    if (state.showGatewaySheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setShowGatewaySheet(false) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF14151B),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(BerandaOrangeBright, BerandaOrangeDeep)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.DirectionsRun,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.gateway_sheet_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.gateway_sheet_desc),
                    fontSize = 13.5.sp,
                    color = BerandaTextMuted,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(BerandaOrangeBright, BerandaOrangeDeep)
                            )
                        )
                        .clickable {
                            viewModel.setShowGatewaySheet(false)
                            onNavigateToLogin()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.btn_login_register),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color(0xFF1D1F28))
                        .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(25.dp))
                        .clickable {
                            viewModel.activateDemoSession()
                            onNavigateToLiveTracking()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.btn_try_demo),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.gateway_demo_warning),
                    fontSize = 11.5.sp,
                    color = BerandaTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ZeviraBrandSlash(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.60f, h * 0.04f)
            lineTo(w * 0.96f, h * 0.04f)
            lineTo(w * 0.42f, h * 0.96f)
            lineTo(w * 0.04f, h * 0.96f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFA9424), Color(0xFFE76110)),
                start = Offset(w, 0f),
                end = Offset(0f, h)
            )
        )
    }
}

@Composable
private fun ModeSelectorPill(
    selectedMode: TrackingMode,
    onSelectMode: (TrackingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isWalk = selectedMode == TrackingMode.WALK
    val isSport = selectedMode == TrackingMode.SPORT

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFF14151A))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x26FFFFFF), Color(0x0DFFFFFF))
                ),
                shape = RoundedCornerShape(25.dp)
            )
            .padding(3.5.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            ModeSegmentItem(
                label = stringResource(R.string.mode_walk),
                selected = isWalk,
                onClick = { onSelectMode(TrackingMode.WALK) },
                icon = { tint ->
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            ModeSegmentItem(
                label = stringResource(R.string.mode_sport),
                selected = isSport,
                onClick = { onSelectMode(TrackingMode.SPORT) },
                icon = { tint ->
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.DirectionsRun,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ModeSegmentItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF9EA1AE),
        animationSpec = tween(220),
        label = "modeSegmentContent"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF8C8F9C),
        animationSpec = tween(220),
        label = "modeSegmentIcon"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (selected) {
                    Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFA8E1C),
                                    Color(0xFFF37812),
                                    Color(0xFFE7620F)
                                )
                            )
                        )
                        .border(
                            width = 0.8.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color(0x70FFE0A3), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon(iconColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeroGoSection(
    glowAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(120),
        label = "goPressScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Faint navigation paper-plane arrow on right side (matching reference backdrop)
        NavigationPaperPlaneIcon(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 26.dp)
                .size(26.dp)
        )

        // Ambient warm halo + outer orbital track ring + top-right active orange arc
        Canvas(
            modifier = Modifier.size(224.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Soft orange ambient radial glow behind the GO circle
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x4DF57B14).copy(alpha = 0.48f * glowAlpha),
                        0.52f to Color(0x2EE86510).copy(alpha = 0.28f * glowAlpha),
                        0.76f to Color(0x12C64C09).copy(alpha = 0.12f * glowAlpha),
                        1.00f to Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.50f
                ),
                radius = size.minDimension * 0.50f,
                center = center
            )

            // Outer subtle dark orbital ring
            val ringRadius = 99.dp.toPx()
            val ringTopLeft = Offset(center.x - ringRadius, center.y - ringRadius)
            val ringSize = Size(ringRadius * 2f, ringRadius * 2f)

            drawCircle(
                color = Color(0xFF282220),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 1.6.dp.toPx())
            )

            // Subtle dark inner recessed shadow halo between outer ring and orange button
            drawCircle(
                color = Color(0x66000000),
                radius = 85.dp.toPx(),
                center = center,
                style = Stroke(width = 6.dp.toPx())
            )

            // Top-right bright orange progress accent arc (12 o'clock to ~1:45)
            drawArc(
                brush = Brush.sweepGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFFE56713),
                        0.75f to Color(0xFFFFA938),
                        0.88f to Color(0xFFF47E16),
                        1.00f to Color(0xFFE56713)
                    ),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = 54f,
                useCenter = false,
                topLeft = ringTopLeft,
                size = ringSize,
                style = Stroke(
                    width = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Main glossy sunset-orange circular GO button
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(pressScale)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF9C26),
                            Color(0xFFF37A14),
                            Color(0xFFE0560C)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0x8CFFCF82),
                            0.30f to Color(0x38FFB24A),
                            0.72f to Color(0x10FFFFFF),
                            1.00f to Color(0x33000000)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.DirectionsRun,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = stringResource(R.string.btn_start_tracking),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp,
                    letterSpacing = 0.8.sp,
                    lineHeight = 42.sp
                )
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    label: String,
    value: String,
    valueColor: Color,
    valueWeight: FontWeight,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(BerandaCardTop, BerandaCardBottom)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(BerandaCardBorderTop, BerandaCardBorderBottom)
                ),
                shape = cardShape
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    color = BerandaTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = value,
                    color = valueColor,
                    fontSize = 18.5.sp,
                    fontWeight = valueWeight,
                    letterSpacing = 0.2.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun QuickCompassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(BerandaCardTop, BerandaCardBottom)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(BerandaCardBorderTop, BerandaCardBorderBottom)
                ),
                shape = cardShape
            )
            .clickable(onClick = onClick)
    ) {
        // Subtle right-side dark topographic contour waves matching the photo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val contourColor = Color.White.copy(alpha = 0.032f)
            val stroke = Stroke(width = 1.dp.toPx())

            for (i in 0..4) {
                val offset = i * 16.dp.toPx()
                val path = Path().apply {
                    moveTo(w * 0.50f + offset * 0.4f, -10f)
                    cubicTo(
                        w * 0.62f + offset * 0.25f, h * 0.35f,
                        w * 0.72f + offset * 0.3f, h * 0.65f,
                        w * 0.64f + offset * 0.55f, h + 10f
                    )
                }
                drawPath(path = path, color = contourColor, style = stroke)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Orange glowing compass badge with dark compass icon inside
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(56.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x40F57E14),
                                Color.Transparent
                            )
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BerandaOrangeBright, BerandaOrangeDeep)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CompassDialDarkIcon(modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.quick_compass),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Buka kompas realtime",
                    color = BerandaTextMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right orange circular chevron button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BerandaOrangeBright, BerandaOrangeDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun UnfinishedTripDarkBanner(
    onResume: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF1A1614))
            .border(1.dp, Color(0x4DF37812), shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Trip belum selesai",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(BerandaOrangeBright, BerandaOrangeDeep)
                        )
                    )
                    .clickable(onClick = onResume)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.btn_restore_yes),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                )
            }
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF23252F))
                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(15.dp))
                    .clickable(onClick = onDiscard)
                    .padding(horizontal = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.btn_restore_discard),
                    color = BerandaTextSilver,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

@Composable
private fun HighwayRoadIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Left perspective road trapezoid wing
        val leftRoad = Path().apply {
            moveTo(w * 0.34f, h * 0.14f)
            lineTo(w * 0.45f, h * 0.14f)
            lineTo(w * 0.44f, h * 0.88f)
            lineTo(w * 0.10f, h * 0.88f)
            close()
        }
        // Right perspective road trapezoid wing
        val rightRoad = Path().apply {
            moveTo(w * 0.55f, h * 0.14f)
            lineTo(w * 0.66f, h * 0.14f)
            lineTo(w * 0.90f, h * 0.88f)
            lineTo(w * 0.56f, h * 0.88f)
            close()
        }
        drawPath(path = leftRoad, color = BerandaIconMuted)
        drawPath(path = rightRoad, color = BerandaIconMuted)

        // Center road divider dashes (3 perspective segments)
        val dash1 = Path().apply {
            moveTo(w * 0.485f, h * 0.16f)
            lineTo(w * 0.515f, h * 0.16f)
            lineTo(w * 0.52f, h * 0.31f)
            lineTo(w * 0.48f, h * 0.31f)
            close()
        }
        val dash2 = Path().apply {
            moveTo(w * 0.478f, h * 0.42f)
            lineTo(w * 0.522f, h * 0.42f)
            lineTo(w * 0.528f, h * 0.60f)
            lineTo(w * 0.472f, h * 0.60f)
            close()
        }
        val dash3 = Path().apply {
            moveTo(w * 0.470f, h * 0.71f)
            lineTo(w * 0.530f, h * 0.71f)
            lineTo(w * 0.538f, h * 0.88f)
            lineTo(w * 0.462f, h * 0.88f)
            close()
        }
        drawPath(path = dash1, color = BerandaIconMuted)
        drawPath(path = dash2, color = BerandaIconMuted)
        drawPath(path = dash3, color = BerandaIconMuted)
    }
}

@Composable
private fun StopwatchOutlineIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.3.dp.toPx()
        val radius = w * 0.36f
        val center = Offset(w * 0.50f, h * 0.57f)

        // Main stopwatch circle
        drawCircle(
            color = BerandaIconMuted,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Top pusher stem & cap bar
        drawLine(
            color = BerandaIconMuted,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, h * 0.12f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = BerandaIconMuted,
            start = Offset(w * 0.38f, h * 0.12f),
            end = Offset(w * 0.62f, h * 0.12f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Top-right angled stopwatch button nub
        drawLine(
            color = BerandaIconMuted,
            start = Offset(center.x + radius * 0.68f, center.y - radius * 0.68f),
            end = Offset(center.x + radius * 0.90f, center.y - radius * 0.90f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Clock hands (12 o'clock and 3 o'clock)
        drawLine(
            color = BerandaIconMuted,
            start = center,
            end = Offset(center.x, center.y - radius * 0.52f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = BerandaIconMuted,
            start = center,
            end = Offset(center.x + radius * 0.42f, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun NavigationPaperPlaneIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        rotate(degrees = 10f) {
            val path = Path().apply {
                moveTo(w * 0.88f, h * 0.12f)
                lineTo(w * 0.10f, h * 0.46f)
                lineTo(w * 0.45f, h * 0.56f)
                lineTo(w * 0.56f, h * 0.90f)
                close()
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.22f),
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            drawLine(
                color = Color.White.copy(alpha = 0.18f),
                start = Offset(w * 0.86f, h * 0.14f),
                end = Offset(w * 0.45f, h * 0.56f),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun CompassDialDarkIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension * 0.42f
        val darkInk = Color(0xFF151418)

        // Crisp dark compass bezel ring
        drawCircle(
            color = darkInk,
            radius = outerRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Diagonal diamond compass needle pointing NE / SW
        rotate(degrees = 45f, pivot = center) {
            val needleLen = outerRadius * 0.68f
            val needleHalfW = outerRadius * 0.28f
            val needlePath = Path().apply {
                moveTo(center.x, center.y - needleLen)
                lineTo(center.x + needleHalfW, center.y)
                lineTo(center.x, center.y + needleLen)
                lineTo(center.x - needleHalfW, center.y)
                close()
            }
            drawPath(path = needlePath, color = darkInk)
        }

        // Tiny orange center pivot dot
        drawCircle(
            color = BerandaOrangeMid,
            radius = 1.8.dp.toPx(),
            center = center
        )
    }
}

@Composable
private fun BerandaContourBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val lineColor = Color.White.copy(alpha = 0.032f)
        val stroke = Stroke(width = 1.1.dp.toPx())

        // Left sweeping contour lines around the GO button zone
        val leftCurve1 = Path().apply {
            moveTo(-20f, h * 0.16f)
            cubicTo(w * 0.20f, h * 0.22f, w * 0.25f, h * 0.40f, -15f, h * 0.54f)
        }
        val leftCurve2 = Path().apply {
            moveTo(-20f, h * 0.10f)
            cubicTo(w * 0.30f, h * 0.18f, w * 0.32f, h * 0.46f, -15f, h * 0.64f)
        }
        drawPath(leftCurve1, lineColor, style = stroke)
        drawPath(leftCurve2, lineColor, style = stroke)

        // Right sweeping contour lines behind the navigation arrow
        val rightCurve1 = Path().apply {
            moveTo(w + 20f, h * 0.14f)
            cubicTo(w * 0.78f, h * 0.20f, w * 0.74f, h * 0.42f, w + 20f, h * 0.56f)
        }
        val rightCurve2 = Path().apply {
            moveTo(w + 20f, h * 0.08f)
            cubicTo(w * 0.68f, h * 0.16f, w * 0.70f, h * 0.48f, w + 20f, h * 0.66f)
        }
        drawPath(rightCurve1, lineColor, style = stroke)
        drawPath(rightCurve2, lineColor, style = stroke)
    }
}
