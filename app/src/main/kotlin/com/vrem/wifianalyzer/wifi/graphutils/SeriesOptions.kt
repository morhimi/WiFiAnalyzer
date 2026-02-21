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

import com.vrem.annotation.OpenClass
import info.appdev.charting.data.LineDataSet

private fun LineDataSet.removeSeriesColor(graphColors: GraphColors) {
    graphColors.addColor(this.color.toLong())
}

private fun LineDataSet.highlightConnected(connected: Boolean) {
    this.lineWidth = (if (connected) THICKNESS_CONNECTED else THICKNESS_REGULAR).toFloat()
}

private fun LineDataSet.seriesColor(graphColors: GraphColors) {
    val graphColor = graphColors.graphColor()
    this.color = graphColor.primary.toInt()
    this.fillColor = graphColor.background.toInt()
}

private fun LineDataSet.drawBackground(drawBackground: Boolean) {
    this.isDrawFilled = drawBackground
}

@OpenClass
class SeriesOptions(
    private val graphColors: GraphColors = GraphColors(),
) {
    fun highlightConnected(
        series: LineDataSet,
        connected: Boolean,
    ) {
        series.highlightConnected(connected)
    }

    fun setSeriesColor(series: LineDataSet) {
        series.seriesColor(graphColors)
    }

    fun drawBackground(
        series: LineDataSet,
        drawBackground: Boolean,
    ) {
        series.drawBackground(drawBackground)
    }

    fun removeSeriesColor(series: LineDataSet) {
        series.removeSeriesColor(graphColors)
    }
}
