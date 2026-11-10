package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.DateUtils
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Animated real-time countdown timer card for hackathon deadlines.
 * Uses Jetpack Compose coroutines and animations to tick every second.
 */
@Composable
fun LiveHackathonCountdownTimer(
    competitionName: String,
    deadlineTitle: String,
    deadlineDateStr: String,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null,
    actionButtonText: String = "Open Workspace"
) {
    val targetMillis = remember(deadlineDateStr) {
        DateUtils.getTargetDeadlineMillis(deadlineDateStr) ?: 0L
    }

    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live 1-second countdown ticker
    LaunchedEffect(targetMillis) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val diffMillis = remember(targetMillis, currentTimeMillis) {
        (targetMillis - currentTimeMillis).coerceAtLeast(0L)
    }

    val isExpired = targetMillis > 0L && diffMillis <= 0L

    val days = remember(diffMillis) { TimeUnit.MILLISECONDS.toDays(diffMillis) }
    val hours = remember(diffMillis) { (TimeUnit.MILLISECONDS.toHours(diffMillis) % 24) }
    val minutes = remember(diffMillis) { (TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60) }
    val seconds = remember(diffMillis) { (TimeUnit.MILLISECONDS.toSeconds(diffMillis) % 60) }

    val isUrgent = days == 0L && !isExpired

    // Urgency pulse animation when deadline is within 24 hours
    val infiniteTransition = rememberInfiniteTransition(label = "urgency_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val urgencyColor = when {
        isExpired -> HackTrackSuccess
        days == 0L -> HackTrackDanger
        days in 1..2 -> HackTrackWarning
        else -> HackTrackPrimary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isUrgent) Modifier.scale(pulseScale) else Modifier),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUrgent) 6.dp else 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            urgencyColor.copy(alpha = if (isUrgent) 0.12f * pulseAlpha else 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header with badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(urgencyColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isExpired) Icons.Default.CheckCircle else Icons.Default.Timer,
                                contentDescription = null,
                                tint = urgencyColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Next Priority Deadline",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = urgencyColor
                            )
                            Text(
                                text = competitionName.ifBlank { "Active Hackathon" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Urgency Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = urgencyColor.copy(alpha = if (isUrgent) 0.2f * pulseAlpha else 0.15f)
                    ) {
                        Text(
                            text = when {
                                isExpired -> "DEADLINE REACHED"
                                days == 0L -> "DUE TODAY ⚡"
                                days == 1L -> "DUE TOMORROW"
                                else -> "$days DAYS REMAINING"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = urgencyColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Deadline title & date
                Text(
                    text = "$deadlineTitle • Due $deadlineDateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (isExpired) {
                    // Deadline completed state
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = HackTrackSuccess.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = HackTrackSuccess,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "This deadline has arrived. Review submissions or log your achievements!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HackTrackSuccess
                            )
                        }
                    }
                } else {
                    // 4-Card Digital Flip Clock Display: [DAYS] [HOURS] [MINS] [SECS]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CountdownUnitCard(
                            value = String.format("%02d", days),
                            label = "DAYS",
                            accentColor = urgencyColor,
                            modifier = Modifier.weight(1f)
                        )
                        CountdownUnitCard(
                            value = String.format("%02d", hours),
                            label = "HOURS",
                            accentColor = urgencyColor,
                            modifier = Modifier.weight(1f)
                        )
                        CountdownUnitCard(
                            value = String.format("%02d", minutes),
                            label = "MINS",
                            accentColor = urgencyColor,
                            modifier = Modifier.weight(1f)
                        )
                        CountdownUnitCard(
                            value = String.format("%02d", seconds),
                            label = "SECS",
                            accentColor = urgencyColor,
                            isTicking = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Action button if callback provided
                if (onActionClick != null) {
                    FilledTonalButton(
                        onClick = onActionClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = urgencyColor.copy(alpha = 0.15f),
                            contentColor = urgencyColor
                        )
                    ) {
                        Text(actionButtonText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Digital Time Card with animated ticking for seconds.
 */
@Composable
private fun CountdownUnitCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isTicking: Boolean = false
) {
    // Subtle scale bounce animation on value change
    val scaleAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "unit_scale"
    )

    Surface(
        modifier = modifier.scale(scaleAnim),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace
                ),
                fontWeight = FontWeight.ExtraBold,
                color = if (isTicking) accentColor else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
