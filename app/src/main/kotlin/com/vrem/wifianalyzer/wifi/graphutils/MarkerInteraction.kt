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

import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.Point
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

class MarkerHandler(
    private val onShowWiFiDetails: (List<WiFiDetail>) -> Unit = {},
) {
    fun event(
        touch: Point,
        thresholdPx: Float,
        dataPointToDetail: Map<Long, MutableList<WiFiDetail>>,
        targets: List<CartesianMarker.Target>,
        seriesList: List<WiFiDetail> = emptyList(),
    ): Boolean {
        val lineTargets = targets.filterIsInstance<LineCartesianLayerMarkerTarget>()
        if (lineTargets.isEmpty()) return false
        val markerPoints =
            lineTargets.flatMap { target ->
                target.points.map {
                    MarkerPoint(
                        DataPoint(Math.round(it.entry.x).toInt(), Math.round(it.entry.y).toInt()),
                        it.canvasY,
                    )
                }
            }
        if (markerPoints.isEmpty()) return false
        val canvasX = lineTargets.first().canvasX
        val wiFiDetails = matchDetails(markerPoints, canvasX, touch, thresholdPx, dataPointToDetail, seriesList)
        if (wiFiDetails.isNotEmpty()) {
            onShowWiFiDetails(wiFiDetails)
            return true
        }
        return false
    }
}

class MarkerInteraction(
    private val markerHandler: MarkerHandler = MarkerHandler(),
    private val thresholdPx: Float = DEFAULT_THRESHOLD_PX,
) {
    constructor(
        onShowWiFiDetails: (List<WiFiDetail>) -> Unit,
        thresholdPx: Float = DEFAULT_THRESHOLD_PX,
    ) : this(
        markerHandler = MarkerHandler(onShowWiFiDetails),
        thresholdPx = thresholdPx,
    )

    private var dataPointToDetail: Map<Long, MutableList<WiFiDetail>> = emptyMap()
    private var seriesList: List<WiFiDetail> = emptyList()

    companion object {
        const val DEFAULT_THRESHOLD_PX: Float = 96f
    }

    val markerController: MarkerControllerWrapper =
        MarkerControllerWrapper(thresholdPx) { interaction, targets ->
            if (interaction is Interaction.Press || interaction is Interaction.Tap) {
                markerHandler.event(interaction.point, thresholdPx, dataPointToDetail, targets, seriesList)
            }
        }

    fun updatePointMap(entries: List<SeriesEntry>) {
        val pointMap = mutableMapOf<Long, MutableList<WiFiDetail>>()
        val list = mutableListOf<WiFiDetail>()
        entries
            .filter { it.key != PLACEHOLDER_DETAIL }
            .forEach { entry ->
                val wiFiDetail = entry.value.wiFiDetail
                list.add(wiFiDetail)
                entry.value.dataPoints.forEach { dataPoint ->
                    pointMap.getOrPut(dataPoint.key) { mutableListOf() }.add(wiFiDetail)
                }
            }
        dataPointToDetail = pointMap
        seriesList = list
    }
}
