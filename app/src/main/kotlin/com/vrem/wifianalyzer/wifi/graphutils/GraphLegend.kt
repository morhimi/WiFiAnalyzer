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

import info.appdev.charting.components.Legend

internal typealias LegendDisplay = (legend: Legend) -> Unit

internal val legendDisplayNone: LegendDisplay = { it.isEnabled = false }

internal val legendDisplayLeft: LegendDisplay = {
    it.isEnabled = true
    it.verticalAlignment = Legend.LegendVerticalAlignment.TOP
    it.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
    it.orientation = Legend.LegendOrientation.VERTICAL
    it.setDrawInside(false)
}

internal val legendDisplayRight: LegendDisplay = {
    it.isEnabled = true
    it.verticalAlignment = Legend.LegendVerticalAlignment.TOP
    it.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
    it.orientation = Legend.LegendOrientation.VERTICAL
    it.setDrawInside(false)
}

enum class GraphLegend(
    val legendDisplay: LegendDisplay,
) {
    LEFT(legendDisplayLeft),
    RIGHT(legendDisplayRight),
    HIDE(legendDisplayNone),
    ;

    fun display(legend: Legend) {
        legendDisplay(legend)
    }
}
