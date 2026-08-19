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

import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class GraphViewportTest {
    private val rangeProvider: CartesianLayerRangeProvider = mock()
    private val placeholderDataPoints = listOf(DataPoint(1, -50))

    @Test
    fun defaultValues() {
        // Act
        val actual = GraphViewport(rangeProvider, placeholderDataPoints)

        // Assert
        assertThat(actual.rangeProvider).isEqualTo(rangeProvider)
        assertThat(actual.placeholderDataPoints).isEqualTo(placeholderDataPoints)
        assertThat(actual.scrollEnabled).isTrue
        assertThat(actual.initialScroll).isEqualTo(Scroll.Absolute.Start)
        assertThat(actual.autoScrollCondition).isEqualTo(AutoScrollCondition.Never)
        assertThat(actual.initialZoom).isEqualTo(Zoom.Content)
        assertThat(actual.zoomEnabled).isFalse
        assertThat(actual.minZoom).isEqualTo(Zoom.Content)
        assertThat(actual.scalable).isFalse
    }

    @Test
    fun customValues() {
        // Arrange
        val initialZoom = Zoom.x(10.0)
        val maxZoom = Zoom.x(20.0)

        // Act
        val actual =
            GraphViewport(
                rangeProvider = rangeProvider,
                placeholderDataPoints = placeholderDataPoints,
                scrollEnabled = false,
                initialScroll = Scroll.Absolute.End,
                autoScrollCondition = AutoScrollCondition.OnModelGrowth,
                initialZoom = initialZoom,
                zoomEnabled = true,
                minZoom = Zoom.Content,
                maxZoom = maxZoom,
                scalable = true,
            )

        // Assert
        assertThat(actual.scrollEnabled).isFalse
        assertThat(actual.initialScroll).isEqualTo(Scroll.Absolute.End)
        assertThat(actual.autoScrollCondition).isEqualTo(AutoScrollCondition.OnModelGrowth)
        assertThat(actual.initialZoom).isEqualTo(initialZoom)
        assertThat(actual.zoomEnabled).isTrue
        assertThat(actual.maxZoom).isEqualTo(maxZoom)
        assertThat(actual.scalable).isTrue
    }

    @Test
    fun copyAndEquality() {
        // Arrange
        val original = GraphViewport(rangeProvider, placeholderDataPoints)
        val copy = original.copy()

        // Assert
        assertThat(original).isEqualTo(copy)
        assertThat(original.hashCode()).isEqualTo(copy.hashCode())
    }
}
