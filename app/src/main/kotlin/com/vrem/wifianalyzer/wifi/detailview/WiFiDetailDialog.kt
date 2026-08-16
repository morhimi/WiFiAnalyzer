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
package com.vrem.wifianalyzer.wifi.detailview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@Composable
fun WiFiDetailDialog(
    wiFiDetails: List<WiFiDetail>,
    onDismiss: () -> Unit,
    onEditAlias: (WiFiDetail) -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentDetail =
        wiFiDetails.getOrNull(currentIndex) ?: run {
            onDismiss()
            return
        }

    val isSequence = wiFiDetails.size > 1
    val isLast = currentIndex >= wiFiDetails.size - 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = currentDetail.wiFiIdentifier.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                WiFiDetailContent(wiFiDetail = currentDetail)
            }
        },
        confirmButton = {
            if (isSequence) {
                Button(
                    onClick = {
                        if (isLast) {
                            onDismiss()
                        } else {
                            currentIndex++
                        }
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            } else {
                Button(onClick = onDismiss) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        },
        dismissButton = {
            if (isSequence) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.filter_close))
                }
            } else if (currentDetail.wiFiIdentifier.bssid.isNotBlank()) {
                TextButton(onClick = { onEditAlias(currentDetail) }) {
                    Text(stringResource(R.string.ap_alias_edit))
                }
            }
        },
    )
}
