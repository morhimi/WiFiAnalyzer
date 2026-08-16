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
package com.vrem.wifianalyzer.wifi.timegraph

import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.graphutils.GraphWrapper
import com.vrem.wifianalyzer.wifi.graphutils.MAX_Y
import com.vrem.wifianalyzer.wifi.model.SortBy
import com.vrem.wifianalyzer.wifi.model.WiFiConnection
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class TimeGraphTest {
    private val dataManager: DataManager = mock()
    private val graphWrapper: GraphWrapper = mock()
    private val fixture: TimeGraph = spy(TimeGraph(WiFiBand.GHZ2, dataManager, graphWrapper))

    @After
    fun tearDown() {
        verifyNoMoreInteractions(dataManager)
        verifyNoMoreInteractions(graphWrapper)
    }

    @Test
    fun update() {
        // Arrange
        val wiFiDetails: List<WiFiDetail> = listOf()
        val newSeries: Set<WiFiDetail> = setOf()
        val wiFiData = WiFiData(wiFiDetails, WiFiConnection.EMPTY)
        val settingsData = SettingsData(wiFiBand = WiFiBand.GHZ2, graphMaximumY = MAX_Y, sortBy = SortBy.SSID)
        doReturn(newSeries).whenever(dataManager).addSeriesData(graphWrapper, wiFiDetails, MAX_Y)

        // Act
        fixture.update(wiFiData, settingsData)

        // Assert
        verify(dataManager).reset(graphWrapper)
        verify(dataManager).addSeriesData(graphWrapper, wiFiDetails, MAX_Y)
        verify(graphWrapper).removeSeries(newSeries)
        verify(graphWrapper).show()
    }

    @Test
    fun updateDoesNotResetWhenBandNotChanged() {
        // Arrange
        val wiFiDetails: List<WiFiDetail> = listOf()
        val newSeries: Set<WiFiDetail> = setOf()
        val wiFiData = WiFiData(wiFiDetails, WiFiConnection.EMPTY)
        val settingsData = SettingsData(wiFiBand = WiFiBand.GHZ2, graphMaximumY = MAX_Y, sortBy = SortBy.SSID)
        doReturn(newSeries).whenever(dataManager).addSeriesData(graphWrapper, wiFiDetails, MAX_Y)

        fixture.update(wiFiData, settingsData)
        // Act
        fixture.update(wiFiData, settingsData)

        // Assert
        verify(dataManager).reset(graphWrapper)
        verify(dataManager, times(2)).addSeriesData(graphWrapper, wiFiDetails, MAX_Y)
        verify(graphWrapper, times(2)).removeSeries(newSeries)
        verify(graphWrapper, times(2)).show()
    }

    @Test
    fun updateResetsWhenBandSwitchedBack() {
        // Arrange
        val wiFiDetails: List<WiFiDetail> = listOf()
        val newSeries: Set<WiFiDetail> = setOf()
        val wiFiData = WiFiData(wiFiDetails, WiFiConnection.EMPTY)
        val settingsDataGhz2 = SettingsData(wiFiBand = WiFiBand.GHZ2, graphMaximumY = MAX_Y, sortBy = SortBy.SSID)
        val settingsDataGhz5 = SettingsData(wiFiBand = WiFiBand.GHZ5, graphMaximumY = MAX_Y, sortBy = SortBy.SSID)
        doReturn(newSeries).whenever(dataManager).addSeriesData(graphWrapper, wiFiDetails, MAX_Y)

        // first call - selected
        fixture.update(wiFiData, settingsDataGhz2)
        // second call - not selected
        fixture.update(wiFiData, settingsDataGhz5)
        // third call - selected again, should reset
        fixture.update(wiFiData, settingsDataGhz2)

        // Assert
        verify(dataManager, times(2)).reset(graphWrapper)
        verify(dataManager, times(2)).addSeriesData(graphWrapper, wiFiDetails, MAX_Y)
        verify(graphWrapper, times(2)).removeSeries(newSeries)
        verify(graphWrapper, times(2)).show()
        verify(graphWrapper).gone()
    }

    @Test
    fun graph() {
        // Arrange
        val expected: CartesianChartView = mock()
        doReturn(expected).whenever(graphWrapper).chartView

        // Act
        val actual = fixture.graph()

        // Assert
        assertThat(actual).isEqualTo(expected)
        verify(graphWrapper).chartView
        verifyNoMoreInteractions(expected)
    }

    @Test
    fun destroy() {
        // Act
        fixture.destroy()

        // Assert
        verify(graphWrapper).destroy()
    }

    @Test
    fun predicate() {
        // Arrange
        val settingsData = SettingsData(wiFiBand = WiFiBand.GHZ2)

        // Act
        val actual = fixture.predicate(settingsData)

        // Assert
        assertThat(actual).isNotNull()
    }
}
