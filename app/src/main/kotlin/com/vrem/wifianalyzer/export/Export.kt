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

package com.vrem.wifianalyzer.export

import android.content.Context
import android.content.Intent
import com.vrem.util.EMPTY
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiSignal.Companion.FREQUENCY_UNITS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class Export
    @Inject
    constructor(
        private val exportIntent: ExportIntent,
    ) {
        constructor() : this(ExportIntent())

        private val textHeader =
            "Time Stamp|" +
                "SSID|" +
                "BSSID|" +
                "Alias|" +
                "Strength|" +
                "Primary Channel|" +
                "Primary Frequency|" +
                "Center Channel|" +
                "Center Frequency|" +
                "Width (Range)|" +
                "Distance|" +
                "802.11mc|" +
                "Security|" +
                "Standard|" +
                "FastRoaming" +
                "\n"

        private val csvHeader =
            "Timestamp," +
                "SSID," +
                "BSSID," +
                "Alias," +
                "Level (dBm)," +
                "Primary Channel," +
                "Primary Frequency (MHz)," +
                "Center Channel," +
                "Center Frequency (MHz)," +
                "Channel Width (MHz)," +
                "Frequency Range (MHz)," +
                "Distance," +
                "802.11mc," +
                "Security," +
                "Standard," +
                "FastRoaming," +
                "Vendor" +
                "\n"

        fun export(
            context: Context,
            wiFiDetails: List<WiFiDetail>,
            format: ExportFormat = ExportFormat.CSV,
            date: Date = Date(),
        ): Intent {
            val timestamp: String = timestamp(date)
            val title: String = title(context, timestamp)
            val data: String = data(context, wiFiDetails, format, timestamp)
            return exportIntent.intent(title, data, format.mimeType)
        }

        fun data(
            context: Context,
            wiFiDetails: List<WiFiDetail>,
            format: ExportFormat = ExportFormat.CSV,
            date: Date = Date(),
        ): String = data(context, wiFiDetails, format, timestamp(date))

        internal fun data(
            context: Context,
            wiFiDetails: List<WiFiDetail>,
            format: ExportFormat,
            timestamp: String,
        ): String =
            when (format) {
                ExportFormat.CSV -> toCsv(context, wiFiDetails, timestamp)
                ExportFormat.JSON -> toJson(context, wiFiDetails, timestamp)
                ExportFormat.TEXT -> toText(context, wiFiDetails, timestamp)
            }

        fun filename(
            context: Context,
            format: ExportFormat,
            date: Date = Date(),
        ): String {
            val formattedDate = SimpleDateFormat(FILE_DATE_FORMAT, Locale.US).format(date)
            val appName = context.getString(R.string.app_name)
            return "$appName-$formattedDate.${format.extension}"
        }

        internal fun title(
            context: Context,
            timestamp: String,
        ): String {
            val title: String = context.getString(R.string.action_access_points)
            return "$title-$timestamp"
        }

        internal fun timestamp(date: Date): String = SimpleDateFormat(TIME_STAMP_FORMAT, Locale.US).format(date)

        private fun toText(
            context: Context,
            wiFiDetails: List<WiFiDetail>,
            timestamp: String,
        ): String =
            textHeader + wiFiDetails.joinToString(separator = String.EMPTY, transform = toTextRow(context, timestamp))

        private fun toTextRow(
            context: Context,
            timestamp: String,
        ): (WiFiDetail) -> String =
            {
                with(it) {
                    "$timestamp|" +
                        "${wiFiIdentifier.ssid}|" +
                        "${wiFiIdentifier.bssid}|" +
                        "${wiFiIdentifier.alias}|" +
                        "${wiFiSignal.level}dBm|" +
                        "${wiFiSignal.primaryWiFiChannel.channel}|" +
                        "${wiFiSignal.primaryFrequency}$FREQUENCY_UNITS|" +
                        "${wiFiSignal.centerWiFiChannel.channel}|" +
                        "${wiFiSignal.centerFrequency}$FREQUENCY_UNITS|" +
                        "${wiFiSignal.wiFiWidth.frequencyWidth}$FREQUENCY_UNITS " +
                        "(${wiFiSignal.wiFiChannelStart.frequency} - ${wiFiSignal.wiFiChannelEnd.frequency})|" +
                        "${wiFiSignal.distance}|" +
                        "${wiFiSignal.extra.is80211mc}|" +
                        wiFiSecurity.capabilities + "|" +
                        wiFiSignal.extra.wiFiStandardDisplay(context) + "|" +
                        wiFiSignal.extra.fastRoamingDisplay(context) +
                        "\n"
                }
            }

        private fun toCsv(
            context: Context,
            wiFiDetails: List<WiFiDetail>,
            timestamp: String,
        ): String =
            csvHeader + wiFiDetails.joinToString(separator = String.EMPTY, transform = toCsvRow(context, timestamp))

        private fun toCsvRow(
            context: Context,
            timestamp: String,
        ): (WiFiDetail) -> String =
            {
                with(it) {
                    val frequencyRange =
                        "${wiFiSignal.wiFiChannelStart.frequency} - ${wiFiSignal.wiFiChannelEnd.frequency}"
                    listOf(
                        timestamp,
                        wiFiIdentifier.ssid,
                        wiFiIdentifier.bssid,
                        wiFiIdentifier.alias,
                        "${wiFiSignal.level}",
                        "${wiFiSignal.primaryWiFiChannel.channel}",
                        "${wiFiSignal.primaryFrequency}",
                        "${wiFiSignal.centerWiFiChannel.channel}",
                        "${wiFiSignal.centerFrequency}",
                        "${wiFiSignal.wiFiWidth.frequencyWidth}",
                        frequencyRange,
                        wiFiSignal.distance,
                        "${wiFiSignal.extra.is80211mc}",
                        wiFiSecurity.capabilities,
                        wiFiSignal.extra.wiFiStandardDisplay(context),
                        wiFiSignal.extra.fastRoamingDisplay(context),
                        wiFiAdditional.vendorName,
                    ).joinToString(separator = ",", postfix = "\n") { field -> escapeCsv(field) }
                }
            }

        private fun escapeCsv(value: String): String =
            if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
                "\"${value.replace("\"", "\"\"")}\""
            } else {
                value
            }

        private fun toJson(
            context: Context,
            wiFiDetails: List<WiFiDetail>,
            timestamp: String,
        ): String {
            val items =
                wiFiDetails.map {
                    with(it) {
                        val frequencyRange =
                            "${wiFiSignal.wiFiChannelStart.frequency} - ${wiFiSignal.wiFiChannelEnd.frequency}"
                        "  {\n" +
                            "    \"timestamp\": \"${escapeJson(timestamp)}\",\n" +
                            "    \"ssid\": \"${escapeJson(wiFiIdentifier.ssid)}\",\n" +
                            "    \"bssid\": \"${escapeJson(wiFiIdentifier.bssid)}\",\n" +
                            "    \"alias\": \"${escapeJson(wiFiIdentifier.alias)}\",\n" +
                            "    \"level\": ${wiFiSignal.level},\n" +
                            "    \"primaryChannel\": ${wiFiSignal.primaryWiFiChannel.channel},\n" +
                            "    \"primaryFrequency\": ${wiFiSignal.primaryFrequency},\n" +
                            "    \"centerChannel\": ${wiFiSignal.centerWiFiChannel.channel},\n" +
                            "    \"centerFrequency\": ${wiFiSignal.centerFrequency},\n" +
                            "    \"channelWidth\": ${wiFiSignal.wiFiWidth.frequencyWidth},\n" +
                            "    \"frequencyRange\": \"${escapeJson(frequencyRange)}\",\n" +
                            "    \"distance\": \"${escapeJson(wiFiSignal.distance)}\",\n" +
                            "    \"is80211mc\": ${wiFiSignal.extra.is80211mc},\n" +
                            "    \"security\": \"${escapeJson(wiFiSecurity.capabilities)}\",\n" +
                            "    \"standard\": \"${escapeJson(wiFiSignal.extra.wiFiStandardDisplay(context))}\",\n" +
                            "    \"fastRoaming\": \"${escapeJson(wiFiSignal.extra.fastRoamingDisplay(context))}\",\n" +
                            "    \"vendor\": \"${escapeJson(wiFiAdditional.vendorName)}\"\n" +
                            "  }"
                    }
                }
            return "[\n" + items.joinToString(",\n") + "\n]\n"
        }

        private fun escapeJson(value: String): String =
            value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")

        companion object {
            private const val TIME_STAMP_FORMAT = "yyyy/MM/dd-HH:mm:ss"
            private const val FILE_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss"
        }
    }
