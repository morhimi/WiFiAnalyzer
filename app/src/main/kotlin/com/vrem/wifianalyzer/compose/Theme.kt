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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.vrem.wifianalyzer.settings.ThemeStyle

private val DarkColorScheme =
    darkColorScheme(
        primary = Selected,
        secondary = ChannelNumber,
        tertiary = SuccessColor,
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onTertiary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = Color.LightGray,
        error = ErrorColor,
    )

private val BlackColorScheme =
    darkColorScheme(
        primary = Selected,
        secondary = ChannelNumber,
        tertiary = SuccessColor,
        background = Color.Black,
        surface = Color.Black,
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onTertiary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = Color.LightGray,
        error = ErrorColor,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Selected,
        secondary = ChannelNumber,
        tertiary = SuccessColor,
        background = Color.White,
        surface = Color(0xFFF5F5F5),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onTertiary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        onSurfaceVariant = Color.DarkGray,
        error = ErrorColor,
    )

@Composable
fun WiFiAnalyzerTheme(
    themeStyle: ThemeStyle = ThemeStyle.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                when (themeStyle) {
                    ThemeStyle.BLACK ->
                        dynamicDarkColorScheme(context).copy(
                            background = Color.Black,
                            surface = Color.Black,
                        )
                    ThemeStyle.DARK -> dynamicDarkColorScheme(context)
                    ThemeStyle.LIGHT -> dynamicLightColorScheme(context)
                    ThemeStyle.SYSTEM ->
                        if (darkTheme) {
                            dynamicDarkColorScheme(context)
                        } else {
                            dynamicLightColorScheme(context)
                        }
                }
            }
            else ->
                when (themeStyle) {
                    ThemeStyle.BLACK -> BlackColorScheme
                    ThemeStyle.DARK -> DarkColorScheme
                    ThemeStyle.LIGHT -> LightColorScheme
                    ThemeStyle.SYSTEM -> if (darkTheme) DarkColorScheme else LightColorScheme
                }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
