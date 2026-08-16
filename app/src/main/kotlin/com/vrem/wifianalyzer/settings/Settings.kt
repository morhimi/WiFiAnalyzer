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

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.vrem.annotation.OpenClass
import com.vrem.util.buildMinVersionQ
import com.vrem.util.defaultCountryCode
import com.vrem.util.defaultLanguageTag
import com.vrem.util.findByLanguageTag
import com.vrem.util.findOne
import com.vrem.util.findSet
import com.vrem.util.ordinals
import com.vrem.wifianalyzer.R
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.enums.EnumEntries

@OpenClass
class Settings(
    private val repository: Repository,
    private val settingsRepository: SettingsRepository? = null,
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _settingsData = MutableStateFlow(SettingsData())
    val settingsData: StateFlow<SettingsData> = _settingsData.asStateFlow()

    private val sharedPreferenceChangeListener =
        OnSharedPreferenceChangeListener { _, key ->
            val currentSettings = transformSync()
            _settingsData.update { currentSettings }

            key?.let { k ->
                scope.launch {
                    when (k) {
                        repository.contextString(
                            R.string.scan_speed_key,
                        ),
                        -> settingsRepository?.updateScanSpeed(currentSettings.scanSpeed.toString())
                        repository.contextString(
                            R.string.cache_off_key,
                        ),
                        -> settingsRepository?.updateCacheOff(currentSettings.cacheOff)
                        repository.contextString(R.string.graph_maximum_y_key) ->
                            settingsRepository?.updateGraphMaximumY(
                                (
                                    currentSettings.graphMaximumY /
                                        -10
                                ).toString(),
                            )
                        repository.contextString(
                            R.string.wifi_band_key,
                        ),
                        -> settingsRepository?.updateWiFiBand(currentSettings.wiFiBand.ordinal)
                        repository.contextString(
                            R.string.country_code_key,
                        ),
                        -> settingsRepository?.updateCountryCode(currentSettings.countryCode)
                        repository.contextString(
                            R.string.language_key,
                        ),
                        -> settingsRepository?.updateLanguage(currentSettings.languageLocale.toLanguageTag())
                        repository.contextString(
                            R.string.sort_by_key,
                        ),
                        -> settingsRepository?.updateSortBy(currentSettings.sortBy.ordinal)
                        repository.contextString(
                            R.string.group_by_key,
                        ),
                        -> settingsRepository?.updateGroupBy(currentSettings.groupBy.ordinal)
                        repository.contextString(
                            R.string.ap_view_key,
                        ),
                        -> settingsRepository?.updateApView(currentSettings.accessPointView.ordinal)
                        repository.contextString(
                            R.string.connection_view_key,
                        ),
                        -> settingsRepository?.updateConnectionView(currentSettings.connectionViewType.ordinal)
                        repository.contextString(
                            R.string.wifi_off_on_exit_key,
                        ),
                        -> settingsRepository?.updateWiFiOffOnExit(currentSettings.wiFiOffOnExit)
                        repository.contextString(
                            R.string.keep_screen_on_key,
                        ),
                        -> settingsRepository?.updateKeepScreenOn(currentSettings.keepScreenOn)
                        repository.contextString(
                            R.string.theme_key,
                        ),
                        -> settingsRepository?.updateTheme(currentSettings.themeStyle.ordinal)
                        repository.contextString(
                            R.string.selected_menu_key,
                        ),
                        -> settingsRepository?.updateSelectedMenu(currentSettings.selectedMenu.ordinal)
                        repository.contextString(
                            R.string.filter_ssid_key,
                        ),
                        -> settingsRepository?.updateFilterSsids(currentSettings.filterSsids)
                        repository.contextString(
                            R.string.filter_wifi_band_key,
                        ),
                        -> settingsRepository?.updateFilterWiFiBands(ordinals(currentSettings.filterWiFiBands))
                        repository.contextString(
                            R.string.filter_strength_key,
                        ),
                        -> settingsRepository?.updateFilterStrengths(ordinals(currentSettings.filterStrengths))
                        repository.contextString(
                            R.string.filter_security_key,
                        ),
                        -> settingsRepository?.updateFilterSecurities(ordinals(currentSettings.filterSecurities))
                    }
                }
            }
        }

    init {
        settingsRepository?.let {
            _settingsData.value = transformSync()
            repository.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        }
    }

    private fun transformSync(): SettingsData =
        SettingsData(
            scanSpeed = scanSpeedSync(),
            cacheOff = cacheOffSync(),
            graphMaximumY = graphMaximumYSync(),
            wiFiBand = wiFiBandSync(),
            countryCode = countryCodeSync(),
            languageLocale = languageLocaleSync(),
            sortBy = sortBySync(),
            groupBy = groupBySync(),
            accessPointView = accessPointViewSync(),
            connectionViewType = connectionViewTypeSync(),
            wiFiOffOnExit = wiFiOffOnExitSync(),
            keepScreenOn = keepScreenOnSync(),
            themeStyle = themeStyleSync(),
            selectedMenu = selectedMenuSync(),
            filterSsids = findSSIDsSync(),
            filterWiFiBands = findWiFiBandsSync(),
            filterStrengths = findStrengthsSync(),
            filterSecurities = findSecuritiesSync(),
        )

    fun initializeDefaultValues() {
        repository.initializeDefaultValues()
    }

    fun registerOnSharedPreferenceChangeListener(
        onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener,
    ): Unit = repository.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)

    // Reactive methods
    fun scanSpeed(): Int = scanSpeedSync()

    fun cacheOff(): Boolean = cacheOffSync()

    fun graphMaximumY(): Int = graphMaximumYSync()

    fun countryCode(): String = countryCodeSync()

    fun languageLocale(): Locale = languageLocaleSync()

    fun sortBy(): SortBy = sortBySync()

    fun groupBy(): GroupBy = groupBySync()

    fun accessPointView(): AccessPointViewType = accessPointViewSync()

    fun connectionViewType(): ConnectionViewType = connectionViewTypeSync()

    fun wiFiBand(): WiFiBand = wiFiBandSync()

    fun wiFiOffOnExit(): Boolean = wiFiOffOnExitSync()

    fun keepScreenOn(): Boolean = keepScreenOnSync()

    fun themeStyle(): ThemeStyle = themeStyleSync()

    fun selectedMenu(): NavigationMenu = selectedMenuSync()

    fun findSSIDs(): Set<String> = findSSIDsSync()

    fun findWiFiBands(): Set<WiFiBand> = findWiFiBandsSync()

    fun findStrengths(): Set<Strength> = findStrengthsSync()

    fun findSecurities(): Set<Security> = findSecuritiesSync()

    // Sync methods (internal/private)
    private fun scanSpeedSync(): Int =
        repository.stringAsInteger(
            R.string.scan_speed_key,
            repository.stringAsInteger(R.string.scan_speed_default, SCAN_SPEED_DEFAULT),
        )

    private fun cacheOffSync(): Boolean =
        repository.boolean(R.string.cache_off_key, repository.resourceBoolean(R.bool.cache_off_default))

    private fun graphMaximumYSync(): Int {
        val defaultValue = repository.stringAsInteger(R.string.graph_maximum_y_default, GRAPH_Y_DEFAULT)
        val result = repository.stringAsInteger(R.string.graph_maximum_y_key, defaultValue)
        return result * GRAPH_Y_MULTIPLIER
    }

    private fun wiFiBandSync(): WiFiBand = settingsFind(WiFiBand.entries, R.string.wifi_band_key, WiFiBand.GHZ2)

    private fun countryCodeSync(): String = repository.string(R.string.country_code_key, defaultCountryCode())

    private fun languageLocaleSync(): Locale {
        val defaultLanguageTag = defaultLanguageTag()
        val languageTag = repository.string(R.string.language_key, defaultLanguageTag)
        return findByLanguageTag(languageTag)
    }

    private fun sortBySync(): SortBy = settingsFind(SortBy.entries, R.string.sort_by_key, SortBy.STRENGTH)

    private fun groupBySync(): GroupBy = settingsFind(GroupBy.entries, R.string.group_by_key, GroupBy.NONE)

    private fun accessPointViewSync(): AccessPointViewType =
        settingsFind(AccessPointViewType.entries, R.string.ap_view_key, AccessPointViewType.COMPLETE)

    private fun connectionViewTypeSync(): ConnectionViewType =
        settingsFind(ConnectionViewType.entries, R.string.connection_view_key, ConnectionViewType.COMPACT)

    private fun wiFiOffOnExitSync(): Boolean =
        if (buildMinVersionQ()) {
            false
        } else {
            repository.boolean(
                R.string.wifi_off_on_exit_key,
                repository.resourceBoolean(R.bool.wifi_off_on_exit_default),
            )
        }

    private fun keepScreenOnSync(): Boolean =
        repository.boolean(R.string.keep_screen_on_key, repository.resourceBoolean(R.bool.keep_screen_on_default))

    private fun themeStyleSync(): ThemeStyle = settingsFind(ThemeStyle.entries, R.string.theme_key, ThemeStyle.DARK)

    private fun selectedMenuSync(): NavigationMenu =
        settingsFind(NavigationMenu.entries, R.string.selected_menu_key, NavigationMenu.ACCESS_POINTS)

    private fun findSSIDsSync(): Set<String> = repository.stringSet(R.string.filter_ssid_key, setOf())

    private fun findWiFiBandsSync(): Set<WiFiBand> =
        settingsFindSet(WiFiBand.entries, R.string.filter_wifi_band_key, WiFiBand.GHZ2)

    private fun findStrengthsSync(): Set<Strength> =
        settingsFindSet(Strength.entries, R.string.filter_strength_key, Strength.FOUR)

    private fun findSecuritiesSync(): Set<Security> =
        settingsFindSet(Security.entries, R.string.filter_security_key, Security.NONE)

    // Save methods
    fun wiFiBand(wiFiBand: WiFiBand) {
        _settingsData.update { it.copy(wiFiBand = wiFiBand) }
        repository.save(R.string.wifi_band_key, wiFiBand.ordinal)
        scope.launch { settingsRepository?.updateWiFiBand(wiFiBand.ordinal) }
    }

    fun saveSelectedMenu(navigationMenu: NavigationMenu) {
        if (MAIN_NAVIGATION.contains(navigationMenu)) {
            _settingsData.update { it.copy(selectedMenu = navigationMenu) }
            repository.save(R.string.selected_menu_key, navigationMenu.ordinal)
            scope.launch { settingsRepository?.updateSelectedMenu(navigationMenu.ordinal) }
        }
    }

    fun saveSSIDs(values: Set<String>) {
        _settingsData.update { it.copy(filterSsids = values) }
        repository.saveStringSet(R.string.filter_ssid_key, values)
        scope.launch { settingsRepository?.updateFilterSsids(values) }
    }

    fun saveWiFiBands(values: Set<WiFiBand>) {
        _settingsData.update { it.copy(filterWiFiBands = values) }
        settingsSaveSet(R.string.filter_wifi_band_key, values)
        scope.launch { settingsRepository?.updateFilterWiFiBands(ordinals(values)) }
    }

    fun saveStrengths(values: Set<Strength>) {
        _settingsData.update { it.copy(filterStrengths = values) }
        settingsSaveSet(R.string.filter_strength_key, values)
        scope.launch { settingsRepository?.updateFilterStrengths(ordinals(values)) }
    }

    fun saveSecurities(values: Set<Security>) {
        _settingsData.update { it.copy(filterSecurities = values) }
        settingsSaveSet(R.string.filter_security_key, values)
        scope.launch { settingsRepository?.updateFilterSecurities(ordinals(values)) }
    }

    private fun <T : Enum<T>> settingsFind(
        values: EnumEntries<T>,
        key: Int,
        defaultValue: T,
    ): T {
        val value = repository.stringAsInteger(key, defaultValue.ordinal)
        return findOne(values, value, defaultValue)
    }

    private fun <T : Enum<T>> settingsFindSet(
        values: EnumEntries<T>,
        key: Int,
        defaultValue: T,
    ): Set<T> {
        val ordinalDefault = ordinals(values)
        val ordinalSaved = repository.stringSet(key, ordinalDefault)
        return findSet(values, ordinalSaved, defaultValue)
    }

    private fun <T : Enum<T>> settingsSaveSet(
        key: Int,
        values: Set<T>,
    ): Unit = repository.saveStringSet(key, ordinals(values))

    companion object {
        private const val SCAN_SPEED_DEFAULT = 5
        private const val GRAPH_Y_MULTIPLIER = -10
        private const val GRAPH_Y_DEFAULT = 2
    }
}
