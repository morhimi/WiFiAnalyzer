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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

data class GraphViewport(
    val rangeProvider: CartesianLayerRangeProvider,
    val placeholderDataPoints: List<DataPoint>,
    val scrollEnabled: Boolean = true,
    val initialScroll: Scroll.Absolute = Scroll.Absolute.Start,
    val autoScrollCondition: AutoScrollCondition = AutoScrollCondition.Never,
    val initialZoom: Zoom = Zoom.Content,
    val zoomEnabled: Boolean = false,
    val minZoom: Zoom = Zoom.Content,
    val maxZoom: Zoom = Zoom.max(Zoom.fixed(ZOOM_MAX), Zoom.Content),
    val scalable: Boolean = false,
)

private const val ZOOM_MAX: Float = 10f

class GraphWrapper(
    val graphViewport: GraphViewport,
    val seriesLabel: SeriesLabel,
    private val seriesCache: SeriesCache = SeriesCache(graphViewport.placeholderDataPoints),
    private val graphColors: GraphColors = GraphColors(),
    val onShowWiFiDetails: (List<WiFiDetail>) -> Unit = {},
    private val chartUpdater: ChartUpdater =
        ChartUpdater(seriesLabel, seriesCache, onShowWiFiDetails = onShowWiFiDetails),
) {
    var chartWidth: Float = 0f
    var chartHeight: Float = 0f

    fun handleTap(
        x: Float,
        y: Float,
    ): Boolean {
        val details = seriesLabel.findDetailsAt(x, y, chartWidth, chartHeight)
        if (details.isNotEmpty()) {
            onShowWiFiDetails(details)
            return true
        }
        return false
    }

    internal val modelProducer: CartesianChartModelProducer = CartesianChartModelProducer()
    internal val coroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    var isVisible: Boolean by mutableStateOf(true)
        internal set

    var lines: List<LineCartesianLayer.Line> by mutableStateOf(
        listOf(LineCartesianLayer.Line(fill = LineCartesianLayer.LineFill.single(Fill(Color.Transparent)))),
    )
        internal set

    fun removeSeries(newSeries: Set<WiFiDetail>) {
        val removed = seriesCache.remove(differenceSeries(newSeries))
        if (removed.isNotEmpty()) {
            chartUpdater.resetStyles()
            chartUpdater.syncPointMap(seriesCache.populatedEntries())
            flushData()
        }
        removed.filterNot { it.connected }.forEach {
            graphColors.addColor(it.graphColor.primary)
        }
    }

    fun reset() {
        removeSeries(emptySet())
        flushData()
    }

    fun differenceSeries(newSeries: Set<WiFiDetail>): List<WiFiDetail> = seriesCache.difference(newSeries)

    fun addSeries(
        wiFiDetail: WiFiDetail,
        dataPoints: List<DataPoint>,
        drawBackground: Boolean,
    ): Boolean =
        if (seriesExists(wiFiDetail)) {
            false
        } else {
            val connected = wiFiDetail.wiFiAdditional.wiFiConnection.connected
            val graphColor = if (connected) graphColors.connectedColor else graphColors.graphColor()
            val seriesData =
                SeriesData(wiFiDetail, dataPoints, graphColor, seriesTitle(wiFiDetail), connected, drawBackground)
            seriesCache.put(wiFiDetail, seriesData)
            true
        }

    fun updateSeries(
        wiFiDetail: WiFiDetail,
        data: List<DataPoint>,
        drawBackground: Boolean,
    ): Boolean {
        val seriesData = seriesCache[wiFiDetail] ?: return false
        seriesData.wiFiDetail = wiFiDetail
        seriesData.replaceAll(data)
        seriesData.title = seriesTitle(wiFiDetail)
        updateConnectionColor(seriesData, wiFiDetail.wiFiAdditional.wiFiConnection.connected)
        seriesData.drawBackground = drawBackground
        return true
    }

    fun appendToSeries(
        wiFiDetail: WiFiDetail,
        data: DataPoint,
        count: Int,
        drawBackground: Boolean,
    ): Boolean {
        val seriesData = seriesCache[wiFiDetail] ?: return false
        seriesData.wiFiDetail = wiFiDetail
        seriesData.append(data, count + 1)
        updateConnectionColor(seriesData, wiFiDetail.wiFiAdditional.wiFiConnection.connected)
        seriesData.drawBackground = drawBackground
        return true
    }

    fun destroy() {
        coroutineScope.cancel()
    }

    fun flushData() {
        val populatedEntries = seriesCache.populatedEntries()
        if (populatedEntries.isEmpty()) return
        val (populatedData, updatedLines) = chartUpdater.sync(populatedEntries)
        if (updatedLines != null) {
            lines = updatedLines
        }
        val snapshot = populatedData.toCoordinates()
        coroutineScope.launch {
            modelProducer.runTransaction {
                lineModel {
                    snapshot.forEach { (x, y) ->
                        series(x = x, y = y)
                    }
                }
            }
        }
    }

    fun newSeries(wiFiDetail: WiFiDetail): Boolean = !seriesExists(wiFiDetail)

    fun show() {
        isVisible = true
    }

    fun gone() {
        isVisible = false
    }

    val markerController: MarkerControllerWrapper
        get() = chartUpdater.markerInteraction.markerController

    private fun updateConnectionColor(
        seriesData: SeriesData,
        connected: Boolean,
    ) {
        if (seriesData.connected == connected) return
        seriesData.graphColor =
            if (connected) {
                graphColors.addColor(seriesData.graphColor.primary)
                graphColors.connectedColor
            } else {
                graphColors.graphColor()
            }
        seriesData.connected = connected
    }

    private fun seriesExists(wiFiDetail: WiFiDetail): Boolean = seriesCache.contains(wiFiDetail)

    private fun seriesTitle(wiFiDetail: WiFiDetail): String {
        val name =
            when {
                wiFiDetail.wiFiIdentifier.alias.isNotBlank() -> wiFiDetail.wiFiIdentifier.alias
                else -> wiFiDetail.wiFiIdentifier.ssid
            }
        return "$name ${wiFiDetail.wiFiSignal.channelDisplay()}"
    }
}
