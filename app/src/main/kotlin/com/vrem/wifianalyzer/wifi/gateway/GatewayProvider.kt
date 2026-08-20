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

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.vrem.util.buildMinVersionQ
import com.vrem.wifianalyzer.wifi.model.convertIpV4Address
import com.vrem.wifianalyzer.wifi.model.convertSSID
import java.net.Inet4Address
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class GatewayProvider
    @Inject
    constructor(
        private val connectivityManager: ConnectivityManager?,
        private val wifiManager: WifiManager?,
    ) {
        open fun getGatewayInfo(): GatewayInfo {
            val cm = connectivityManager ?: return GatewayInfo.EMPTY
            val activeNetwork = cm.activeNetwork ?: return GatewayInfo.EMPTY
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return GatewayInfo.EMPTY
            if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return GatewayInfo.EMPTY
            }

            val linkProperties = cm.getLinkProperties(activeNetwork) ?: return GatewayInfo.EMPTY
            val gatewayIp = extractGatewayIp(linkProperties)
            val localIp = extractLocalIp(linkProperties)
            val interfaceName = linkProperties.interfaceName.orEmpty()
            val (ssid, linkSpeed) = extractWifiInfo(caps)

            return GatewayInfo(
                gatewayIp = gatewayIp,
                localIp = localIp,
                interfaceName = interfaceName,
                isConnected = gatewayIp.isNotEmpty(),
                ssid = ssid,
                linkSpeedMbps = linkSpeed,
            )
        }

        internal open fun extractGatewayIp(linkProperties: LinkProperties): String {
            for (route in linkProperties.routes) {
                if (route.isDefaultRoute && route.gateway != null) {
                    val gw = route.gateway
                    if (gw is Inet4Address) {
                        return gw.hostAddress ?: ""
                    }
                }
            }
            @Suppress("DEPRECATION")
            val dhcp = wifiManager?.dhcpInfo
            if (dhcp != null && dhcp.gateway != 0) {
                return convertIpV4Address(dhcp.gateway)
            }
            return ""
        }

        internal open fun extractLocalIp(linkProperties: LinkProperties): String =
            linkProperties.linkAddresses
                .mapNotNull { it.address }
                .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
                ?.hostAddress ?: ""

        internal open fun extractWifiInfo(caps: NetworkCapabilities): Pair<String, Int> {
            var ssid = ""
            var linkSpeed = 0

            if (buildMinVersionQ()) {
                val transportInfo = caps.transportInfo
                if (transportInfo is WifiInfo) {
                    ssid = convertSSID(transportInfo.ssid ?: "")
                    linkSpeed = transportInfo.linkSpeed
                }
            }

            if (ssid.isEmpty() || ssid == "<unknown ssid>") {
                @Suppress("DEPRECATION")
                val wifiInfo = wifiManager?.connectionInfo
                if (wifiInfo != null) {
                    ssid = convertSSID(wifiInfo.ssid ?: "")
                    if (linkSpeed <= 0) {
                        linkSpeed = wifiInfo.linkSpeed
                    }
                }
            }
            return Pair(ssid, linkSpeed)
        }
    }
