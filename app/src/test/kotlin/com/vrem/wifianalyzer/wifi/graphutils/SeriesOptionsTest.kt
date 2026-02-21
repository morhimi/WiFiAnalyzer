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

import info.appdev.charting.data.LineDataSet
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class SeriesOptionsTest {
    private val graphColors: GraphColors = mock()
    private val lineDataSet: LineDataSet = mock()
    private val graphColor = GraphColor(22, 11)
    private val fixture = SeriesOptions(graphColors)

    @After
    fun tearDown() {
        verifyNoMoreInteractions(lineDataSet)
        verifyNoMoreInteractions(graphColors)
    }

    @Test
    fun removeSeries() {
        // setup
        val color = 10
        whenever(lineDataSet.color).thenReturn(color)
        // execute
        fixture.removeSeriesColor(lineDataSet)
        // validate
        verify(lineDataSet).color
        verify(graphColors).addColor(color.toLong())
    }

    @Test
    fun highlightConnectedSetsConnectedThickness() {
        // execute
        fixture.highlightConnected(lineDataSet, true)
        // validate
        verify(lineDataSet).lineWidth = THICKNESS_CONNECTED.toFloat()
    }

    @Test
    fun highlightConnectedSetsNotConnectedThickness() {
        // execute
        fixture.highlightConnected(lineDataSet, false)
        // validate
        verify(lineDataSet).lineWidth = THICKNESS_REGULAR.toFloat()
    }

    @Test
    fun setSeriesColor() {
        // setup
        whenever(graphColors.graphColor()).thenReturn(graphColor)
        // execute
        fixture.setSeriesColor(lineDataSet)
        // validate
        verify(graphColors).graphColor()
        verify(lineDataSet).color = graphColor.primary.toInt()
        verify(lineDataSet).fillColor = graphColor.background.toInt()
    }

    @Test
    fun drawBackground() {
        // execute
        fixture.drawBackground(lineDataSet, true)
        // validate
        verify(lineDataSet).isDrawFilled = true
    }
}
