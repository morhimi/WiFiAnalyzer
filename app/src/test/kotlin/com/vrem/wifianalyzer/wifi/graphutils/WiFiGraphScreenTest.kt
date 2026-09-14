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
package com.vrem.wifianalyzer.wifi.graphutils

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.model.WiFiData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class WiFiGraphScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val graphAdapter: GraphAdapter = mock()

    @Test
    fun graphScreenUpdatesGraphAdapter() {
        val settingsData = SettingsData()

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                WiFiGraphScreen(
                    wiFiData = WiFiData.EMPTY,
                    settingsData = settingsData,
                    graphAdapter = graphAdapter,
                    displayedChild = 0,
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

        composeTestRule.waitForIdle()
        verify(graphAdapter).update(WiFiData.EMPTY, settingsData)
    }
}
