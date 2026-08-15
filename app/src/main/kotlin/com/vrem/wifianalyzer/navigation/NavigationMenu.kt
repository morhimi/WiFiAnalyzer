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

import com.vrem.wifianalyzer.MainActivity
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.navigation.items.NavigationItem
import com.vrem.wifianalyzer.navigation.items.navigationItemAbout
import com.vrem.wifianalyzer.navigation.items.navigationItemAccessPoints
import com.vrem.wifianalyzer.navigation.items.navigationItemChannelAvailable
import com.vrem.wifianalyzer.navigation.items.navigationItemChannelGraph
import com.vrem.wifianalyzer.navigation.items.navigationItemChannelRating
import com.vrem.wifianalyzer.navigation.items.navigationItemExport
import com.vrem.wifianalyzer.navigation.items.navigationItemSettings
import com.vrem.wifianalyzer.navigation.items.navigationItemTimeGraph
import com.vrem.wifianalyzer.navigation.items.navigationItemVendors

val MAIN_NAVIGATION =
    listOf(
        NavigationMenu.ACCESS_POINTS,
        NavigationMenu.CHANNEL_RATING,
        NavigationMenu.CHANNEL_GRAPH,
        NavigationMenu.TIME_GRAPH,
    )

enum class NavigationMenu(
    val title: Int,
    val icon: Int,
    val navigationItem: NavigationItem,
    val route: String,
    val showWiFiBandSelector: Boolean = false,
    val showFilter: Boolean = false,
    val showScanner: Boolean = false,
) {
    ACCESS_POINTS(
        R.string.action_access_points,
        R.drawable.ic_network_wifi,
        navigationItemAccessPoints,
        "access_points",
        showWiFiBandSelector = false,
        showFilter = true,
        showScanner = true
    ),
    CHANNEL_RATING(
        R.string.action_channel_rating,
        R.drawable.ic_wifi_tethering,
        navigationItemChannelRating,
        "channel_rating",
        showWiFiBandSelector = true,
        showFilter = false,
        showScanner = true
    ),
    CHANNEL_GRAPH(
        R.string.action_channel_graph,
        R.drawable.ic_insert_chart,
        navigationItemChannelGraph,
        "channel_graph",
        showWiFiBandSelector = true,
        showFilter = true,
        showScanner = true
    ),
    TIME_GRAPH(
        R.string.action_time_graph,
        R.drawable.ic_show_chart,
        navigationItemTimeGraph,
        "time_graph",
        showWiFiBandSelector = true,
        showFilter = true,
        showScanner = true
    ),
    EXPORT(
        title = R.string.action_export,
        icon = R.drawable.ic_import_export,
        navigationItem = navigationItemExport,
        route = "export",
    ),
    CHANNEL_AVAILABLE(
        title = R.string.action_channel_available,
        icon = R.drawable.ic_location_on,
        navigationItem = navigationItemChannelAvailable,
        route = "channel_available",
    ),
    VENDORS(
        title = R.string.action_vendors,
        icon = R.drawable.ic_list,
        navigationItem = navigationItemVendors,
        route = "vendors",
    ),
    SETTINGS(
        title = R.string.action_settings,
        icon = R.drawable.ic_settings,
        navigationItem = navigationItemSettings,
        route = "settings",
    ),
    ABOUT(
        title = R.string.action_about,
        icon = R.drawable.ic_info_outline,
        navigationItem = navigationItemAbout,
        route = "about",
    ),
    ;

    fun activateNavigationMenu(mainActivity: MainActivity) = navigationItem.activate(mainActivity, this)

    companion object {
        fun findByRoute(route: String?): NavigationMenu =
            entries.firstOrNull { it.route == route } ?: ACCESS_POINTS
    }
}
