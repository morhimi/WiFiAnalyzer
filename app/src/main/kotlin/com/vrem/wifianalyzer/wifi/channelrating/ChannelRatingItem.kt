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

import android.content.res.ColorStateList
import android.widget.RatingBar
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.vrem.wifianalyzer.R
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
        // Rating Stars (using legacy RatingBar for visual consistency)
        AndroidView(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            factory = { context ->
                RatingBar(context, null, android.R.attr.ratingBarStyleSmall).apply {
                    numStars = 5
                    stepSize = 1f
                }
            },
            update = { ratingBar ->
                val rating = Strength.reverse(strength).ordinal + 1f
                ratingBar.rating = rating
                val color = ContextCompat.getColor(ratingBar.context, Strength.reverse(strength).colorResource)
                ratingBar.progressTintList = ColorStateList.valueOf(color)
            },
        )

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
