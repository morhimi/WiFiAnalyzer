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
package com.vrem.wifianalyzer.wifi.detailview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.compose.ChannelNumber
import com.vrem.wifianalyzer.compose.Frequency
import com.vrem.wifianalyzer.compose.Security
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointItem
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@Composable
fun WiFiDetailContent(wiFiDetail: WiFiDetail) {
    val context = LocalContext.current
    val signal = wiFiDetail.wiFiSignal

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            AccessPointItem(wiFiDetail = wiFiDetail, viewType = AccessPointViewType.COMPLETE)

            Text(
                text = stringResource(id = signal.wiFiBand.textResource),
                color = Frequency,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )

            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.channel_short_name),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = "${signal.wiFiChannelStart.channel}",
                    color = ChannelNumber,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = " " + stringResource(id = R.string.channel_from_to) + " ",
                    color = ChannelNumber,
                )
                Text(
                    text = "${signal.wiFiChannelEnd.channel}",
                    color = ChannelNumber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = "(${stringResource(id = signal.wiFiWidth.textResource)})",
                    color = Frequency,
                )
            }

            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = stringResource(id = signal.extra.wiFiStandard.fullResource),
                    fontWeight = FontWeight.Bold,
                    color = Frequency,
                    modifier = Modifier.padding(end = 8.dp),
                )
                if (signal.extra.is80211mc) {
                    Text(
                        text = stringResource(id = R.string.mc_flag),
                        color = Frequency,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                Text(
                    text = signal.extra.fastRoamingDisplay(context),
                    color = Frequency,
                )
            }

            Text(
                text = wiFiDetail.wiFiSecurity.capabilities,
                color = Security,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
            )

            Text(
                text = wiFiDetail.wiFiSecurity.wiFiSecurityTypesDisplay(context),
                color = Security,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (wiFiDetail.wiFiAdditional.vendorName.isNotEmpty()) {
                Text(
                    text = wiFiDetail.wiFiAdditional.vendorName,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (wiFiDetail.wiFiIdentifier.bssid.isNotEmpty()) {
                Text(
                    text = wiFiDetail.wiFiIdentifier.bssid,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
