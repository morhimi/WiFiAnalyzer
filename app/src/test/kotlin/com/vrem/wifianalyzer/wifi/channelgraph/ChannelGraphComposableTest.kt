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

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.RobolectricUtil
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import com.vrem.wifianalyzer.wifi.model.WiFiSecurity
import com.vrem.wifianalyzer.wifi.model.WiFiSignal
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ChannelGraphComposableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val activity = RobolectricUtil.INSTANCE.activity

    @Test
    fun tappingOnChannelGraphInvokesOnShowWiFiDetails() {
        var capturedDetails: List<WiFiDetail>? = null
        val channelGraph =
            ChannelGraph(
                wiFiBand = WiFiBand.GHZ2,
                context = activity,
                onShowWiFiDetails = { capturedDetails = it },
            )
        val wiFiDetail =
            WiFiDetail(
                wiFiIdentifier = WiFiIdentifier("TestSSID", "00:11:22:33:44:55"),
                wiFiSecurity = WiFiSecurity.EMPTY,
                wiFiSignal = WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
            )
        val wiFiData = WiFiData(listOf(wiFiDetail), com.vrem.wifianalyzer.wifi.model.WiFiConnection.EMPTY)
        val settingsData = SettingsData(wiFiBand = WiFiBand.GHZ2)

        channelGraph.update(wiFiData, settingsData)

        composeTestRule.setContent {
            channelGraph.Content(
                modifier = Modifier.fillMaxSize().testTag("channel_graph"),
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("channel_graph").performTouchInput {
            click(
                androidx.compose.ui.geometry
                    .Offset(x = width * 0.15f, y = height * 0.5f),
            )
        }

        composeTestRule.waitForIdle()

        assertThat(capturedDetails).containsExactly(wiFiDetail)
    }
}
