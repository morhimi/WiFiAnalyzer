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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PingServiceTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val gatewayProvider: GatewayProvider = mock()
    private val pingRunner: PingRunner = mock()

    private lateinit var fixture: PingService

    @Before
    fun setUp() {
        fixture = PingService(gatewayProvider, pingRunner)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(gatewayProvider, pingRunner)
    }

    @Test
    fun initialStateIsDefault() {
        val state = fixture.pingState.value
        assertThat(state.status).isEqualTo(PingStatus.IDLE)
        assertThat(state.packetsSent).isEqualTo(0)
        assertThat(state.packetsReceived).isEqualTo(0)
        assertThat(state.currentRttMs).isEqualTo(0.0)
    }

    @Test
    fun updateStateWithPingResultCalculatesMetricsCorrectly() {
        val gw = GatewayInfo(gatewayIp = "192.168.1.1", isConnected = true)

        fixture.updateStateWithPingResult(gw, 8.0)
        var state = fixture.pingState.value
        assertThat(state.packetsSent).isEqualTo(1)
        assertThat(state.packetsReceived).isEqualTo(1)
        assertThat(state.currentRttMs).isEqualTo(8.0)
        assertThat(state.minRttMs).isEqualTo(8.0)
        assertThat(state.maxRttMs).isEqualTo(8.0)
        assertThat(state.avgRttMs).isEqualTo(8.0)
        assertThat(state.jitterMs).isEqualTo(0.0)
        assertThat(state.packetLossPercent).isEqualTo(0.0)
        assertThat(state.quality).isEqualTo(NetworkQuality.EXCELLENT)
        assertThat(state.history).hasSize(1)

        fixture.updateStateWithPingResult(gw, 24.0)
        state = fixture.pingState.value
        assertThat(state.packetsSent).isEqualTo(2)
        assertThat(state.packetsReceived).isEqualTo(2)
        assertThat(state.currentRttMs).isEqualTo(24.0)
        assertThat(state.minRttMs).isEqualTo(8.0)
        assertThat(state.maxRttMs).isEqualTo(24.0)
        assertThat(state.avgRttMs).isEqualTo(16.0)
        assertThat(state.jitterMs).isEqualTo(1.0)
        assertThat(state.packetLossPercent).isEqualTo(0.0)
        assertThat(state.quality).isEqualTo(NetworkQuality.GOOD)
        assertThat(state.history).hasSize(2)

        fixture.updateStateWithPingResult(gw, null)
        state = fixture.pingState.value
        assertThat(state.packetsSent).isEqualTo(3)
        assertThat(state.packetsReceived).isEqualTo(2)
        assertThat(state.packetLossPercent).isGreaterThan(33.0)
    }

    @Test
    fun updateStateWithDifferentQualityLevels() {
        val gw = GatewayInfo(gatewayIp = "192.168.1.1", isConnected = true)

        fixture.updateStateWithPingResult(gw, 50.0)
        assertThat(fixture.pingState.value.quality).isEqualTo(NetworkQuality.FAIR)

        val poorFixture = PingService(gatewayProvider, pingRunner)
        poorFixture.updateStateWithPingResult(gw, 95.0)
        assertThat(poorFixture.pingState.value.quality).isEqualTo(NetworkQuality.POOR)

        val unreachableFixture = PingService(gatewayProvider, pingRunner)
        unreachableFixture.updateStateWithPingResult(gw, null)
        assertThat(unreachableFixture.pingState.value.quality).isEqualTo(NetworkQuality.UNREACHABLE)
    }

    @Test
    fun startWhenNotConnectedRemainsIdle() {
        val gw = GatewayInfo(isConnected = false)
        whenever(gatewayProvider.getGatewayInfo()).thenReturn(gw)

        fixture.start(testScope, testDispatcher)

        assertThat(fixture.pingState.value.status).isEqualTo(PingStatus.IDLE)
        assertThat(fixture.isRunning()).isFalse()
        verify(gatewayProvider).getGatewayInfo()
    }

    @Test
    fun startWhenConnectedStartsPingLoop() =
        testScope.runTest {
            val gw = GatewayInfo(gatewayIp = "192.168.1.1", isConnected = true)
            whenever(gatewayProvider.getGatewayInfo()).thenReturn(gw)
            whenever(pingRunner.ping("192.168.1.1", 1000)).thenReturn(12.5)

            fixture.start(testScope, testDispatcher)
            assertThat(fixture.isRunning()).isTrue()
            advanceTimeBy(1100)

            assertThat(fixture.pingState.value.packetsSent).isGreaterThanOrEqualTo(1)
            assertThat(fixture.pingState.value.currentRttMs).isEqualTo(12.5)

            fixture.pause()
            assertThat(fixture.isRunning()).isFalse()
            assertThat(fixture.pingState.value.status).isEqualTo(PingStatus.PAUSED)

            verify(gatewayProvider, org.mockito.kotlin.atLeastOnce()).getGatewayInfo()
            verify(pingRunner, org.mockito.kotlin.atLeastOnce()).ping("192.168.1.1", 1000)
        }

    @Test
    fun startWhenAlreadyRunningIsNoOp() =
        testScope.runTest {
            val gw = GatewayInfo(gatewayIp = "192.168.1.1", isConnected = true)
            whenever(gatewayProvider.getGatewayInfo()).thenReturn(gw)
            whenever(pingRunner.ping("192.168.1.1", 1000)).thenReturn(10.0)

            fixture.start(testScope, testDispatcher)
            fixture.start(testScope, testDispatcher)

            assertThat(fixture.isRunning()).isTrue()
            fixture.pause()

            verify(gatewayProvider, org.mockito.kotlin.atLeastOnce()).getGatewayInfo()
        }

    @Test
    fun startDisconnectsMidLoop() =
        testScope.runTest {
            val connectedGw = GatewayInfo(gatewayIp = "192.168.1.1", isConnected = true)
            val disconnectedGw = GatewayInfo(isConnected = false)
            whenever(gatewayProvider.getGatewayInfo())
                .thenReturn(connectedGw)
                .thenReturn(disconnectedGw)

            fixture.start(testScope, testDispatcher)
            advanceTimeBy(100)

            assertThat(fixture.isRunning()).isFalse()
            assertThat(fixture.pingState.value.status).isEqualTo(PingStatus.IDLE)

            verify(gatewayProvider, org.mockito.kotlin.atLeastOnce()).getGatewayInfo()
        }

    @Test
    fun resetCancelsJobAndClearsStats() {
        val gw = GatewayInfo(gatewayIp = "192.168.1.1", isConnected = true)
        whenever(gatewayProvider.getGatewayInfo()).thenReturn(gw)

        fixture.updateStateWithPingResult(gw, 15.0)
        assertThat(fixture.pingState.value.packetsSent).isEqualTo(1)

        fixture.reset()
        val state = fixture.pingState.value
        assertThat(state.status).isEqualTo(PingStatus.IDLE)
        assertThat(state.packetsSent).isEqualTo(0)
        assertThat(state.packetsReceived).isEqualTo(0)
        assertThat(state.history).isEmpty()

        verify(gatewayProvider).getGatewayInfo()
    }
}
