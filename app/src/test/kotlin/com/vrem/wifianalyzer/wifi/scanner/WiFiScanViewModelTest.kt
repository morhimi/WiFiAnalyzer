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

import com.vrem.wifianalyzer.wifi.model.WiFiData
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
    private val initialWiFiData: WiFiData = mock()
    private lateinit var fixture: WiFiScanViewModel

    @Before
    fun setUp() {
        whenever(scannerService.wiFiData()).thenReturn(initialWiFiData)
        fixture = WiFiScanViewModel(scannerService)
    }

    @After
    fun tearDown() {
        verify(scannerService).wiFiData()
        verify(scannerService).runningFlow
        verify(scannerService).register(fixture.updateNotifier)
        verifyNoMoreInteractions(scannerService, initialWiFiData)
    }

    @Test
    fun initialWiFiData() {
        assertThat(fixture.wiFiData.value).isEqualTo(initialWiFiData)
    }

    @Test
    fun update() {
        // execute
        fixture.update()
        // validate
        verify(scannerService).update()
    }

    @Test
    fun updateNotifierEmitsNewData() {
        // setup
        val newWiFiData: WiFiData = mock()
        // execute
        fixture.updateNotifier.update(newWiFiData)
        // validate
        assertThat(fixture.wiFiData.value).isEqualTo(newWiFiData)
    }

    @Test
    fun onCleared() {
        // execute
        fixture.onCleared()
        // validate
        verify(scannerService).unregister(fixture.updateNotifier)
    }
}
