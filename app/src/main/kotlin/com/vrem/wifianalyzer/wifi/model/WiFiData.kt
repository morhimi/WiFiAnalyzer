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
package com.vrem.wifianalyzer.wifi.model

import com.vrem.wifianalyzer.wifi.predicate.Predicate

class WiFiData(
    val wiFiDetails: List<WiFiDetail>,
    val wiFiConnection: WiFiConnection,
) {
    fun connection(): WiFiDetail =
        wiFiDetails.firstOrNull { connected(it) }?.let { copy(it) }
            ?: if (wiFiConnection.connected) {
                WiFiDetail(
                    wiFiIdentifier = wiFiConnection.wiFiIdentifier,
                    wiFiAdditional = WiFiAdditional(wiFiConnection = wiFiConnection),
                )
            } else {
                WiFiDetail.EMPTY
            }

    fun wiFiDetails(
        predicate: Predicate,
        sortBy: SortBy,
    ): List<WiFiDetail> = wiFiDetails(predicate, sortBy, GroupBy.NONE)

    fun wiFiDetails(
        predicate: Predicate,
        sortBy: SortBy,
        groupBy: GroupBy,
    ): List<WiFiDetail> {
        val connection: WiFiDetail = connection()
        val filtered =
            wiFiDetails
                .filter { predicate(it) }
                .map { transform(it, connection) }

        return if (groupBy.none) {
            filtered.sortedWith(sortBy.sort)
        } else {
            filtered
                .groupBy { groupBy.group(it) }
                .values
                .map(map(sortBy, groupBy))
                .sortedWith(sortBy.sort)
        }
    }

    private fun map(
        sortBy: SortBy,
        groupBy: GroupBy,
    ): (List<WiFiDetail>) -> WiFiDetail =
        {
            val sortedWith: List<WiFiDetail> = it.sortedWith(groupBy.sort)
            when (sortedWith.size) {
                1 -> sortedWith.first()
                else ->
                    WiFiDetail(
                        sortedWith.first(),
                        sortedWith.subList(1, sortedWith.size).sortedWith(sortBy.sort),
                    )
            }
        }

    private fun transform(
        wiFiDetail: WiFiDetail,
        connection: WiFiDetail,
    ): WiFiDetail =
        when (wiFiDetail) {
            connection -> connection
            else -> wiFiDetail
        }

    private fun connected(it: WiFiDetail): Boolean {
        val connBssid = wiFiConnection.wiFiIdentifier.bssid
        val detailBssid = it.wiFiIdentifier.bssid
        val validBssid = connBssid.isNotEmpty() && connBssid != "02:00:00:00:00:00" && connBssid != "00:00:00:00:00:00"
        return if (validBssid && detailBssid.isNotEmpty()) {
            connBssid.equals(detailBssid, ignoreCase = true)
        } else {
            wiFiConnection.wiFiIdentifier.equals(it.wiFiIdentifier, ignoreCase = true)
        }
    }

    private fun copy(wiFiDetail: WiFiDetail): WiFiDetail {
        val wiFiAdditional = WiFiAdditional(wiFiDetail.wiFiAdditional.vendorName, wiFiConnection)
        return WiFiDetail(wiFiDetail, wiFiAdditional)
    }

    companion object {
        val EMPTY = WiFiData(listOf(), WiFiConnection.EMPTY)
    }
}
