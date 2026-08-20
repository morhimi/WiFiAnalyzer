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
import org.junit.Before
import org.junit.Test

class PingRunnerTest {
    private lateinit var fixture: PingRunner

    @Before
    fun setUp() {
        fixture = PingRunner()
    }

    @Test
    fun pingReturnsNullWhenHostIsBlank() {
        assertThat(fixture.ping("")).isNull()
        assertThat(fixture.ping("   ")).isNull()
    }

    @Test
    fun pingIcmpHandlesInvalidHostGracefully() {
        val actual = fixture.pingIcmp("invalid.nonexistent.local", 100)
        assertThat(actual).isNull()
    }

    @Test
    fun parseIcmpOutputParsesValidPingOutput() {
        val output = "64 bytes from 192.168.1.1: icmp_seq=1 ttl=64 time=14.32 ms"
        val actual = fixture.parseIcmpOutput(output)
        assertThat(actual).isEqualTo(14.32)
    }

    @Test
    fun parseIcmpOutputReturnsNullForInvalidOutput() {
        val output = "Request timeout for icmp_seq 0"
        val actual = fixture.parseIcmpOutput(output)
        assertThat(actual).isNull()
    }

    @Test
    fun pingSocketHandlesUnreachableHostGracefully() {
        val actual = fixture.pingSocket("192.0.2.1", 100) // TEST-NET-1 unreachable address
        assertThat(actual).isNull()
    }

    @Test
    fun pingSocketPortHandlesUnreachableHostGracefully() {
        val actual = fixture.pingSocketPort("192.0.2.1", 53, 100)
        assertThat(actual).isNull()
    }

    @Test
    fun pingFallsBackToSocketWhenIcmpFails() {
        val testRunner =
            object : PingRunner() {
                override fun pingIcmp(
                    host: String,
                    timeoutMs: Int,
                ): Double? = null

                override fun pingSocket(
                    host: String,
                    timeoutMs: Int,
                ): Double = 4.2
            }

        val actual = testRunner.ping("192.168.1.1", 1000)
        assertThat(actual).isEqualTo(4.2)
    }

    @Test
    fun pingReturnsIcmpResultWhenSuccessful() {
        val testRunner =
            object : PingRunner() {
                override fun pingIcmp(
                    host: String,
                    timeoutMs: Int,
                ): Double = 3.5

                override fun pingSocket(
                    host: String,
                    timeoutMs: Int,
                ): Double = 10.0
            }

        val actual = testRunner.ping("192.168.1.1", 1000)
        assertThat(actual).isEqualTo(3.5)
    }
}
