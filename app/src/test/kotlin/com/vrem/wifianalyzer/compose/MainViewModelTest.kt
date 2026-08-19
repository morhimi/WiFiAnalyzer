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
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.export.Export
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import com.vrem.wifianalyzer.wifi.model.WiFiSignal
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import kotlinx.coroutines.flow.MutableStateFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class MainViewModelTest {
    private val settings: Settings = mock()
    private val scannerService: ScannerService = mock()
    private val apAliasService: ApAliasService = mock()
    private val filtersAdapter: FiltersAdapter = mock()
    private val export: Export = mock()
    private val settingsDataFlow = MutableStateFlow(SettingsData())
    private val runningFlow = MutableStateFlow(false)
    private lateinit var fixture: MainViewModel

    @Before
    fun setUp() {
        whenever(settings.settingsData).thenReturn(settingsDataFlow)
        whenever(scannerService.runningFlow).thenReturn(runningFlow)
        fixture =
            MainViewModel(
                settings = settings,
                scannerService = scannerService,
                apAliasService = apAliasService,
                filtersAdapter = filtersAdapter,
                export = export,
            )
    }

    @After
    fun tearDown() {
        verify(settings).settingsData
        verify(scannerService).runningFlow
        verifyNoMoreInteractions(settings)
        verifyNoMoreInteractions(scannerService)
        verifyNoMoreInteractions(apAliasService)
        verifyNoMoreInteractions(filtersAdapter)
        verifyNoMoreInteractions(export)
    }

    @Test
    fun shouldExposeSettingsDataFlow() {
        assertThat(fixture.settingsData).isEqualTo(settingsDataFlow)
    }

    @Test
    fun shouldExposeIsScanningFlow() {
        assertThat(fixture.isScanning).isEqualTo(runningFlow)
    }

    @Test
    fun shouldExposeSettingsAndFiltersAdapter() {
        assertThat(fixture.settings).isEqualTo(settings)
        assertThat(fixture.filtersAdapter).isEqualTo(filtersAdapter)
    }

    @Test
    fun shouldToggleScanning() {
        // execute
        fixture.toggleScanning()

        // verify
        verify(scannerService).toggle()
    }

    @Test
    fun shouldUpdateScan() {
        // execute
        fixture.updateScan()

        // verify
        verify(scannerService).update()
    }

    @Test
    fun shouldSaveAliasAndUpdateScanner() {
        // arrange
        val bssid = "00:11:22:33:44:55"
        val alias = "My Router"

        // execute
        fixture.saveAlias(bssid, alias)

        // verify
        verify(apAliasService).saveAlias(bssid, alias)
        verify(scannerService).update()
    }

    @Test
    fun shouldRemoveAliasAndUpdateScanner() {
        // arrange
        val bssid = "00:11:22:33:44:55"

        // execute
        fixture.removeAlias(bssid)

        // verify
        verify(apAliasService).removeAlias(bssid)
        verify(scannerService).update()
    }

    @Test
    fun shouldExportWhenNoData() {
        // arrange
        val context = ApplicationProvider.getApplicationContext<Context>()
        doReturn(WiFiData.EMPTY).whenever(scannerService).wiFiData()

        // execute
        fixture.export(context)

        // verify
        verify(scannerService).wiFiData()
        verify(
            export,
            never(),
        ).export(
            any<Context>(),
            any<List<WiFiDetail>>(),
            any<com.vrem.wifianalyzer.export.ExportFormat>(),
            any<java.util.Date>(),
        )
    }

    @Test
    fun shouldShareExportWhenDataAvailable() {
        // arrange
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val context = spy(baseContext)
        val wiFiDetail =
            WiFiDetail(
                wiFiIdentifier = WiFiIdentifier("SSID", "00:11:22:33:44:55"),
                wiFiSignal = WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
            )
        val wiFiData = WiFiData(listOf(wiFiDetail), com.vrem.wifianalyzer.wifi.model.WiFiConnection.EMPTY)
        val intent: Intent = mock()
        doReturn(wiFiData).whenever(scannerService).wiFiData()
        doReturn(
            intent,
        ).whenever(
            export,
        ).export(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.CSV),
            any<java.util.Date>(),
        )

        // execute
        fixture.export(context)

        // verify
        verify(scannerService).wiFiData()
        verify(
            export,
        ).export(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.CSV),
            any<java.util.Date>(),
        )
        verify(context).startActivity(intent)
    }

    @Test
    fun shouldHandleActivityNotFoundExceptionOnShareExport() {
        // arrange
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val context = spy(baseContext)
        val wiFiDetail =
            WiFiDetail(
                wiFiIdentifier = WiFiIdentifier("SSID", "00:11:22:33:44:55"),
                wiFiSignal = WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
            )
        val wiFiData = WiFiData(listOf(wiFiDetail), com.vrem.wifianalyzer.wifi.model.WiFiConnection.EMPTY)
        val intent: Intent = mock()
        doReturn(wiFiData).whenever(scannerService).wiFiData()
        doReturn(
            intent,
        ).whenever(
            export,
        ).export(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.JSON),
            any<java.util.Date>(),
        )
        doThrow(ActivityNotFoundException()).whenever(context).startActivity(intent)

        // execute
        fixture.shareExport(context, com.vrem.wifianalyzer.export.ExportFormat.JSON)

        // verify
        verify(scannerService).wiFiData()
        verify(
            export,
        ).export(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.JSON),
            any<java.util.Date>(),
        )
        verify(context).startActivity(intent)
    }

    @Test
    fun shouldHandleGenericExceptionOnShareExport() {
        // arrange
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val context = spy(baseContext)
        val wiFiDetail =
            WiFiDetail(
                wiFiIdentifier = WiFiIdentifier("SSID", "00:11:22:33:44:55"),
                wiFiSignal = WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
            )
        val wiFiData = WiFiData(listOf(wiFiDetail), com.vrem.wifianalyzer.wifi.model.WiFiConnection.EMPTY)
        val intent: Intent = mock()
        doReturn(wiFiData).whenever(scannerService).wiFiData()
        doReturn(
            intent,
        ).whenever(
            export,
        ).export(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.TEXT),
            any<java.util.Date>(),
        )
        doThrow(RuntimeException("Export error")).whenever(context).startActivity(intent)

        // execute
        fixture.shareExport(context, com.vrem.wifianalyzer.export.ExportFormat.TEXT)

        // verify
        verify(scannerService).wiFiData()
        verify(
            export,
        ).export(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.TEXT),
            any<java.util.Date>(),
        )
        verify(context).startActivity(intent)
    }

    @Test
    fun shouldSaveExportToFileWhenNoDataReturnsFalse() {
        // arrange
        val context = ApplicationProvider.getApplicationContext<Context>()
        val uri = android.net.Uri.parse("content://test/file.csv")
        doReturn(WiFiData.EMPTY).whenever(scannerService).wiFiData()

        // execute
        val actual = fixture.saveExportToFile(context, uri, com.vrem.wifianalyzer.export.ExportFormat.CSV)

        // verify
        assertThat(actual).isFalse()
        verify(scannerService).wiFiData()
        verify(
            export,
            never(),
        ).data(
            any<Context>(),
            any<List<WiFiDetail>>(),
            any<com.vrem.wifianalyzer.export.ExportFormat>(),
            any<java.util.Date>(),
        )
    }

    @Test
    fun shouldSaveExportToFileSuccessfully() {
        // arrange
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val context = spy(baseContext)
        val contentResolver: android.content.ContentResolver = mock()
        val outputStream = java.io.ByteArrayOutputStream()
        val uri = android.net.Uri.parse("content://test/file.csv")
        val wiFiDetail =
            WiFiDetail(
                wiFiIdentifier = WiFiIdentifier("SSID", "00:11:22:33:44:55"),
                wiFiSignal = WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
            )
        val wiFiData = WiFiData(listOf(wiFiDetail), com.vrem.wifianalyzer.wifi.model.WiFiConnection.EMPTY)
        doReturn(wiFiData).whenever(scannerService).wiFiData()
        doReturn(
            "test csv data",
        ).whenever(
            export,
        ).data(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.CSV),
            any<java.util.Date>(),
        )
        doReturn(contentResolver).whenever(context).contentResolver
        whenever(contentResolver.openOutputStream(uri)).thenReturn(outputStream)

        // execute
        val actual = fixture.saveExportToFile(context, uri, com.vrem.wifianalyzer.export.ExportFormat.CSV)

        // verify
        assertThat(actual).isTrue()
        assertThat(outputStream.toString(Charsets.UTF_8.name())).isEqualTo("test csv data")
        verify(scannerService).wiFiData()
        verify(
            export,
        ).data(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.CSV),
            any<java.util.Date>(),
        )
        verify(contentResolver).openOutputStream(uri)
    }

    @Test
    fun shouldHandleSaveExportToFileExceptionReturnsFalse() {
        // arrange
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val context = spy(baseContext)
        val contentResolver: android.content.ContentResolver = mock()
        val uri = android.net.Uri.parse("content://test/file.csv")
        val wiFiDetail =
            WiFiDetail(
                wiFiIdentifier = WiFiIdentifier("SSID", "00:11:22:33:44:55"),
                wiFiSignal = WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
            )
        val wiFiData = WiFiData(listOf(wiFiDetail), com.vrem.wifianalyzer.wifi.model.WiFiConnection.EMPTY)
        doReturn(wiFiData).whenever(scannerService).wiFiData()
        doReturn(
            "test csv data",
        ).whenever(
            export,
        ).data(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.CSV),
            any<java.util.Date>(),
        )
        doReturn(contentResolver).whenever(context).contentResolver
        whenever(contentResolver.openOutputStream(uri)).thenThrow(RuntimeException("IO error"))

        // execute
        val actual = fixture.saveExportToFile(context, uri, com.vrem.wifianalyzer.export.ExportFormat.CSV)

        // verify
        assertThat(actual).isFalse()
        verify(scannerService).wiFiData()
        verify(
            export,
        ).data(
            org.mockito.kotlin.eq(context),
            org.mockito.kotlin.eq(wiFiData.wiFiDetails),
            org.mockito.kotlin.eq(com.vrem.wifianalyzer.export.ExportFormat.CSV),
            any<java.util.Date>(),
        )
        verify(contentResolver).openOutputStream(uri)
    }
}
