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
package com.vrem.wifianalyzer.compose

import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import kotlinx.coroutines.flow.MutableStateFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class MainViewModelTest {
    private val settings: Settings = mock()
    private val scannerService: ScannerService = mock()
    private val apAliasService: ApAliasService = mock()
    private val filtersAdapter: FiltersAdapter = mock()
    private val settingsDataFlow = MutableStateFlow(SettingsData())
    private val runningFlow = MutableStateFlow(false)
    private lateinit var fixture: MainViewModel

    @Before
    fun setUp() {
        whenever(settings.settingsData).thenReturn(settingsDataFlow)
        whenever(scannerService.runningFlow).thenReturn(runningFlow)
        fixture =
            MainViewModel(
                settings = settings,
                scannerService = scannerService,
                apAliasService = apAliasService,
                filtersAdapter = filtersAdapter,
            )
    }

    @After
    fun tearDown() {
        verify(settings).settingsData
        verify(scannerService).runningFlow
        verifyNoMoreInteractions(settings)
        verifyNoMoreInteractions(scannerService)
        verifyNoMoreInteractions(apAliasService)
        verifyNoMoreInteractions(filtersAdapter)
    }

    @Test
    fun shouldExposeSettingsDataFlow() {
        assertThat(fixture.settingsData).isEqualTo(settingsDataFlow)
    }

    @Test
    fun shouldExposeIsScanningFlow() {
        assertThat(fixture.isScanning).isEqualTo(runningFlow)
    }

    @Test
    fun shouldExposeSettingsAndFiltersAdapter() {
        assertThat(fixture.settings).isEqualTo(settings)
        assertThat(fixture.filtersAdapter).isEqualTo(filtersAdapter)
    }

    @Test
    fun shouldToggleScanning() {
        // execute
        fixture.toggleScanning()

        // verify
        verify(scannerService).toggle()
    }

    @Test
    fun shouldUpdateScan() {
        // execute
        fixture.updateScan()

        // verify
        verify(scannerService).update()
    }

    @Test
    fun shouldSaveAliasAndUpdateScanner() {
        // arrange
        val bssid = "00:11:22:33:44:55"
        val alias = "My Router"

        // execute
        fixture.saveAlias(bssid, alias)

        // verify
        verify(apAliasService).saveAlias(bssid, alias)
        verify(scannerService).update()
    }

    @Test
    fun shouldRemoveAliasAndUpdateScanner() {
        // arrange
        val bssid = "00:11:22:33:44:55"

        // execute
        fixture.removeAlias(bssid)

        // verify
        verify(apAliasService).removeAlias(bssid)
        verify(scannerService).update()
    }
}
