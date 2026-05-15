package com.kokila.jalsanchay

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

fun createChartBitmap(
    chart: LineChart
): android.graphics.Bitmap {

    return chart.chartBitmap
}

@Composable
fun AnalyticsChart(
    data: List<HarvestEntry>
) {

    // EMPTY CHECK
    if (data.isEmpty()) return

    AndroidView(

        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),

        factory = { context ->

            val chart = LineChart(context)

            ChartHolder.chart = chart

            val entries = ArrayList<Entry>()

            data.forEachIndexed { index, value ->

                entries.add(
                    Entry(
                        index.toFloat(),
                        value.water
                    )
                )
            }

            val dataSet =
                LineDataSet(
                    entries,
                    "Water Harvested"
                )

            dataSet.color = Color.BLUE
            dataSet.lineWidth = 3f
            dataSet.circleRadius = 4f
            dataSet.setCircleColor(Color.BLUE)
            dataSet.valueTextSize = 12f
            dataSet.setDrawValues(false)

            dataSet.mode =
                LineDataSet.Mode.CUBIC_BEZIER

            dataSet.setDrawFilled(true)
            dataSet.fillColor = Color.CYAN
            dataSet.fillAlpha = 80

            val lineData = LineData(dataSet)

            chart.data = lineData

            // X Axis Labels
            chart.xAxis.valueFormatter =
                IndexAxisValueFormatter(
                    data.map { it.date }
                )

            chart.xAxis.position =
                XAxis.XAxisPosition.BOTTOM

            chart.xAxis.granularity = 1f
            chart.xAxis.setLabelCount(5, true)

            chart.xAxis.labelRotationAngle = -45f

            chart.axisRight.isEnabled = false

            chart.xAxis.setDrawGridLines(false)

            chart.axisLeft.setDrawGridLines(false)
            chart.axisLeft.axisMinimum = 0f
            chart.axisLeft.textSize = 10f

            chart.description.isEnabled = false

            chart.animateXY(1200, 1200)
            chart.setTouchEnabled(true)
            chart.isDragEnabled = true
            chart.setScaleEnabled(true)
            chart.setPinchZoom(true)
            chart.extraBottomOffset = 16f

            chart.invalidate()

            chart
        }
    )
}