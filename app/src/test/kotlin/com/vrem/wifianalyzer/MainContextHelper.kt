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
import com.vrem.util.EMPTY
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

enum class MainContextHelper {
    INSTANCE,
    ;

    private val saved: MutableMap<Class<*>, Any> = mutableMapOf()
    private val mainContext: MainContext = MainContext.INSTANCE

    val context: Context
        get() {
            runCatching { saved[Context::class.java] = mainContext.context }
            mainContext.context = mock()
            return mainContext.context
        }

    val settings: Settings
        get() {
            runCatching { saved[Settings::class.java] = mainContext.settings }
            mainContext.settings = mock()
            return mainContext.settings
        }

    val vendorService: VendorService
        get() {
            runCatching { saved[VendorService::class.java] = mainContext.vendorService }
            mainContext.vendorService = mock()
            return mainContext.vendorService
        }

    val apAliasService: ApAliasService
        get() {
            runCatching { saved[ApAliasService::class.java] = mainContext.apAliasService }
            val mockApAliasService: ApAliasService = mock()
            whenever(mockApAliasService.getAlias(any())).thenReturn(String.EMPTY)
            mainContext.apAliasService = mockApAliasService
            return mainContext.apAliasService
        }

    val permissionService: PermissionService
        get() {
            runCatching { saved[PermissionService::class.java] = mainContext.permissionService }
            mainContext.permissionService = mock()
            return mainContext.permissionService
        }

    val scannerService: ScannerService
        get() {
            runCatching { saved[ScannerService::class.java] = mainContext.scannerService }
            mainContext.scannerService = mock()
            return mainContext.scannerService
        }

    val configuration: Configuration
        get() {
            runCatching { saved[Configuration::class.java] = mainContext.configuration }
            mainContext.configuration = mock()
            return mainContext.configuration
        }

    val filterAdapter: FiltersAdapter
        get() {
            runCatching { saved[FiltersAdapter::class.java] = mainContext.filtersAdapter }
            mainContext.filtersAdapter = mock()
            return mainContext.filtersAdapter
        }

    val wiFiManagerWrapper: WiFiManagerWrapper
        get() {
            runCatching { saved[WiFiManagerWrapper::class.java] = mainContext.wiFiManagerWrapper }
            mainContext.wiFiManagerWrapper = mock()
            return mainContext.wiFiManagerWrapper
        }

    fun restore() {
        saved.entries.forEach {
            when (it.key) {
                Context::class.java -> mainContext.context = it.value as Context
                Settings::class.java -> mainContext.settings = it.value as Settings
                VendorService::class.java -> mainContext.vendorService = it.value as VendorService
                ApAliasService::class.java -> mainContext.apAliasService = it.value as ApAliasService
                ScannerService::class.java -> mainContext.scannerService = it.value as ScannerService
                Configuration::class.java -> mainContext.configuration = it.value as Configuration
                FiltersAdapter::class.java -> mainContext.filtersAdapter = it.value as FiltersAdapter
                WiFiManagerWrapper::class.java -> mainContext.wiFiManagerWrapper = it.value as WiFiManagerWrapper
            }
        }
        saved.clear()
        runCatching {
            val activity = RobolectricUtil.INSTANCE.activity
            activity.viewModelStore.clear()
            mainContext.initialize(activity.applicationContext, activity.largeScreen)
        }
    }
}
