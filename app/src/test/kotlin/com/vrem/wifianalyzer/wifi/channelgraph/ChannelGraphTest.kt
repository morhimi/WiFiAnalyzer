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
package com.vrem.wifianalyzer.wifi.channelgraph

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
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class ChannelGraphTest {
    private val graphWrapper: GraphWrapper = mock()
    private val dataManager: DataManager = mock()
    private val fixture: ChannelGraph = spy(ChannelGraph(WiFiBand.GHZ2, dataManager, graphWrapper))

    @After
    fun tearDown() {
        verifyNoMoreInteractions(graphWrapper)
        verifyNoMoreInteractions(dataManager)
    }

    @Test
    fun update() {
        // Arrange
        val newSeries: Set<WiFiDetail> = setOf()
        val wiFiDetails: List<WiFiDetail> = listOf()
        val wiFiData = WiFiData(wiFiDetails, WiFiConnection.EMPTY)
        val settingsData = SettingsData(wiFiBand = WiFiBand.GHZ2, graphMaximumY = MAX_Y, sortBy = SortBy.CHANNEL)
        doReturn(newSeries).whenever(dataManager).newSeries(wiFiDetails)

        // Act
        fixture.update(wiFiData, settingsData)

        // Assert
        verify(graphWrapper).show()
        verify(graphWrapper).reset()
        verify(dataManager).newSeries(wiFiDetails)
        verify(graphWrapper).removeSeries(newSeries)
        verify(dataManager).addSeriesData(graphWrapper, newSeries, MAX_Y)
    }

    @Test
    fun updateWhenNotSelected() {
        // Arrange
        val wiFiData = WiFiData(listOf(), WiFiConnection.EMPTY)
        val settingsData = SettingsData(wiFiBand = WiFiBand.GHZ5)

        // Act
        fixture.update(wiFiData, settingsData)

        // Assert
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
