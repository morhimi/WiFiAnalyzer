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
package com.vrem.wifianalyzer.wifi.gateway

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class PingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pingScreenDisplaysNoGatewayWhenDisconnected() {
        val state = PingUiState(gatewayInfo = GatewayInfo(isConnected = false))

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                PingScreen(
                    uiState = state,
                    onToggle = {},
                    onReset = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No active Wi-Fi gateway detected").assertExists()
    }

    @Test
    fun pingScreenDisplaysMetricsWhenConnected() {
        val state =
            PingUiState(
                gatewayInfo =
                    GatewayInfo(
                        gatewayIp = "192.168.1.1",
                        localIp = "192.168.1.100",
                        isConnected = true,
                        ssid = "MyHomeWiFi",
                        linkSpeedMbps = 433,
                    ),
                status = PingStatus.MEASURING,
                currentRttMs = 12.4,
                minRttMs = 8.1,
                maxRttMs = 25.0,
                avgRttMs = 14.2,
                jitterMs = 1.35,
                packetsSent = 10,
                packetsReceived = 10,
                packetLossPercent = 0.0,
                quality = NetworkQuality.GOOD,
            )

        var toggleClicked = false
        var resetClicked = false

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                PingScreen(
                    uiState = state,
                    onToggle = { toggleClicked = true },
                    onReset = { resetClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("MyHomeWiFi").assertExists()
        composeTestRule.onNodeWithText("Default Gateway: 192.168.1.1").assertExists()
        composeTestRule.onNodeWithText("12.4").assertExists()
        composeTestRule.onNodeWithText("Good").assertExists()
        composeTestRule.onNodeWithText("1.35 ms").assertExists()
        composeTestRule.onNodeWithText("0.0%").assertExists()

        composeTestRule.onNodeWithText("Pause").performScrollTo().performClick()
        assertThat(toggleClicked).isTrue

        composeTestRule.onNodeWithText("Reset").performScrollTo().performClick()
        assertThat(resetClicked).isTrue
    }
}
