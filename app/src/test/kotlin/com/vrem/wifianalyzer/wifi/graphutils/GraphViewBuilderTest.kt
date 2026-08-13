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

import android.view.View
import android.view.ViewGroup
import com.vrem.wifianalyzer.settings.ThemeStyle
import info.appdev.charting.charts.LineChart
import info.appdev.charting.components.XAxis
import info.appdev.charting.formatter.IAxisValueFormatter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GraphViewBuilderTest {
    private val numHorizontalLabels = 5
    private val graphView: LineChart = mock()
    private val labelFormatter: IAxisValueFormatter = mock()

    @Test
    fun layout() {
        // setup
        val layoutParams = ViewGroup.LayoutParams(10, 10)
        // execute
        graphView.layout(layoutParams)
        // validate
        verify(graphView).layoutParams = layoutParams
        verify(graphView).visibility = View.GONE
    }

    @Test
    fun labels() {
        // setup
        val xAxis: XAxis = mock()
        whenever(graphView.xAxis).thenReturn(xAxis)
        whenever(graphView.description).thenReturn(mock())
        whenever(graphView.legend).thenReturn(mock())
        // execute
        graphView.labels(true, 10)
        // validate
        verify(xAxis).setDrawLabels(true)
        verify(xAxis).position = XAxis.XAxisPosition.BOTTOM
        verify(xAxis).setLabelCount(10, true)
    }

    @Test
    fun labelFormat() {
        // setup
        val xAxis: XAxis = mock()
        whenever(graphView.xAxis).thenReturn(xAxis)
        // execute
        graphView.labelFormat(labelFormatter)
        // validate
        verify(xAxis).valueFormatter = labelFormatter
    }

    @Test
    fun getNumVerticalLabels() {
        // setup
        val expected = 9
        val fixture = GraphViewBuilder(numHorizontalLabels, MAX_Y_DEFAULT, ThemeStyle.DARK, true)
        // execute
        val actual = fixture.numVerticalLabels
        // validate
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun getMaximumYLimits() {
        validateMaximumY(1, MAX_Y_DEFAULT)
        validateMaximumY(0, 0)
        validateMaximumY(-50, -50)
        validateMaximumY(-51, MAX_Y_DEFAULT)
    }

    private fun validateMaximumY(
        maximumY: Int,
        expected: Int,
    ) {
        val fixture = GraphViewBuilder(numHorizontalLabels, maximumY, ThemeStyle.DARK, true)
        assertThat(fixture.maximumPortY).isEqualTo(expected)
    }
}
