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
import com.vrem.util.ordinals
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.model.GroupBy
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.Strength
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val settingsRepository: SettingsRepository = mock()
    private val settingsDataFlow = MutableStateFlow(SettingsData())
    private lateinit var fixture: Settings

    @Before
    fun setUp() {
        whenever(settingsRepository.settingsData).thenReturn(settingsDataFlow)
        fixture = Settings(settingsRepository, testScope)
        testScope.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        verify(settingsRepository).settingsData
        verifyNoMoreInteractions(settingsRepository)
    }

    @Test
    fun defaultValues() {
        assertThat(fixture.scanSpeed()).isEqualTo(5)
        assertThat(fixture.cacheOff()).isFalse()
        assertThat(fixture.graphMaximumY()).isEqualTo(-20)
        assertThat(fixture.countryCode()).isEqualTo(defaultCountryCode())
        assertThat(fixture.languageLocale()).isEqualTo(findByLanguageTag(defaultLanguageTag()))
        assertThat(fixture.sortBy()).isEqualTo(SortBy.STRENGTH)
        assertThat(fixture.groupBy()).isEqualTo(GroupBy.NONE)
        assertThat(fixture.accessPointView()).isEqualTo(AccessPointViewType.COMPLETE)
        assertThat(fixture.connectionViewType()).isEqualTo(ConnectionViewType.COMPACT)
        assertThat(fixture.wiFiBand()).isEqualTo(WiFiBand.GHZ2)
        assertThat(fixture.wiFiOffOnExit()).isFalse()
        assertThat(fixture.keepScreenOn()).isFalse()
        assertThat(fixture.themeStyle()).isEqualTo(ThemeStyle.DARK)
        assertThat(fixture.selectedMenu()).isEqualTo(NavigationMenu.ACCESS_POINTS)
        assertThat(fixture.findSSIDs()).isEmpty()
        assertThat(fixture.findWiFiBands()).containsExactlyInAnyOrder(WiFiBand.GHZ2, WiFiBand.GHZ5, WiFiBand.GHZ6)
        assertThat(fixture.findStrengths()).containsExactlyInAnyOrder(*Strength.entries.toTypedArray())
        assertThat(fixture.findSecurities()).containsExactlyInAnyOrder(*Security.entries.toTypedArray())
    }

    @Test
    fun wiFiBandUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            val band = WiFiBand.GHZ5
            fixture.wiFiBand(band)
            advanceUntilIdle()

            assertThat(fixture.wiFiBand()).isEqualTo(band)
            verify(settingsRepository).updateWiFiBand(band.ordinal)
        }

    @Test
    fun saveSelectedMenuUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            val menu = NavigationMenu.CHANNEL_RATING
            fixture.saveSelectedMenu(menu)
            advanceUntilIdle()

            assertThat(fixture.selectedMenu()).isEqualTo(menu)
            verify(settingsRepository).updateSelectedMenu(menu.ordinal)
        }

    @Test
    fun saveSelectedMenuIgnoresNonMainNavigation() =
        runTest(testDispatcher) {
            val menu = NavigationMenu.ABOUT
            fixture.saveSelectedMenu(menu)
            advanceUntilIdle()

            assertThat(fixture.selectedMenu()).isEqualTo(NavigationMenu.ACCESS_POINTS)
        }

    @Test
    fun saveSSIDsUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            val ssids = setOf("SSID1", "SSID2")
            fixture.saveSSIDs(ssids)
            advanceUntilIdle()

            assertThat(fixture.findSSIDs()).isEqualTo(ssids)
            verify(settingsRepository).updateFilterSsids(ssids)
        }

    @Test
    fun saveWiFiBandsUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            val bands = setOf(WiFiBand.GHZ5)
            fixture.saveWiFiBands(bands)
            advanceUntilIdle()

            assertThat(fixture.findWiFiBands()).isEqualTo(bands)
            verify(settingsRepository).updateFilterWiFiBands(ordinals(bands))
        }

    @Test
    fun saveStrengthsUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            val strengths = setOf(Strength.FOUR)
            fixture.saveStrengths(strengths)
            advanceUntilIdle()

            assertThat(fixture.findStrengths()).isEqualTo(strengths)
            verify(settingsRepository).updateFilterStrengths(ordinals(strengths))
        }

    @Test
    fun saveSecuritiesUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            val securities = setOf(Security.WPA2)
            fixture.saveSecurities(securities)
            advanceUntilIdle()

            assertThat(fixture.findSecurities()).isEqualTo(securities)
            verify(settingsRepository).updateFilterSecurities(ordinals(securities))
        }

    @Test
    fun updateScanSpeedUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateScanSpeed(10)
            advanceUntilIdle()

            assertThat(fixture.scanSpeed()).isEqualTo(10)
            verify(settingsRepository).updateScanSpeed("10")
        }

    @Test
    fun updateCacheOffUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateCacheOff(true)
            advanceUntilIdle()

            assertThat(fixture.cacheOff()).isTrue()
            verify(settingsRepository).updateCacheOff(true)
        }

    @Test
    fun updateGraphMaximumYUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateGraphMaximumY(-30)
            advanceUntilIdle()

            assertThat(fixture.graphMaximumY()).isEqualTo(-30)
            verify(settingsRepository).updateGraphMaximumY("3")
        }

    @Test
    fun updateCountryCodeUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateCountryCode("US")
            advanceUntilIdle()

            assertThat(fixture.countryCode()).isEqualTo("US")
            verify(settingsRepository).updateCountryCode("US")
        }

    @Test
    fun updateLanguageUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateLanguage("en-US")
            advanceUntilIdle()

            assertThat(fixture.languageLocale()).isEqualTo(findByLanguageTag("en-US"))
            verify(settingsRepository).updateLanguage("en-US")
        }

    @Test
    fun updateSortByUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateSortBy(SortBy.CHANNEL)
            advanceUntilIdle()

            assertThat(fixture.sortBy()).isEqualTo(SortBy.CHANNEL)
            verify(settingsRepository).updateSortBy(SortBy.CHANNEL.ordinal)
        }

    @Test
    fun updateGroupByUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateGroupBy(GroupBy.CHANNEL)
            advanceUntilIdle()

            assertThat(fixture.groupBy()).isEqualTo(GroupBy.CHANNEL)
            verify(settingsRepository).updateGroupBy(GroupBy.CHANNEL.ordinal)
        }

    @Test
    fun updateAccessPointViewUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateAccessPointView(AccessPointViewType.COMPACT)
            advanceUntilIdle()

            assertThat(fixture.accessPointView()).isEqualTo(AccessPointViewType.COMPACT)
            verify(settingsRepository).updateApView(AccessPointViewType.COMPACT.ordinal)
        }

    @Test
    fun updateConnectionViewUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateConnectionView(ConnectionViewType.HIDE)
            advanceUntilIdle()

            assertThat(fixture.connectionViewType()).isEqualTo(ConnectionViewType.HIDE)
            verify(settingsRepository).updateConnectionView(ConnectionViewType.HIDE.ordinal)
        }

    @Test
    fun updateWiFiOffOnExitUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateWiFiOffOnExit(true)
            advanceUntilIdle()

            assertThat(fixture.wiFiOffOnExit()).isTrue()
            verify(settingsRepository).updateWiFiOffOnExit(true)
        }

    @Test
    fun updateKeepScreenOnUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateKeepScreenOn(true)
            advanceUntilIdle()

            assertThat(fixture.keepScreenOn()).isTrue()
            verify(settingsRepository).updateKeepScreenOn(true)
        }

    @Test
    fun updateThemeUpdatesStateAndRepository() =
        runTest(testDispatcher) {
            fixture.updateTheme(ThemeStyle.LIGHT)
            advanceUntilIdle()

            assertThat(fixture.themeStyle()).isEqualTo(ThemeStyle.LIGHT)
            verify(settingsRepository).updateTheme(ThemeStyle.LIGHT.ordinal)
        }

    @Test
    fun resetCallsRepository() =
        runTest(testDispatcher) {
            fixture.reset()
            advanceUntilIdle()

            verify(settingsRepository).resetToDefaults()
        }

    @Test
    fun initializeDefaultValuesIsNoOp() {
        fixture.initializeDefaultValues()
    }
}
