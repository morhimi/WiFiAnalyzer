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

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class PingRunner
    @Inject
    constructor() {
        open fun ping(
            host: String,
            timeoutMs: Int = 1000,
        ): Double? {
            if (host.isBlank()) return null
            return pingIcmp(host, timeoutMs) ?: pingSocket(host, timeoutMs)
        }

        internal open fun pingIcmp(
            host: String,
            timeoutMs: Int,
        ): Double? =
            runCatching {
                val timeoutSec = maxOf(1, timeoutMs / 1000)
                val process =
                    ProcessBuilder("/system/bin/ping", "-c", "1", "-W", "$timeoutSec", host)
                        .redirectErrorStream(true)
                        .start()
                val text = process.inputStream.bufferedReader().use { it.readText() }
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    parseIcmpOutput(text)
                } else {
                    null
                }
            }.getOrNull()

        internal open fun parseIcmpOutput(output: String): Double? {
            val regex = """time[=<]([0-9.]+)\s*ms""".toRegex(RegexOption.IGNORE_CASE)
            val match = regex.find(output)
            return match?.groupValues?.get(1)?.toDoubleOrNull()
        }

        internal open fun pingSocket(
            host: String,
            timeoutMs: Int,
        ): Double? =
            runCatching {
                val start = System.nanoTime()
                val inet = InetAddress.getByName(host)
                if (inet.isReachable(timeoutMs)) {
                    val end = System.nanoTime()
                    (end - start) / 1_000_000.0
                } else {
                    pingSocketPort(host, 53, timeoutMs)
                }
            }.getOrNull()

        internal open fun pingSocketPort(
            host: String,
            port: Int,
            timeoutMs: Int,
        ): Double? =
            runCatching {
                val start = System.nanoTime()
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeoutMs)
                }
                val end = System.nanoTime()
                (end - start) / 1_000_000.0
            }.getOrNull()
    }
