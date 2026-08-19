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
package com.vrem.wifianalyzer.wifi.graphutils

import android.graphics.Canvas
import android.graphics.RectF
import android.os.Build
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

private const val Y_OFFSET = 12f

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class SeriesLabelTest {
    private val context: CartesianDrawingContext = mock()
    private val nativeCanvas: Canvas = mock()
    private val composeCanvas: androidx.compose.ui.graphics.Canvas =
        androidx.compose.ui.graphics
            .Canvas(nativeCanvas)
    private val calculateLabelPosition: CalculateLabelPosition = mock()
    private val configureLabel: ConfigureLabel = mock()
    private val layerBounds = Rect(0f, 0f, 200f, 200f)

    private val fixture = SeriesLabel(calculateLabelPosition, configureLabel)

    @After
    fun tearDown() {
        verifyNoMoreInteractions(context, nativeCanvas, calculateLabelPosition, configureLabel)
    }

    @Test
    fun paintIsAntiAliased() {
        // Assert
        assertThat(fixture.paint.isAntiAlias).isTrue
    }

    @Test
    fun drawOverLayersSkipsWhenSeriesEmpty() {
        // Act
        fixture.drawOverLayers(context)
        // Assert
        verify(context, never()).density
        verify(nativeCanvas, never()).drawText(any<String>(), any(), any(), any())
    }

    @Test
    fun drawOverLayersSkipsDrawText() {
        // Arrange
        val series = withSeries()
        withContext()
        fixture.seriesSnapshot = series
        series.forEach { seriesData ->
            doReturn(null).whenever(calculateLabelPosition).invoke(context, seriesData)
        }
        // Act
        fixture.drawOverLayers(context)
        // Assert
        verifyContext()
        verify(configureLabel, never()).invoke(any(), any(), any(), any())
        series.forEach { seriesData ->
            verify(calculateLabelPosition).invoke(context, seriesData)
            verify(nativeCanvas, never()).drawText(eq(seriesData.title), any(), any(), any())
        }
    }

    @Test
    fun drawOverLayers() {
        // Arrange
        val labelPosition = LabelPosition(30f, 60f)
        val series = withSeries()
        withContext()
        fixture.seriesSnapshot = series
        series.forEach { seriesData ->
            doReturn(labelPosition).whenever(calculateLabelPosition).invoke(context, seriesData)
        }
        // Act
        fixture.drawOverLayers(context)
        // Assert
        verifyContext()
        series.forEach { seriesData ->
            verify(calculateLabelPosition).invoke(context, seriesData)
            verify(configureLabel).invoke(context, labelPosition, seriesData, fixture.paint)
            verify(nativeCanvas).drawText(seriesData.title, labelPosition.x, labelPosition.y - Y_OFFSET, fixture.paint)
        }
    }

    @Test
    fun findDetailsAtWhenSeriesEmptyReturnsEmptyList() {
        // Arrange
        fixture.seriesSnapshot = emptyList()
        // Act
        val actual = fixture.findDetailsAt(100f, 100f)
        // Assert
        assertThat(actual).isEmpty()
    }

    @Test
    fun findDetailsAtChannelGraphMatchesInRangeWiFiDetail() {
        // Arrange
        val wiFiDetail =
            com.vrem.wifianalyzer.wifi.model.WiFiDetail(
                wiFiIdentifier =
                    com.vrem.wifianalyzer.wifi.model
                        .WiFiIdentifier("SSID1", "00:11:22:33:44:55"),
                wiFiSignal =
                    com.vrem.wifianalyzer.wifi.model.WiFiSignal(
                        2412,
                        2412,
                        com.vrem.wifianalyzer.wifi.model.WiFiWidth.MHZ_20,
                        -50,
                    ),
            )
        val seriesData = SeriesData(wiFiDetail = wiFiDetail, dataPoints = listOf(DataPoint(2412, -50)))
        fixture.seriesSnapshot = listOf(seriesData)
        // Act - center of 2400..2500 MHz on a 1000px width chart corresponds to 2412 around x=120px
        val actual = fixture.findDetailsAt(120f, 400f, chartWidth = 1000f, chartHeight = 800f)
        // Assert
        assertThat(actual).containsExactly(wiFiDetail)
    }

    @Test
    fun findDetailsAtTimeGraphMatchesNearbyDataPoint() {
        // Arrange
        val wiFiDetail =
            com.vrem.wifianalyzer.wifi.model.WiFiDetail(
                wiFiIdentifier =
                    com.vrem.wifianalyzer.wifi.model
                        .WiFiIdentifier("SSID1", "00:11:22:33:44:55"),
            )
        val seriesData = SeriesData(wiFiDetail = wiFiDetail, dataPoints = listOf(DataPoint(2450, -60)))
        fixture.seriesSnapshot = listOf(seriesData)
        // Act - at center of chart x=500px, y=400px (dataX=2450, dataY=-60)
        val actual = fixture.findDetailsAt(500f, 400f, chartWidth = 1000f, chartHeight = 800f)
        // Assert
        assertThat(actual).containsExactly(wiFiDetail)
    }

    @Test
    fun findDetailsAtOutsideRangeReturnsEmptyList() {
        // Arrange
        val wiFiDetail =
            com.vrem.wifianalyzer.wifi.model.WiFiDetail(
                wiFiIdentifier =
                    com.vrem.wifianalyzer.wifi.model
                        .WiFiIdentifier("SSID1", "00:11:22:33:44:55"),
                wiFiSignal =
                    com.vrem.wifianalyzer.wifi.model.WiFiSignal(
                        2412,
                        2412,
                        com.vrem.wifianalyzer.wifi.model.WiFiWidth.MHZ_20,
                        -50,
                    ),
            )
        val seriesData = SeriesData(wiFiDetail = wiFiDetail, dataPoints = listOf(DataPoint(2412, -50)))
        fixture.seriesSnapshot = listOf(seriesData)
        // Act - tap far away at x=900px (around 2490 MHz)
        val actual = fixture.findDetailsAt(900f, 400f, chartWidth = 1000f, chartHeight = 800f)
        // Assert
        assertThat(actual).isEmpty()
    }

    @Test
    fun findDetailsAtWithDrawingContext() {
        // Arrange
        val ranges =
            com.patrykandpatrick.vico.compose.cartesian.data
                .MutableCartesianChartRanges()
                .apply {
                    tryUpdate(2400.0, 2500.0, -100.0, -20.0, null)
                }
        doReturn(ranges).whenever(context).ranges
        doReturn(layerBounds).whenever(context).layerBounds
        fixture.lastDrawingContext = context

        val wiFiDetail =
            com.vrem.wifianalyzer.wifi.model.WiFiDetail(
                wiFiIdentifier =
                    com.vrem.wifianalyzer.wifi.model
                        .WiFiIdentifier("SSID1", "00:11:22:33:44:55"),
                wiFiSignal =
                    com.vrem.wifianalyzer.wifi.model.WiFiSignal(
                        2412,
                        2412,
                        com.vrem.wifianalyzer.wifi.model.WiFiWidth.MHZ_20,
                        -50,
                    ),
            )
        val seriesData = SeriesData(wiFiDetail = wiFiDetail, dataPoints = listOf(DataPoint(2412, -50)))
        fixture.seriesSnapshot = listOf(seriesData)
        // Act - on a 200px width chart, 2412 is around 24px
        val actual = fixture.findDetailsAt(24f, 100f)
        // Assert
        assertThat(actual).containsExactly(wiFiDetail)
        verify(context, org.mockito.Mockito.atLeastOnce()).ranges
        verify(context).layerBounds
    }

    @Test
    fun findDetailsAtWhenZeroDimensionsReturnsEmptyList() {
        // Arrange
        val wiFiDetail =
            com.vrem.wifianalyzer.wifi.model.WiFiDetail(
                wiFiIdentifier =
                    com.vrem.wifianalyzer.wifi.model
                        .WiFiIdentifier("SSID1", "00:11:22:33:44:55"),
            )
        fixture.seriesSnapshot = listOf(SeriesData(wiFiDetail = wiFiDetail, dataPoints = listOf(DataPoint(1, 1))))
        // Act
        val actual = fixture.findDetailsAt(100f, 100f, chartWidth = -1f, chartHeight = -1f)
        // Assert
        assertThat(actual).isEmpty()
    }

    private fun withSeries(): List<SeriesData> =
        listOf(
            SeriesData(dataPoints = listOf(DataPoint(1, -50)), title = "SSID1"),
            SeriesData(dataPoints = listOf(DataPoint(2, -60)), title = "SSID2"),
            SeriesData(dataPoints = listOf(DataPoint(3, -70)), title = "SSID3"),
        )

    private fun verifyContext() {
        verify(context).density
        verify(context).canvas
        verify(context).layerBounds
        verify(nativeCanvas).save()
        verify(nativeCanvas).clipRect(RectF(0f, 0f, 200f, 200f))
        verify(nativeCanvas).restoreToCount(0)
    }

    private fun withContext() {
        doReturn(Density(2f, 1f)).whenever(context).density
        doReturn(composeCanvas).whenever(context).canvas
        doReturn(layerBounds).whenever(context).layerBounds
    }
}
