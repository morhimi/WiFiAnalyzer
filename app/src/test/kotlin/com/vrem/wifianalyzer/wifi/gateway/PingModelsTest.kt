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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PingModelsTest {
    @Test
    fun gatewayInfoDefaultValues() {
        val gw = GatewayInfo()
        assertThat(gw.gatewayIp).isEmpty()
        assertThat(gw.localIp).isEmpty()
        assertThat(gw.interfaceName).isEmpty()
        assertThat(gw.isConnected).isFalse()
        assertThat(gw.ssid).isEmpty()
        assertThat(gw.linkSpeedMbps).isEqualTo(0)
    }

    @Test
    fun pingSampleDefaultValues() {
        val sample = PingSample(timestamp = 1000L, rttMs = 12.3, success = true)
        assertThat(sample.timestamp).isEqualTo(1000L)
        assertThat(sample.rttMs).isEqualTo(12.3)
        assertThat(sample.success).isTrue()
    }

    @Test
    fun pingUiStateDefaultValues() {
        val state = PingUiState.INITIAL
        assertThat(state.gatewayInfo.isConnected).isFalse()
        assertThat(state.status).isEqualTo(PingStatus.IDLE)
        assertThat(state.currentRttMs).isEqualTo(0.0)
        assertThat(state.minRttMs).isEqualTo(0.0)
        assertThat(state.maxRttMs).isEqualTo(0.0)
        assertThat(state.avgRttMs).isEqualTo(0.0)
        assertThat(state.jitterMs).isEqualTo(0.0)
        assertThat(state.packetsSent).isEqualTo(0)
        assertThat(state.packetsReceived).isEqualTo(0)
        assertThat(state.packetLossPercent).isEqualTo(0.0)
        assertThat(state.quality).isEqualTo(NetworkQuality.UNREACHABLE)
        assertThat(state.history).isEmpty()
    }
}
