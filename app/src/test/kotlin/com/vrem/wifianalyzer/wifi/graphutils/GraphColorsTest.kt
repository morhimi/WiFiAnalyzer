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

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.R
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class GraphColorsTest {
    private val resources: Resources = mock()
    private val context: Context = mock()
    private lateinit var fixture: GraphColors

    @Before
    fun setUp() {
        whenever(context.resources).thenReturn(resources)
        whenever(resources.getStringArray(R.array.graph_colors)).thenReturn(withColors())
        fixture = GraphColors(context)
    }

    @After
    fun tearDown() {
        verify(context).resources
        verify(resources).getStringArray(R.array.graph_colors)
        verifyNoMoreInteractions(context)
        verifyNoMoreInteractions(resources)
    }

    @Test
    fun getColorStartsOverWhenEndIsReached() {
        // setup
        val graphColors = withGraphColors()
        // validate & execute
        assertThat(fixture.graphColor()).isEqualTo(graphColors[2])
        assertThat(fixture.graphColor()).isEqualTo(graphColors[1])
        assertThat(fixture.graphColor()).isEqualTo(graphColors[0])
        assertThat(fixture.graphColor()).isEqualTo(graphColors[2])
        assertThat(fixture.graphColor()).isEqualTo(graphColors[1])
        assertThat(fixture.graphColor()).isEqualTo(graphColors[0])
    }

    @Test
    fun addColorAddsNewColor() {
        // setup
        val graphColors = withGraphColors()
        val expected = graphColors[1]
        // execute
        fixture.addColor(expected.primary)
        // validate
        assertThat(fixture.graphColor()).isEqualTo(expected)
        assertThat(fixture.graphColor()).isEqualTo(graphColors[2])
    }

    @Test
    fun addColorDoesNotAddColorIfItDoesNotExist() {
        // setup
        val graphColors = withGraphColors()
        val expected = graphColors[2]
        // execute
        fixture.addColor(0x000000)
        // validate
        assertThat(fixture.graphColor()).isEqualTo(expected)
        assertThat(fixture.graphColor()).isEqualTo(graphColors[1])
    }

    @Test
    fun addColorDoesNotAddColorIfItAlreadyExists() {
        // setup
        val graphColors = withGraphColors()
        val expected = graphColors[1]
        val original = fixture.graphColor()
        // execute
        fixture.addColor(expected.primary)
        // validate
        assertThat(fixture.graphColor()).isEqualTo(expected)
        assertThat(fixture.graphColor()).isEqualTo(graphColors[0])
    }

    @Test
    fun nullContextReturnsEmptyAndZeroConnectedColor() {
        val nullContextColors = GraphColors(null)
        assertThat(nullContextColors.connectedColor).isEqualTo(GraphColor(0, 0))
        assertThat(fixture.graphColor()).isNotNull()
    }

    @Test
    fun connectedColorWithRealContext() {
        val realContext = ApplicationProvider.getApplicationContext<Context>()
        val realColors = GraphColors(realContext)
        assertThat(realColors.connectedColor).isNotEqualTo(GraphColor(0, 0))
        assertThat(fixture.graphColor()).isNotNull()
    }

    private fun withColors(): Array<String> =
        arrayOf("#FB1554", "#33FB1554", "#74FF89", "#3374FF89", "#8B1EFC", "#338B1EFC")

    private fun withGraphColors(): Array<GraphColor> =
        arrayOf(
            GraphColor(0xFB1554, 0x33FB1554),
            GraphColor(0x74FF89, 0x3374FF89),
            GraphColor(0x8B1EFC, 0x338B1EFC),
        )
}
