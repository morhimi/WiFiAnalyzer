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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrem.wifianalyzer.compose.ChannelNumber
import com.vrem.wifianalyzer.compose.Selected
import com.vrem.wifianalyzer.wifi.band.WiFiChannel
import com.vrem.wifianalyzer.wifi.model.Strength

@Composable
fun ChannelRatingItem(
    wiFiChannel: WiFiChannel,
    wiFiWidthName: String,
    apCount: Int,
    strength: Strength,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Rating Stars
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val rating = Strength.reverse(strength).ordinal + 1
            val activeColor = colorResource(Strength.reverse(strength).colorResource)
            val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            for (star in 1..5) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (star <= rating) activeColor else inactiveColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Channel and Width
        Row(
            modifier = Modifier.weight(1.6f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = wiFiChannel.channel.toString(),
                color = ChannelNumber,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = wiFiWidthName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        // AP Count
        Text(
            text = apCount.toString(),
            color = Selected,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(0.4f),
        )
    }
}
