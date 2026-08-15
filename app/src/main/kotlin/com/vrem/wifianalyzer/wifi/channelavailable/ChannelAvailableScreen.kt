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
package com.vrem.wifianalyzer.wifi.channelavailable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.band.WiFiChannelCountry
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import java.util.Locale

@Composable
fun ChannelAvailableScreen(
    countryCode: String,
    languageLocale: Locale,
) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = stringResource(R.string.wifi_channels_list_url),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = countryCode,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = WiFiChannelCountry.find(countryCode).countryName(languageLocale),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            listOf(
                Triple(R.string.wifi_band_2ghz, WiFiBand.GHZ2, listOf(WiFiWidth.MHZ_20, WiFiWidth.MHZ_40)),
                Triple(R.string.wifi_band_5ghz, WiFiBand.GHZ5, listOf(WiFiWidth.MHZ_20, WiFiWidth.MHZ_40, WiFiWidth.MHZ_80, WiFiWidth.MHZ_160)),
                Triple(R.string.wifi_band_6ghz, WiFiBand.GHZ6, listOf(WiFiWidth.MHZ_20, WiFiWidth.MHZ_40, WiFiWidth.MHZ_80, WiFiWidth.MHZ_160, WiFiWidth.MHZ_320))
            ).forEach { (bandNameRes, wiFiBand, widths) ->
                Text(
                    text = stringResource(bandNameRes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                widths.forEach { width ->
                    Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                        Text(
                            text = width.toString(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = wiFiBand.wiFiChannels.availableChannels(width, wiFiBand, countryCode).joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
