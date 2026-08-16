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
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.WiFiScanViewModel
import com.vrem.wifianalyzer.Configuration as WiFiConfiguration

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    wiFiScanViewModel: WiFiScanViewModel,
    settings: Settings,
    wiFiManagerWrapper: WiFiManagerWrapper,
    permissionService: PermissionService,
    scannerService: ScannerService,
    vendorService: VendorService,
    configuration: WiFiConfiguration,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = NavigationMenu.ACCESS_POINTS.route,
        modifier = modifier,
    ) {
        composable(NavigationMenu.ACCESS_POINTS.route) {
            AccessPointsRoute(
                wiFiScanViewModel = wiFiScanViewModel,
                settings = settings,
                wiFiManagerWrapper = wiFiManagerWrapper,
                permissionService = permissionService,
                scannerService = scannerService,
            )
        }
        composable(NavigationMenu.CHANNEL_RATING.route) {
            ChannelRatingRoute(
                wiFiScanViewModel = wiFiScanViewModel,
                settings = settings,
                wiFiManagerWrapper = wiFiManagerWrapper,
                permissionService = permissionService,
                scannerService = scannerService,
            )
        }
        composable(NavigationMenu.CHANNEL_GRAPH.route) {
            ChannelGraphRoute(
                wiFiScanViewModel = wiFiScanViewModel,
                settings = settings,
                wiFiManagerWrapper = wiFiManagerWrapper,
                permissionService = permissionService,
                scannerService = scannerService,
            )
        }
        composable(NavigationMenu.TIME_GRAPH.route) {
            TimeGraphRoute(
                wiFiScanViewModel = wiFiScanViewModel,
                settings = settings,
                wiFiManagerWrapper = wiFiManagerWrapper,
                permissionService = permissionService,
                scannerService = scannerService,
            )
        }
        composable(NavigationMenu.ABOUT.route) {
            AboutRoute(
                wiFiManagerWrapper = wiFiManagerWrapper,
                configuration = configuration,
            )
        }
        composable(NavigationMenu.VENDORS.route) {
            VendorsRoute(vendorService = vendorService)
        }
        composable(NavigationMenu.CHANNEL_AVAILABLE.route) {
            ChannelAvailableRoute(settings = settings)
        }
        composable(NavigationMenu.SETTINGS.route) {
            SettingsRoute(settings = settings)
        }
    }
}
