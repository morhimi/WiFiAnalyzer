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
package com.vrem.wifianalyzer.settings

import android.os.Build
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val settingsRepository: SettingsRepository = mock()
    private val settingsDataFlow = MutableStateFlow(SettingsData())
    private lateinit var settings: Settings

    @Before
    fun setUp() {
        whenever(settingsRepository.settingsData).thenReturn(settingsDataFlow)
        settings = Settings(settingsRepository)
    }

    @Test
    fun displaysAllCategoriesAndSettings() {
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                SettingsScreen(settings = settings)
            }
        }

        composeTestRule.onNode(hasText("Scan Interval") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Sort Access Points By") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Group Access Points By") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Connection Display") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Access Point Display") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Graph Maximum Signal Strength") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Theme") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Keep screen on") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Country") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Language") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Reset") and hasClickAction()).assertExists()
        composeTestRule.onNode(hasText("Cache off") and hasClickAction()).assertExists()
    }

    @Test
    fun clickingScanSpeedOpensDialog() {
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                SettingsScreen(settings = settings)
            }
        }

        composeTestRule.onNode(hasText("Scan Interval") and hasClickAction()).performClick()
        composeTestRule.onNodeWithText("10 seconds").assertExists()
    }

    @Test
    fun clickingResetOpensConfirmationDialogAndTriggersReset() =
        runTest {
            composeTestRule.setContent {
                WiFiAnalyzerTheme {
                    SettingsScreen(settings = settings)
                }
            }

            composeTestRule
                .onNode(hasText("Reset") and hasClickAction())
                .performScrollTo()
                .performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNode(hasText("Apply") and hasClickAction()).performClick()
            composeTestRule.waitForIdle()

            verify(settingsRepository).resetToDefaults()
        }
}
