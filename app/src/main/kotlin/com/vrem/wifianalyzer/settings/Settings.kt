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

import com.vrem.util.findByLanguageTag
import com.vrem.util.ordinals
import com.vrem.wifianalyzer.navigation.MAIN_NAVIGATION
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.GroupBy
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.Strength
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class Settings(
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
) {
    private val dataFlow = settingsRepository.settingsData
    private val _settingsData = MutableStateFlow(SettingsData())
    val settingsData: StateFlow<SettingsData> = _settingsData.asStateFlow()

    init {
        scope.launch {
            dataFlow.collectLatest { data ->
                _settingsData.value = data
            }
        }
    }

    // Reactive / Synchronous snapshot accessors
    fun scanSpeed(): Int = settingsData.value.scanSpeed

    fun cacheOff(): Boolean = settingsData.value.cacheOff

    fun graphMaximumY(): Int = settingsData.value.graphMaximumY

    fun countryCode(): String = settingsData.value.countryCode

    fun languageLocale(): Locale = settingsData.value.languageLocale

    fun sortBy(): SortBy = settingsData.value.sortBy

    fun groupBy(): GroupBy = settingsData.value.groupBy

    fun accessPointView(): AccessPointViewType = settingsData.value.accessPointView

    fun connectionViewType(): ConnectionViewType = settingsData.value.connectionViewType

    fun wiFiBand(): WiFiBand = settingsData.value.wiFiBand

    fun wiFiOffOnExit(): Boolean = settingsData.value.wiFiOffOnExit

    fun keepScreenOn(): Boolean = settingsData.value.keepScreenOn

    fun themeStyle(): ThemeStyle = settingsData.value.themeStyle

    fun selectedMenu(): NavigationMenu = settingsData.value.selectedMenu

    fun findSSIDs(): Set<String> = settingsData.value.filterSsids

    fun findWiFiBands(): Set<WiFiBand> = settingsData.value.filterWiFiBands

    fun findStrengths(): Set<Strength> = settingsData.value.filterStrengths

    fun findSecurities(): Set<Security> = settingsData.value.filterSecurities

    // Update / Save methods
    fun wiFiBand(wiFiBand: WiFiBand) {
        _settingsData.update { it.copy(wiFiBand = wiFiBand) }
        scope.launch { settingsRepository.updateWiFiBand(wiFiBand.ordinal) }
    }

    fun saveSelectedMenu(navigationMenu: NavigationMenu) {
        if (MAIN_NAVIGATION.contains(navigationMenu)) {
            _settingsData.update { it.copy(selectedMenu = navigationMenu) }
            scope.launch { settingsRepository.updateSelectedMenu(navigationMenu.ordinal) }
        }
    }

    fun saveSSIDs(values: Set<String>) {
        _settingsData.update { it.copy(filterSsids = values) }
        scope.launch { settingsRepository.updateFilterSsids(values) }
    }

    fun saveWiFiBands(values: Set<WiFiBand>) {
        _settingsData.update { it.copy(filterWiFiBands = values) }
        scope.launch { settingsRepository.updateFilterWiFiBands(ordinals(values)) }
    }

    fun saveStrengths(values: Set<Strength>) {
        _settingsData.update { it.copy(filterStrengths = values) }
        scope.launch { settingsRepository.updateFilterStrengths(ordinals(values)) }
    }

    fun saveSecurities(values: Set<Security>) {
        _settingsData.update { it.copy(filterSecurities = values) }
        scope.launch { settingsRepository.updateFilterSecurities(ordinals(values)) }
    }

    fun updateScanSpeed(scanSpeed: Int) {
        _settingsData.update { it.copy(scanSpeed = scanSpeed) }
        scope.launch { settingsRepository.updateScanSpeed(scanSpeed.toString()) }
    }

    fun updateCacheOff(cacheOff: Boolean) {
        _settingsData.update { it.copy(cacheOff = cacheOff) }
        scope.launch { settingsRepository.updateCacheOff(cacheOff) }
    }

    fun updateGraphMaximumY(graphMaximumY: Int) {
        _settingsData.update { it.copy(graphMaximumY = graphMaximumY) }
        scope.launch { settingsRepository.updateGraphMaximumY((graphMaximumY / GRAPH_Y_MULTIPLIER).toString()) }
    }

    fun updateCountryCode(countryCode: String) {
        _settingsData.update { it.copy(countryCode = countryCode) }
        scope.launch { settingsRepository.updateCountryCode(countryCode) }
    }

    fun updateLanguage(languageTag: String) {
        _settingsData.update { it.copy(languageLocale = findByLanguageTag(languageTag)) }
        scope.launch { settingsRepository.updateLanguage(languageTag) }
    }

    fun updateSortBy(sortBy: SortBy) {
        _settingsData.update { it.copy(sortBy = sortBy) }
        scope.launch { settingsRepository.updateSortBy(sortBy.ordinal) }
    }

    fun updateGroupBy(groupBy: GroupBy) {
        _settingsData.update { it.copy(groupBy = groupBy) }
        scope.launch { settingsRepository.updateGroupBy(groupBy.ordinal) }
    }

    fun updateAccessPointView(accessPointView: AccessPointViewType) {
        _settingsData.update { it.copy(accessPointView = accessPointView) }
        scope.launch { settingsRepository.updateApView(accessPointView.ordinal) }
    }

    fun updateConnectionView(connectionViewType: ConnectionViewType) {
        _settingsData.update { it.copy(connectionViewType = connectionViewType) }
        scope.launch { settingsRepository.updateConnectionView(connectionViewType.ordinal) }
    }

    fun updateWiFiOffOnExit(wiFiOffOnExit: Boolean) {
        _settingsData.update { it.copy(wiFiOffOnExit = wiFiOffOnExit) }
        scope.launch { settingsRepository.updateWiFiOffOnExit(wiFiOffOnExit) }
    }

    fun updateKeepScreenOn(keepScreenOn: Boolean) {
        _settingsData.update { it.copy(keepScreenOn = keepScreenOn) }
        scope.launch { settingsRepository.updateKeepScreenOn(keepScreenOn) }
    }

    fun updateTheme(themeStyle: ThemeStyle) {
        _settingsData.update { it.copy(themeStyle = themeStyle) }
        scope.launch { settingsRepository.updateTheme(themeStyle.ordinal) }
    }

    fun reset() {
        scope.launch { settingsRepository.resetToDefaults() }
    }

    companion object {
        private const val GRAPH_Y_MULTIPLIER = -10
    }
}
