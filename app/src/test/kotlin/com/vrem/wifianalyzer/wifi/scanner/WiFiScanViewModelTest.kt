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
package com.vrem.wifianalyzer.wifi.scanner

import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.WiFiData
import kotlinx.coroutines.flow.MutableStateFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class WiFiScanViewModelTest {
    private val scannerService: ScannerService = mock()
    private val settings: Settings = mock()
    private val wiFiManagerWrapper: WiFiManagerWrapper = mock()
    private val permissionService: PermissionService = mock()
    private val wiFiDataFlow = MutableStateFlow(WiFiData.EMPTY)
    private val runningFlow = MutableStateFlow(false)
    private val settingsDataFlow = MutableStateFlow(SettingsData())
    private lateinit var fixture: WiFiScanViewModel

    @Before
    fun setUp() {
        whenever(scannerService.wiFiDataFlow).thenReturn(wiFiDataFlow)
        whenever(scannerService.runningFlow).thenReturn(runningFlow)
        whenever(settings.settingsData).thenReturn(settingsDataFlow)
        fixture =
            WiFiScanViewModel(
                scannerService = scannerService,
                settings = settings,
                wiFiManagerWrapper = wiFiManagerWrapper,
                permissionService = permissionService,
            )
    }

    @After
    fun tearDown() {
        verify(scannerService).wiFiDataFlow
        verify(scannerService).runningFlow
        verify(settings).settingsData
        verifyNoMoreInteractions(scannerService)
        verifyNoMoreInteractions(settings)
        verifyNoMoreInteractions(wiFiManagerWrapper)
        verifyNoMoreInteractions(permissionService)
    }

    @Test
    fun wiFiDataExposesScannerServiceFlow() {
        assertThat(fixture.wiFiData).isEqualTo(wiFiDataFlow)
    }

    @Test
    fun isScanningExposesScannerServiceFlow() {
        assertThat(fixture.isScanning).isEqualTo(runningFlow)
    }

    @Test
    fun settingsDataExposesSettingsFlow() {
        assertThat(fixture.settingsData).isEqualTo(settingsDataFlow)
    }

    @Test
    fun exposesInjectedDependencies() {
        assertThat(fixture.wiFiManagerWrapper).isEqualTo(wiFiManagerWrapper)
        assertThat(fixture.permissionService).isEqualTo(permissionService)
        assertThat(fixture.scannerService).isEqualTo(scannerService)
        assertThat(fixture.settings).isEqualTo(settings)
    }

    @Test
    fun update() {
        // execute
        fixture.update()
        // validate
        verify(scannerService).update()
    }
}
