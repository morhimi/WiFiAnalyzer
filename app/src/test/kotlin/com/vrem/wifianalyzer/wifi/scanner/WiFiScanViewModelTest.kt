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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
        assertThat(fixture.scannerService).isEqualTo(scannerService)
        assertThat(fixture.settings).isEqualTo(settings)
    }

    @Test
    fun isScanThrottleEnabledDelegatesToWiFiManagerWrapper() {
        whenever(wiFiManagerWrapper.isScanThrottleEnabled()).thenReturn(true)
        assertThat(fixture.isScanThrottleEnabled).isTrue()
        verify(wiFiManagerWrapper).isScanThrottleEnabled()
    }

    @Test
    fun isPermissionEnabledDelegatesToPermissionService() {
        whenever(permissionService.enabled()).thenReturn(true)
        assertThat(fixture.isPermissionEnabled).isTrue()
        verify(permissionService).enabled()
    }

    @Test
    fun isBandAvailableDelegatesToBand() {
        whenever(wiFiManagerWrapper.is5GHzBandSupported()).thenReturn(true)
        assertThat(fixture.isBandAvailable(com.vrem.wifianalyzer.wifi.band.WiFiBand.GHZ5)).isTrue()
        verify(wiFiManagerWrapper).is5GHzBandSupported()
    }

    @Test
    fun accessPointsUiStateHasInitialValue() {
        assertThat(fixture.accessPointsUiState.value).isEqualTo(AccessPointsUiState())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun accessPointsUiStateCombinesInputs() =
        runTest {
            whenever(wiFiManagerWrapper.isScanThrottleEnabled()).thenReturn(true)
            whenever(permissionService.enabled()).thenReturn(true)

            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    fixture.accessPointsUiState.collect {}
                }

            val state = fixture.accessPointsUiState.value
            assertThat(state.scanThrottleEnabled).isTrue()
            assertThat(state.permissionEnabled).isTrue()
            assertThat(state.wiFiBandAvailable).isTrue()
            assertThat(state.isScanning).isFalse()
            assertThat(state.wiFiData).isEqualTo(WiFiData.EMPTY)
            job.cancel()

            verify(wiFiManagerWrapper).isScanThrottleEnabled()
            verify(permissionService).enabled()
        }

    private val testWiFiDetail =
        com.vrem.wifianalyzer.wifi.model.WiFiDetail(
            wiFiIdentifier =
                com.vrem.wifianalyzer.wifi.model
                    .WiFiIdentifier("SSID", "00:11:22:33:44:55"),
            wiFiSignal =
                com.vrem.wifianalyzer.wifi.model.WiFiSignal(
                    2412,
                    2412,
                    com.vrem.wifianalyzer.wifi.model.WiFiWidth.MHZ_20,
                    -50,
                ),
        )

    @Test
    fun accessPointsUiStateEqualsAndHashCode() {
        val base = AccessPointsUiState()
        val same = AccessPointsUiState()

        assertThat(base).isEqualTo(base)
        assertThat(base).isEqualTo(same)
        assertThat(base.hashCode()).isEqualTo(same.hashCode())
        assertThat(base).isNotEqualTo(null)
        assertThat(base).isNotEqualTo("other")

        assertThat(base).isNotEqualTo(
            AccessPointsUiState(
                wiFiData = WiFiData(listOf(testWiFiDetail), com.vrem.wifianalyzer.wifi.model.WiFiConnection.EMPTY),
            ),
        )
        assertThat(base).isNotEqualTo(AccessPointsUiState(wiFiDetails = listOf(testWiFiDetail)))
        assertThat(base).isNotEqualTo(
            AccessPointsUiState(viewType = com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType.COMPACT),
        )
        assertThat(base).isNotEqualTo(
            AccessPointsUiState(wiFiBand = com.vrem.wifianalyzer.wifi.band.WiFiBand.GHZ5),
        )
        assertThat(base).isNotEqualTo(AccessPointsUiState(wiFiBandAvailable = false))
        assertThat(base).isNotEqualTo(AccessPointsUiState(scanThrottleEnabled = true))
        assertThat(base).isNotEqualTo(AccessPointsUiState(permissionEnabled = true))
        assertThat(base).isNotEqualTo(AccessPointsUiState(isScanning = true))
        assertThat(base).isNotEqualTo(
            AccessPointsUiState(
                connectionViewType = com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType.COMPLETE,
            ),
        )
    }

    @Test
    fun channelRatingUiStateHasInitialValue() {
        assertThat(fixture.channelRatingUiState.value).isEqualTo(ChannelRatingUiState())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun channelRatingUiStateCombinesInputs() =
        runTest {
            whenever(wiFiManagerWrapper.isScanThrottleEnabled()).thenReturn(true)
            whenever(permissionService.enabled()).thenReturn(true)

            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    fixture.channelRatingUiState.collect {}
                }

            val state = fixture.channelRatingUiState.value
            assertThat(state.scanThrottleEnabled).isTrue()
            assertThat(state.permissionEnabled).isTrue()
            assertThat(state.wiFiBandAvailable).isTrue()
            assertThat(state.isScanning).isFalse()
            assertThat(state.wiFiData).isEqualTo(WiFiData.EMPTY)
            job.cancel()

            verify(wiFiManagerWrapper).isScanThrottleEnabled()
            verify(permissionService).enabled()
        }

    @Test
    fun channelRatingUiStateEqualsAndHashCode() {
        val base = ChannelRatingUiState()
        val same = ChannelRatingUiState()

        assertThat(base).isEqualTo(base)
        assertThat(base).isEqualTo(same)
        assertThat(base.hashCode()).isEqualTo(same.hashCode())
        assertThat(base).isNotEqualTo(null)
        assertThat(base).isNotEqualTo("other")

        assertThat(base).isNotEqualTo(
            ChannelRatingUiState(
                wiFiData = WiFiData(listOf(testWiFiDetail), com.vrem.wifianalyzer.wifi.model.WiFiConnection.EMPTY),
            ),
        )
        assertThat(base).isNotEqualTo(
            ChannelRatingUiState(wiFiBand = com.vrem.wifianalyzer.wifi.band.WiFiBand.GHZ5),
        )
        assertThat(base).isNotEqualTo(
            ChannelRatingUiState(
                wiFiChannels =
                    listOf(
                        com.vrem.wifianalyzer.wifi.band
                            .WiFiChannel(1, 2412),
                    ),
            ),
        )
        assertThat(base).isNotEqualTo(
            ChannelRatingUiState(
                bestChannels =
                    listOf(
                        com.vrem.wifianalyzer.wifi.model.ChannelAPCount(
                            com.vrem.wifianalyzer.wifi.band
                                .WiFiChannel(1, 2412),
                            com.vrem.wifianalyzer.wifi.model.WiFiWidth.MHZ_20,
                            1,
                        ),
                    ),
            ),
        )
        assertThat(base).isNotEqualTo(
            ChannelRatingUiState(
                channelRating =
                    com.vrem.wifianalyzer.wifi.model
                        .ChannelRating(mutableListOf(testWiFiDetail)),
            ),
        )
        assertThat(base).isNotEqualTo(ChannelRatingUiState(wiFiBandAvailable = false))
        assertThat(base).isNotEqualTo(ChannelRatingUiState(scanThrottleEnabled = true))
        assertThat(base).isNotEqualTo(ChannelRatingUiState(permissionEnabled = true))
        assertThat(base).isNotEqualTo(ChannelRatingUiState(isScanning = true))
        assertThat(base).isNotEqualTo(
            ChannelRatingUiState(
                connectionViewType = com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType.COMPLETE,
            ),
        )
    }

    @Test
    fun update() {
        // execute
        fixture.update()
        // validate
        verify(scannerService).update()
    }
}
