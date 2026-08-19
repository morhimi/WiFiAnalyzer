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
package com.vrem.wifianalyzer.wifi.channelgraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.band.FREQUENCY_SPREAD
import com.vrem.wifianalyzer.wifi.graphutils.Render
import com.vrem.wifianalyzer.wifi.graphutils.signalYAxisFormatter

@Composable
internal fun ChannelGraph.Content(modifier: Modifier = Modifier) {
    val yAxisFormatter = signalYAxisFormatter
    val xAxisFormatter = channelXAxisFormatter(wiFiBand)
    val itemPlacer = channelItemPlacer(wiFiBand)
    val verticalTitle = stringResource(R.string.graph_axis_y)
    val horizontalTitle = stringResource(R.string.graph_channel_axis_x)

    graphWrapper.Render(
        xAxisFormatter = xAxisFormatter,
        yAxisFormatter = yAxisFormatter,
        itemPlacer = itemPlacer,
        verticalTitle = verticalTitle,
        horizontalTitle = horizontalTitle,
        xStep = FREQUENCY_SPREAD.toDouble(),
        modifier = modifier,
    )
}
