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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.vrem.wifianalyzer.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
) {
    private val scanSpeedKey = stringPreferencesKey(context.getString(R.string.scan_speed_key))
    private val cacheOffKey = booleanPreferencesKey(context.getString(R.string.cache_off_key))
    private val graphMaximumYKey = stringPreferencesKey(context.getString(R.string.graph_maximum_y_key))
    private val wiFiBandKey = stringPreferencesKey(context.getString(R.string.wifi_band_key))
    private val countryCodeKey = stringPreferencesKey(context.getString(R.string.country_code_key))
    private val languageKey = stringPreferencesKey(context.getString(R.string.language_key))
    private val sortByKey = stringPreferencesKey(context.getString(R.string.sort_by_key))
    private val groupByKey = stringPreferencesKey(context.getString(R.string.group_by_key))
    private val apViewKey = stringPreferencesKey(context.getString(R.string.ap_view_key))
    private val connectionViewKey = stringPreferencesKey(context.getString(R.string.connection_view_key))
    private val wifiOffOnExitKey = booleanPreferencesKey(context.getString(R.string.wifi_off_on_exit_key))
    private val keepScreenOnKey = booleanPreferencesKey(context.getString(R.string.keep_screen_on_key))
    private val themeKey = stringPreferencesKey(context.getString(R.string.theme_key))
    private val selectedMenuKey = stringPreferencesKey(context.getString(R.string.selected_menu_key))
    private val filterSsidKey = stringSetPreferencesKey(context.getString(R.string.filter_ssid_key))
    private val filterWifiBandKey = stringSetPreferencesKey(context.getString(R.string.filter_wifi_band_key))
    private val filterStrengthKey = stringSetPreferencesKey(context.getString(R.string.filter_strength_key))
    private val filterSecurityKey = stringSetPreferencesKey(context.getString(R.string.filter_security_key))

    val preferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    suspend fun updateScanSpeed(value: String) {
        dataStore.edit { preferences -> preferences[scanSpeedKey] = value }
    }

    suspend fun updateCacheOff(value: Boolean) {
        dataStore.edit { preferences -> preferences[cacheOffKey] = value }
    }

    suspend fun updateGraphMaximumY(value: String) {
        dataStore.edit { preferences -> preferences[graphMaximumYKey] = value }
    }

    suspend fun updateWiFiBand(value: Int) {
        dataStore.edit { preferences -> preferences[wiFiBandKey] = value.toString() }
    }

    suspend fun updateCountryCode(value: String) {
        dataStore.edit { preferences -> preferences[countryCodeKey] = value }
    }

    suspend fun updateLanguage(value: String) {
        dataStore.edit { preferences -> preferences[languageKey] = value }
    }

    suspend fun updateSortBy(value: Int) {
        dataStore.edit { preferences -> preferences[sortByKey] = value.toString() }
    }

    suspend fun updateGroupBy(value: Int) {
        dataStore.edit { preferences -> preferences[groupByKey] = value.toString() }
    }

    suspend fun updateApView(value: Int) {
        dataStore.edit { preferences -> preferences[apViewKey] = value.toString() }
    }

    suspend fun updateConnectionView(value: Int) {
        dataStore.edit { preferences -> preferences[connectionViewKey] = value.toString() }
    }

    suspend fun updateWiFiOffOnExit(value: Boolean) {
        dataStore.edit { preferences -> preferences[wifiOffOnExitKey] = value }
    }

    suspend fun updateKeepScreenOn(value: Boolean) {
        dataStore.edit { preferences -> preferences[keepScreenOnKey] = value }
    }

    suspend fun updateTheme(value: Int) {
        dataStore.edit { preferences -> preferences[themeKey] = value.toString() }
    }

    suspend fun updateSelectedMenu(value: Int) {
        dataStore.edit { preferences -> preferences[selectedMenuKey] = value.toString() }
    }

    suspend fun updateFilterSsids(values: Set<String>) {
        dataStore.edit { preferences -> preferences[filterSsidKey] = values }
    }

    suspend fun updateFilterWiFiBands(values: Set<String>) {
        dataStore.edit { preferences -> preferences[filterWifiBandKey] = values }
    }

    suspend fun updateFilterStrengths(values: Set<String>) {
        dataStore.edit { preferences -> preferences[filterStrengthKey] = values }
    }

    suspend fun updateFilterSecurities(values: Set<String>) {
        dataStore.edit { preferences -> preferences[filterSecurityKey] = values }
    }
}
