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

enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    UNREACHABLE,
}

enum class PingStatus {
    IDLE,
    MEASURING,
    PAUSED,
}

data class PingSample(
    val timestamp: Long,
    val rttMs: Double,
    val success: Boolean,
)

data class PingUiState(
    val gatewayInfo: GatewayInfo = GatewayInfo.EMPTY,
    val status: PingStatus = PingStatus.IDLE,
    val currentRttMs: Double = 0.0,
    val minRttMs: Double = 0.0,
    val maxRttMs: Double = 0.0,
    val avgRttMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val packetsSent: Int = 0,
    val packetsReceived: Int = 0,
    val packetLossPercent: Double = 0.0,
    val quality: NetworkQuality = NetworkQuality.UNREACHABLE,
    val history: List<PingSample> = emptyList(),
) {
    companion object {
        val INITIAL = PingUiState()
    }
}
