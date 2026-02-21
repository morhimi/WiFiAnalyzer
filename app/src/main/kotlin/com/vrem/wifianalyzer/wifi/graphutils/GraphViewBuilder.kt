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

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.vrem.util.EMPTY
import com.vrem.wifianalyzer.settings.ThemeStyle
import info.appdev.charting.charts.LineChart
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.AxisBase
import info.appdev.charting.formatter.IAxisValueFormatter

internal fun LineChart.layout(layoutParams: ViewGroup.LayoutParams): LineChart {
    this.layoutParams = layoutParams
    this.visibility = View.GONE
    return this
}

internal fun LineChart.initialize(
    maximumY: Int,
    scalable: Boolean,
): LineChart {
    this.isScaleXEnabled = scalable
    this.isScaleYEnabled = false
    this.axisLeft.axisMinimum = MIN_Y.toFloat()
    this.axisLeft.axisMaximum = maximumY.toFloat()
    this.axisRight.isEnabled = false
    return this
}

internal fun LineChart.colors(themeStyle: ThemeStyle): LineChart {
    this.setBorderColor(Color.GRAY)
    this.axisLeft.textColor = themeStyle.colorGraphText
    this.axisLeft.axisLineColor = themeStyle.colorGraphText
    this.xAxis.textColor = themeStyle.colorGraphText
    this.xAxis.axisLineColor = themeStyle.colorGraphText
    return this
}

internal fun LineChart.xAxisTitle(title: String): LineChart {
    if (title.isNotEmpty()) {
        this.xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return "$value $title"
            }
        }
    }
    return this
}

internal fun LineChart.yAxisTitle(title: String): LineChart {
    if (title.isNotEmpty()) {
        this.axisLeft.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return "$value $title"
            }
        }
    }
    return this
}

internal fun LineChart.labelFormat(labelFormatter: IAxisValueFormatter?): LineChart {
    labelFormatter?.let {
        this.xAxis.valueFormatter = labelFormatter
    }
    return this
}

internal fun LineChart.labels(
    horizontalLabelsVisible: Boolean,
): LineChart {
    this.xAxis.setDrawLabels(horizontalLabelsVisible)
    this.xAxis.position = XAxis.XAxisPosition.BOTTOM
    this.description.isEnabled = false
    this.legend.isEnabled = false
    return this
}

class GraphViewBuilder(
    private val numHorizontalLabels: Int,
    private val maximumY: Int,
    private val themeStyle: ThemeStyle,
    private val horizontalLabelsVisible: Boolean = true,
) {
    private var labelFormatter: IAxisValueFormatter? = null
    private var verticalTitle: String = String.EMPTY
    private var horizontalTitle: String = String.EMPTY

    fun setLabelFormatter(labelFormatter: IAxisValueFormatter): GraphViewBuilder {
        this.labelFormatter = labelFormatter
        return this
    }

    fun setVerticalTitle(verticalTitle: String): GraphViewBuilder {
        this.verticalTitle = verticalTitle
        return this
    }

    fun setHorizontalTitle(horizontalTitle: String): GraphViewBuilder {
        this.horizontalTitle = horizontalTitle
        return this
    }

    fun build(
        context: Context,
        scalable: Boolean,
    ): LineChart =
        LineChart(context)
            .layout(layoutParams)
            .gridLabelInitialize()
            .viewportInitialize(scalable)

    private fun LineChart.viewportInitialize(scalable: Boolean): LineChart {
        this.initialize(maximumY, scalable)
        return this
    }

    private fun LineChart.gridLabelInitialize(): LineChart {
        this.labels(horizontalLabelsVisible)
            .labelFormat(labelFormatter)
            .xAxisTitle(horizontalTitle)
            .yAxisTitle(verticalTitle)
            .colors(themeStyle)
        return this
    }

    val numVerticalLabels: Int get() = (maximumPortY - MIN_Y) / 10 + 1

    val maximumPortY: Int get() = if (maximumY > MAX_Y || maximumY < MIN_Y_HALF) MAX_Y_DEFAULT else maximumY

    val layoutParams: ViewGroup.LayoutParams =
        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
}
