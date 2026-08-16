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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.compose.ChannelNumber
import com.vrem.wifianalyzer.compose.Distance
import com.vrem.wifianalyzer.compose.Frequency
import com.vrem.wifianalyzer.compose.Regular
import com.vrem.wifianalyzer.compose.Security
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiSignal

@Composable
fun AccessPointItem(
    wiFiDetail: WiFiDetail,
    viewType: AccessPointViewType,
    modifier: Modifier = Modifier,
    isChild: Boolean = false,
    expanded: Boolean = false,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isChild) {
            Spacer(modifier = Modifier.width(24.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isChild && wiFiDetail.children.isNotEmpty()) {
                    Icon(
                        painter =
                            painterResource(
                                id = if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more,
                            ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text =
                        if (!isChild && wiFiDetail.children.isNotEmpty()) {
                            "${wiFiDetail.wiFiIdentifier.title} (${wiFiDetail.children.size + 1})"
                        } else {
                            wiFiDetail.wiFiIdentifier.title
                        },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            if (viewType == AccessPointViewType.COMPLETE) {
                AccessPointComplete(wiFiDetail)
            } else {
                AccessPointCompact(wiFiDetail)
            }
        }
    }
}

@Composable
private fun AccessPointComplete(wiFiDetail: WiFiDetail) {
    val context = LocalContext.current
    val signal = wiFiDetail.wiFiSignal

    Row(modifier = Modifier.padding(top = 4.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 8.dp),
        ) {
            Text(text = "${signal.level}dBm", fontSize = 12.sp)
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = signal.strength.imageResource),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    text = stringResource(id = signal.extra.wiFiStandard.valueResource),
                    color = Frequency,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
                Icon(
                    painter = painterResource(id = wiFiDetail.wiFiSecurity.security.imageResource),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd),
                )
            }
        }

        Column {
            Row {
                Text(
                    text = stringResource(id = R.string.channel_short_name),
                    color = Regular,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = signal.channelDisplay(),
                    color = ChannelNumber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = "${signal.primaryFrequency}${WiFiSignal.FREQUENCY_UNITS}",
                    color = Frequency,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = signal.distance,
                    color = Distance,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row {
                Text(
                    text = "${signal.wiFiChannelStart.frequency} - ${signal.wiFiChannelEnd.frequency}",
                    color = Frequency,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = "(${stringResource(id = signal.wiFiWidth.textResource)})",
                    color = Frequency,
                    modifier = Modifier.padding(end = 4.dp),
                )
                if (wiFiDetail.wiFiAdditional.vendorName.isNotEmpty()) {
                    Text(
                        text = wiFiDetail.wiFiAdditional.vendorName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = wiFiDetail.wiFiSecurity.wiFiSecurityTypesDisplay(context),
                color = Security,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun AccessPointCompact(wiFiDetail: WiFiDetail) {
    val signal = wiFiDetail.wiFiSignal

    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${signal.level}dBm",
            modifier = Modifier.padding(start = 24.dp, end = 4.dp),
            fontSize = 12.sp,
        )
        Text(
            text = stringResource(id = R.string.channel_short_name),
            color = Regular,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = signal.channelDisplay(),
            color = ChannelNumber,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = "${signal.primaryFrequency}${WiFiSignal.FREQUENCY_UNITS}",
            color = Frequency,
            modifier = Modifier.padding(end = 4.dp),
        )
        Icon(
            painter = painterResource(id = wiFiDetail.wiFiSecurity.security.imageResource),
            contentDescription = null,
            modifier =
                Modifier
                    .size(12.dp)
                    .padding(end = 4.dp),
        )
        Text(
            text = signal.distance,
            color = Distance,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccessPointItemCompletePreview() {
    WiFiAnalyzerTheme {
        AccessPointItem(
            wiFiDetail = WiFiDetail.EMPTY,
            viewType = AccessPointViewType.COMPLETE,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccessPointItemCompactPreview() {
    WiFiAnalyzerTheme {
        AccessPointItem(
            wiFiDetail = WiFiDetail.EMPTY,
            viewType = AccessPointViewType.COMPACT,
        )
    }
}
