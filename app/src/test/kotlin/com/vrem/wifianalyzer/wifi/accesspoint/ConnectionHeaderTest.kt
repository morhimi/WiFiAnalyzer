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

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional
import com.vrem.wifianalyzer.wifi.model.WiFiConnection
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import com.vrem.wifianalyzer.wifi.model.WiFiSecurity
import com.vrem.wifianalyzer.wifi.model.WiFiSignal
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ConnectionHeaderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val identifier = WiFiIdentifier("TestNetwork", "00:11:22:33:44:55", "Home Router")
    private val connection = WiFiConnection(identifier, "192.168.1.100", 866)
    private val connectedDetail =
        WiFiDetail(
            identifier,
            WiFiSecurity.EMPTY,
            WiFiSignal(5180, 5180, WiFiWidth.MHZ_80, -45),
            WiFiAdditional("VendorName", connection),
        )

    @Test
    fun displaysConnectionHeaderInLightMode() {
        val wiFiData = WiFiData(listOf(connectedDetail), connectedDetail.wiFiAdditional.wiFiConnection)

        composeTestRule.setContent {
            WiFiAnalyzerTheme(darkTheme = false) {
                ConnectionHeader(
                    wiFiData = wiFiData,
                    wiFiBandAvailable = true,
                    wiFiBandName = "5 GHz",
                    scanThrottleEnabled = false,
                    permissionEnabled = true,
                    isScanning = false,
                    onDetailClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Current connection").assertExists()
        composeTestRule.onNodeWithText("Home Router (TestNetwork)").assertExists()
        composeTestRule.onNodeWithText("866Mbps").assertExists()
        composeTestRule.onNodeWithText("192.168.1.100").assertExists()
    }

    @Test
    fun displaysConnectionHeaderInDarkMode() {
        val wiFiData = WiFiData(listOf(connectedDetail), connectedDetail.wiFiAdditional.wiFiConnection)

        composeTestRule.setContent {
            WiFiAnalyzerTheme(darkTheme = true) {
                ConnectionHeader(
                    wiFiData = wiFiData,
                    wiFiBandAvailable = true,
                    wiFiBandName = "5 GHz",
                    scanThrottleEnabled = false,
                    permissionEnabled = true,
                    isScanning = false,
                    onDetailClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Current connection").assertExists()
        composeTestRule.onNodeWithText("Home Router (TestNetwork)").assertExists()
    }
}
