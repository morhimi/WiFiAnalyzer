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

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object AccessPoints : Screen

    @Serializable
    data object ChannelRating : Screen

    @Serializable
    data object ChannelGraph : Screen

    @Serializable
    data object TimeGraph : Screen

    @Serializable
    data object About : Screen

    @Serializable
    data object Vendors : Screen

    @Serializable
    data object ChannelAvailable : Screen

    @Serializable
    data object Settings : Screen
}
