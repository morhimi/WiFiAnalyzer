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
package com.vrem.wifianalyzer

import android.content.Context
import android.content.res.Resources
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.makeScannerService

enum class MainContext {
    INSTANCE,
    ;

    lateinit var context: Context
    lateinit var settings: Settings
    lateinit var wiFiManagerWrapper: WiFiManagerWrapper
    lateinit var permissionService: PermissionService
    lateinit var scannerService: ScannerService
    lateinit var vendorService: VendorService
    lateinit var apAliasService: ApAliasService
    lateinit var configuration: Configuration
    lateinit var filtersAdapter: FiltersAdapter

    val resources: Resources get() = context.resources

    private val wiFiManager: WifiManager get() = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun initialize(
        context: Context,
        largeScreen: Boolean,
    ) {
        this.context = context.applicationContext
        configuration = Configuration(largeScreen)
        val repository = Repository(this.context)
        settings = Settings(repository)
        apAliasService = ApAliasService(repository)
        vendorService = VendorService(this.context.resources)
        wiFiManagerWrapper = WiFiManagerWrapper(wiFiManager)
        permissionService = PermissionService(this.context)
        scannerService = makeScannerService(this.context, wiFiManagerWrapper, Handler(Looper.getMainLooper()), settings)
        filtersAdapter = FiltersAdapter(settings)
    }
}
