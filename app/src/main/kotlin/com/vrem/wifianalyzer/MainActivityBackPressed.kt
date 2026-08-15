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

package com.vrem.wifianalyzer

import androidx.activity.OnBackPressedCallback
import com.vrem.wifianalyzer.navigation.NavigationMenu

class MainActivityBackPressed(
    private val mainActivity: MainActivity,
) : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        if (mainActivity.closeDrawer()) return

        val navController = mainActivity.navController
        if (navController.currentBackStackEntry?.destination?.route != NavigationMenu.ACCESS_POINTS.route) {
            mainActivity.onNavigationItemSelected(
                mainActivity.navigationView().menu.findItem(NavigationMenu.ACCESS_POINTS.idDrawer)
            )
        } else {
            mainActivity.finish()
        }
    }
}
