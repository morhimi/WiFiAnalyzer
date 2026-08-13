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

import info.appdev.charting.data.EntryFloat
import info.appdev.charting.data.LineDataSet
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class SeriesOptionsTest {
    private val graphColors: GraphColors = mock()
    private val lineDataSet: LineDataSet<EntryFloat> = mock()
    private val fixture = SeriesOptions(graphColors)

    @After
    fun tearDown() {
        verifyNoMoreInteractions(graphColors)
        verifyNoMoreInteractions(lineDataSet)
    }

    @Test
    fun removeSeriesColor() {
        // setup
        val color = 111L
        whenever(lineDataSet.colors).thenReturn(mutableListOf(color.toInt()))
        // execute
        fixture.removeSeriesColor(lineDataSet)
        // validate
        verify(lineDataSet).colors
        verify(graphColors).addColor(color)
    }

    @Test
    fun highlightConnectedTrue() {
        // execute
        fixture.highlightConnected(lineDataSet, true)
        // validate
        verify(lineDataSet).lineWidth = THICKNESS_CONNECTED.toFloat()
    }

    @Test
    fun highlightConnectedFalse() {
        // execute
        fixture.highlightConnected(lineDataSet, false)
        // validate
        verify(lineDataSet).lineWidth = THICKNESS_REGULAR.toFloat()
    }

    @Test
    fun setSeriesColor() {
        // setup
        val graphColor = GraphColor(1, 2)
        whenever(graphColors.graphColor()).thenReturn(graphColor)
        // execute
        fixture.setSeriesColor(lineDataSet)
        // validate
        verify(graphColors).graphColor()
        verify(lineDataSet).setColors(graphColor.primary.toInt())
        verify(lineDataSet).fillColor = graphColor.background.toInt()
        verify(lineDataSet).isDrawCircles = false
        verify(lineDataSet).isDrawValues = false
        verify(lineDataSet).valueTextColor = graphColor.primary.toInt()
        verify(lineDataSet).valueTextSize = 10f
        verify(lineDataSet).setDrawHighlightIndicators(false)
        verify(lineDataSet).fillFormatter = any()
    }

    @Test
    fun drawBackground() {
        // execute
        fixture.drawBackground(lineDataSet, true)
        // validate
        verify(lineDataSet).isDrawFilled = true
    }
}
