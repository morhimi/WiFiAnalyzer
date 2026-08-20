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
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [android.os.Build.VERSION_CODES.BAKLAVA])
class SettingsRepositoryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var fixture: SettingsRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val tempFile = tempFolder.newFile("test_settings.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(produceFile = { tempFile })
        fixture = SettingsRepository(dataStore, context)
    }

    @Test
    fun toSettingsDataReturnsDefaultsWhenEmpty() =
        runTest {
            val data = fixture.settingsData.first()

            assertThat(data.scanSpeed).isEqualTo(5)
            assertThat(data.cacheOff).isFalse()
            assertThat(data.graphMaximumY).isEqualTo(-20)
            assertThat(data.wiFiBand).isEqualTo(WiFiBand.GHZ2)
            assertThat(data.countryCode).isEqualTo(defaultCountryCode())
            assertThat(data.languageLocale).isEqualTo(findByLanguageTag(defaultLanguageTag()))
            assertThat(data.sortBy).isEqualTo(SortBy.STRENGTH)
            assertThat(data.groupBy).isEqualTo(GroupBy.NONE)
            assertThat(data.accessPointView).isEqualTo(AccessPointViewType.COMPLETE)
            assertThat(data.connectionViewType).isEqualTo(ConnectionViewType.COMPACT)
            assertThat(data.wiFiOffOnExit).isFalse()
            assertThat(data.keepScreenOn).isFalse()
            assertThat(data.themeStyle).isEqualTo(ThemeStyle.DARK)
            assertThat(data.dynamicColor).isTrue()
            assertThat(data.selectedMenu).isEqualTo(NavigationMenu.ACCESS_POINTS)
            assertThat(data.filterSsids).isEmpty()
            assertThat(data.filterWiFiBands).containsExactlyInAnyOrder(WiFiBand.GHZ2, WiFiBand.GHZ5, WiFiBand.GHZ6)
            assertThat(data.filterStrengths).containsExactlyInAnyOrder(*Strength.entries.toTypedArray())
            assertThat(data.filterSecurities).containsExactlyInAnyOrder(*Security.entries.toTypedArray())
        }

    @Test
    fun updateScanSpeedSavesValue() =
        runTest {
            fixture.updateScanSpeed("15")
            val data = fixture.settingsData.first()
            assertThat(data.scanSpeed).isEqualTo(15)
        }

    @Test
    fun updateCacheOffSavesValue() =
        runTest {
            fixture.updateCacheOff(true)
            val data = fixture.settingsData.first()
            assertThat(data.cacheOff).isTrue()
        }

    @Test
    fun updateGraphMaximumYSavesValue() =
        runTest {
            fixture.updateGraphMaximumY("4")
            val data = fixture.settingsData.first()
            assertThat(data.graphMaximumY).isEqualTo(-40)
        }

    @Test
    fun updateWiFiBandSavesValue() =
        runTest {
            fixture.updateWiFiBand(WiFiBand.GHZ5.ordinal)
            val data = fixture.settingsData.first()
            assertThat(data.wiFiBand).isEqualTo(WiFiBand.GHZ5)
        }

    @Test
    fun updateCountryCodeSavesValue() =
        runTest {
            fixture.updateCountryCode("GB")
            val data = fixture.settingsData.first()
            assertThat(data.countryCode).isEqualTo("GB")
        }

    @Test
    fun updateLanguageSavesValue() =
        runTest {
            fixture.updateLanguage("fr-FR")
            val data = fixture.settingsData.first()
            assertThat(data.languageLocale).isEqualTo(findByLanguageTag("fr-FR"))
        }

    @Test
    fun updateSortBySavesValue() =
        runTest {
            fixture.updateSortBy(SortBy.SSID.ordinal)
            val data = fixture.settingsData.first()
            assertThat(data.sortBy).isEqualTo(SortBy.SSID)
        }

    @Test
    fun updateGroupBySavesValue() =
        runTest {
            fixture.updateGroupBy(GroupBy.CHANNEL.ordinal)
            val data = fixture.settingsData.first()
            assertThat(data.groupBy).isEqualTo(GroupBy.CHANNEL)
        }

    @Test
    fun updateApViewSavesValue() =
        runTest {
            fixture.updateApView(AccessPointViewType.COMPACT.ordinal)
            val data = fixture.settingsData.first()
            assertThat(data.accessPointView).isEqualTo(AccessPointViewType.COMPACT)
        }

    @Test
    fun updateConnectionViewSavesValue() =
        runTest {
            fixture.updateConnectionView(ConnectionViewType.HIDE.ordinal)
            val data = fixture.settingsData.first()
            assertThat(data.connectionViewType).isEqualTo(ConnectionViewType.HIDE)
        }

    @Test
    fun updateWiFiOffOnExitSavesValue() =
        runTest {
            fixture.updateWiFiOffOnExit(true)
            val data = fixture.settingsData.first()
            assertThat(data.wiFiOffOnExit).isTrue()
        }

    @Test
    fun updateKeepScreenOnSavesValue() =
        runTest {
            fixture.updateKeepScreenOn(true)
            val data = fixture.settingsData.first()
            assertThat(data.keepScreenOn).isTrue()
        }

    @Test
    fun updateThemeSavesValue() =
        runTest {
            fixture.updateTheme(ThemeStyle.LIGHT.ordinal)
            val data = fixture.settingsData.first()
            assertThat(data.themeStyle).isEqualTo(ThemeStyle.LIGHT)
        }

    @Test
    fun updateDynamicColorSavesValue() =
        runTest {
            fixture.updateDynamicColor(false)
            val data = fixture.settingsData.first()
            assertThat(data.dynamicColor).isFalse()
        }

    @Test
    fun updateSelectedMenuSavesValue() =
        runTest {
            fixture.updateSelectedMenu(NavigationMenu.CHANNEL_GRAPH.ordinal)
            val data = fixture.settingsData.first()
            assertThat(data.selectedMenu).isEqualTo(NavigationMenu.CHANNEL_GRAPH)
        }

    @Test
    fun updateFiltersSavesValues() =
        runTest {
            val ssids = setOf("SSID_A", "SSID_B")
            val bands = setOf(WiFiBand.GHZ5.ordinal.toString())
            val strengths = setOf(Strength.THREE.ordinal.toString())
            val securities = setOf(Security.WPA3.ordinal.toString())

            fixture.updateFilterSsids(ssids)
            fixture.updateFilterWiFiBands(bands)
            fixture.updateFilterStrengths(strengths)
            fixture.updateFilterSecurities(securities)

            val data = fixture.settingsData.first()
            assertThat(data.filterSsids).isEqualTo(ssids)
            assertThat(data.filterWiFiBands).containsExactly(WiFiBand.GHZ5)
            assertThat(data.filterStrengths).containsExactly(Strength.THREE)
            assertThat(data.filterSecurities).containsExactly(Security.WPA3)
        }

    @Test
    fun saveAndGetAlias() =
        runTest {
            val bssid = "00:11:22:33:44:55"
            val alias = "My Router"

            fixture.saveAlias(bssid, alias)
            assertThat(fixture.getAlias(bssid).first()).isEqualTo(alias)
            assertThat(fixture.getAliasSync(bssid)).isEqualTo(alias)

            fixture.saveAlias(bssid, "")
            assertThat(fixture.getAlias(bssid).first()).isEmpty()
            assertThat(fixture.getAliasSync(bssid)).isEmpty()
        }

    @Test
    fun resetToDefaultsClearsAllData() =
        runTest {
            fixture.updateScanSpeed("20")
            fixture.updateCacheOff(true)
            fixture.saveAlias("AA:BB:CC:DD:EE:FF", "Alias")

            fixture.resetToDefaults()

            val data = fixture.settingsData.first()
            assertThat(data.scanSpeed).isEqualTo(5)
            assertThat(data.cacheOff).isFalse()
            assertThat(fixture.getAliasSync("AA:BB:CC:DD:EE:FF")).isEmpty()
        }

    @Test
    fun preferencesFlowHandlesIOExceptionByEmittingEmptyPreferences() =
        runTest {
            val mockDataStore: DataStore<Preferences> = mock()
            val errorFlow: Flow<Preferences> =
                flow {
                    throw java.io.IOException("Disk read error")
                }
            whenever(mockDataStore.data).thenReturn(errorFlow)

            val testRepository = SettingsRepository(mockDataStore, context)
            val preferences = testRepository.preferencesFlow.first()

            assertThat(preferences).isEqualTo(emptyPreferences())
        }

    @Test
    fun preferencesFlowRethrowsNonIOException() =
        runTest {
            val mockDataStore: DataStore<Preferences> = mock()
            val errorFlow: Flow<Preferences> =
                flow {
                    throw IllegalStateException("Fatal error")
                }
            whenever(mockDataStore.data).thenReturn(errorFlow)

            val testRepository = SettingsRepository(mockDataStore, context)
            var thrown: Throwable? = null
            try {
                testRepository.preferencesFlow.first()
            } catch (e: Throwable) {
                thrown = e
            }

            assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        }
}
