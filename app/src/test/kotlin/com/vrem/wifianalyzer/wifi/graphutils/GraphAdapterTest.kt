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
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.model.WiFiData
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class GraphAdapterTest {
    private val graphNotifier1: GraphNotifier = mock()
    private val graphNotifier2: GraphNotifier = mock()
    private val view1: View = mock()
    private val view2: View = mock()
    private val wiFiData: WiFiData = mock()
    private val settingsData: SettingsData = mock()

    private lateinit var fixture: GraphAdapter

    @Before
    fun setUp() {
        whenever(graphNotifier1.graph()).thenReturn(view1)
        whenever(graphNotifier2.graph()).thenReturn(view2)
        fixture = GraphAdapter(listOf(graphNotifier1, graphNotifier2))
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(graphNotifier1, graphNotifier2, view1, view2, wiFiData, settingsData)
    }

    @Test
    fun graphsReturnsViewsFromNotifiers() {
        val actual = fixture.graphs()

        assertThat(actual).containsExactly(view1, view2)
        verify(graphNotifier1).graph()
        verify(graphNotifier2).graph()
    }

    @Test
    fun updateNotifiesAllNotifiers() {
        fixture.update(wiFiData, settingsData)

        verify(graphNotifier1).update(wiFiData, settingsData)
        verify(graphNotifier2).update(wiFiData, settingsData)
    }

    @Test
    fun destroyDestroysAllNotifiers() {
        fixture.destroy()

        verify(graphNotifier1).destroy()
        verify(graphNotifier2).destroy()
    }
}
