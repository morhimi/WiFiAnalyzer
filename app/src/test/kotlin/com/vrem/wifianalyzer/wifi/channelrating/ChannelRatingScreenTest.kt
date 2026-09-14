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

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.band.WiFiChannel
import com.vrem.wifianalyzer.wifi.model.ChannelAPCount
import com.vrem.wifianalyzer.wifi.model.ChannelRating
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ChannelRatingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysBestChannelsAndRatingItems() {
        val wiFiBand = WiFiBand.GHZ2
        val channel1 = WiFiChannel(1, 2412)
        val channel6 = WiFiChannel(6, 2437)
        val wiFiChannels = listOf(channel1, channel6)
        val bestChannels =
            listOf(
                ChannelAPCount(channel1, WiFiWidth.MHZ_20, 0),
                ChannelAPCount(channel6, WiFiWidth.MHZ_20, 1),
            )
        val channelRating = ChannelRating()

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ChannelRatingScreen(
                    wiFiData = WiFiData.EMPTY,
                    wiFiBand = wiFiBand,
                    wiFiChannels = wiFiChannels,
                    bestChannels = bestChannels,
                    channelRating = channelRating,
                    wiFiBandAvailable = true,
                    wiFiBandName = "2.4 GHz",
                    scanThrottleEnabled = false,
                    permissionEnabled = true,
                    isScanning = true,
                    isRefreshing = false,
                    onRefresh = {},
                    onDetailClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithText("6").assertExists()
    }
}
