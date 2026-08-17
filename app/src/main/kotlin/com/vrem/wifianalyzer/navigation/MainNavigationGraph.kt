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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    onDetailClick: (WiFiDetail) -> Unit,
    onShowWiFiDetails: (List<WiFiDetail>) -> Unit = { details -> details.firstOrNull()?.let(onDetailClick) },
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.AccessPoints,
        modifier = modifier,
    ) {
        composable<Screen.AccessPoints> {
            AccessPointsRoute(onDetailClick = onDetailClick)
        }
        composable<Screen.ChannelRating> {
            ChannelRatingRoute(onDetailClick = onDetailClick)
        }
        composable<Screen.ChannelGraph> {
            ChannelGraphRoute(
                onDetailClick = onDetailClick,
                onShowWiFiDetails = onShowWiFiDetails,
            )
        }
        composable<Screen.TimeGraph> {
            TimeGraphRoute(
                onDetailClick = onDetailClick,
                onShowWiFiDetails = onShowWiFiDetails,
            )
        }
        composable<Screen.About> {
            AboutRoute()
        }
        composable<Screen.Vendors> {
            VendorsRoute()
        }
        composable<Screen.ChannelAvailable> {
            ChannelAvailableRoute()
        }
        composable<Screen.Settings> {
            SettingsRoute()
        }
    }
}
