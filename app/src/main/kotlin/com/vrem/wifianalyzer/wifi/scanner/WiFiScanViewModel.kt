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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.band.WiFiChannel
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ChannelAPCount
import com.vrem.wifianalyzer.wifi.model.ChannelRating
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.predicate.makeAccessPointsPredicate
import com.vrem.wifianalyzer.wifi.predicate.predicate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class AccessPointsUiState(
    val wiFiData: WiFiData = WiFiData.EMPTY,
    val wiFiDetails: List<WiFiDetail> = emptyList(),
    val viewType: AccessPointViewType = AccessPointViewType.COMPLETE,
    val wiFiBand: WiFiBand = WiFiBand.GHZ2,
    val wiFiBandAvailable: Boolean = true,
    val scanThrottleEnabled: Boolean = false,
    val permissionEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val connectionViewType: ConnectionViewType = ConnectionViewType.COMPACT,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccessPointsUiState) return false
        return wiFiData == other.wiFiData &&
            wiFiDetails == other.wiFiDetails &&
            viewType == other.viewType &&
            wiFiBand == other.wiFiBand &&
            wiFiBandAvailable == other.wiFiBandAvailable &&
            scanThrottleEnabled == other.scanThrottleEnabled &&
            permissionEnabled == other.permissionEnabled &&
            isScanning == other.isScanning &&
            connectionViewType == other.connectionViewType
    }

    override fun hashCode(): Int {
        var result = wiFiData.hashCode()
        result = 31 * result + wiFiDetails.hashCode()
        result = 31 * result + viewType.hashCode()
        result = 31 * result + wiFiBand.hashCode()
        result = 31 * result + wiFiBandAvailable.hashCode()
        result = 31 * result + scanThrottleEnabled.hashCode()
        result = 31 * result + permissionEnabled.hashCode()
        result = 31 * result + isScanning.hashCode()
        result = 31 * result + connectionViewType.hashCode()
        return result
    }
}

class ChannelRatingUiState(
    val wiFiData: WiFiData = WiFiData.EMPTY,
    val wiFiBand: WiFiBand = WiFiBand.GHZ2,
    val wiFiChannels: List<WiFiChannel> = emptyList(),
    val bestChannels: List<ChannelAPCount> = emptyList(),
    val channelRating: ChannelRating = ChannelRating(),
    val wiFiBandAvailable: Boolean = true,
    val scanThrottleEnabled: Boolean = false,
    val permissionEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val connectionViewType: ConnectionViewType = ConnectionViewType.COMPACT,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChannelRatingUiState) return false
        return wiFiData == other.wiFiData &&
            wiFiBand == other.wiFiBand &&
            wiFiChannels == other.wiFiChannels &&
            bestChannels == other.bestChannels &&
            channelRating == other.channelRating &&
            wiFiBandAvailable == other.wiFiBandAvailable &&
            scanThrottleEnabled == other.scanThrottleEnabled &&
            permissionEnabled == other.permissionEnabled &&
            isScanning == other.isScanning &&
            connectionViewType == other.connectionViewType
    }

    override fun hashCode(): Int {
        var result = wiFiData.hashCode()
        result = 31 * result + wiFiBand.hashCode()
        result = 31 * result + wiFiChannels.hashCode()
        result = 31 * result + bestChannels.hashCode()
        result = 31 * result + channelRating.hashCode()
        result = 31 * result + wiFiBandAvailable.hashCode()
        result = 31 * result + scanThrottleEnabled.hashCode()
        result = 31 * result + permissionEnabled.hashCode()
        result = 31 * result + isScanning.hashCode()
        result = 31 * result + connectionViewType.hashCode()
        return result
    }
}

@HiltViewModel
class WiFiScanViewModel
    @Inject
    constructor(
        val scannerService: ScannerService,
        val settings: Settings,
        private val wiFiManagerWrapper: WiFiManagerWrapper,
        private val permissionService: PermissionService,
    ) : ViewModel() {
        val wiFiData: StateFlow<WiFiData> = scannerService.wiFiDataFlow
        val isScanning: StateFlow<Boolean> = scannerService.runningFlow
        val settingsData: StateFlow<SettingsData> = settings.settingsData

        val isScanThrottleEnabled: Boolean
            get() = wiFiManagerWrapper.isScanThrottleEnabled()

        val isPermissionEnabled: Boolean
            get() = permissionService.enabled()

        fun isBandAvailable(band: WiFiBand): Boolean = band.available(wiFiManagerWrapper)

        val accessPointsUiState: StateFlow<AccessPointsUiState> =
            combine(
                wiFiData,
                settingsData,
                isScanning,
            ) { wiFiData, settingsData, isScanning ->
                val wiFiDetails =
                    wiFiData.wiFiDetails(
                        makeAccessPointsPredicate(settingsData),
                        settingsData.sortBy,
                        settingsData.groupBy,
                    )
                val wiFiBand = settingsData.wiFiBand
                AccessPointsUiState(
                    wiFiData = wiFiData,
                    wiFiDetails = wiFiDetails,
                    viewType = settingsData.accessPointView,
                    wiFiBand = wiFiBand,
                    wiFiBandAvailable = wiFiBand.available(wiFiManagerWrapper),
                    scanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
                    permissionEnabled = permissionService.enabled(),
                    isScanning = isScanning,
                    connectionViewType = settingsData.connectionViewType,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AccessPointsUiState(),
            )

        val channelRatingUiState: StateFlow<ChannelRatingUiState> =
            combine(
                wiFiData,
                settingsData,
                isScanning,
            ) { wiFiData, settingsData, isScanning ->
                val wiFiBand = settingsData.wiFiBand
                val countryCode = settingsData.countryCode
                val channelRating = ChannelRating()
                val wiFiChannels = wiFiBand.wiFiChannels.availableChannels(wiFiBand, countryCode)
                channelRating.wiFiDetails(
                    wiFiData.wiFiDetails(
                        wiFiBand.predicate(),
                        SortBy.STRENGTH,
                    ),
                )
                val bestChannels = channelRating.bestChannels(wiFiBand, wiFiChannels)

                ChannelRatingUiState(
                    wiFiData = wiFiData,
                    wiFiBand = wiFiBand,
                    wiFiChannels = wiFiChannels,
                    bestChannels = bestChannels,
                    channelRating = channelRating,
                    wiFiBandAvailable = wiFiBand.available(wiFiManagerWrapper),
                    scanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
                    permissionEnabled = permissionService.enabled(),
                    isScanning = isScanning,
                    connectionViewType = settingsData.connectionViewType,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ChannelRatingUiState(),
            )

        fun update() {
            scannerService.update()
        }
    }
