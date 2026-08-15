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

import android.graphics.Color
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.patrykandpatrick.vico.views.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.views.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.views.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.views.cartesian.marker.Interaction
import com.patrykandpatrick.vico.views.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.views.common.Fill
import com.patrykandpatrick.vico.views.common.Point
import com.patrykandpatrick.vico.views.common.component.ShapeComponent
import com.patrykandpatrick.vico.views.common.component.TextComponent
import com.patrykandpatrick.vico.views.common.shape.CorneredShape
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

private const val THRESHOLD_PX = 50f

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class CreateMarkerTest {
    @Test
    fun createsDefaultCartesianMarker() {
        // Arrange
        val expected =
            DefaultCartesianMarker(
                label = TextComponent(color = Color.TRANSPARENT, textSizeSp = 0f),
                indicator = MARKER_INDICATOR,
            )
        // Act
        val actual = createMarker()
        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
        val expectedIndicator = ShapeComponent(fill = Fill(0xFF0000), shape = CorneredShape.Pill)
        assertThat(MARKER_INDICATOR(0xFF0000)).usingRecursiveComparison().isEqualTo(expectedIndicator)
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class MarkerControllerWrapperTest {
    private var capturedInteraction: Interaction? = null
    private var capturedTargets: List<CartesianMarker.Target>? = null
    private val controller =
        MarkerControllerWrapper(THRESHOLD_PX) { interaction, targets ->
            capturedInteraction = interaction
            capturedTargets = targets
        }

    @Test
    fun acceptsLongPress() {
        // Act & Assert
        assertThat(controller.acceptsLongPress).isFalse()
    }

    @Test
    fun shouldAcceptInteractionRelease() {
        // Act
        val actual = controller.shouldAcceptInteraction(Interaction.Release(Point(0f, 0f)), emptyList())
        // Assert
        assertThat(actual).isTrue()
    }

    @Test
    fun shouldAcceptInteractionMove() {
        // Act
        val actual = controller.shouldAcceptInteraction(Interaction.Move(Point(0f, 0f)), emptyList())
        // Assert
        assertThat(actual).isFalse()
    }

    @Test
    fun shouldAcceptInteractionPressEmptyTargets() {
        // Act
        val actual = controller.shouldAcceptInteraction(Interaction.Press(Point(0f, 0f)), emptyList())
        // Assert
        assertThat(actual).isFalse()
    }

    @Test
    fun shouldAcceptInteractionPressNonLineTarget() {
        // Arrange
        val target: CartesianMarker.Target = mock()
        // Act
        val actual = controller.shouldAcceptInteraction(Interaction.Press(Point(0f, 0f)), listOf(target))
        // Assert
        assertThat(actual).isFalse()
    }

    @Test
    fun shouldAcceptInteractionPressWithinProximity() {
        // Arrange
        val point = Point(100f, 200f)
        val lineTarget: LineCartesianLayerMarkerTarget = mock()
        val entry = LineCartesianLayerModel.Entry(1, -50)
        val markerPoint = LineCartesianLayerMarkerTarget.Point(entry, 200f, 0)
        whenever(lineTarget.canvasX).thenReturn(100f)
        whenever(lineTarget.points).thenReturn(listOf(markerPoint))

        // Act
        val actual = controller.shouldAcceptInteraction(Interaction.Press(point), listOf(lineTarget))

        // Assert
        assertThat(actual).isTrue()
        assertThat(capturedInteraction).isEqualTo(Interaction.Press(point))
        assertThat(capturedTargets).isEqualTo(listOf(lineTarget))
    }

    @Test
    fun shouldAcceptInteractionTapWithinProximity() {
        // Arrange
        val point = Point(100f, 200f)
        val lineTarget: LineCartesianLayerMarkerTarget = mock()
        val entry = LineCartesianLayerModel.Entry(1, -50)
        val markerPoint = LineCartesianLayerMarkerTarget.Point(entry, 200f, 0)
        whenever(lineTarget.canvasX).thenReturn(100f)
        whenever(lineTarget.points).thenReturn(listOf(markerPoint))

        // Act
        val actual = controller.shouldAcceptInteraction(Interaction.Tap(point), listOf(lineTarget))

        // Assert
        assertThat(actual).isTrue()
        assertThat(capturedInteraction).isEqualTo(Interaction.Tap(point))
        assertThat(capturedTargets).isEqualTo(listOf(lineTarget))
    }

    @Test
    fun shouldAcceptInteractionPressOutsideProximity() {
        // Arrange
        val point = Point(100f, 100f)
        val lineTarget: LineCartesianLayerMarkerTarget = mock()
        val entry = LineCartesianLayerModel.Entry(1, -50)
        val markerPoint = LineCartesianLayerMarkerTarget.Point(entry, 200f, 0)
        whenever(lineTarget.canvasX).thenReturn(100f)
        whenever(lineTarget.points).thenReturn(listOf(markerPoint))

        // Act
        val actual = controller.shouldAcceptInteraction(Interaction.Press(point), listOf(lineTarget))

        // Assert
        assertThat(actual).isFalse()
    }

    @Test
    fun shouldShowMarkerPress() {
        // Act
        val actual = controller.shouldShowMarker(Interaction.Press(Point(0f, 0f)), emptyList())
        // Assert
        assertThat(actual).isTrue()
    }

    @Test
    fun shouldShowMarkerRelease() {
        // Act
        val actual = controller.shouldShowMarker(Interaction.Release(Point(0f, 0f)), emptyList())
        // Assert
        assertThat(actual).isFalse()
    }
}
