package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.CareerMilestoneBadge
import com.example.util.CareerProgressSummary

/**
 * Career Level & XP progress card showing current Rank, XP meter, and next tier unlock.
 */
@Composable
fun CareerLevelXpCard(
    summary: CareerProgressSummary,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = summary.levelProgressFraction,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "xp_progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            HackTrackPrimary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(HackTrackPrimary, HackTrackSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L${summary.rankLevel}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = summary.rankTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Level ${summary.rankLevel} Innovator • ${summary.totalXp} Total XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEAB308).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${summary.totalXp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                // XP Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Tier Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${summary.currentLevelXp} / ${summary.nextLevelTargetXp} XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(HackTrackPrimary, HackTrackSecondary, HackTrackSuccess)
                                    )
                                )
                        )
                    }
                }

                // 4 Career Statistics Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CareerStatMiniItem(label = "Hacks", value = "${summary.totalHackathons}", icon = Icons.Default.RocketLaunch, color = HackTrackPrimary)
                    CareerStatMiniItem(label = "Tasks", value = "${summary.totalTasksCompleted}", icon = Icons.Default.Checklist, color = Color(0xFF0284C7))
                    CareerStatMiniItem(label = "Shipped", value = "${summary.totalSubmissions}", icon = Icons.Default.CloudDone, color = HackTrackSuccess)
                    CareerStatMiniItem(label = "Wins", value = "${summary.totalWins}", icon = Icons.Default.EmojiEvents, color = Color(0xFFEAB308))
                }
            }
        }
    }
}

@Composable
private fun CareerStatMiniItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = color)
                Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Career Trajectory Area Curve: Smooth Canvas-rendered milestone curve visualizing cumulative growth.
 */
@Composable
fun CareerTrajectoryCurveChart(
    points: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    val maxVal = remember(points) {
        points.maxOfOrNull { it.second }?.takeIf { it > 0 }?.toFloat() ?: 100f
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Career Growth Trajectory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cumulative experience points and participation pace",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HackTrackPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Trajectory",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HackTrackPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Canvas Area Curve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (points.size < 2) return@Canvas

                    val w = size.width
                    val h = size.height - 20f
                    val stepX = w / (points.size - 1)

                    val path = Path()
                    val areaPath = Path()

                    val coordinates = points.mapIndexed { idx, pair ->
                        val fraction = (pair.second.toFloat() / maxVal) * animProgress.value
                        val x = idx * stepX
                        val y = h - (fraction * (h - 20f)).coerceAtLeast(10f)
                        Offset(x, y)
                    }

                    // Build smooth cubic bezier line path
                    coordinates.forEachIndexed { i, pt ->
                        if (i == 0) {
                            path.moveTo(pt.x, pt.y)
                            areaPath.moveTo(pt.x, h)
                            areaPath.lineTo(pt.x, pt.y)
                        } else {
                            val prev = coordinates[i - 1]
                            val cx1 = prev.x + (pt.x - prev.x) / 2f
                            val cy1 = prev.y
                            val cx2 = prev.x + (pt.x - prev.x) / 2f
                            val cy2 = pt.y
                            path.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                            areaPath.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                        }
                    }

                    areaPath.lineTo(w, h)
                    areaPath.close()

                    // Draw gradient area under the curve
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                HackTrackPrimary.copy(alpha = 0.25f),
                                HackTrackPrimary.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Draw main curve stroke
                    drawPath(
                        path = path,
                        color = HackTrackPrimary,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw milestone node points
                    coordinates.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = HackTrackPrimary,
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { pt ->
                    Text(
                        text = pt.first,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Individual Milestone Badge Item Card.
 */
@Composable
fun MilestoneBadgeCard(
    badge: CareerMilestoneBadge,
    modifier: Modifier = Modifier
) {
    val progressFraction = (badge.currentCount.toFloat() / badge.targetCount.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (badge.isUnlocked) BorderStroke(1.2.dp, badge.badgeColor.copy(alpha = 0.4f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (badge.isUnlocked) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Circular Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) badge.badgeColor.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badge.icon,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) badge.badgeColor else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (badge.isUnlocked) HackTrackSuccess.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (badge.isUnlocked) "UNLOCKED 🏆" else "+${badge.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.isUnlocked) HackTrackSuccess else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (badge.isUnlocked) badge.badgeColor else HackTrackPrimary)
                        )
                    }

                    Text(
                        text = "${badge.currentCount}/${badge.targetCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) badge.badgeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
