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
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.model.FastRoaming
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import com.vrem.wifianalyzer.wifi.model.WiFiSecurity
import com.vrem.wifianalyzer.wifi.model.WiFiSignal
import com.vrem.wifianalyzer.wifi.model.WiFiSignalExtra
import com.vrem.wifianalyzer.wifi.model.WiFiStandard
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ExportTest {
    private val name = "name"
    private val appName = "WiFiAnalyzer"
    private val date = Date()

    private val context: Context = mock()
    private val exportIntent: ExportIntent = mock()
    private val intent: Intent = mock()
    private val fixture = Export(exportIntent)

    @After
    fun tearDown() {
        verifyNoMoreInteractions(exportIntent)
        verifyNoMoreInteractions(intent)
        verifyNoMoreInteractions(context)
    }

    @Test
    fun exportText() {
        // setup
        val wiFiDetails = withWiFiDetails()
        val count = wiFiDetails.size
        val timestamp = timestamp(date)
        val title = title(timestamp)
        val data = textData(timestamp)
        doReturn(name).whenever(context).getString(R.string.action_access_points)
        doReturn("802.11AC").whenever(context).getString(WiFiStandard.AC.fullResource)
        doReturn("802.11R").whenever(context).getString(FastRoaming.FR_802_11R.textResource)
        whenever(exportIntent.intent(title, data, ExportFormat.TEXT.mimeType)).thenReturn(intent)
        // execute
        val actual = fixture.export(context, wiFiDetails, ExportFormat.TEXT, date)
        // validate
        assertThat(actual).isEqualTo(intent)
        verify(context).getString(R.string.action_access_points)
        verify(context, times(count)).getString(WiFiStandard.AC.fullResource)
        verify(context, times(count)).getString(FastRoaming.FR_802_11R.textResource)
        verify(exportIntent).intent(title, data, ExportFormat.TEXT.mimeType)
    }

    @Test
    fun exportCsv() {
        // setup
        val wiFiDetails = withWiFiDetails()
        val count = wiFiDetails.size
        val timestamp = timestamp(date)
        val title = title(timestamp)
        val data = csvData(timestamp)
        doReturn(name).whenever(context).getString(R.string.action_access_points)
        doReturn("802.11AC").whenever(context).getString(WiFiStandard.AC.fullResource)
        doReturn("802.11R").whenever(context).getString(FastRoaming.FR_802_11R.textResource)
        whenever(exportIntent.intent(title, data, ExportFormat.CSV.mimeType)).thenReturn(intent)
        // execute
        val actual = fixture.export(context, wiFiDetails, ExportFormat.CSV, date)
        // validate
        assertThat(actual).isEqualTo(intent)
        verify(context).getString(R.string.action_access_points)
        verify(context, times(count)).getString(WiFiStandard.AC.fullResource)
        verify(context, times(count)).getString(FastRoaming.FR_802_11R.textResource)
        verify(exportIntent).intent(title, data, ExportFormat.CSV.mimeType)
    }

    @Test
    fun exportJson() {
        // setup
        val wiFiDetails = withWiFiDetails()
        val count = wiFiDetails.size
        val timestamp = timestamp(date)
        val title = title(timestamp)
        val data = jsonData(timestamp)
        doReturn(name).whenever(context).getString(R.string.action_access_points)
        doReturn("802.11AC").whenever(context).getString(WiFiStandard.AC.fullResource)
        doReturn("802.11R").whenever(context).getString(FastRoaming.FR_802_11R.textResource)
        whenever(exportIntent.intent(title, data, ExportFormat.JSON.mimeType)).thenReturn(intent)
        // execute
        val actual = fixture.export(context, wiFiDetails, ExportFormat.JSON, date)
        // validate
        assertThat(actual).isEqualTo(intent)
        verify(context).getString(R.string.action_access_points)
        verify(context, times(count)).getString(WiFiStandard.AC.fullResource)
        verify(context, times(count)).getString(FastRoaming.FR_802_11R.textResource)
        verify(exportIntent).intent(title, data, ExportFormat.JSON.mimeType)
    }

    @Test
    fun exportWithDefaultFormatAndDate() {
        val wiFiDetails = withWiFiDetails()
        val count = wiFiDetails.size
        doReturn(name).whenever(context).getString(R.string.action_access_points)
        doReturn("802.11AC").whenever(context).getString(WiFiStandard.AC.fullResource)
        doReturn("802.11R").whenever(context).getString(FastRoaming.FR_802_11R.textResource)
        whenever(exportIntent.intent(any(), any(), eq(ExportFormat.CSV.mimeType))).thenReturn(intent)

        val actual = fixture.export(context, wiFiDetails)

        assertThat(actual).isEqualTo(intent)
        verify(context).getString(R.string.action_access_points)
        verify(context, times(count)).getString(WiFiStandard.AC.fullResource)
        verify(context, times(count)).getString(FastRoaming.FR_802_11R.textResource)
        verify(exportIntent).intent(any(), any(), eq(ExportFormat.CSV.mimeType))
    }

    @Test
    fun timestamp() {
        // setup
        val expected = timestamp(date)
        // execute
        val actual = fixture.timestamp(date)
        // validate
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun dataText() {
        // setup
        val wiFiDetails = withWiFiDetails()
        val count = wiFiDetails.size
        val timestamp = timestamp(date)
        val expected = textData(timestamp)
        doReturn("802.11AC").whenever(context).getString(WiFiStandard.AC.fullResource)
        doReturn("802.11R").whenever(context).getString(FastRoaming.FR_802_11R.textResource)
        // execute
        val actual = fixture.data(context, wiFiDetails, ExportFormat.TEXT, timestamp)
        // validate
        assertThat(actual).isEqualTo(expected)
        verify(context, times(count)).getString(WiFiStandard.AC.fullResource)
        verify(context, times(count)).getString(FastRoaming.FR_802_11R.textResource)
    }

    @Test
    fun dataCsv() {
        // setup
        val wiFiDetails = withWiFiDetails()
        val count = wiFiDetails.size
        val timestamp = timestamp(date)
        val expected = csvData(timestamp)
        doReturn("802.11AC").whenever(context).getString(WiFiStandard.AC.fullResource)
        doReturn("802.11R").whenever(context).getString(FastRoaming.FR_802_11R.textResource)
        // execute
        val actual = fixture.data(context, wiFiDetails, ExportFormat.CSV, timestamp)
        // validate
        assertThat(actual).isEqualTo(expected)
        verify(context, times(count)).getString(WiFiStandard.AC.fullResource)
        verify(context, times(count)).getString(FastRoaming.FR_802_11R.textResource)
    }

    @Test
    fun dataCsvWithSpecialCharactersEscaped() {
        // setup
        val specialDetail =
            WiFiDetail(
                wiFiIdentifier = WiFiIdentifier("SSID,with,comma and \"quotes\"", "BSSID0", "Alias\nnewline"),
                wiFiSecurity = WiFiSecurity("[WPA2-PSK]"),
                wiFiSignal =
                    WiFiSignal(
                        2412,
                        2412,
                        WiFiWidth.MHZ_20,
                        -50,
                        WiFiSignalExtra(true, WiFiStandard.AC, listOf(FastRoaming.FR_802_11R)),
                    ),
                wiFiAdditional = WiFiAdditional("Vendor, Inc."),
            )
        val timestamp = timestamp(date)
        doReturn("802.11AC").whenever(context).getString(WiFiStandard.AC.fullResource)
        doReturn("802.11R").whenever(context).getString(FastRoaming.FR_802_11R.textResource)
        // execute
        val actual = fixture.data(context, listOf(specialDetail), ExportFormat.CSV, timestamp)
        // validate
        assertThat(actual).contains("\"SSID,with,comma and \"\"quotes\"\"\"")
        assertThat(actual).contains("\"Alias\nnewline\"")
        assertThat(actual).contains("\"Vendor, Inc.\"")
        verify(context).getString(WiFiStandard.AC.fullResource)
        verify(context).getString(FastRoaming.FR_802_11R.textResource)
    }

    @Test
    fun dataJson() {
        // setup
        val wiFiDetails = withWiFiDetails()
        val count = wiFiDetails.size
        val timestamp = timestamp(date)
        val expected = jsonData(timestamp)
        doReturn("802.11AC").whenever(context).getString(WiFiStandard.AC.fullResource)
        doReturn("802.11R").whenever(context).getString(FastRoaming.FR_802_11R.textResource)
        // execute
        val actual = fixture.data(context, wiFiDetails, ExportFormat.JSON, timestamp)
        // validate
        assertThat(actual).isEqualTo(expected)
        verify(context, times(count)).getString(WiFiStandard.AC.fullResource)
        verify(context, times(count)).getString(FastRoaming.FR_802_11R.textResource)
    }

    @Test
    fun filename() {
        // setup
        doReturn(appName).whenever(context).getString(R.string.app_name)
        val formattedDate = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(date)
        // execute
        val actualCsv = fixture.filename(context, ExportFormat.CSV, date)
        val actualJson = fixture.filename(context, ExportFormat.JSON, date)
        val actualText = fixture.filename(context, ExportFormat.TEXT, date)
        // validate
        assertThat(actualCsv).isEqualTo("WiFiAnalyzer-$formattedDate.csv")
        assertThat(actualJson).isEqualTo("WiFiAnalyzer-$formattedDate.json")
        assertThat(actualText).isEqualTo("WiFiAnalyzer-$formattedDate.txt")
        verify(context, times(3)).getString(R.string.app_name)
    }

    @Test
    fun title() {
        // setup
        val timestamp = timestamp(date)
        val expected = "$name-$timestamp"
        doReturn(name).whenever(context).getString(R.string.action_access_points)
        // execute
        val actual = fixture.title(context, timestamp)
        // validate
        assertThat(actual).isEqualTo(expected)
        verify(context).getString(R.string.action_access_points)
    }

    private fun title(timestamp: String): String = "$name-$timestamp"

    private fun textData(timestamp: String): String =
        "Time Stamp|SSID|BSSID|Alias|Strength|Primary Channel|Primary Frequency|Center Channel|Center Frequency|" +
            "Width (Range)|Distance|802.11mc|Security|Standard|FastRoaming\n" +
            timestamp +
            "|SSID10|BSSID10||-10dBm|3|2422MHz|5|2432MHz|40MHz (2412 - 2452)|~0.0m|true|capabilities10|802.11AC|" +
            "802.11R\n" +
            timestamp +
            "|SSID20|BSSID20||-20dBm|5|2432MHz|7|2442MHz|40MHz (2422 - 2462)|~0.1m|true|capabilities20|802.11AC|" +
            "802.11R\n" +
            timestamp +
            "|SSID30|BSSID30||-30dBm|7|2442MHz|9|2452MHz|40MHz (2432 - 2472)|~0.3m|true|capabilities30|802.11AC|" +
            "802.11R\n"

    private fun csvData(timestamp: String): String =
        "Timestamp,SSID,BSSID,Alias,Level (dBm),Primary Channel,Primary Frequency (MHz)," +
            "Center Channel,Center Frequency (MHz),Channel Width (MHz),Frequency Range (MHz)," +
            "Distance,802.11mc,Security,Standard,FastRoaming,Vendor\n" +
            "$timestamp,SSID10,BSSID10,,-10,3,2422,5,2432,40,2412 - 2452,~0.0m,true," +
            "capabilities10,802.11AC,802.11R,Vendor10\n" +
            "$timestamp,SSID20,BSSID20,,-20,5,2432,7,2442,40,2422 - 2462,~0.1m,true," +
            "capabilities20,802.11AC,802.11R,Vendor20\n" +
            "$timestamp,SSID30,BSSID30,,-30,7,2442,9,2452,40,2432 - 2472,~0.3m,true," +
            "capabilities30,802.11AC,802.11R,Vendor30\n"

    private fun jsonData(timestamp: String): String =
        "[\n" +
            "  {\n" +
            "    \"timestamp\": \"$timestamp\",\n" +
            "    \"ssid\": \"SSID10\",\n" +
            "    \"bssid\": \"BSSID10\",\n" +
            "    \"alias\": \"\",\n" +
            "    \"level\": -10,\n" +
            "    \"primaryChannel\": 3,\n" +
            "    \"primaryFrequency\": 2422,\n" +
            "    \"centerChannel\": 5,\n" +
            "    \"centerFrequency\": 2432,\n" +
            "    \"channelWidth\": 40,\n" +
            "    \"frequencyRange\": \"2412 - 2452\",\n" +
            "    \"distance\": \"~0.0m\",\n" +
            "    \"is80211mc\": true,\n" +
            "    \"security\": \"capabilities10\",\n" +
            "    \"standard\": \"802.11AC\",\n" +
            "    \"fastRoaming\": \"802.11R\",\n" +
            "    \"vendor\": \"Vendor10\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"timestamp\": \"$timestamp\",\n" +
            "    \"ssid\": \"SSID20\",\n" +
            "    \"bssid\": \"BSSID20\",\n" +
            "    \"alias\": \"\",\n" +
            "    \"level\": -20,\n" +
            "    \"primaryChannel\": 5,\n" +
            "    \"primaryFrequency\": 2432,\n" +
            "    \"centerChannel\": 7,\n" +
            "    \"centerFrequency\": 2442,\n" +
            "    \"channelWidth\": 40,\n" +
            "    \"frequencyRange\": \"2422 - 2462\",\n" +
            "    \"distance\": \"~0.1m\",\n" +
            "    \"is80211mc\": true,\n" +
            "    \"security\": \"capabilities20\",\n" +
            "    \"standard\": \"802.11AC\",\n" +
            "    \"fastRoaming\": \"802.11R\",\n" +
            "    \"vendor\": \"Vendor20\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"timestamp\": \"$timestamp\",\n" +
            "    \"ssid\": \"SSID30\",\n" +
            "    \"bssid\": \"BSSID30\",\n" +
            "    \"alias\": \"\",\n" +
            "    \"level\": -30,\n" +
            "    \"primaryChannel\": 7,\n" +
            "    \"primaryFrequency\": 2442,\n" +
            "    \"centerChannel\": 9,\n" +
            "    \"centerFrequency\": 2452,\n" +
            "    \"channelWidth\": 40,\n" +
            "    \"frequencyRange\": \"2432 - 2472\",\n" +
            "    \"distance\": \"~0.3m\",\n" +
            "    \"is80211mc\": true,\n" +
            "    \"security\": \"capabilities30\",\n" +
            "    \"standard\": \"802.11AC\",\n" +
            "    \"fastRoaming\": \"802.11R\",\n" +
            "    \"vendor\": \"Vendor30\"\n" +
            "  }\n" +
            "]\n"

    private fun timestamp(date: Date): String = SimpleDateFormat("yyyy/MM/dd-HH:mm:ss", Locale.US).format(date)

    private fun withWiFiDetails(): List<WiFiDetail> = listOf(withWiFiDetail(10), withWiFiDetail(20), withWiFiDetail(30))

    private fun withWiFiDetail(offset: Int): WiFiDetail {
        val wiFiSignalExtra = WiFiSignalExtra(true, WiFiStandard.AC, listOf(FastRoaming.FR_802_11R))
        val wiFiSignal = WiFiSignal(2412 + offset, 2422 + offset, WiFiWidth.MHZ_40, -offset, wiFiSignalExtra)
        val wiFiIdentifier = WiFiIdentifier("SSID$offset", "BSSID$offset")
        val wiFiSecurity = WiFiSecurity("capabilities$offset")
        val wiFiAdditional = WiFiAdditional("Vendor$offset")
        return WiFiDetail(wiFiIdentifier, wiFiSecurity, wiFiSignal, wiFiAdditional)
    }
}
