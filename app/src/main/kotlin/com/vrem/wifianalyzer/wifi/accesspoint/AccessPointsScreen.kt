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
package com.vrem.wifianalyzer.wifi.accesspoint

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessPointsScreen(
    wiFiData: WiFiData,
    wiFiDetails: List<WiFiDetail>,
    viewType: AccessPointViewType,
    wiFiBandAvailable: Boolean,
    wiFiBandName: String,
    scanThrottleEnabled: Boolean,
    permissionEnabled: Boolean,
    isScanning: Boolean,
    isRefreshing: Boolean,
    connectionViewType: ConnectionViewType = ConnectionViewType.COMPACT,
    onRefresh: () -> Unit,
    onDetailClick: (WiFiDetail) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ConnectionHeader(
                    wiFiData = wiFiData,
                    wiFiBandAvailable = wiFiBandAvailable,
                    wiFiBandName = wiFiBandName,
                    scanThrottleEnabled = scanThrottleEnabled,
                    permissionEnabled = permissionEnabled,
                    isScanning = isScanning,
                    connectionViewType = connectionViewType,
                    onDetailClick = onDetailClick,
                )
                AccessPointsList(
                    wiFiDetails = wiFiDetails,
                    viewType = viewType,
                    onDetailClick = onDetailClick,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                )
            }
        }
    }
}
