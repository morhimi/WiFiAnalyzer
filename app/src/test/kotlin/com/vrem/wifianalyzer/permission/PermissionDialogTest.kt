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
package com.vrem.wifianalyzer.permission

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class PermissionDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun displaysDialogAndConfirms() {
        var confirmed = false
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                PermissionRationaleDialog(
                    onConfirm = { confirmed = true },
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.app_full_name)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.throttling_msg)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.permission_msg)).assertExists()
        composeTestRule.onNodeWithText(context.getString(android.R.string.ok)).performClick()

        assertThat(confirmed).isTrue()
    }

    @Test
    fun displaysDialogAndDismisses() {
        var dismissed = false
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                PermissionRationaleDialog(
                    onConfirm = {},
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(android.R.string.cancel)).performClick()

        assertThat(dismissed).isTrue()
    }
}
