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
package com.vrem.wifianalyzer.navigation

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.vrem.wifianalyzer.settings.SettingsFragment

@Composable
fun LegacyFragmentRoute(fragmentProvider: () -> Fragment) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    AndroidView(
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = View.generateViewId()
                fragmentActivity?.supportFragmentManager?.commit {
                    replace(id, fragmentProvider())
                }
            }
        },
        update = { /* No-op */ },
    )
}

@Composable
fun SettingsRoute() = LegacyFragmentRoute { SettingsFragment() }
