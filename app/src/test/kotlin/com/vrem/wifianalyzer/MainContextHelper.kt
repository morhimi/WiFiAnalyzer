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
 * along with this program.  1f not, see <http://www.gnu.org/licenses/>
 */
package com.vrem.wifianalyzer

import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.accesspoint.AliasRepository
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import org.mockito.kotlin.mock

enum class MainContextHelper {
    INSTANCE,
    ;

    private val saved: MutableMap<Class<*>, Any> = mutableMapOf()
    private val mainContext: MainContext = MainContext.INSTANCE

    val settings: Settings
        get() {
            runCatching { save(Settings::class.java, mainContext.settings) }
            mainContext.settings = mock()
            return mainContext.settings
        }

    val vendorService: VendorService
        get() {
            runCatching { save(VendorService::class.java, mainContext.vendorService) }
            mainContext.vendorService = mock()
            return mainContext.vendorService
        }

    val permissionService: PermissionService
        get() {
            runCatching { save(PermissionService::class.java, mainContext.permissionService) }
            mainContext.permissionService = mock()
            return mainContext.permissionService
        }

    val scannerService: ScannerService
        get() {
            runCatching { save(ScannerService::class.java, mainContext.scannerService) }
            mainContext.scannerService = mock()
            return mainContext.scannerService
        }

    val mainActivity: MainActivity
        get() {
            runCatching { save(MainActivity::class.java, mainContext.mainActivity) }
            mainContext.mainActivity = mock()
            return mainContext.mainActivity
        }

    val configuration: Configuration
        get() {
            runCatching { save(Configuration::class.java, mainContext.configuration) }
            mainContext.configuration = mock()
            return mainContext.configuration
        }

    val filterAdapter: FiltersAdapter
        get() {
            runCatching { save(FiltersAdapter::class.java, mainContext.filtersAdapter) }
            mainContext.filtersAdapter = mock()
            return mainContext.filtersAdapter
        }

    val aliasRepository: AliasRepository
        get() {
            runCatching { save(AliasRepository::class.java, mainContext.aliasRepository) }
            mainContext.aliasRepository = mock()
            return mainContext.aliasRepository
        }

    val wiFiManagerWrapper: WiFiManagerWrapper
        get() {
            runCatching { save(WiFiManagerWrapper::class.java, mainContext.wiFiManagerWrapper) }
            mainContext.wiFiManagerWrapper = mock()
            return mainContext.wiFiManagerWrapper
        }

    private fun save(
        clazz: Class<*>,
        value: Any,
    ) {
        if (!saved.containsKey(clazz)) {
            saved[clazz] = value
        }
    }

    fun restore() {
        saved.entries.forEach {
            when (it.key) {
                Settings::class.java -> mainContext.settings = it.value as Settings
                VendorService::class.java -> mainContext.vendorService = it.value as VendorService
                ScannerService::class.java -> mainContext.scannerService = it.value as ScannerService
                MainActivity::class.java -> mainContext.mainActivity = it.value as MainActivity
                Configuration::class.java -> mainContext.configuration = it.value as Configuration
                FiltersAdapter::class.java -> mainContext.filtersAdapter = it.value as FiltersAdapter
                AliasRepository::class.java -> mainContext.aliasRepository = it.value as AliasRepository
                WiFiManagerWrapper::class.java -> mainContext.wiFiManagerWrapper = it.value as WiFiManagerWrapper
            }
        }
        saved.clear()
    }
}
