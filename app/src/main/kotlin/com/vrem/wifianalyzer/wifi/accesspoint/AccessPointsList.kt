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
package com.vrem.wifianalyzer.wifi.accesspoint

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@Composable
fun AccessPointsList(
    wiFiDetails: List<WiFiDetail>,
    viewType: AccessPointViewType,
    onDetailClick: (WiFiDetail) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(
            items = wiFiDetails,
            key = { it.wiFiIdentifier.bssid + "-" + it.wiFiIdentifier.ssid + "-" + it.children.size },
        ) { detail ->
            val groupKey =
                if (detail.children.isNotEmpty()) {
                    detail.wiFiIdentifier.ssid + "-" + detail.wiFiSignal.primaryFrequency
                } else {
                    detail.wiFiIdentifier.bssid
                }
            val isExpanded = expandedStates[groupKey] ?: false
            AccessPointItem(
                wiFiDetail = detail,
                viewType = viewType,
                expanded = isExpanded,
                modifier =
                    Modifier.clickable {
                        if (detail.children.isNotEmpty()) {
                            expandedStates[groupKey] = !isExpanded
                        } else {
                            onDetailClick(detail)
                        }
                    },
            )

            if (isExpanded) {
                detail.children.forEach { child ->
                    AccessPointItem(
                        wiFiDetail = child,
                        viewType = viewType,
                        isChild = true,
                        modifier = Modifier.clickable { onDetailClick(child) },
                    )
                }
            }
        }
    }
}
