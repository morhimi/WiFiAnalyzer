/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2026 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.vrem.wifianalyzer.wifi.graphutils

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent

@Composable
internal fun GraphWrapper.Render(
    xAxisFormatter: CartesianValueFormatter,
    yAxisFormatter: CartesianValueFormatter,
    itemPlacer: HorizontalAxis.ItemPlacer,
    verticalTitle: String,
    horizontalTitle: String,
    xStep: Double? = null,
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return

    val textColor = MaterialTheme.colorScheme.onSurface
    val labelComponent =
        rememberTextComponent(
            style = TextStyle(color = textColor, fontSize = (12 * TEXT_SIZE_ADJUSTMENT).sp),
        )
    val titleComponent =
        rememberTextComponent(
            style = TextStyle(color = textColor, fontSize = (12 * AXIS_TEXT_SIZE_ADJUSTMENT).sp),
        )
    val guideline = rememberLineComponent(fill = Fill(Color.Gray.copy(alpha = 0.5f)), thickness = 0.5.dp)
    val axisLine = rememberLineComponent(fill = Fill(Color.Gray), thickness = 1.dp)

    val startAxis =
        VerticalAxis.rememberStart(
            line = axisLine,
            label = labelComponent,
            valueFormatter = yAxisFormatter,
            guideline = guideline,
            itemPlacer = VerticalAxis.ItemPlacer.step({ 10.0 }),
            titleComponent = titleComponent,
            title = { verticalTitle },
        )
    val endAxis =
        VerticalAxis.rememberEnd(
            line = axisLine,
        )
    val bottomAxis =
        HorizontalAxis.rememberBottom(
            line = axisLine,
            label = labelComponent,
            valueFormatter = xAxisFormatter,
            guideline = guideline,
            itemPlacer = itemPlacer,
            titleComponent = titleComponent,
            title = { horizontalTitle },
        )

    val lineLayer =
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(lines),
            rangeProvider = graphViewport.rangeProvider,
        )

    val marker = rememberGraphMarker()

    val baseChart =
        rememberCartesianChart(
            lineLayer,
            startAxis = startAxis,
            endAxis = endAxis,
            bottomAxis = bottomAxis,
            decorations = listOf(seriesLabel),
            marker = marker,
            markerController = markerController,
        )
    val chart =
        remember(baseChart, xStep) {
            xStep?.let { value -> baseChart.copy(getXStep = { _, _, _ -> value }) } ?: baseChart
        }

    val scrollState =
        rememberVicoScrollState(
            scrollEnabled = graphViewport.scrollEnabled,
            initialScroll = graphViewport.initialScroll,
            autoScrollCondition = graphViewport.autoScrollCondition,
        )
    val zoomState =
        rememberVicoZoomState(
            zoomEnabled = graphViewport.zoomEnabled,
            initialZoom = graphViewport.initialZoom,
            minZoom = graphViewport.minZoom,
            maxZoom = graphViewport.maxZoom,
        )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier =
            modifier
                .onGloballyPositioned { coordinates ->
                    chartWidth = coordinates.size.width.toFloat()
                    chartHeight = coordinates.size.height.toFloat()
                }.pointerInput(this) {
                    detectTapGestures { offset ->
                        this@Render.handleTap(offset.x, offset.y)
                    }
                },
        scrollState = scrollState,
        zoomState = zoomState,
        animationSpec = null,
    )
}
