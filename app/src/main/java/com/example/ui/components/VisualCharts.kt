package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

data class ChartSlice(
    val id: String,
    val label: String,
    val value: Float,
    val color: Color,
    val subtitle: String = ""
)

data class BarItemData(
    val label: String,
    val value: Float,
    val displayValue: String,
    val color: Color,
    val isHighlighted: Boolean = false
)

/**
 * High-fidelity Interactive Donut Chart with animated sweep, center stats, and responsive legend.
 */
@Composable
fun InteractiveDonutChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 160.dp,
    strokeWidth: Dp = 22.dp,
    centerTitle: String = "Total",
    centerSubtitle: String? = null,
    onSliceSelected: ((ChartSlice?) -> Unit)? = null
) {
    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat() }
    var selectedSliceId by remember { mutableStateOf<String?>(null) }

    // Animation progress
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val selectedSlice = remember(selectedSliceId, slices) {
        slices.firstOrNull { it.id == selectedSliceId }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = strokeWidth.toPx()
                val radius = (size.minDimension - strokePx) / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                if (total <= 0f) {
                    // Empty state track
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.2f),
                        radius = radius,
                        center = centerOffset,
                        style = Stroke(width = strokePx)
                    )
                } else {
                    var startAngle = -90f
                    val animatedTotalSweep = 360f * animationProgress.value

                    slices.forEach { slice ->
                        val sliceSweep = if (total > 0f) (slice.value / total) * 360f else 0f
                        val actualSweep = (sliceSweep * animationProgress.value).coerceAtLeast(0f)
                        val isSelected = slice.id == selectedSliceId

                        if (actualSweep > 0.5f) {
                            drawArc(
                                color = if (selectedSliceId == null || isSelected) slice.color else slice.color.copy(alpha = 0.35f),
                                startAngle = startAngle,
                                sweepAngle = actualSweep - (if (slices.size > 1) 2.5f else 0f),
                                useCenter = false,
                                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(
                                    width = if (isSelected) strokePx + 4f else strokePx,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                        startAngle += sliceSweep
                    }
                }
            }

            // Center Text inside the Donut
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                if (selectedSlice != null) {
                    val percent = if (total > 0f) (selectedSlice.value / total * 100f).toInt() else 0
                    Text(
                        text = "${selectedSlice.value.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = selectedSlice.color
                    )
                    Text(
                        text = "$percent% of total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedSlice.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "${total.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = centerTitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (centerSubtitle != null) {
                        Text(
                            text = centerSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Legend Items
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.forEach { slice ->
                val isSelected = slice.id == selectedSliceId
                val percentage = if (total > 0f) (slice.value / total * 100f).toInt() else 0

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) slice.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedSliceId = if (selectedSliceId == slice.id) null else slice.id
                            onSliceSelected?.invoke(if (selectedSliceId != null) slice else null)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(slice.color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = slice.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${slice.value.toInt()} tasks",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = slice.color.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = slice.color,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Bar Chart with vertical rounded bars, values above bars, and category labels.
 */
@Composable
fun AnimatedBarChart(
    items: List<BarItemData>,
    modifier: Modifier = Modifier,
    barMaxHeight: Dp = 140.dp
) {
    val maxValue = remember(items) {
        items.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(items) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barMaxHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEach { bar ->
                val barFraction = ((bar.value / maxValue) * animationProgress.value).coerceIn(0.04f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    // Value label above bar
                    Text(
                        text = bar.displayValue,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (bar.value > 0f) bar.color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar Pillar with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .fillMaxHeight(barFraction)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        bar.color,
                                        bar.color.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )
                }
            }
        }

        // Horizontal baseline divider
        HorizontalDivider(
            thickness = 1.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Category labels below baseline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { bar ->
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 240-degree Radial Health Gauge showing Overall Sprint Momentum and Health Score.
 */
@Composable
fun SprintVelocityHealthGauge(
    score: Int, // 0 to 100
    title: String = "Sprint Velocity Score",
    statusMessage: String = "Optimal Sprint Rhythm",
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "gauge_score"
    )

    val gaugeColor = when {
        score >= 80 -> HackTrackSuccess
        score >= 50 -> HackTrackWarning
        else -> HackTrackDanger
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 18.dp.toPx()
                val radius = (size.minDimension - strokePx) / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                val startAngle = 150f
                val totalSweep = 240f

                // Inactive track
                drawArc(
                    color = Color.Gray.copy(alpha = 0.15f),
                    startAngle = startAngle,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Active progress arc
                val currentSweep = (animatedScore / 100f) * totalSweep
                if (currentSweep > 1f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0.0f to HackTrackPrimary,
                            0.5f to HackTrackWarning,
                            1.0f to HackTrackSuccess
                        ),
                        startAngle = startAngle,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animatedScore.toInt()}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = gaugeColor
                )
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Visual Funnel representing conversion stages of hackathons from Registered -> Building -> Winner.
 */
@Composable
fun ConversionFunnelView(
    stages: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxCount = remember(stages) {
        stages.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        stages.forEachIndexed { index, (stageName, count) ->
            val fraction = (count.toFloat() / maxCount.toFloat()).coerceIn(0.12f, 1f)
            val stageColor = when (index) {
                0 -> HackTrackPrimary
                1 -> Color(0xFF6366F1)
                2 -> Color(0xFF06B6D4)
                3 -> HackTrackWarning
                4 -> Color(0xFFF97316)
                else -> HackTrackSuccess
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stageName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$count competitions",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = stageColor
                    )
                }

                // Funnel Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        stageColor,
                                        stageColor.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * Segmented horizontal distribution bar with color indicators and percentages.
 */
@Composable
fun SegmentedDistributionBar(
    segments: List<ChartSlice>,
    modifier: Modifier = Modifier
) {
    val total = remember(segments) { segments.sumOf { it.value.toDouble() }.toFloat() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Multi-colored bar
        if (total > 0f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                segments.filter { it.value > 0f }.forEach { seg ->
                    val weight = (seg.value / total).coerceAtLeast(0.01f)
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(seg.color)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
        }

        // Legend chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            segments.forEach { seg ->
                val percent = if (total > 0f) (seg.value / total * 100f).toInt() else 0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(seg.color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${seg.label}: $percent%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Metric Card with icon, title, value, and trend pill.
 */
@Composable
fun AnalyticsMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
