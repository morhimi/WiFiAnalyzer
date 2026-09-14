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
package com.vrem.wifianalyzer.about

import android.os.Build
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class AboutScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysApplicationInformation() {
        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                AboutScreen(
                    applicationName = "WiFiAnalyzer",
                    packageName = "com.vrem.wifianalyzer",
                    versionInfo = "1.0.0 (100)",
                    copyright = "Copyright © 2015 - 2026",
                    device = "Pixel 7 Pro",
                    isScanThrottleEnabled = false,
                    is5GHzBandSupported = true,
                    is6GHzBandSupported = true,
                    onWriteReview = {},
                    onShowLicense = { _, _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithText("WiFiAnalyzer").assertExists()
        composeTestRule.onNodeWithText("com.vrem.wifianalyzer").assertExists()
        composeTestRule.onNodeWithText("1.0.0 (100)").assertExists()
        composeTestRule.onNodeWithText("Copyright © 2015 - 2026").assertExists()
        composeTestRule.onNodeWithText("Pixel 7 Pro").assertExists()
    }

    @Test
    fun writeReviewAndContributorsButtonTriggersCallbacks() {
        var reviewClicked = false
        var licenseTitleId = 0

        composeTestRule.setContent {
            WiFiAnalyzerTheme {
                AboutScreen(
                    applicationName = "WiFiAnalyzer",
                    packageName = "com.vrem.wifianalyzer",
                    versionInfo = "1.0.0 (100)",
                    copyright = "Copyright © 2015 - 2026",
                    device = "Pixel 7 Pro",
                    isScanThrottleEnabled = false,
                    is5GHzBandSupported = true,
                    is6GHzBandSupported = true,
                    onWriteReview = { reviewClicked = true },
                    onShowLicense = { title, _, _ -> licenseTitleId = title },
                )
            }
        }

        composeTestRule.onNode(hasText("Contributors")).performScrollTo().performClick()
        assertThat(licenseTitleId).isEqualTo(R.string.about_contributor_title)

        composeTestRule.onNode(hasText("Write a Review")).performScrollTo().performClick()
        assertThat(reviewClicked).isTrue
    }
}
