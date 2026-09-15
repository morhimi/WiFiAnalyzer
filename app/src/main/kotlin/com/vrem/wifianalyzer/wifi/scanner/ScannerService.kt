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

import android.content.Context
import android.net.ConnectivityManager
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.model.WiFiData
import kotlinx.coroutines.flow.StateFlow

interface ScannerService {
    val runningFlow: StateFlow<Boolean>
    val wiFiDataFlow: StateFlow<WiFiData>

    fun update()

    fun wiFiData(): WiFiData

    fun pause()

    fun running(): Boolean

    fun resume()

    fun resumeWithDelay()

    fun stop()

    fun toggle()
}

fun makeScannerService(
    context: Context,
    wiFiManagerWrapper: WiFiManagerWrapper,
    permissionService: PermissionService,
    settings: Settings,
    apAliasService: ApAliasService,
    vendorService: VendorService,
    connectivityManager: ConnectivityManager? = null,
): ScannerService {
    val cache = Cache(settings)
    val transformer = Transformer(cache, apAliasService, vendorService)
    val scannerCallback = ScannerCallback(wiFiManagerWrapper, cache)
    val scanResultsReceiver = ScanResultsReceiver(context, scannerCallback)
    val scanner =
        Scanner(
            wiFiManagerWrapper = wiFiManagerWrapper,
            settings = settings,
            permissionService = permissionService,
            transformer = transformer,
            cache = cache,
            scanResultsReceiver = scanResultsReceiver,
            scannerCallback = scannerCallback,
        )
    scanner.wiFiConnectionCallback =
        connectivityManager?.let { cm ->
            WiFiConnectionCallback(cm, scanner::onConnectionChanged)
        }
    return scanner
}
