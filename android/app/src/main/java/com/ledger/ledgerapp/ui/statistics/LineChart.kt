package com.ledger.ledgerapp.ui.statistics

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.ledger.ledgerapp.viewmodel.DailyStat

@Composable
fun LineChart(
    data: List<DailyStat>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Text("暂无趋势数据", modifier = Modifier.padding(16.dp))
        return
    }

    val incomeColor = Color(0xFF66BB6A) // Green
    val expenseColor = Color(0xFFEF5350) // Red
    val axisColor = Color.Gray

    Column(modifier = modifier) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = incomeColor, label = "收入")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = expenseColor, label = "支出")
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val paddingStart = 60.dp.toPx()
            val paddingBottom = 40.dp.toPx()
            val paddingTop = 20.dp.toPx()
            val paddingEnd = 20.dp.toPx()
            
            val chartWidth = width - paddingStart - paddingEnd
            val chartHeight = height - paddingBottom - paddingTop

            // Find max value for Y axis scaling
            val maxIncome = data.maxOfOrNull { it.income } ?: 0.0
            val maxExpense = data.maxOfOrNull { it.expense } ?: 0.0
            val maxYValue = maxOf(maxIncome, maxExpense).toFloat()
            // Add some buffer to the top
            val maxY = if (maxYValue == 0f) 100f else maxYValue * 1.1f

            // Draw Axes
            // X-Axis
            drawLine(
                color = axisColor,
                start = Offset(paddingStart, height - paddingBottom),
                end = Offset(width - paddingEnd, height - paddingBottom),
                strokeWidth = 2f
            )
            // Y-Axis
            drawLine(
                color = axisColor,
                start = Offset(paddingStart, height - paddingBottom),
                end = Offset(paddingStart, paddingTop),
                strokeWidth = 2f
            )

            // Draw Grid Lines (Horizontal)
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height - paddingBottom - (i.toFloat() / gridLines) * chartHeight
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(paddingStart, y),
                    end = Offset(width - paddingEnd, y),
                    strokeWidth = 1f
                )
                
                // Y-Axis Labels
                val labelValue = (maxY * i / gridLines).toInt()
                drawContext.canvas.nativeCanvas.drawText(
                    labelValue.toString(),
                    paddingStart - 10f,
                    y + 10f,
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = Paint.Align.RIGHT
                    }
                )
            }

            // Draw Lines
            val incomePath = Path()
            val expensePath = Path()
            
            val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)

            data.forEachIndexed { index, stat ->
                val x = paddingStart + index * stepX
                val yIncome = height - paddingBottom - (stat.income.toFloat() / maxY) * chartHeight
                val yExpense = height - paddingBottom - (stat.expense.toFloat() / maxY) * chartHeight

                if (index == 0) {
                    incomePath.moveTo(x, yIncome)
                    expensePath.moveTo(x, yExpense)
                } else {
                    incomePath.lineTo(x, yIncome)
                    expensePath.lineTo(x, yExpense)
                }
                
                // Draw points
                drawCircle(color = incomeColor, radius = 6f, center = Offset(x, yIncome))
                drawCircle(color = expenseColor, radius = 6f, center = Offset(x, yExpense))
            }

            drawPath(
                path = incomePath,
                color = incomeColor,
                style = Stroke(width = 4f)
            )
            
            drawPath(
                path = expensePath,
                color = expenseColor,
                style = Stroke(width = 4f)
            )
            
            // X-Axis Labels (Show max 5 labels to avoid overcrowding)
            val labelStep = (data.size / 5).coerceAtLeast(1)
            for (i in data.indices step labelStep) {
                val x = paddingStart + i * stepX
                val dateLabel = if (data[i].date.length >= 10) data[i].date.substring(5) else data[i].date
                
                drawContext.canvas.nativeCanvas.drawText(
                    dateLabel,
                    x,
                    height - paddingBottom + 30f,
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = CircleShape,
            color = color
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
