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
package com.vrem.wifianalyzer.wifi.timegraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.graphutils.Render
import com.vrem.wifianalyzer.wifi.graphutils.signalYAxisFormatter

@Composable
internal fun TimeGraph.Content(modifier: Modifier = Modifier) {
    val yAxisFormatter = signalYAxisFormatter
    val verticalTitle = stringResource(R.string.graph_axis_y)
    val horizontalTitle = stringResource(R.string.graph_time_axis_x)

    graphWrapper.Render(
        xAxisFormatter = CartesianValueFormatter.decimal(),
        yAxisFormatter = yAxisFormatter,
        itemPlacer = HorizontalAxis.ItemPlacer.aligned(spacing = { 2 }, shiftExtremeLines = false),
        verticalTitle = verticalTitle,
        horizontalTitle = horizontalTitle,
        modifier = modifier,
    )
}
