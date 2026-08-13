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
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.formatter.IFillFormatter
import info.appdev.charting.formatter.IValueFormatter
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.utils.ViewPortHandler

private fun LineDataSet<EntryFloat>.removeSeriesColor(graphColors: GraphColors) {
    graphColors.addColor(this.colors[0].toLong())
}

private fun LineDataSet<EntryFloat>.highlightConnected(connected: Boolean) {
    this.lineWidth = (if (connected) THICKNESS_CONNECTED else THICKNESS_REGULAR).toFloat()
}

private fun LineDataSet<EntryFloat>.seriesColor(graphColors: GraphColors) {
    val graphColor = graphColors.graphColor()
    val color = graphColor.primary.toInt()
    this.setColors(color)
    this.fillColor = graphColor.background.toInt()
    this.isDrawCircles = false
    this.isDrawValues = false
    this.valueTextColor = color
    this.valueTextSize = 10f
    this.setDrawHighlightIndicators(false)
    this.fillFormatter = object : IFillFormatter {
        override fun getFillLinePosition(dataSet: ILineDataSet<*>?, dataProvider: LineDataProvider): Float {
            return MIN_Y.toFloat()
        }
    }
}

private fun LineDataSet<EntryFloat>.drawBackground(drawBackground: Boolean) {
    this.isDrawFilled = drawBackground
}

@OpenClass
class SeriesOptions(
    private val graphColors: GraphColors = GraphColors(),
) {
    fun highlightConnected(
        series: LineDataSet<EntryFloat>,
        connected: Boolean,
    ) {
        series.highlightConnected(connected)
    }

    fun setSeriesColor(series: LineDataSet<EntryFloat>) {
        series.seriesColor(graphColors)
    }

    fun drawBackground(
        series: LineDataSet<EntryFloat>,
        drawBackground: Boolean,
    ) {
        series.drawBackground(drawBackground)
    }

    fun removeSeriesColor(series: LineDataSet<EntryFloat>) {
        series.removeSeriesColor(graphColors)
    }

    fun setupLabels(series: LineDataSet<EntryFloat>, label: String) {
        series.isDrawValues = true
        series.valueFormatter = object : IValueFormatter {
            override fun getFormattedValue(value: Float, entryFloat: EntryFloat?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
                return if (entryFloat?.data == "PEAK") label else ""
            }
        }
    }
}
