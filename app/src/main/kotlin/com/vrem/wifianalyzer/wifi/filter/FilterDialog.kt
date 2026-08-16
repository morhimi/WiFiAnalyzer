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
package com.vrem.wifianalyzer.wifi.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vrem.util.specialTrim
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.model.Security
import com.vrem.wifianalyzer.wifi.model.Strength

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    filtersAdapter: FiltersAdapter,
    settings: Settings,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var ssidText by remember {
        mutableStateOf(
            filtersAdapter
                .ssidAdapter()
                .selections
                .joinToString(" "),
        )
    }
    var selectedBands by remember { mutableStateOf(filtersAdapter.wiFiBandAdapter().selections) }
    var selectedStrengths by remember { mutableStateOf(filtersAdapter.strengthAdapter().selections) }
    var selectedSecurities by remember { mutableStateOf(filtersAdapter.securityAdapter().selections) }

    fun <T> toggleSelection(
        current: Set<T>,
        item: T,
    ): Set<T> =
        if (current.contains(item)) {
            if (current.size > 1) current - item else current
        } else {
            current + item
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(R.string.filter_title),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // SSID Filter Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.filter_ssid_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlinedTextField(
                        value = ssidText,
                        onValueChange = { ssidText = it },
                        placeholder = { Text(stringResource(R.string.filter_ssid_hint)) },
                        singleLine = true,
                        trailingIcon = {
                            if (ssidText.isNotBlank()) {
                                IconButton(onClick = { ssidText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Wi-Fi Band Section (visible only when in Access Points)
                if (NavigationMenu.ACCESS_POINTS == settings.selectedMenu()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.filter_wifi_band_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            WiFiBand.entries.forEach { band ->
                                val selected = selectedBands.contains(band)
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedBands = toggleSelection(selectedBands, band) },
                                    label = { Text(stringResource(band.textResource)) },
                                    colors = FilterChipDefaults.filterChipColors(),
                                )
                            }
                        }
                    }
                }

                // Signal Strength Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.filter_strength_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf(Strength.FOUR, Strength.THREE, Strength.TWO, Strength.ONE).forEach { strength ->
                            val selected = selectedStrengths.contains(strength)
                            FilterChip(
                                selected = selected,
                                onClick = { selectedStrengths = toggleSelection(selectedStrengths, strength) },
                                label = {
                                    Icon(
                                        painter = painterResource(strength.imageResource),
                                        contentDescription = "Signal Strength",
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                    }
                }

                // Security Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.filter_security_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Security.entries.forEach { security ->
                            val selected = selectedSecurities.contains(security)
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSecurities = toggleSelection(selectedSecurities, security) },
                                label = { Text(security.name) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(security.imageResource),
                                        contentDescription = security.name,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedSsids =
                        ssidText
                            .specialTrim()
                            .split(" ")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .toSet()
                    filtersAdapter.ssidAdapter().selections = parsedSsids
                    filtersAdapter.wiFiBandAdapter().selections = selectedBands
                    filtersAdapter.strengthAdapter().selections = selectedStrengths
                    filtersAdapter.securityAdapter().selections = selectedSecurities
                    filtersAdapter.save()
                    onApply()
                },
            ) {
                Text(stringResource(R.string.filter_apply))
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        ssidText = ""
                        selectedBands = WiFiBand.entries.toSet()
                        selectedStrengths = Strength.entries.toSet()
                        selectedSecurities = Security.entries.toSet()
                        filtersAdapter.reset()
                        onReset()
                    },
                ) {
                    Text(stringResource(R.string.filter_reset))
                }
                TextButton(
                    onClick = {
                        filtersAdapter.reload()
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.filter_close))
                }
            }
        },
    )
}
