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
package com.vrem.wifianalyzer.wifi.channelrating

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.compose.ChannelNumber
import com.vrem.wifianalyzer.compose.ErrorColor
import com.vrem.wifianalyzer.compose.Frequency
import com.vrem.wifianalyzer.compose.SuccessColor
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.ChannelAPCount
import com.vrem.wifianalyzer.wifi.model.WiFiWidth

@Composable
fun ChannelRatingBest(
    wiFiBand: WiFiBand,
    bestChannels: List<ChannelAPCount>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.channel_rating_best),
                color = ChannelNumber,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 4.dp),
            )
            if (bestChannels.isEmpty()) {
                Text(
                    text = errorMessage(wiFiBand),
                    color = ErrorColor,
                )
            }
        }

        WiFiWidth.entries.forEach { wiFiWidth ->
            val channels =
                bestChannels
                    .filter { it.wiFiWidth == wiFiWidth }
                    .map { it.wiFiChannel.channel }
                    .joinToString(",")

            if (channels.isNotEmpty()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 2.dp),
                ) {
                    Text(
                        text = stringResource(id = wiFiWidth.textResource),
                        color = Frequency,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp),
                        fontSize = 14.sp,
                    )
                    Text(
                        text = channels,
                        color = SuccessColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun errorMessage(wiFiBand: WiFiBand): String =
    if (WiFiBand.GHZ2 == wiFiBand) {
        stringResource(
            id = R.string.channel_rating_best_alternative,
            stringResource(id = R.string.channel_rating_best_none),
            stringResource(id = WiFiBand.GHZ5.textResource),
        )
    } else {
        stringResource(id = R.string.channel_rating_best_none)
    }
