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

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

internal class WiFiConnectionCallback(
    private val connectivityManager: ConnectivityManager,
    private val onConnectionChanged: () -> Unit,
) : ConnectivityManager.NetworkCallback() {
    internal var registered: Boolean = false
        private set

    fun register() {
        if (!registered) {
            runCatching {
                val request =
                    NetworkRequest
                        .Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build()
                connectivityManager.registerNetworkCallback(request, this)
                registered = true
            }
        }
    }

    fun unregister() {
        if (registered) {
            runCatching {
                connectivityManager.unregisterNetworkCallback(this)
            }
            registered = false
        }
    }

    override fun onAvailable(network: Network) {
        onConnectionChanged()
    }

    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities,
    ) {
        onConnectionChanged()
    }

    override fun onLinkPropertiesChanged(
        network: Network,
        linkProperties: LinkProperties,
    ) {
        onConnectionChanged()
    }

    override fun onLost(network: Network) {
        onConnectionChanged()
    }
}
