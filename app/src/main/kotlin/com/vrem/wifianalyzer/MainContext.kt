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
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsRepository
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.makeScannerService
import kotlin.properties.Delegates

enum class MainContext {
    INSTANCE,
    ;

    var context: Context by Delegates.notNull()
    var settings: Settings by Delegates.notNull()
    var wiFiManagerWrapper: WiFiManagerWrapper by Delegates.notNull()
    var permissionService: PermissionService by Delegates.notNull()
    var scannerService: ScannerService by Delegates.notNull()
    var vendorService: VendorService by Delegates.notNull()
    var apAliasService: ApAliasService by Delegates.notNull()
    var configuration: Configuration by Delegates.notNull()
    var filtersAdapter: FiltersAdapter by Delegates.notNull()

    val resources: Resources get() = context.resources

    private val wiFiManager: WifiManager get() = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun initialize(
        context: Context,
        settings: Settings,
        wiFiManagerWrapper: WiFiManagerWrapper,
        permissionService: PermissionService,
        scannerService: ScannerService,
        vendorService: VendorService,
        apAliasService: ApAliasService,
        configuration: Configuration,
        filtersAdapter: FiltersAdapter,
    ) {
        this.context = context
        this.settings = settings
        this.wiFiManagerWrapper = wiFiManagerWrapper
        this.permissionService = permissionService
        this.scannerService = scannerService
        this.vendorService = vendorService
        this.apAliasService = apAliasService
        this.configuration = configuration
        this.filtersAdapter = filtersAdapter
    }

    fun initialize(
        context: Context,
        largeScreen: Boolean,
    ) {
        // This is a fallback/legacy initializer.
        // In a Hilt-enabled app, we should use the other initialize method.
        this.context = context.applicationContext
        configuration = Configuration(largeScreen)
        val settingsRepo =
            SettingsRepository(
                PreferenceDataStoreFactory.create(
                    produceFile = { this.context.preferencesDataStoreFile("settings_fallback") },
                ),
                this.context,
            )
        settings = Settings(settingsRepo)
        apAliasService = ApAliasService(settingsRepo)
        vendorService = VendorService(this.context.resources)
        wiFiManagerWrapper = WiFiManagerWrapper(wiFiManager)
        permissionService = PermissionService(this.context)
        scannerService =
            makeScannerService(
                this.context,
                wiFiManagerWrapper,
                permissionService,
                Handler(Looper.getMainLooper()),
                settings,
            )
        filtersAdapter = FiltersAdapter(settings)
    }
}
