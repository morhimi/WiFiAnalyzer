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

import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.data.LineDataSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class SeriesCacheTest {
    private val series1: LineDataSet<EntryFloat> = mock()
    private val series2: LineDataSet<EntryFloat> = mock()
    private val series3: LineDataSet<EntryFloat> = mock()
    private val wiFiDetail1 = WiFiDetail(wiFiIdentifier = WiFiIdentifier("SSID1", "BSSID1"))
    private val wiFiDetail2 = WiFiDetail(wiFiIdentifier = WiFiIdentifier("SSID2", "BSSID2"))
    private val wiFiDetail3 = WiFiDetail(wiFiIdentifier = WiFiIdentifier("SSID3", "BSSID3"))
    private val fixture = SeriesCache()

    @Test
    fun difference() {
        // setup
        fixture.put(wiFiDetail1, series1)
        fixture.put(wiFiDetail2, series2)
        fixture.put(wiFiDetail3, series3)
        // execute
        val actual = fixture.difference(setOf(wiFiDetail1))
        // validate
        assertThat(actual).containsExactlyInAnyOrder(wiFiDetail2, wiFiDetail3)
    }

    @Test
    fun remove() {
        // setup
        fixture.put(wiFiDetail1, series1)
        fixture.put(wiFiDetail2, series2)
        fixture.put(wiFiDetail3, series3)
        // execute
        val actual = fixture.remove(listOf(wiFiDetail1, wiFiDetail3))
        // validate
        assertThat(actual).containsExactlyInAnyOrder(series1, series3)
        assertThat(fixture.contains(wiFiDetail1)).isFalse
        assertThat(fixture.contains(wiFiDetail3)).isFalse
        assertThat(fixture.contains(wiFiDetail2)).isTrue
    }

    @Test
    fun find() {
        // setup
        fixture.put(wiFiDetail1, series1)
        fixture.put(wiFiDetail2, series2)
        // execute
        val actual = fixture.find(series2)
        // validate
        assertThat(actual).isEqualTo(wiFiDetail2)
    }

    @Test
    fun contains() {
        // setup
        fixture.put(wiFiDetail1, series1)
        // execute & validate
        assertThat(fixture.contains(wiFiDetail1)).isTrue
        assertThat(fixture.contains(wiFiDetail2)).isFalse
    }

    @Test
    fun get() {
        // setup
        fixture.put(wiFiDetail1, series1)
        // execute & validate
        assertThat(fixture[wiFiDetail1]).isEqualTo(series1)
    }

    @Test
    fun put() {
        // execute
        fixture.put(wiFiDetail1, series1)
        // validate
        assertThat(fixture[wiFiDetail1]).isEqualTo(series1)
    }
}
