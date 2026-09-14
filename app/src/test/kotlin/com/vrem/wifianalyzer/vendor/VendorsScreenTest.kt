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
package com.vrem.wifianalyzer.vendor

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class VendorsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysVendorsAndMacAddresses() {
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                VendorsScreen(
                    searchQuery = "",
                    onSearchQueryChange = {},
                    vendors = listOf("Apple", "Google"),
                    findMacAddresses = { vendor ->
                        if (vendor == "Apple") listOf("00:11:22", "33:44:55") else listOf("66:77:88")
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Apple").assertExists()
        composeTestRule.onNodeWithText("00:11:22, 33:44:55").assertExists()
        composeTestRule.onNodeWithText("Google").assertExists()
        composeTestRule.onNodeWithText("66:77:88").assertExists()
    }

    @Test
    fun typingInSearchFieldTriggersCallback() {
        var newQuery = ""

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                VendorsScreen(
                    searchQuery = "",
                    onSearchQueryChange = { newQuery = it },
                    vendors = emptyList(),
                    findMacAddresses = { emptyList() },
                )
            }
        }

        composeTestRule.onNodeWithText("00:0C:41 CISCO").performTextInput("Cisco")
        assertThat(newQuery).isEqualTo("Cisco")
    }
}
