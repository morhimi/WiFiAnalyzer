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
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class ApAliasDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val detail =
        WiFiDetail(
            WiFiIdentifier("HomeNetwork", "00:11:22:33:44:55", "Living Room"),
            WiFiSecurity.EMPTY,
            WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
            WiFiAdditional.EMPTY,
        )

    @Test
    fun displaysDialogAndSavesAlias() {
        var savedAlias: String? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ApAliasDialog(
                    wiFiDetail = detail,
                    onSave = { savedAlias = it },
                    onClear = {},
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Access Point Name").assertExists()
        composeTestRule.onNodeWithText("Save").performClick()
        assertThat(savedAlias).isEqualTo("Living Room")
    }

    @Test
    fun triggersClearAlias() {
        var cleared = false
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ApAliasDialog(
                    wiFiDetail = detail,
                    onSave = {},
                    onClear = { cleared = true },
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Clear").performClick()
        assertThat(cleared).isTrue()
    }

    @Test
    fun triggersCancelDismiss() {
        var dismissed = false
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ApAliasDialog(
                    wiFiDetail = detail,
                    onSave = {},
                    onClear = {},
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertThat(dismissed).isTrue()
    }
}
