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
import com.vrem.annotation.OpenClass
import com.vrem.wifianalyzer.wifi.model.WiFiData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
@OpenClass
class WiFiScanViewModel
    @Inject
    constructor(
        private val scannerService: ScannerService,
    ) : ViewModel() {
        private val _wiFiData: MutableStateFlow<WiFiData> = MutableStateFlow(scannerService.wiFiData())
        val wiFiData: StateFlow<WiFiData> = _wiFiData.asStateFlow()
        val isScanning: StateFlow<Boolean> = scannerService.runningFlow

        internal val updateNotifier: UpdateNotifier =
            UpdateNotifier { data ->
                _wiFiData.value = data
            }

        init {
            scannerService.register(updateNotifier)
        }

        fun update() {
            scannerService.update()
        }

        public override fun onCleared() {
            scannerService.unregister(updateNotifier)
            super.onCleared()
        }
    }
