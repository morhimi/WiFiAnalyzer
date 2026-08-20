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
import android.net.DhcpInfo
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.RouteInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.net.Inet6Address
import java.net.InetAddress

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class GatewayProviderTest {
    private val connectivityManager: ConnectivityManager = mock()
    private val wifiManager: WifiManager = mock()
    private val network: Network = mock()
    private val networkCapabilities: NetworkCapabilities = mock()
    private val linkProperties: LinkProperties = mock()
    private val wifiInfo: WifiInfo = mock()

    private lateinit var fixture: GatewayProvider

    @Before
    fun setUp() {
        fixture = GatewayProvider(connectivityManager, wifiManager)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(connectivityManager, wifiManager)
    }

    @Test
    fun getGatewayInfoReturnsEmptyWhenConnectivityManagerIsNull() {
        val provider = GatewayProvider(null, wifiManager)
        val actual = provider.getGatewayInfo()
        assertThat(actual).isEqualTo(GatewayInfo.EMPTY)
    }

    @Test
    fun getGatewayInfoReturnsEmptyWhenActiveNetworkIsNull() {
        whenever(connectivityManager.activeNetwork).thenReturn(null)

        val actual = fixture.getGatewayInfo()
        assertThat(actual).isEqualTo(GatewayInfo.EMPTY)

        verify(connectivityManager).activeNetwork
    }

    @Test
    fun getGatewayInfoReturnsEmptyWhenCapabilitiesNull() {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(null)

        val actual = fixture.getGatewayInfo()
        assertThat(actual).isEqualTo(GatewayInfo.EMPTY)

        verify(connectivityManager).activeNetwork
        verify(connectivityManager).getNetworkCapabilities(network)
    }

    @Test
    fun getGatewayInfoReturnsEmptyWhenNotWifi() {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(false)

        val actual = fixture.getGatewayInfo()
        assertThat(actual).isEqualTo(GatewayInfo.EMPTY)

        verify(connectivityManager).activeNetwork
        verify(connectivityManager).getNetworkCapabilities(network)
    }

    @Test
    fun getGatewayInfoReturnsEmptyWhenLinkPropertiesNull() {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        whenever(connectivityManager.getLinkProperties(network)).thenReturn(null)

        val actual = fixture.getGatewayInfo()
        assertThat(actual).isEqualTo(GatewayInfo.EMPTY)

        verify(connectivityManager).activeNetwork
        verify(connectivityManager).getNetworkCapabilities(network)
        verify(connectivityManager).getLinkProperties(network)
    }

    @Test
    fun getGatewayInfoReturnsConnectedGatewayWithRoutesAndWifiInfo() {
        val gatewayAddress = InetAddress.getByName("192.168.1.1")
        val localAddress = InetAddress.getByName("192.168.1.50")
        val routeInfo: RouteInfo = mock()
        whenever(routeInfo.isDefaultRoute).thenReturn(true)
        whenever(routeInfo.gateway).thenReturn(gatewayAddress)

        val linkAddress: LinkAddress = mock()
        whenever(linkAddress.address).thenReturn(localAddress)

        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        whenever(connectivityManager.getLinkProperties(network)).thenReturn(linkProperties)
        whenever(linkProperties.routes).thenReturn(listOf(routeInfo))
        whenever(linkProperties.linkAddresses).thenReturn(listOf(linkAddress))
        whenever(linkProperties.interfaceName).thenReturn("wlan0")

        whenever(networkCapabilities.transportInfo).thenReturn(wifiInfo)
        whenever(wifiInfo.ssid).thenReturn("\"TestSSID\"")
        whenever(wifiInfo.linkSpeed).thenReturn(300)

        val actual = fixture.getGatewayInfo()

        assertThat(actual.isConnected).isTrue()
        assertThat(actual.gatewayIp).isEqualTo("192.168.1.1")
        assertThat(actual.localIp).isEqualTo("192.168.1.50")
        assertThat(actual.interfaceName).isEqualTo("wlan0")
        assertThat(actual.ssid).isEqualTo("TestSSID")
        assertThat(actual.linkSpeedMbps).isEqualTo(300)

        verify(connectivityManager).activeNetwork
        verify(connectivityManager).getNetworkCapabilities(network)
        verify(connectivityManager).getLinkProperties(network)
    }

    @Test
    fun getGatewayInfoFallsBackToDhcpInfoAndLegacyWifiInfo() {
        val localAddress = InetAddress.getByName("10.0.0.5")
        val linkAddress: LinkAddress = mock()
        whenever(linkAddress.address).thenReturn(localAddress)
        val dhcpInfo =
            DhcpInfo().apply {
                gateway = 0x0100000a // 10.0.0.1 in network byte order
            }

        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        whenever(connectivityManager.getLinkProperties(network)).thenReturn(linkProperties)
        whenever(linkProperties.routes).thenReturn(emptyList())
        whenever(linkProperties.linkAddresses).thenReturn(listOf(linkAddress))
        whenever(linkProperties.interfaceName).thenReturn("wlan0")
        whenever(networkCapabilities.transportInfo).thenReturn(null)

        @Suppress("DEPRECATION")
        whenever(wifiManager.dhcpInfo).thenReturn(dhcpInfo)
        @Suppress("DEPRECATION")
        whenever(wifiManager.connectionInfo).thenReturn(wifiInfo)
        whenever(wifiInfo.ssid).thenReturn("\"FallbackSSID\"")
        whenever(wifiInfo.linkSpeed).thenReturn(150)

        val actual = fixture.getGatewayInfo()

        assertThat(actual.isConnected).isTrue()
        assertThat(actual.gatewayIp).isEqualTo("10.0.0.1")
        assertThat(actual.localIp).isEqualTo("10.0.0.5")
        assertThat(actual.ssid).isEqualTo("FallbackSSID")
        assertThat(actual.linkSpeedMbps).isEqualTo(150)

        verify(connectivityManager).activeNetwork
        verify(connectivityManager).getNetworkCapabilities(network)
        verify(connectivityManager).getLinkProperties(network)
        @Suppress("DEPRECATION")
        verify(wifiManager).dhcpInfo
        @Suppress("DEPRECATION")
        verify(wifiManager).connectionInfo
    }

    @Test
    fun extractGatewayIpHandlesNonDefaultRoutesAndIPv6() {
        val ipv6Address = mock<Inet6Address>()
        val nonDefaultRoute: RouteInfo = mock()
        whenever(nonDefaultRoute.isDefaultRoute).thenReturn(false)
        val nullGatewayRoute: RouteInfo = mock()
        whenever(nullGatewayRoute.isDefaultRoute).thenReturn(true)
        whenever(nullGatewayRoute.gateway).thenReturn(null)
        val ipv6Route: RouteInfo = mock()
        whenever(ipv6Route.isDefaultRoute).thenReturn(true)
        whenever(ipv6Route.gateway).thenReturn(ipv6Address)

        whenever(linkProperties.routes).thenReturn(listOf(nonDefaultRoute, nullGatewayRoute, ipv6Route))
        @Suppress("DEPRECATION")
        whenever(wifiManager.dhcpInfo).thenReturn(null)

        val actual = fixture.extractGatewayIp(linkProperties)
        assertThat(actual).isEmpty()

        @Suppress("DEPRECATION")
        verify(wifiManager).dhcpInfo
    }

    @Test
    fun extractGatewayIpHandlesZeroDhcpGateway() {
        val dhcpInfo =
            DhcpInfo().apply {
                gateway = 0
            }
        whenever(linkProperties.routes).thenReturn(emptyList())
        @Suppress("DEPRECATION")
        whenever(wifiManager.dhcpInfo).thenReturn(dhcpInfo)

        val actual = fixture.extractGatewayIp(linkProperties)
        assertThat(actual).isEmpty()

        @Suppress("DEPRECATION")
        verify(wifiManager).dhcpInfo
    }

    @Test
    fun extractLocalIpIgnoresLoopbackAndIPv6Addresses() {
        val loopback = InetAddress.getByName("127.0.0.1")
        val ipv6Address = mock<Inet6Address>()
        val loopbackAddress: LinkAddress = mock()
        whenever(loopbackAddress.address).thenReturn(loopback)
        val ipv6LinkAddress: LinkAddress = mock()
        whenever(ipv6LinkAddress.address).thenReturn(ipv6Address)

        whenever(linkProperties.linkAddresses).thenReturn(listOf(loopbackAddress, ipv6LinkAddress))

        val actual = fixture.extractLocalIp(linkProperties)
        assertThat(actual).isEmpty()
    }

    @Test
    fun extractWifiInfoWithNullWifiManager() {
        val provider = GatewayProvider(connectivityManager, null)
        whenever(networkCapabilities.transportInfo).thenReturn(null)

        val (ssid, speed) = provider.extractWifiInfo(networkCapabilities)
        assertThat(ssid).isEmpty()
        assertThat(speed).isEqualTo(0)
    }

    @Test
    fun extractWifiInfoWithUnknownSsidAndNullConnectionInfo() {
        whenever(networkCapabilities.transportInfo).thenReturn(wifiInfo)
        whenever(wifiInfo.ssid).thenReturn("<unknown ssid>")
        whenever(wifiInfo.linkSpeed).thenReturn(100)

        @Suppress("DEPRECATION")
        whenever(wifiManager.connectionInfo).thenReturn(null)

        val (ssid, speed) = fixture.extractWifiInfo(networkCapabilities)
        assertThat(ssid).isEqualTo("<unknown ssid>")
        assertThat(speed).isEqualTo(100)

        @Suppress("DEPRECATION")
        verify(wifiManager).connectionInfo
    }
}
