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

import com.vrem.annotation.OpenClass
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.WiFiData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OpenClass
internal class Scanner(
    val wiFiManagerWrapper: WiFiManagerWrapper,
    val settings: Settings,
    val permissionService: PermissionService,
    val transformer: Transformer,
) : ScannerService {
    private val _runningFlow = MutableStateFlow(false)
    override val runningFlow: StateFlow<Boolean> = _runningFlow.asStateFlow()

    private val _wiFiDataFlow = MutableStateFlow(WiFiData.EMPTY)
    override val wiFiDataFlow: StateFlow<WiFiData> = _wiFiDataFlow.asStateFlow()

    private val updateNotifiers: MutableList<UpdateNotifier> = mutableListOf()

    private var wiFiData: WiFiData = WiFiData.EMPTY
    private var initialScan: Boolean = false

    lateinit var periodicScan: PeriodicScan
    lateinit var scannerCallback: ScannerCallback
    lateinit var scanResultsReceiver: ScanResultsReceiver

    override fun update() {
        wiFiManagerWrapper.enableWiFi()
        if (permissionService.enabled()) {
            scanResultsReceiver.register()
            wiFiManagerWrapper.startScan()
            if (!initialScan) {
                scannerCallback.onSuccess()
                initialScan = true
            }
        }
        wiFiData = transformer.transformToWiFiData()
        _wiFiDataFlow.value = wiFiData
        updateNotifiers.forEach { it.update(wiFiData) }
    }

    override fun wiFiData(): WiFiData = wiFiData

    override fun register(updateNotifier: UpdateNotifier): Boolean = updateNotifiers.add(updateNotifier)

    override fun unregister(updateNotifier: UpdateNotifier): Boolean = updateNotifiers.remove(updateNotifier)

    override fun pause() {
        periodicScan.stop()
        _runningFlow.value = false
        scanResultsReceiver.unregister()
    }

    override fun running(): Boolean = periodicScan.running

    override fun resume() {
        periodicScan.start()
        _runningFlow.value = true
    }

    override fun resumeWithDelay() {
        periodicScan.startWithDelay()
        _runningFlow.value = true
    }

    override fun stop() {
        periodicScan.stop()
        _runningFlow.value = false
        updateNotifiers.clear()
        if (settings.wiFiOffOnExit()) {
            wiFiManagerWrapper.disableWiFi()
        }
        scanResultsReceiver.unregister()
    }

    override fun toggle() {
        if (periodicScan.running) {
            pause()
        } else {
            resume()
        }
    }

    fun registered(): Int = updateNotifiers.size
}
