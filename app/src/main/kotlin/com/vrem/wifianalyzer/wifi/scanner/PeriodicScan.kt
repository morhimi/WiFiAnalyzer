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

import com.vrem.wifianalyzer.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class PeriodicScan(
    private val scanner: ScannerService,
    private val settings: Settings,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    private var job: Job? = null

    internal val running: Boolean
        get() = job?.isActive == true

    fun stop() {
        job?.cancel()
        job = null
    }

    fun start() {
        schedule(DELAY_INITIAL)
    }

    fun startWithDelay() {
        schedule(settings.scanSpeed() * DELAY_INTERVAL)
    }

    private fun schedule(initialDelay: Long) {
        stop()
        job =
            scope.launch(dispatcher) {
                delay(initialDelay)
                while (isActive) {
                    scanner.update()
                    delay(settings.scanSpeed() * DELAY_INTERVAL)
                }
            }
    }

    companion object {
        private const val DELAY_INITIAL = 1L
        const val DELAY_INTERVAL = 1000L
    }
}
