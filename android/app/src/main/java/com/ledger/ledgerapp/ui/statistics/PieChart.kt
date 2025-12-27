package com.ledger.ledgerapp.ui.statistics

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PieChartData(
    val value: Double,
    val color: Color,
    val label: String
)

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    radiusOuter: Dp = 90.dp,
    chartBarWidth: Dp = 20.dp,
    animDuration: Int = 1000,
) {
    val totalSum = data.sumOf { it.value }
    val floatValue = mutableListOf<Float>()

    // Calculate angles
    data.forEachIndexed { index, pieChartData ->
        floatValue.add(index, 360 * pieChartData.value.toFloat() / totalSum.toFloat())
    }

    // Animation
    var animationPlayed by remember { mutableStateOf(false) }
    val lastValue = 0f

    // Animate the sweep angle
    val animateSize by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        ),
        label = "pieChartAnimation"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        modifier = modifier.size(radiusOuter * 2f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-90f)
        ) {
            var startAngle = 0f

            data.forEachIndexed { index, pieChartData ->
                val sweepAngle = floatValue[index]

                drawArc(
                    color = pieChartData.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * animateSize,
                    useCenter = false,
                    style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
    }
}
