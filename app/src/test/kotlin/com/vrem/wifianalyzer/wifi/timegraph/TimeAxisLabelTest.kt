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

import com.vrem.util.EMPTY
import com.vrem.wifianalyzer.wifi.graphutils.MAX_Y
import com.vrem.wifianalyzer.wifi.graphutils.MIN_Y
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TimeAxisLabelTest {
    private val fixture = TimeAxisLabel()

    @Test
    fun yAxis() {
        assertThat(fixture.getFormattedValue(MIN_Y.toFloat(), null)).isEqualTo(String.EMPTY)
        assertThat(fixture.getFormattedValue((MIN_Y + 1).toFloat(), null)).isEqualTo("-99")
        assertThat(fixture.getFormattedValue(MAX_Y.toFloat(), null)).isEqualTo("0")
        assertThat(fixture.getFormattedValue((MAX_Y + 1).toFloat(), null)).isEqualTo(String.EMPTY)
    }

    @Test
    fun xAxis() {
        val xAxis = info.appdev.charting.components.XAxis()
        assertThat(fixture.getFormattedValue(-2.0f, xAxis)).isEqualTo(String.EMPTY)
        assertThat(fixture.getFormattedValue(-1.0f, xAxis)).isEqualTo(String.EMPTY)
        assertThat(fixture.getFormattedValue(0.0f, xAxis)).isEqualTo(String.EMPTY)
        assertThat(fixture.getFormattedValue(1.0f, xAxis)).isEqualTo(String.EMPTY)
        assertThat(fixture.getFormattedValue(2.0f, xAxis)).isEqualTo("2")
        assertThat(fixture.getFormattedValue(10.0f, xAxis)).isEqualTo("10")
    }
}
