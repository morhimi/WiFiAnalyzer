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
import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.Point
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import com.vrem.wifianalyzer.wifi.model.WiFiSecurity
import com.vrem.wifianalyzer.wifi.model.WiFiSignal
import com.vrem.wifianalyzer.wifi.model.WiFiWidth
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.util.AbstractMap

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class MarkerInteractionTest {
    private val markerHandler: MarkerHandler = mock()
    private val thresholdPx = 48f
    private val tapPoint = Point(100f, 200f)
    private val lineTarget: LineCartesianLayerMarkerTarget = mock()
    private val entry = LineCartesianLayerModel.Entry(10.0, -50.0)
    private val markerPoint = LineCartesianLayerMarkerTarget.Point(entry, 200f, Color.Black)
    private val wiFiDetail1 =
        WiFiDetail(
            wiFiIdentifier = WiFiIdentifier("SSID1", "AA:BB:CC:DD:EE:01"),
            wiFiSecurity = WiFiSecurity.EMPTY,
            wiFiSignal = WiFiSignal(2412, 2412, WiFiWidth.MHZ_20, -50),
        )
    private val wiFiDetail2 =
        WiFiDetail(
            wiFiIdentifier = WiFiIdentifier("SSID2", "AA:BB:CC:DD:EE:02"),
            wiFiSecurity = WiFiSecurity.EMPTY,
            wiFiSignal = WiFiSignal(2437, 2437, WiFiWidth.MHZ_20, -60),
        )
    private val fixture = MarkerInteraction(markerHandler, thresholdPx)

    @After
    fun tearDown() {
        verifyNoMoreInteractions(markerHandler, lineTarget)
    }

    @Test
    fun secondaryConstructorInstantiatesSuccessfully() {
        // Act
        val custom = MarkerInteraction({ _ -> }, thresholdPx)
        // Assert
        assertThat(custom.markerController).isNotNull
    }

    @Test
    fun updatePointMapWithEmptyEntriesPassesEmptyMapToHandlerOnTap() {
        // Arrange
        val targets = withMatchingTargets()
        doReturn(false).whenever(markerHandler).event(tapPoint, thresholdPx, emptyMap(), targets, emptyList())
        // Act
        fixture.updatePointMap(emptyList())
        val accepted = fixture.markerController.shouldAcceptInteraction(Interaction.Tap(tapPoint), targets)
        // Assert
        assertThat(accepted).isTrue
        verify(markerHandler).event(tapPoint, thresholdPx, emptyMap(), targets, emptyList())
        verify(lineTarget).points
        verify(lineTarget).canvasX
    }

    @Test
    fun updatePointMapMapsDataPointKeyToWiFiDetail() {
        // Arrange
        val dataPoint = DataPoint(10, -50)
        val entries = listOf(withEntry(wiFiDetail1, listOf(dataPoint)))
        val targets = withMatchingTargets()
        val expectedMap = mapOf(dataPoint.key to mutableListOf(wiFiDetail1))
        val expectedList = listOf(wiFiDetail1)
        doReturn(true).whenever(markerHandler).event(tapPoint, thresholdPx, expectedMap, targets, expectedList)
        // Act
        fixture.updatePointMap(entries)
        val accepted = fixture.markerController.shouldAcceptInteraction(Interaction.Tap(tapPoint), targets)
        // Assert
        assertThat(accepted).isTrue
        verify(markerHandler).event(tapPoint, thresholdPx, expectedMap, targets, expectedList)
        verify(lineTarget).points
        verify(lineTarget).canvasX
    }

    @Test
    fun updatePointMapGroupsMultipleDetailsAtSameDataPoint() {
        // Arrange
        val dataPoint = DataPoint(10, -50)
        val entries =
            listOf(
                withEntry(wiFiDetail1, listOf(dataPoint)),
                withEntry(wiFiDetail2, listOf(dataPoint)),
            )
        val targets = withMatchingTargets()
        val expectedMap = mapOf(dataPoint.key to mutableListOf(wiFiDetail1, wiFiDetail2))
        val expectedList = listOf(wiFiDetail1, wiFiDetail2)
        doReturn(true).whenever(markerHandler).event(tapPoint, thresholdPx, expectedMap, targets, expectedList)
        // Act
        fixture.updatePointMap(entries)
        val accepted = fixture.markerController.shouldAcceptInteraction(Interaction.Tap(tapPoint), targets)
        // Assert
        assertThat(accepted).isTrue
        verify(markerHandler).event(tapPoint, thresholdPx, expectedMap, targets, expectedList)
        verify(lineTarget).points
        verify(lineTarget).canvasX
    }

    @Test
    fun updatePointMapCreatesDistinctKeysForDifferentDataPoints() {
        // Arrange
        val dataPoint1 = DataPoint(10, -50)
        val dataPoint2 = DataPoint(20, -60)
        val entries = listOf(withEntry(wiFiDetail1, listOf(dataPoint1, dataPoint2)))
        val targets = withMatchingTargets()
        val expectedMap =
            mapOf(
                dataPoint1.key to mutableListOf(wiFiDetail1),
                dataPoint2.key to mutableListOf(wiFiDetail1),
            )
        val expectedList = listOf(wiFiDetail1)
        doReturn(true).whenever(markerHandler).event(tapPoint, thresholdPx, expectedMap, targets, expectedList)
        // Act
        fixture.updatePointMap(entries)
        val accepted = fixture.markerController.shouldAcceptInteraction(Interaction.Tap(tapPoint), targets)
        // Assert
        assertThat(accepted).isTrue
        verify(markerHandler).event(tapPoint, thresholdPx, expectedMap, targets, expectedList)
        verify(lineTarget).points
        verify(lineTarget).canvasX
    }

    @Test
    fun updatePointMapExcludesPlaceholder() {
        // Arrange
        val placeholderPoint = DataPoint(10, MIN_Y)
        val realPoint = DataPoint(10, -50)
        val entries =
            listOf(
                withEntry(PLACEHOLDER_DETAIL, listOf(placeholderPoint)),
                withEntry(wiFiDetail1, listOf(realPoint)),
            )
        val targets = withMatchingTargets()
        val expectedMap = mapOf(realPoint.key to mutableListOf(wiFiDetail1))
        val expectedList = listOf(wiFiDetail1)
        doReturn(true).whenever(markerHandler).event(tapPoint, thresholdPx, expectedMap, targets, expectedList)
        // Act
        fixture.updatePointMap(entries)
        val accepted = fixture.markerController.shouldAcceptInteraction(Interaction.Tap(tapPoint), targets)
        // Assert
        assertThat(accepted).isTrue
        verify(markerHandler).event(tapPoint, thresholdPx, expectedMap, targets, expectedList)
        verify(lineTarget).points
        verify(lineTarget).canvasX
    }

    @Test
    fun pressInteractionInvokesMarkerHandlerEvent() {
        // Arrange
        val targets = withMatchingTargets()
        doReturn(true).whenever(markerHandler).event(tapPoint, thresholdPx, emptyMap(), targets, emptyList())
        // Act
        val accepted = fixture.markerController.shouldAcceptInteraction(Interaction.Press(tapPoint), targets)
        // Assert
        assertThat(accepted).isTrue
        verify(markerHandler).event(tapPoint, thresholdPx, emptyMap(), targets, emptyList())
        verify(lineTarget).points
        verify(lineTarget).canvasX
    }

    private fun withMatchingTargets(): List<CartesianMarker.Target> {
        whenever(lineTarget.canvasX).thenReturn(tapPoint.x)
        whenever(lineTarget.points).thenReturn(listOf(markerPoint))
        return listOf(lineTarget)
    }

    private fun withEntry(
        wiFiDetail: WiFiDetail,
        dataPoints: List<DataPoint>,
    ): SeriesEntry = AbstractMap.SimpleEntry(wiFiDetail, SeriesData(wiFiDetail, dataPoints))
}
