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
class AccessPointsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysAccessPointsList() {
        val detail1 =
            WiFiDetail(
                WiFiIdentifier("MyHomeWiFi", "00:11:22:33:44:55"),
                WiFiSecurity.EMPTY,
                WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
            )
        val detail2 =
            WiFiDetail(
                WiFiIdentifier("OfficeWiFi", "AA:BB:CC:DD:EE:FF"),
                WiFiSecurity.EMPTY,
                WiFiSignal(5180, 5180, WiFiWidth.MHZ_80, -60),
            )

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                AccessPointsScreen(
                    wiFiData = WiFiData.EMPTY,
                    wiFiDetails = listOf(detail1, detail2),
                    viewType = AccessPointViewType.COMPLETE,
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

        composeTestRule.onNodeWithText("MyHomeWiFi (00:11:22:33:44:55)").assertExists()
        composeTestRule.onNodeWithText("OfficeWiFi (AA:BB:CC:DD:EE:FF)").assertExists()
    }
}
