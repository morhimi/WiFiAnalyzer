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

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.annotation.Config
import java.util.AbstractMap.SimpleEntry

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ChartUpdaterTest {
    private val seriesLabel = SeriesLabel(calculateLabelPosition = { _, _ -> null })
    private val seriesCache = SeriesCache(emptyList())
    private val lineStyleTracker = LineStyleTracker()
    private val lineLayerFactory = LineLayerFactory()
    private val markerInteraction: MarkerInteraction = mock()

    private val fixture =
        ChartUpdater(
            seriesLabel = seriesLabel,
            seriesCache = seriesCache,
            lineStyleTracker = lineStyleTracker,
            lineLayerFactory = lineLayerFactory,
            markerInteraction = markerInteraction,
        )

    @After
    fun tearDown() {
        verifyNoMoreInteractions(markerInteraction)
    }

    @Test
    fun syncReturnsLinesOnFirstCall() {
        // Arrange
        val expected = populatedData()
        val entries = withEntries(expected)
        // Act
        val (actualData, actualLines) = fixture.sync(entries)
        // Assert
        assertThat(actualData).isEqualTo(expected)
        assertThat(actualLines).isNotNull
        assertThat(actualLines).hasSize(1)
        verify(markerInteraction).updatePointMap(entries)
    }

    @Test
    fun syncUpdatesSeriesSnapshot() {
        // Arrange
        val expected = populatedData()
        val entries = withEntries(expected)
        // Act
        val (actualData, _) = fixture.sync(entries)
        // Assert
        assertThat(actualData).isEqualTo(expected)
        assertThat(seriesLabel.seriesSnapshot).isEqualTo(expected)
        verify(markerInteraction).updatePointMap(entries)
    }

    @Test
    fun syncForwardsEntriesToMarkerInteraction() {
        // Arrange
        val expected = populatedData()
        val entries = withEntries(expected)
        // Act
        val (actualData, _) = fixture.sync(entries)
        // Assert
        assertThat(actualData).isEqualTo(expected)
        verify(markerInteraction).updatePointMap(entries)
    }

    @Test
    fun syncSkipsLineRegenerationWhenStylesUnchanged() {
        // Arrange
        val firstEntries = withEntries(populatedData())
        val seriesData = seriesData(dataPoints = listOf(DataPoint(99, -60)))
        val expected = populatedData(seriesData)
        val secondEntries = listOf(SimpleEntry(WiFiDetail.EMPTY, seriesData))
        fixture.sync(firstEntries)
        // Act
        val (actualData, actualLines) = fixture.sync(secondEntries)
        // Assert
        assertThat(actualData).isEqualTo(expected)
        assertThat(actualLines).isNull()
        assertThat(seriesLabel.seriesSnapshot).isEqualTo(expected)
        verify(markerInteraction).updatePointMap(firstEntries)
        verify(markerInteraction).updatePointMap(secondEntries)
    }

    @Test
    fun syncPointMapForwardsToMarkerInteraction() {
        // Arrange
        val entries = withEntries(populatedData())
        // Act
        fixture.syncPointMap(entries)
        // Assert
        verify(markerInteraction).updatePointMap(entries)
    }

    @Test
    fun resetStylesForcesNextSyncToRegenerateLines() {
        // Arrange
        val expected = populatedData()
        val entries = withEntries(expected)
        fixture.sync(entries)
        // Act
        fixture.resetStyles()
        val (actualData, actualLines) = fixture.sync(entries)
        // Assert
        assertThat(actualData).isEqualTo(expected)
        assertThat(actualLines).isNotNull
        assertThat(actualLines).hasSize(1)
        verify(markerInteraction, times(2)).updatePointMap(entries)
    }

    private fun seriesData(dataPoints: List<DataPoint> = listOf(DataPoint(1, -50))): SeriesData =
        SeriesData(
            dataPoints = dataPoints,
            graphColor = GraphColor(0xFF0000, 0x00FF00),
        )

    private fun populatedData(seriesData: SeriesData = seriesData()) = listOf(seriesData)

    private fun withEntries(populatedData: List<SeriesData>): List<SeriesEntry> =
        populatedData.map { SimpleEntry(WiFiDetail.EMPTY, it) }
}
