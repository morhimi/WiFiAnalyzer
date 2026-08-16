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
package com.vrem.wifianalyzer.wifi.detailview

import android.os.Build
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional
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
class WiFiDetailDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createDetail(
        ssid: String = "TestSSID",
        bssid: String = "00:11:22:33:44:55",
    ): WiFiDetail =
        WiFiDetail(
            WiFiIdentifier(ssid, bssid),
            WiFiSecurity.EMPTY,
            WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -60),
            WiFiAdditional.EMPTY,
        )

    @Test
    fun displaysSingleDetailDialogAndDismisses() {
        var dismissed = false
        var editAliasDetail: WiFiDetail? = null
        val detail = createDetail()

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                WiFiDetailDialog(
                    wiFiDetails = listOf(detail),
                    onDismiss = { dismissed = true },
                    onEditAlias = { editAliasDetail = it },
                )
            }
        }

        composeTestRule.onAllNodesWithText("TestSSID", substring = true)[0].assertExists()
        composeTestRule.onNodeWithText("Edit Alias").performClick()
        assertThat(editAliasDetail).isEqualTo(detail)
    }

    @Test
    fun displaysSequenceDetailDialogAndStepsThrough() {
        var dismissed = false
        val detail1 = createDetail("SSID_ONE")
        val detail2 = createDetail("SSID_TWO")

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                WiFiDetailDialog(
                    wiFiDetails = listOf(detail1, detail2),
                    onDismiss = { dismissed = true },
                    onEditAlias = {},
                )
            }
        }

        composeTestRule.onAllNodesWithText("SSID_ONE", substring = true)[0].assertExists()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onAllNodesWithText("SSID_TWO", substring = true)[0].assertExists()
        composeTestRule.onNodeWithText("OK").performClick()
        assertThat(dismissed).isTrue()
    }
}
