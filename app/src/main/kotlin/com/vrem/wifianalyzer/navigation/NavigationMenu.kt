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

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.vrem.wifianalyzer.R

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
    val screen: Screen?,
    val showWiFiBandSelector: Boolean = false,
    val showFilter: Boolean = false,
    val showScanner: Boolean = false,
) {
    ACCESS_POINTS(
        title = R.string.action_access_points,
        icon = R.drawable.ic_network_wifi,
        screen = Screen.AccessPoints,
        showWiFiBandSelector = false,
        showFilter = true,
        showScanner = true,
    ),
    CHANNEL_RATING(
        title = R.string.action_channel_rating,
        icon = R.drawable.ic_wifi_tethering,
        screen = Screen.ChannelRating,
        showWiFiBandSelector = true,
        showFilter = false,
        showScanner = true,
    ),
    CHANNEL_GRAPH(
        title = R.string.action_channel_graph,
        icon = R.drawable.ic_insert_chart,
        screen = Screen.ChannelGraph,
        showWiFiBandSelector = true,
        showFilter = true,
        showScanner = true,
    ),
    TIME_GRAPH(
        title = R.string.action_time_graph,
        icon = R.drawable.ic_show_chart,
        screen = Screen.TimeGraph,
        showWiFiBandSelector = true,
        showFilter = true,
        showScanner = true,
    ),
    EXPORT(
        title = R.string.action_export,
        icon = R.drawable.ic_import_export,
        screen = null,
    ),
    CHANNEL_AVAILABLE(
        title = R.string.action_channel_available,
        icon = R.drawable.ic_location_on,
        screen = Screen.ChannelAvailable,
    ),
    VENDORS(
        title = R.string.action_vendors,
        icon = R.drawable.ic_list,
        screen = Screen.Vendors,
    ),
    SETTINGS(
        title = R.string.action_settings,
        icon = R.drawable.ic_settings,
        screen = Screen.Settings,
    ),
    PING(
        title = R.string.action_ping,
        icon = R.drawable.ic_network_check,
        screen = Screen.Ping,
    ),
    ABOUT(
        title = R.string.action_about,
        icon = R.drawable.ic_info_outline,
        screen = Screen.About,
    ),
    ;

    companion object {
        fun findByDestination(destination: NavDestination?): NavigationMenu =
            entries.firstOrNull { menu ->
                menu.screen != null && destination?.hasRoute(menu.screen::class) == true
            } ?: ACCESS_POINTS
    }
}
