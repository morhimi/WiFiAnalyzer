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
package com.vrem.wifianalyzer.wifi.accesspoint

import android.net.wifi.WifiInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.compose.ChannelNumber
import com.vrem.wifianalyzer.compose.ErrorColor
import com.vrem.wifianalyzer.compose.Frequency
import com.vrem.wifianalyzer.compose.Selected
import com.vrem.wifianalyzer.compose.SuccessColor
import com.vrem.wifianalyzer.compose.WarningColor
import com.vrem.wifianalyzer.wifi.model.WiFiConnection
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@Composable
fun ConnectionHeader(
    wiFiData: WiFiData,
    wiFiBandAvailable: Boolean,
    wiFiBandName: String,
    scanThrottleEnabled: Boolean,
    permissionEnabled: Boolean,
    isScanning: Boolean,
    onDetailClick: (WiFiDetail) -> Unit,
) {
    val connection = wiFiData.connection()
    val wiFiConnection = connection.wiFiAdditional.wiFiConnection

    Column(modifier = Modifier.fillMaxWidth()) {
        if (wiFiConnection.connected) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onDetailClick(connection) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.current_connection),
                        color = Color(0xFF90CAF9), // Brighter blue for better readability
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = connection.wiFiIdentifier.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = connection.wiFiSignal.wiFiBand.textResource),
                        color = Frequency,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = "CH ${connection.wiFiSignal.channelDisplay()}",
                        color = ChannelNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    if (wiFiConnection.linkSpeed != WiFiConnection.LINK_SPEED_INVALID) {
                        Text(
                            text = "${wiFiConnection.linkSpeed}${WifiInfo.LINK_SPEED_UNITS}",
                            color = Selected,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    Text(
                        text = wiFiConnection.ipAddress,
                        color = Selected,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
                HorizontalDivider(
                    color = Selected,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        if (!wiFiBandAvailable) {
            WiFiSupportWarning(wiFiBandName)
        }

        if (scanThrottleEnabled) {
            WiFiThrottlingWarning()
        }

        val noData = wiFiData.wiFiDetails.isEmpty()
        if (noData || !permissionEnabled) {
            WarningSection(
                noData = noData,
                permissionEnabled = permissionEnabled,
                isScanning = isScanning,
            )
        }
    }
}

@Composable
private fun WiFiSupportWarning(wiFiBandName: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = null,
            tint = ErrorColor,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = wiFiBandName,
            color = ErrorColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun WiFiThrottlingWarning() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = null,
            tint = ErrorColor,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(id = R.string.wifi_throttling_on),
            color = ErrorColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun WarningSection(
    noData: Boolean,
    permissionEnabled: Boolean,
    isScanning: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isScanning) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        }

        Text(
            text = stringResource(id = R.string.scanner_message),
            color = SuccessColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (noData) {
            Text(
                text = stringResource(id = R.string.no_data),
                color = ErrorColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center,
            )
        }

        if (!permissionEnabled) {
            Text(
                text = stringResource(id = R.string.permission_msg),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location_on),
                    contentDescription = null,
                    tint = Selected,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(id = R.string.location_msg),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Text(
                text = stringResource(id = R.string.throttling_msg),
                color = WarningColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Text(
            text = stringResource(id = R.string.no_data_msg),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(id = R.string.no_data_url),
            color = Selected,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp),
            textAlign = TextAlign.Center,
        )
    }
}
