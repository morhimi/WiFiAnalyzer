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
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.Point
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

private const val THRESHOLD_PX = 50f
private const val CANVAS_X = 100f
private const val CANVAS_Y = 200f

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class MarkerHandlerTest {
    private val lineCartesianLayerMarkerTarget: LineCartesianLayerMarkerTarget = mock()
    private val lastTouch = Point(CANVAS_X, CANVAS_Y)
    private var capturedDetails: List<WiFiDetail>? = null

    private val fixture = MarkerHandler(onShowWiFiDetails = { capturedDetails = it })

    @After
    fun tearDown() {
        verifyNoMoreInteractions(lineCartesianLayerMarkerTarget)
    }

    @Test
    fun eventReturnsFalseWhenNoLineTarget() {
        // Arrange
        val targets: List<CartesianMarker.Target> = emptyList()
        // Act
        val actual = fixture.event(lastTouch, THRESHOLD_PX, emptyMap(), targets)
        // Assert
        assertThat(actual).isFalse
    }

    @Test
    fun eventReturnsFalseWhenEmptyPoints() {
        // Arrange
        val points = emptyList<LineCartesianLayerMarkerTarget.Point>()
        doReturn(CANVAS_X).whenever(lineCartesianLayerMarkerTarget).canvasX
        doReturn(points).whenever(lineCartesianLayerMarkerTarget).points
        // Act
        val actual = fixture.event(lastTouch, THRESHOLD_PX, emptyMap(), listOf(lineCartesianLayerMarkerTarget))
        // Assert
        assertThat(actual).isFalse
        verify(lineCartesianLayerMarkerTarget).points
    }

    @Test
    fun eventInvokesCallbackWhenDetailsMatch() {
        // Arrange
        val wiFiDetails = withWiFiDetails()
        val targetPoints = withTargetPoints()
        val pointMap = withPointMap(targetPoints, wiFiDetails)
        doReturn(CANVAS_X).whenever(lineCartesianLayerMarkerTarget).canvasX
        doReturn(targetPoints).whenever(lineCartesianLayerMarkerTarget).points
        // Act
        val actual = fixture.event(lastTouch, THRESHOLD_PX, pointMap, listOf(lineCartesianLayerMarkerTarget))
        // Assert
        assertThat(actual).isTrue
        verify(this.lineCartesianLayerMarkerTarget).canvasX
        verify(this.lineCartesianLayerMarkerTarget).points
        assertThat(capturedDetails).isEqualTo(wiFiDetails)
    }

    @Test
    fun eventInvokesCallbackWhenDetailsMatchViaSeriesList() {
        // Arrange
        val wiFiDetail =
            WiFiDetail(
                wiFiIdentifier = WiFiIdentifier("SSID1", "AA:BB:CC:DD:EE:01"),
                wiFiSignal =
                    com.vrem.wifianalyzer.wifi.model.WiFiSignal(
                        2412,
                        2412,
                        com.vrem.wifianalyzer.wifi.model.WiFiWidth.MHZ_20,
                        -50,
                    ),
            )
        val targetPoint = withTargetPoint(2412, -50)
        doReturn(CANVAS_X).whenever(lineCartesianLayerMarkerTarget).canvasX
        doReturn(listOf(targetPoint)).whenever(lineCartesianLayerMarkerTarget).points
        // Act
        val actual =
            fixture.event(
                lastTouch,
                THRESHOLD_PX,
                emptyMap(),
                listOf(lineCartesianLayerMarkerTarget),
                listOf(wiFiDetail),
            )
        // Assert
        assertThat(actual).isTrue
        verify(this.lineCartesianLayerMarkerTarget).canvasX
        verify(this.lineCartesianLayerMarkerTarget).points
        assertThat(capturedDetails).containsExactly(wiFiDetail)
    }

    @Test
    fun eventDoesNotInvokeCallbackWhenNoMatchingDetails() {
        // Arrange
        val targetPoints = withTargetPoints()
        doReturn(CANVAS_X).whenever(lineCartesianLayerMarkerTarget).canvasX
        doReturn(targetPoints).whenever(lineCartesianLayerMarkerTarget).points
        // Act
        val actual = fixture.event(lastTouch, THRESHOLD_PX, emptyMap(), listOf(lineCartesianLayerMarkerTarget))
        // Assert
        assertThat(actual).isFalse
        verify(lineCartesianLayerMarkerTarget).canvasX
        verify(lineCartesianLayerMarkerTarget).points
        assertThat(capturedDetails).isNull()
    }

    @Test
    fun eventDoesNotInvokeCallbackWhenTouchOutsideYThreshold() {
        // Arrange - touch is far above in Y from the target points
        val touchFarInY = Point(CANVAS_X, CANVAS_Y - THRESHOLD_PX - 1f)
        val wiFiDetails = withWiFiDetails()
        val targetPoints = withTargetPoints()
        val pointMap = withPointMap(targetPoints, wiFiDetails)
        doReturn(CANVAS_X).whenever(lineCartesianLayerMarkerTarget).canvasX
        doReturn(targetPoints).whenever(lineCartesianLayerMarkerTarget).points
        // Act
        val actual = fixture.event(touchFarInY, THRESHOLD_PX, pointMap, listOf(lineCartesianLayerMarkerTarget))
        // Assert
        assertThat(actual).isFalse
        verify(lineCartesianLayerMarkerTarget).canvasX
        verify(lineCartesianLayerMarkerTarget).points
        assertThat(capturedDetails).isNull()
    }

    private fun withWiFiDetails(): List<WiFiDetail> =
        listOf(
            WiFiDetail(wiFiIdentifier = WiFiIdentifier("SSID1", "AA:BB:CC:DD:EE:01")),
            WiFiDetail(wiFiIdentifier = WiFiIdentifier("SSID2", "AA:BB:CC:DD:EE:02")),
            WiFiDetail(wiFiIdentifier = WiFiIdentifier("SSID3", "AA:BB:CC:DD:EE:03")),
        )

    private fun withTargetPoints(): List<LineCartesianLayerMarkerTarget.Point> =
        listOf(
            withTargetPoint(1, -50),
            withTargetPoint(2, -60),
            withTargetPoint(3, -70),
        )

    private fun withPointMap(
        targetPoints: List<LineCartesianLayerMarkerTarget.Point>,
        wiFiDetails: List<WiFiDetail>,
    ): Map<Long, MutableList<WiFiDetail>> =
        targetPoints.zip(wiFiDetails).associate { (targetPoint, wiFiDetail) ->
            DataPoint(targetPoint.entry.x.toInt(), targetPoint.entry.y.toInt()).key to mutableListOf(wiFiDetail)
        }

    private fun withTargetPoint(
        x: Int,
        y: Int,
    ): LineCartesianLayerMarkerTarget.Point {
        val entry = LineCartesianLayerModel.Entry(x.toDouble(), y.toDouble())
        return LineCartesianLayerMarkerTarget.Point(entry, CANVAS_Y, androidx.compose.ui.graphics.Color.Black)
    }
}
