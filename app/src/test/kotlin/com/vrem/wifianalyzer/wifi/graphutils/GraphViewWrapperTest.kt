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
import com.vrem.wifianalyzer.SIZE_MAX
import com.vrem.wifianalyzer.SIZE_MIN
import com.vrem.wifianalyzer.MainContextHelper
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import info.appdev.charting.charts.LineChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GraphViewWrapperTest {
    private val graphView: LineChart = mock()
    private val lineData: LineData = mock()
    private val seriesCache: SeriesCache = mock()
    private val seriesOptions: SeriesOptions = mock()
    private val lineDataSet: LineDataSet<EntryFloat> = mock()
    private val wiFiDetail = WiFiDetail.EMPTY
    private val fixture =
        spy(GraphViewWrapper(graphView, GraphLegend.HIDE, ThemeStyle.DARK, seriesCache, seriesOptions))

    @Before
    fun setUp() {
        MainContextHelper.INSTANCE.aliasRepository
        whenever(graphView.data).thenReturn(lineData)
        assertThat(fixture.graphLegend).isEqualTo(GraphLegend.HIDE)
    }

    @After
    fun tearDown() {
        MainContextHelper.INSTANCE.restore()
    }

    @Test
    fun removeSeries() {
        // setup
        val newSeries: Set<WiFiDetail> = setOf()
        val difference: List<WiFiDetail> = listOf()
        val removed = listOf(lineDataSet)
        whenever(seriesCache.difference(newSeries)).thenReturn(difference)
        whenever(seriesCache.remove(difference)).thenReturn(removed)
        // execute
        fixture.removeSeries(newSeries)
        // validate
        verify(seriesCache).difference(newSeries)
        verify(seriesCache).remove(difference)
        verify(seriesOptions).removeSeriesColor(lineDataSet)
        verify(lineData).removeDataSet(lineDataSet)
        verify(graphView).notifyDataSetChanged()
        verify(graphView).invalidate()
    }

    @Test
    fun differenceSeries() {
        // setup
        val newSeries: Set<WiFiDetail> = setOf()
        val expected: List<WiFiDetail> = listOf()
        whenever(seriesCache.difference(newSeries)).thenReturn(expected)
        // execute
        val actual = fixture.differenceSeries(newSeries)
        // validate
        assertThat(actual).isEqualTo(expected)
        verify(seriesCache).difference(newSeries)
    }

    @Test
    fun addSeriesDirectly() {
        // execute
        fixture.addSeries(lineDataSet)
        // validate
        verify(lineData).addDataSet(lineDataSet)
        verify(graphView).notifyDataSetChanged()
        verify(graphView).invalidate()
    }

    @Test
    fun addSeriesWhenSeriesExistsDoesNotAddSeries() {
        // setup
        whenever(seriesCache.contains(wiFiDetail)).thenReturn(true)
        // execute
        val actual = fixture.addSeries(wiFiDetail, lineDataSet, false)
        // validate
        assertThat(actual).isFalse
        verify(seriesCache).contains(wiFiDetail)
        verify(seriesCache, never()).put(wiFiDetail, lineDataSet)
    }

    @Test
    fun addSeriesAddsSeries() {
        // setup
        val expectedLabel = wiFiDetail.wiFiIdentifier.ssid + " " + wiFiDetail.wiFiSignal.channelDisplay() + "\n(" + wiFiDetail.wiFiIdentifier.bssid + ")"
        val connected = wiFiDetail.wiFiAdditional.wiFiConnection.connected
        whenever(seriesCache.contains(wiFiDetail)).thenReturn(false)
        // execute
        val actual = fixture.addSeries(wiFiDetail, lineDataSet, true)
        // validate
        assertThat(actual).isTrue
        verify(seriesCache).contains(wiFiDetail)
        verify(seriesCache).put(wiFiDetail, lineDataSet)
        verify(lineDataSet).label = expectedLabel
        verify(seriesOptions).highlightConnected(lineDataSet, connected)
        verify(seriesOptions).setSeriesColor(lineDataSet)
        verify(seriesOptions).drawBackground(lineDataSet, true)
        verify(lineData).addDataSet(lineDataSet)
        verify(graphView).notifyDataSetChanged()
        verify(graphView).invalidate()
    }

    @Test
    fun updateSeriesWhenSeriesDoesNotExistsDoesNotUpdateSeries() {
        // setup
        val dataPoints = arrayOf(GraphDataPoint(1, 2))
        whenever(seriesCache.contains(wiFiDetail)).thenReturn(false)
        // execute
        val actual = fixture.updateSeries(wiFiDetail, dataPoints, true)
        // validate
        assertThat(actual).isFalse
        verify(seriesCache).contains(wiFiDetail)
        verify(seriesCache, never())[wiFiDetail]
    }

    @Test
    fun updateSeriesWhenSeriesDoesExists() {
        // setup
        val dataPoint = GraphDataPoint(1, 2)
        val dataPoints = arrayOf(dataPoint)
        val expectedLabel = wiFiDetail.wiFiIdentifier.ssid + " " + wiFiDetail.wiFiSignal.channelDisplay() + "\n(" + wiFiDetail.wiFiIdentifier.bssid + ")"
        val connected = wiFiDetail.wiFiAdditional.wiFiConnection.connected
        whenever(seriesCache.contains(wiFiDetail)).thenReturn(true)
        whenever(seriesCache[wiFiDetail]).thenReturn(lineDataSet)
        // execute
        val actual = fixture.updateSeries(wiFiDetail, dataPoints, true)
        // validate
        assertThat(actual).isTrue
        verify(seriesCache).contains(wiFiDetail)
        verify(seriesCache)[wiFiDetail]
        verify(lineDataSet).clear()
        verify(lineDataSet).addEntry(any())
        verify(lineDataSet).label = expectedLabel
        verify(seriesOptions).highlightConnected(lineDataSet, connected)
        verify(seriesOptions).drawBackground(lineDataSet, true)
        verify(graphView).notifyDataSetChanged()
        verify(graphView).invalidate()
    }

    @Test
    fun appendSeriesWhenSeriesDoesExists() {
        // setup
        val dataPoint = GraphDataPoint(1, 2)
        val count = 10
        val connected = wiFiDetail.wiFiAdditional.wiFiConnection.connected
        whenever(seriesCache.contains(wiFiDetail)).thenReturn(true)
        whenever(seriesCache[wiFiDetail]).thenReturn(lineDataSet)
        // execute
        val actual = fixture.appendToSeries(wiFiDetail, dataPoint, count, true)
        // validate
        assertThat(actual).isTrue
        verify(seriesCache).contains(wiFiDetail)
        verify(seriesCache)[wiFiDetail]
        verify(lineDataSet).addEntry(any())
        verify(seriesOptions).highlightConnected(lineDataSet, connected)
        verify(seriesOptions).drawBackground(lineDataSet, true)
        verify(graphView).notifyDataSetChanged()
        verify(graphView).invalidate()
    }

    @Test
    fun updateLegend() {
        // setup
        val legend: Legend = mock()
        whenever(graphView.legend).thenReturn(legend)
        // execute
        fixture.updateLegend(GraphLegend.RIGHT)
        // validate
        assertThat(fixture.graphLegend).isEqualTo(GraphLegend.RIGHT)
        verify(graphView).invalidate()
    }

    @Test
    fun setVisibility() {
        // execute
        fixture.visibility(View.VISIBLE)
        // validate
        verify(graphView).visibility = View.VISIBLE
    }

    @Test
    fun setHorizontalLabelsVisible() {
        // setup
        val xAxis: XAxis = mock()
        whenever(graphView.xAxis).thenReturn(xAxis)
        // execute
        fixture.setHorizontalLabelsVisible(true)
        // validate
        verify(xAxis).setDrawLabels(true)
    }

    @Test
    fun calculateGraphType() {
        // execute \u0026 validate
        assertThat(fixture.calculateGraphType()).isNotZero()
    }

    @Test
    fun setViewport() {
        // setup
        val xAxis: XAxis = mock()
        whenever(graphView.xAxis).thenReturn(xAxis)
        whenever(xAxis.labelCount).thenReturn(11)
        // execute
        fixture.setViewport()
        // validate
        verify(graphView).setVisibleXRangeMaximum(10f)
        verify(graphView).moveViewToX(0f)
    }

    @Test
    fun getSize() {
        // execute \u0026 validate
        assertThat(fixture.size(TYPE1)).isEqualTo(SIZE_MAX)
        assertThat(fixture.size(TYPE2)).isEqualTo(SIZE_MAX)
        assertThat(fixture.size(TYPE3)).isEqualTo(SIZE_MAX)
        assertThat(fixture.size(TYPE4)).isEqualTo(SIZE_MIN)
    }

    @Test
    fun setViewportSetsMinAndMaxX() {
        // setup
        val xAxis: XAxis = mock()
        whenever(graphView.xAxis).thenReturn(xAxis)
        // execute
        fixture.setViewport(1, 10)
        // validate
        verify(xAxis).axisMinimum = 1f
        verify(xAxis).axisMaximum = 10f
    }
}
