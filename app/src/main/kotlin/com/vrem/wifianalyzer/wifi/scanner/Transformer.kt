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

import android.net.wifi.WifiInfo
import com.vrem.util.nullToEmpty
import com.vrem.util.ssid
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.model.FastRoaming
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional
import com.vrem.wifianalyzer.wifi.model.WiFiConnection
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import com.vrem.wifianalyzer.wifi.model.WiFiSecurity
import com.vrem.wifianalyzer.wifi.model.WiFiSecurityType
import com.vrem.wifianalyzer.wifi.model.WiFiSignal
import com.vrem.wifianalyzer.wifi.model.WiFiSignalExtra
import com.vrem.wifianalyzer.wifi.model.WiFiStandard
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import com.vrem.wifianalyzer.wifi.model.convertIpV4Address
import com.vrem.wifianalyzer.wifi.model.convertSSID

fun WifiInfo.ipV4Address(): Int = ipAddress

internal class Transformer(
    private val cache: Cache,
    private val apAliasService: ApAliasService,
    private val vendorService: VendorService,
) {
    internal fun transformWifiInfo(): WiFiConnection {
        val wifiInfo: WifiInfo? = cache.wifiInfo
        return if (wifiInfo == null || wifiInfo.networkId == -1) {
            WiFiConnection.EMPTY
        } else {
            val ssid = convertSSID(String.nullToEmpty(wifiInfo.ssid))
            val bssid = String.nullToEmpty(wifiInfo.bssid)
            val alias = apAliasService.getAlias(bssid)
            val wiFiIdentifier = WiFiIdentifier(ssid, bssid, alias)
            WiFiConnection(wiFiIdentifier, convertIpV4Address(wifiInfo.ipV4Address()), wifiInfo.linkSpeed)
        }
    }

    internal fun transformCacheResults(): List<WiFiDetail> = cache.scanResults().map { transform(it) }

    internal fun transformToWiFiData(): WiFiData = WiFiData(transformCacheResults(), transformWifiInfo())

    private fun transform(cacheResult: CacheResult): WiFiDetail {
        val scanResult = cacheResult.scanResult
        val wiFiWidth = WiFiWidth.findOne(scanResult.channelWidth)
        val bssid = String.nullToEmpty(scanResult.BSSID)
        val alias = apAliasService.getAlias(bssid)
        val vendorName = vendorService.findVendorName(bssid)
        return WiFiDetail(
            wiFiIdentifier = WiFiIdentifier(scanResult.ssid(), bssid, alias),
            wiFiSecurity = WiFiSecurity(String.nullToEmpty(scanResult.capabilities), WiFiSecurityType.find(scanResult)),
            wiFiSignal =
                WiFiSignal(
                    scanResult.frequency,
                    wiFiWidth.calculateCenter(scanResult.frequency, scanResult.centerFreq0, scanResult.centerFreq1),
                    wiFiWidth,
                    cacheResult.average,
                    WiFiSignalExtra(
                        scanResult.is80211mcResponder,
                        WiFiStandard.findOne(scanResult),
                        FastRoaming.find(scanResult),
                    ),
                ),
            wiFiAdditional = WiFiAdditional(vendorName, WiFiConnection.EMPTY),
        )
    }
}
