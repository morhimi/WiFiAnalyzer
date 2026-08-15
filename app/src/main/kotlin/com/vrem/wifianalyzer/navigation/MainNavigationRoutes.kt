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

import android.content.Intent
import android.os.Build
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vrem.util.packageInfo
import com.vrem.util.readFile
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.about.AboutScreen
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.vendor.VendorsScreen
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointsScreen
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.channelavailable.ChannelAvailableScreen
import com.vrem.wifianalyzer.wifi.channelgraph.ChannelGraph
import com.vrem.wifianalyzer.wifi.channelrating.ChannelRatingScreen
import com.vrem.wifianalyzer.wifi.detailview.WiFiDetailPopup
import com.vrem.wifianalyzer.wifi.graphutils.GraphAdapter
import com.vrem.wifianalyzer.wifi.graphutils.WiFiGraphScreen
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ChannelRating
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.predicate.makeAccessPointsPredicate
import com.vrem.wifianalyzer.wifi.predicate.predicate
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.WiFiScanViewModel
import com.vrem.wifianalyzer.wifi.timegraph.TimeGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import com.vrem.wifianalyzer.Configuration as WiFiConfiguration

@Composable
fun AccessPointsRoute(
    wiFiScanViewModel: WiFiScanViewModel,
    settings: Settings,
    wiFiManagerWrapper: WiFiManagerWrapper,
    permissionService: PermissionService,
    scannerService: ScannerService,
) {
    val wiFiData by wiFiScanViewModel.wiFiData.collectAsStateWithLifecycle()
    val settingsData by settings.settingsData.collectAsStateWithLifecycle()
    val wiFiBand = settingsData.wiFiBand
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity

    AccessPointsScreen(
        wiFiData = wiFiData,
        wiFiDetails =
            wiFiData.wiFiDetails(
                makeAccessPointsPredicate(settingsData),
                settingsData.sortBy,
                settingsData.groupBy,
            ),
        viewType = settingsData.accessPointView,
        wiFiBandAvailable = wiFiBand.available(),
        wiFiBandName = stringResource(wiFiBand.textResource),
        scanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
        permissionEnabled = permissionService.enabled(),
        isScanning = scannerService.running(),
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                wiFiScanViewModel.update()
                delay(1.seconds)
                isRefreshing = false
            }
        },
        onDetailClick = { detail ->
            fragmentActivity?.supportFragmentManager?.let {
                WiFiDetailPopup.show(it, detail)
            }
        },
    )
}

@Composable
fun ChannelRatingRoute(
    wiFiScanViewModel: WiFiScanViewModel,
    settings: Settings,
    wiFiManagerWrapper: WiFiManagerWrapper,
    permissionService: PermissionService,
    scannerService: ScannerService,
) {
    val wiFiData by wiFiScanViewModel.wiFiData.collectAsStateWithLifecycle()
    val settingsData by settings.settingsData.collectAsStateWithLifecycle()
    val wiFiBand = settingsData.wiFiBand
    val countryCode = settingsData.countryCode
    val channelRating = remember { ChannelRating() }
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity

    val wiFiChannels = wiFiBand.wiFiChannels.availableChannels(wiFiBand, countryCode)
    val wiFiDetails = wiFiData.wiFiDetails(wiFiBand.predicate(), SortBy.STRENGTH)
    channelRating.wiFiDetails(wiFiDetails)
    val bestChannels = channelRating.bestChannels(wiFiBand, wiFiChannels)

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ChannelRatingScreen(
        wiFiData = wiFiData,
        wiFiBand = wiFiBand,
        wiFiChannels = wiFiChannels,
        bestChannels = bestChannels,
        channelRating = channelRating,
        wiFiBandAvailable = wiFiBand.available(),
        wiFiBandName = stringResource(wiFiBand.textResource),
        scanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
        permissionEnabled = permissionService.enabled(),
        isScanning = scannerService.running(),
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                wiFiScanViewModel.update()
                delay(1.seconds)
                isRefreshing = false
            }
        },
        onDetailClick = { detail ->
            fragmentActivity?.supportFragmentManager?.let {
                WiFiDetailPopup.show(it, detail)
            }
        },
    )
}

@Composable
fun ChannelGraphRoute(
    wiFiScanViewModel: WiFiScanViewModel,
    settings: Settings,
    wiFiManagerWrapper: WiFiManagerWrapper,
    permissionService: PermissionService,
    scannerService: ScannerService,
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val graphAdapter = remember {
        val channelGraphs = WiFiBand.entries.map { ChannelGraph(it, context = context) }
        GraphAdapter(channelGraphs)
    }

    DisposableEffect(graphAdapter) {
        onDispose {
            graphAdapter.destroy()
        }
    }

    val wiFiData by wiFiScanViewModel.wiFiData.collectAsStateWithLifecycle()
    val settingsData by settings.settingsData.collectAsStateWithLifecycle()
    val wiFiBand = settingsData.wiFiBand
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    WiFiGraphScreen(
        wiFiData = wiFiData,
        graphAdapter = graphAdapter,
        displayedChild = wiFiBand.ordinal,
        wiFiBandAvailable = wiFiBand.available(),
        wiFiBandName = stringResource(wiFiBand.textResource),
        scanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
        permissionEnabled = permissionService.enabled(),
        isScanning = scannerService.running(),
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                wiFiScanViewModel.update()
                delay(1.seconds)
                isRefreshing = false
            }
        },
        onDetailClick = { detail ->
            fragmentActivity?.supportFragmentManager?.let {
                WiFiDetailPopup.show(it, detail)
            }
        },
    )
}

@Composable
fun TimeGraphRoute(
    wiFiScanViewModel: WiFiScanViewModel,
    settings: Settings,
    wiFiManagerWrapper: WiFiManagerWrapper,
    permissionService: PermissionService,
    scannerService: ScannerService,
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val graphAdapter = remember {
        val timeGraphs = WiFiBand.entries.map { TimeGraph(it, context = context) }
        GraphAdapter(timeGraphs)
    }

    DisposableEffect(graphAdapter) {
        onDispose {
            graphAdapter.destroy()
        }
    }

    val wiFiData by wiFiScanViewModel.wiFiData.collectAsStateWithLifecycle()
    val settingsData by settings.settingsData.collectAsStateWithLifecycle()
    val wiFiBand = settingsData.wiFiBand
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    WiFiGraphScreen(
        wiFiData = wiFiData,
        graphAdapter = graphAdapter,
        displayedChild = wiFiBand.ordinal,
        wiFiBandAvailable = wiFiBand.available(),
        wiFiBandName = stringResource(wiFiBand.textResource),
        scanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
        permissionEnabled = permissionService.enabled(),
        isScanning = scannerService.running(),
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                wiFiScanViewModel.update()
                delay(1.seconds)
                isRefreshing = false
            }
        },
        onDetailClick = { detail ->
            fragmentActivity?.supportFragmentManager?.let {
                WiFiDetailPopup.show(it, detail)
            }
        },
    )
}

@Composable
fun VendorsRoute(vendorService: VendorService) {
    VendorsScreen(vendorService = vendorService)
}

@Composable
fun ChannelAvailableRoute(settings: Settings) {
    val settingsData by settings.settingsData.collectAsStateWithLifecycle()
    ChannelAvailableScreen(
        countryCode = settingsData.countryCode,
        languageLocale = settingsData.languageLocale,
    )
}

@Composable
fun AboutRoute(
    wiFiManagerWrapper: WiFiManagerWrapper,
    configuration: WiFiConfiguration,
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    AboutScreen(
        applicationName = stringResource(R.string.app_full_name),
        packageName = activity.packageName,
        versionInfo = version(activity, configuration),
        copyright = copyright(activity),
        device = device(),
        isScanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
        is5GHzBandSupported = wiFiManagerWrapper.is5GHzBandSupported(),
        is6GHzBandSupported = wiFiManagerWrapper.is6GHzBandSupported(),
        onWriteReview = { onWriteReview(activity) },
        onShowLicense = { titleId, resourceId, isSmallFont ->
            showLicense(activity, titleId, resourceId, isSmallFont)
        },
    )
}

private fun onWriteReview(activity: FragmentActivity) {
    val url = "market://details?id=" + activity.applicationContext.packageName
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    runCatching {
        activity.startActivity(intent)
    }.getOrElse {
        Toast.makeText(activity, it.localizedMessage, Toast.LENGTH_LONG).show()
    }
}

private fun showLicense(
    activity: FragmentActivity,
    titleId: Int,
    resourceId: Int,
    isSmallFont: Boolean,
) {
    if (!activity.isFinishing) {
        val text = readFile(activity.resources, resourceId)
        val alertDialog: AlertDialog =
            AlertDialog
                .Builder(activity)
                .setTitle(titleId)
                .setMessage(text)
                .setNeutralButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .create()
        alertDialog.show()
        if (isSmallFont) {
            alertDialog.findViewById<TextView>(android.R.id.message)?.textSize = 8f
        }
    }
}

private fun device(): String = Build.MANUFACTURER + " - " + Build.BRAND + " - " + Build.MODEL

private fun copyright(activity: FragmentActivity): String =
    activity.resources.getString(R.string.app_copyright) + SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

private fun version(activity: FragmentActivity, configuration: WiFiConfiguration): String {
    return applicationVersion(activity) +
        (if (configuration.sizeAvailable) "S" else "") +
        (if (configuration.largeScreen) "L" else "") +
        " (" + Build.VERSION.RELEASE + "-" + Build.VERSION.SDK_INT + ")"
}

private fun applicationVersion(activity: FragmentActivity): String =
    runCatching {
        val packageInfo = activity.packageInfo()
        packageInfo.versionName + " - " + PackageInfoCompat.getLongVersionCode(packageInfo)
    }.getOrDefault("")
