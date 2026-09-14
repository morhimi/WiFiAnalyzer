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
package com.vrem.wifianalyzer.navigation

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vrem.util.readFile
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.about.AboutScreen
import com.vrem.wifianalyzer.about.AboutViewModel
import com.vrem.wifianalyzer.compose.MainViewModel
import com.vrem.wifianalyzer.settings.SettingsScreen
import com.vrem.wifianalyzer.vendor.VendorsScreen
import com.vrem.wifianalyzer.vendor.VendorsViewModel
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointsScreen
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.channelavailable.ChannelAvailableScreen
import com.vrem.wifianalyzer.wifi.channelgraph.ChannelGraph
import com.vrem.wifianalyzer.wifi.channelrating.ChannelRatingScreen
import com.vrem.wifianalyzer.wifi.graphutils.GraphAdapter
import com.vrem.wifianalyzer.wifi.graphutils.WiFiGraphScreen
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.scanner.WiFiScanViewModel
import com.vrem.wifianalyzer.wifi.timegraph.TimeGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun AccessPointsRoute(
    onDetailClick: (WiFiDetail) -> Unit,
    wiFiScanViewModel: WiFiScanViewModel = hiltViewModel(),
) {
    val uiState by wiFiScanViewModel.accessPointsUiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AccessPointsScreen(
        wiFiData = uiState.wiFiData,
        wiFiDetails = uiState.wiFiDetails,
        viewType = uiState.viewType,
        wiFiBandAvailable = uiState.wiFiBandAvailable,
        wiFiBandName = stringResource(uiState.wiFiBand.textResource),
        scanThrottleEnabled = uiState.scanThrottleEnabled,
        permissionEnabled = uiState.permissionEnabled,
        isScanning = uiState.isScanning,
        isRefreshing = isRefreshing,
        connectionViewType = uiState.connectionViewType,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                wiFiScanViewModel.update()
                delay(1.seconds)
                isRefreshing = false
            }
        },
        onDetailClick = onDetailClick,
    )
}

@Composable
fun ChannelRatingRoute(
    onDetailClick: (WiFiDetail) -> Unit,
    wiFiScanViewModel: WiFiScanViewModel = hiltViewModel(),
) {
    val uiState by wiFiScanViewModel.channelRatingUiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ChannelRatingScreen(
        wiFiData = uiState.wiFiData,
        wiFiBand = uiState.wiFiBand,
        wiFiChannels = uiState.wiFiChannels,
        bestChannels = uiState.bestChannels,
        channelRating = uiState.channelRating,
        wiFiBandAvailable = uiState.wiFiBandAvailable,
        wiFiBandName = stringResource(uiState.wiFiBand.textResource),
        scanThrottleEnabled = uiState.scanThrottleEnabled,
        permissionEnabled = uiState.permissionEnabled,
        isScanning = uiState.isScanning,
        isRefreshing = isRefreshing,
        connectionViewType = uiState.connectionViewType,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                wiFiScanViewModel.update()
                delay(1.seconds)
                isRefreshing = false
            }
        },
        onDetailClick = onDetailClick,
    )
}

@Composable
fun ChannelGraphRoute(
    onDetailClick: (WiFiDetail) -> Unit,
    onShowWiFiDetails: (List<WiFiDetail>) -> Unit = { details -> details.firstOrNull()?.let(onDetailClick) },
    wiFiScanViewModel: WiFiScanViewModel = hiltViewModel(),
) {
    val currentOnShowWiFiDetails by rememberUpdatedState(onShowWiFiDetails)

    val graphAdapter =
        remember {
            val channelGraphs =
                WiFiBand.entries.map {
                    ChannelGraph(it, onShowWiFiDetails = { details -> currentOnShowWiFiDetails(details) })
                }
            GraphAdapter(channelGraphs)
        }

    DisposableEffect(graphAdapter) {
        onDispose {
            graphAdapter.destroy()
        }
    }

    val wiFiData by wiFiScanViewModel.wiFiData.collectAsStateWithLifecycle()
    val settingsData by wiFiScanViewModel.settingsData.collectAsStateWithLifecycle()
    val isScanning by wiFiScanViewModel.isScanning.collectAsStateWithLifecycle()
    val wiFiBand = settingsData.wiFiBand
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    WiFiGraphScreen(
        wiFiData = wiFiData,
        settingsData = settingsData,
        graphAdapter = graphAdapter,
        displayedChild = wiFiBand.ordinal,
        wiFiBandAvailable = wiFiScanViewModel.isBandAvailable(wiFiBand),
        wiFiBandName = stringResource(wiFiBand.textResource),
        scanThrottleEnabled = wiFiScanViewModel.isScanThrottleEnabled,
        permissionEnabled = wiFiScanViewModel.isPermissionEnabled,
        isScanning = isScanning,
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                wiFiScanViewModel.update()
                delay(1.seconds)
                isRefreshing = false
            }
        },
        onDetailClick = onDetailClick,
    )
}

@Composable
fun TimeGraphRoute(
    onDetailClick: (WiFiDetail) -> Unit,
    onShowWiFiDetails: (List<WiFiDetail>) -> Unit = { details -> details.firstOrNull()?.let(onDetailClick) },
    wiFiScanViewModel: WiFiScanViewModel = hiltViewModel(),
) {
    val currentOnShowWiFiDetails by rememberUpdatedState(onShowWiFiDetails)

    val graphAdapter =
        remember {
            val timeGraphs =
                WiFiBand.entries.map {
                    TimeGraph(it, onShowWiFiDetails = { details -> currentOnShowWiFiDetails(details) })
                }
            GraphAdapter(timeGraphs)
        }

    DisposableEffect(graphAdapter) {
        onDispose {
            graphAdapter.destroy()
        }
    }

    val wiFiData by wiFiScanViewModel.wiFiData.collectAsStateWithLifecycle()
    val settingsData by wiFiScanViewModel.settingsData.collectAsStateWithLifecycle()
    val isScanning by wiFiScanViewModel.isScanning.collectAsStateWithLifecycle()
    val wiFiBand = settingsData.wiFiBand
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    WiFiGraphScreen(
        wiFiData = wiFiData,
        settingsData = settingsData,
        graphAdapter = graphAdapter,
        displayedChild = wiFiBand.ordinal,
        wiFiBandAvailable = wiFiScanViewModel.isBandAvailable(wiFiBand),
        wiFiBandName = stringResource(wiFiBand.textResource),
        scanThrottleEnabled = wiFiScanViewModel.isScanThrottleEnabled,
        permissionEnabled = wiFiScanViewModel.isPermissionEnabled,
        isScanning = isScanning,
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                wiFiScanViewModel.update()
                delay(1.seconds)
                isRefreshing = false
            }
        },
        onDetailClick = onDetailClick,
    )
}

@Composable
fun VendorsRoute(viewModel: VendorsViewModel = hiltViewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    VendorsScreen(
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        vendors = vendors,
        findMacAddresses = viewModel::findMacAddresses,
    )
}

@Composable
fun ChannelAvailableRoute(viewModel: MainViewModel = hiltViewModel()) {
    val settingsData by viewModel.settingsData.collectAsStateWithLifecycle()
    ChannelAvailableScreen(
        countryCode = settingsData.countryCode,
        languageLocale = settingsData.languageLocale,
    )
}

@Composable
fun SettingsRoute(viewModel: MainViewModel = hiltViewModel()) {
    SettingsScreen(settings = viewModel.settings)
}

private data class LicenseDialogData(
    val titleId: Int,
    val resourceId: Int,
    val isSmallFont: Boolean,
)

@Composable
fun AboutRoute(viewModel: AboutViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var licenseData by remember { mutableStateOf<LicenseDialogData?>(null) }

    licenseData?.let { data ->
        val text = readFile(context.resources, data.resourceId)
        AlertDialog(
            onDismissRequest = { licenseData = null },
            title = { Text(stringResource(data.titleId)) },
            text = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = text,
                        style =
                            if (data.isSmallFont) {
                                MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                    )
                }
            },
            confirmButton = {
                Button(onClick = { licenseData = null }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        )
    }

    val uiState = viewModel.uiState
    AboutScreen(
        applicationName = stringResource(R.string.app_full_name),
        packageName = uiState.packageName,
        versionInfo = uiState.versionInfo,
        copyright = uiState.copyright,
        device = uiState.device,
        isScanThrottleEnabled = uiState.isScanThrottleEnabled,
        is5GHzBandSupported = uiState.is5GHzBandSupported,
        is6GHzBandSupported = uiState.is6GHzBandSupported,
        onWriteReview = { onWriteReview(context) },
        onShowLicense = { titleId, resourceId, isSmallFont ->
            licenseData = LicenseDialogData(titleId, resourceId, isSmallFont)
        },
    )
}

private fun onWriteReview(context: Context) {
    val url = "market://details?id=" + context.applicationContext.packageName
    val intent =
        Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    runCatching {
        context.startActivity(intent)
    }.getOrElse {
        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun PingRoute(
    modifier: Modifier = Modifier,
    viewModel: com.vrem.wifianalyzer.wifi.gateway.PingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    com.vrem.wifianalyzer.wifi.gateway.PingScreen(
        uiState = uiState,
        onToggle = viewModel::toggle,
        onReset = viewModel::reset,
        modifier = modifier,
    )
}
