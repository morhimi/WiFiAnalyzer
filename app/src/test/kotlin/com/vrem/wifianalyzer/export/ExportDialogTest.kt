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

package com.vrem.wifianalyzer.export

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ExportDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysDialogAndSharesDefaultCsv() {
        var sharedFormat: ExportFormat? = null
        var dismissed = false

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ExportDialog(
                    onDismiss = { dismissed = true },
                    onShare = { sharedFormat = it },
                    onSaveToFile = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Export").assertExists()
        composeTestRule.onNodeWithText("Export Format").assertExists()
        composeTestRule.onNodeWithText("CSV").assertExists()
        composeTestRule.onNodeWithText("JSON").assertExists()
        composeTestRule.onNodeWithText("Plain Text").assertExists()

        composeTestRule.onNodeWithText("Share").performClick()

        assertThat(sharedFormat).isEqualTo(ExportFormat.CSV)
        assertThat(dismissed).isTrue()
    }

    @Test
    fun selectsJsonAndShares() {
        var sharedFormat: ExportFormat? = null
        var dismissed = false

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ExportDialog(
                    onDismiss = { dismissed = true },
                    onShare = { sharedFormat = it },
                    onSaveToFile = {},
                )
            }
        }

        composeTestRule.onNodeWithText("JSON").performClick()
        composeTestRule.onNodeWithText("Share").performClick()

        assertThat(sharedFormat).isEqualTo(ExportFormat.JSON)
        assertThat(dismissed).isTrue()
    }

    @Test
    fun selectsTextAndSavesToFile() {
        var savedFormat: ExportFormat? = null
        var dismissed = false

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ExportDialog(
                    onDismiss = { dismissed = true },
                    onShare = {},
                    onSaveToFile = { savedFormat = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Plain Text").performClick()
        composeTestRule.onNodeWithText("Save to File").performClick()

        assertThat(savedFormat).isEqualTo(ExportFormat.TEXT)
        assertThat(dismissed).isTrue()
    }

    @Test
    fun cancelsDialog() {
        var dismissed = false

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                ExportDialog(
                    onDismiss = { dismissed = true },
                    onShare = {},
                    onSaveToFile = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertThat(dismissed).isTrue()
    }
}
