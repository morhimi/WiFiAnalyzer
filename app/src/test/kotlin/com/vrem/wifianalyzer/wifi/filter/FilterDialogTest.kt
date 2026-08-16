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
package com.vrem.wifianalyzer.wifi.filter

import android.os.Build
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.settings.SettingsRepository
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class FilterDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val settingsRepository: SettingsRepository = mock()
    private val settingsDataFlow = MutableStateFlow(SettingsData(selectedMenu = NavigationMenu.ACCESS_POINTS))
    private lateinit var settings: Settings
    private lateinit var filtersAdapter: FiltersAdapter

    @Before
    fun setUp() {
        whenever(settingsRepository.settingsData).thenReturn(settingsDataFlow)
        settings = Settings(settingsRepository)
        filtersAdapter = FiltersAdapter(settings)
    }

    @Test
    fun displaysFilterDialogSectionsAndTriggersApply() {
        var applied = false
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                FilterDialog(
                    filtersAdapter = filtersAdapter,
                    settings = settings,
                    onApply = { applied = true },
                    onReset = {},
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Filter").assertExists()
        composeTestRule.onNodeWithText("SSID (case sensitive)").assertExists()
        composeTestRule.onNodeWithText("Wi-Fi Band").assertExists()
        composeTestRule.onNodeWithText("Signal Strength").assertExists()
        composeTestRule.onNodeWithText("Security").assertExists()

        composeTestRule.onNodeWithText("Apply").performClick()
        assertThat(applied).isTrue()
    }

    @Test
    fun triggersResetAction() {
        var reset = false
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                FilterDialog(
                    filtersAdapter = filtersAdapter,
                    settings = settings,
                    onApply = {},
                    onReset = { reset = true },
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Reset").performClick()
        assertThat(reset).isTrue()
    }

    @Test
    fun triggersCloseAction() {
        var dismissed = false
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                FilterDialog(
                    filtersAdapter = filtersAdapter,
                    settings = settings,
                    onApply = {},
                    onReset = {},
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Close").performClick()
        assertThat(dismissed).isTrue()
    }
}
