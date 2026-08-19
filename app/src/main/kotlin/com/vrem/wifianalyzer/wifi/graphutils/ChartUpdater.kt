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

import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

class ChartUpdater(
    private val seriesLabel: SeriesLabel,
    seriesCache: SeriesCache,
    private val lineStyleTracker: LineStyleTracker = LineStyleTracker(),
    private val lineLayerFactory: LineLayerFactory = LineLayerFactory(),
    onShowWiFiDetails: (List<WiFiDetail>) -> Unit = {},
    val markerInteraction: MarkerInteraction = MarkerInteraction(onShowWiFiDetails),
) {
    fun sync(entries: List<SeriesEntry>): Pair<List<SeriesData>, List<LineCartesianLayer.Line>?> {
        val populatedData = entries.map { it.value }
        seriesLabel.seriesSnapshot = populatedData
        val updatedLines =
            if (lineStyleTracker.changed(populatedData)) {
                lineLayerFactory.lines(populatedData)
            } else {
                null
            }
        markerInteraction.updatePointMap(entries)
        return Pair(populatedData, updatedLines)
    }

    fun resetStyles() {
        lineStyleTracker.reset()
    }

    fun syncPointMap(entries: List<SeriesEntry>) {
        markerInteraction.updatePointMap(entries)
    }
}
