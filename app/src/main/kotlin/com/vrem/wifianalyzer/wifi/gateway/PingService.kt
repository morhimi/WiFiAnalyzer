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
package com.vrem.wifianalyzer.wifi.gateway

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

private const val MAX_HISTORY = 30
private const val PING_INTERVAL_MS = 1000L
private const val PING_TIMEOUT_MS = 1000

@Singleton
class PingService
    @Inject
    constructor(
        private val gatewayProvider: GatewayProvider,
        private val pingRunner: PingRunner,
    ) {
        private val _pingState = MutableStateFlow(PingUiState.INITIAL)
        val pingState: StateFlow<PingUiState> = _pingState.asStateFlow()

        private var pingJob: Job? = null
        private val rtts = mutableListOf<Double>()
        private var lastRtt: Double? = null

        fun start(
            scope: CoroutineScope,
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ) {
            if (pingJob?.isActive == true) return

            val gwInfo = gatewayProvider.getGatewayInfo()
            _pingState.value =
                _pingState.value.copy(
                    gatewayInfo = gwInfo,
                    status = if (gwInfo.isConnected) PingStatus.MEASURING else PingStatus.IDLE,
                )

            if (!gwInfo.isConnected) return

            pingJob =
                scope.launch(dispatcher) {
                    while (isActive) {
                        val currentGw = gatewayProvider.getGatewayInfo()
                        if (!currentGw.isConnected) {
                            _pingState.value =
                                _pingState.value.copy(
                                    gatewayInfo = currentGw,
                                    status = PingStatus.IDLE,
                                )
                            break
                        }

                        val rtt = pingRunner.ping(currentGw.gatewayIp, PING_TIMEOUT_MS)
                        updateStateWithPingResult(currentGw, rtt)
                        delay(PING_INTERVAL_MS)
                    }
                }
        }

        internal fun updateStateWithPingResult(
            gwInfo: GatewayInfo,
            rtt: Double?,
        ) {
            val currentState = _pingState.value
            val newSent = currentState.packetsSent + 1
            val isSuccess = rtt != null
            val newReceived = if (isSuccess) currentState.packetsReceived + 1 else currentState.packetsReceived
            val packetLoss = if (newSent > 0) ((newSent - newReceived).toDouble() / newSent) * 100.0 else 0.0

            var newJitter = currentState.jitterMs
            if (rtt != null) {
                rtts.add(rtt)
                val prev = lastRtt
                newJitter =
                    if (prev != null) {
                        newJitter + (abs(rtt - prev) - newJitter) / 16.0
                    } else {
                        0.0
                    }
                lastRtt = rtt
            }

            val minRtt = rtts.minOrNull() ?: 0.0
            val maxRtt = rtts.maxOrNull() ?: 0.0
            val avgRtt = if (rtts.isNotEmpty()) rtts.average() else 0.0

            val quality =
                when {
                    newReceived == 0 -> NetworkQuality.UNREACHABLE
                    avgRtt < 10.0 -> NetworkQuality.EXCELLENT
                    avgRtt < 30.0 -> NetworkQuality.GOOD
                    avgRtt < 70.0 -> NetworkQuality.FAIR
                    else -> NetworkQuality.POOR
                }

            val sample =
                PingSample(
                    timestamp = System.currentTimeMillis(),
                    rttMs = rtt ?: 0.0,
                    success = isSuccess,
                )
            val newHistory = (currentState.history + sample).takeLast(MAX_HISTORY)

            _pingState.value =
                PingUiState(
                    gatewayInfo = gwInfo,
                    status = PingStatus.MEASURING,
                    currentRttMs = rtt ?: 0.0,
                    minRttMs = minRtt,
                    maxRttMs = maxRtt,
                    avgRttMs = avgRtt,
                    jitterMs = newJitter,
                    packetsSent = newSent,
                    packetsReceived = newReceived,
                    packetLossPercent = packetLoss,
                    quality = quality,
                    history = newHistory,
                )
        }

        fun pause() {
            pingJob?.cancel()
            pingJob = null
            _pingState.value = _pingState.value.copy(status = PingStatus.PAUSED)
        }

        fun reset() {
            pingJob?.cancel()
            pingJob = null
            rtts.clear()
            lastRtt = null
            val gwInfo = gatewayProvider.getGatewayInfo()
            _pingState.value =
                PingUiState(
                    gatewayInfo = gwInfo,
                    status = PingStatus.IDLE,
                )
        }

        fun isRunning(): Boolean = pingJob?.isActive == true
    }
