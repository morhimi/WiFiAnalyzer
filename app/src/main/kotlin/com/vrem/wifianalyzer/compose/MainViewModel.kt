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
package com.vrem.wifianalyzer.compose

import android.content.ActivityNotFoundException
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.export.Export
import com.vrem.wifianalyzer.export.ExportFormat
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        val settings: Settings,
        val scannerService: ScannerService,
        val apAliasService: ApAliasService,
        val filtersAdapter: FiltersAdapter,
        val export: Export,
    ) : ViewModel() {
        val settingsData: StateFlow<SettingsData> = settings.settingsData
        val isScanning: StateFlow<Boolean> = scannerService.runningFlow

        fun toggleScanning() {
            scannerService.toggle()
        }

        fun updateScan() {
            scannerService.update()
        }

        fun saveAlias(
            bssid: String,
            alias: String,
        ) {
            apAliasService.saveAlias(bssid, alias)
            scannerService.update()
        }

        fun removeAlias(bssid: String) {
            apAliasService.removeAlias(bssid)
            scannerService.update()
        }

        fun export(
            context: Context,
            format: ExportFormat = ExportFormat.CSV,
        ) = shareExport(context, format)

        fun shareExport(
            context: Context,
            format: ExportFormat,
        ) {
            val wiFiDetails = scannerService.wiFiData().wiFiDetails
            if (wiFiDetails.isEmpty()) {
                Toast.makeText(context, R.string.no_data, Toast.LENGTH_LONG).show()
                return
            }
            val intent = export.export(context, wiFiDetails, format)
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, R.string.export_not_available, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }

        fun saveExportToFile(
            context: Context,
            uri: android.net.Uri,
            format: ExportFormat,
        ): Boolean {
            val wiFiDetails = scannerService.wiFiData().wiFiDetails
            if (wiFiDetails.isEmpty()) {
                Toast.makeText(context, R.string.no_data, Toast.LENGTH_LONG).show()
                return false
            }
            return try {
                val data = export.data(context, wiFiDetails, format)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(data.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, R.string.export_save_success, Toast.LENGTH_SHORT).show()
                true
            } catch (e: Exception) {
                val errorMsg = context.getString(R.string.export_save_failed) + ": " + (e.localizedMessage ?: "")
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                false
            }
        }
    }
