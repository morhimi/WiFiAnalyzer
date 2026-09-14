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
package com.vrem.wifianalyzer.wifi.channelavailable

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ChannelAvailableScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysCountryNameAndBands() {
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ChannelAvailableScreen(
                    countryCode = "US",
                    languageLocale = Locale.US,
                )
            }
        }

        composeTestRule.onNodeWithText("US").assertExists()
        composeTestRule.onNodeWithText("United States").assertExists()
        composeTestRule.onNodeWithText("2.4 GHz").assertExists()
        composeTestRule.onNodeWithText("5 GHz").assertExists()
        composeTestRule.onNodeWithText("6 GHz").assertExists()
    }
}
