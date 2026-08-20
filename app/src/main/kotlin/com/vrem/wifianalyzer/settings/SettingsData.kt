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
package com.vrem.wifianalyzer.settings

import com.vrem.util.defaultCountryCode
import com.vrem.util.defaultLanguageTag
import com.vrem.util.findByLanguageTag
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.GroupBy
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.Strength
import java.util.Locale

data class SettingsData(
    val scanSpeed: Int = 5,
    val cacheOff: Boolean = false,
    val graphMaximumY: Int = -20,
    val wiFiBand: WiFiBand = WiFiBand.GHZ2,
    val countryCode: String = defaultCountryCode(),
    val languageLocale: Locale = findByLanguageTag(defaultLanguageTag()),
    val sortBy: SortBy = SortBy.STRENGTH,
    val groupBy: GroupBy = GroupBy.NONE,
    val accessPointView: AccessPointViewType = AccessPointViewType.COMPLETE,
    val connectionViewType: ConnectionViewType = ConnectionViewType.COMPACT,
    val wiFiOffOnExit: Boolean = false,
    val keepScreenOn: Boolean = false,
    val themeStyle: ThemeStyle = ThemeStyle.DARK,
    val dynamicColor: Boolean = true,
    val selectedMenu: NavigationMenu = NavigationMenu.ACCESS_POINTS,
    val filterSsids: Set<String> = emptySet(),
    val filterWiFiBands: Set<WiFiBand> = setOf(WiFiBand.GHZ2, WiFiBand.GHZ5, WiFiBand.GHZ6),
    val filterStrengths: Set<Strength> = Strength.entries.toSet(),
    val filterSecurities: Set<Security> = Security.entries.toSet(),
)
