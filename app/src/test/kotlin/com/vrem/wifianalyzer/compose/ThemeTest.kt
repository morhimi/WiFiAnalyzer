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
package com.vrem.wifianalyzer.compose

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.settings.ThemeStyle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ThemeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun wiFiAnalyzerThemeWithDarkThemeStyle() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.DARK, dynamicColor = false) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
        assertThat(colorScheme?.background).isEqualTo(Color(0xFF121212))
    }

    @Test
    fun wiFiAnalyzerThemeWithBlackThemeStyle() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.BLACK, dynamicColor = false) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
        assertThat(colorScheme?.background).isEqualTo(Color.Black)
        assertThat(colorScheme?.surface).isEqualTo(Color.Black)
    }

    @Test
    fun wiFiAnalyzerThemeWithLightThemeStyle() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.LIGHT, dynamicColor = false) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
        assertThat(colorScheme?.background).isEqualTo(Color.White)
    }

    @Test
    fun wiFiAnalyzerThemeWithSystemDarkTheme() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.SYSTEM, darkTheme = true, dynamicColor = false) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
        assertThat(colorScheme?.background).isEqualTo(Color(0xFF121212))
    }

    @Test
    fun wiFiAnalyzerThemeWithSystemLightTheme() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.SYSTEM, darkTheme = false, dynamicColor = false) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
        assertThat(colorScheme?.background).isEqualTo(Color.White)
    }

    @Test
    fun wiFiAnalyzerThemeWithDynamicColorDark() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.DARK, dynamicColor = true) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
    }

    @Test
    fun wiFiAnalyzerThemeWithDynamicColorLight() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.LIGHT, dynamicColor = true) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
    }

    @Test
    fun wiFiAnalyzerThemeWithDynamicColorBlack() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.BLACK, dynamicColor = true) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
        assertThat(colorScheme?.background).isEqualTo(Color.Black)
        assertThat(colorScheme?.surface).isEqualTo(Color.Black)
    }

    @Test
    fun wiFiAnalyzerThemeWithDynamicColorSystemDark() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.SYSTEM, darkTheme = true, dynamicColor = true) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
    }

    @Test
    fun wiFiAnalyzerThemeWithDynamicColorSystemLight() {
        var colorScheme: ColorScheme? = null
        composeTestRule.setContent {
            WiFiAnalyzerTheme(themeStyle = ThemeStyle.SYSTEM, darkTheme = false, dynamicColor = true) {
                colorScheme = MaterialTheme.colorScheme
            }
        }
        assertThat(colorScheme).isNotNull
    }
}
