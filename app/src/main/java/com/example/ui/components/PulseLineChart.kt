package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPulse
import com.example.ui.theme.EmeraldPulse

@Composable
fun PulseLineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = CyanPulse,
    fillColor: Color = CyanPulse.copy(alpha = 0.15f),
    maxValue: Float = 100f,
    minValue: Float = 0f,
    label: String = "CPU ACTIVITY WAVE"
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            val currentVal = dataPoints.lastOrNull() ?: 0f
            Text(
                text = "${currentVal.toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = lineColor
            )
        }

        val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val width = size.width
            val height = size.height

            // Draw horizontal reference grid lines
            for (step in 0..4) {
                val y = height * (step / 4f)
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            if (dataPoints.size < 2) return@Canvas

            val stepX = width / (dataPoints.size - 1)
            val range = (maxValue - minValue).coerceAtLeast(1f)

            val path = Path()
            val fillPath = Path()

            val points = dataPoints.mapIndexed { index, value ->
                val normY = ((value - minValue) / range).coerceIn(0f, 1f)
                val x = index * stepX
                val y = height - (normY * height)
                Offset(x, y)
            }

            // Start path
            path.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX = (p0.x + p1.x) / 2f
                path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw filled gradient area under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor, Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw sparkline stroke
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw pulsating head dot at the latest point
            val lastPoint = points.last()
            drawCircle(
                color = lineColor.copy(alpha = 0.4f),
                radius = 7.dp.toPx(),
                center = lastPoint
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = lastPoint
            )
        }
    }
}
