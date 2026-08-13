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
package com.vrem.wifianalyzer.wifi.accesspoint

import android.content.Context
import android.content.SharedPreferences
import com.vrem.util.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AliasRepositoryTest {
    private val context: Context = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val editor: SharedPreferences.Editor = mock()
    private val bssid = "BSSID"
    private val alias = "ALIAS"
    private lateinit var fixture: AliasRepository

    @Before
    fun setUp() {
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(editor.remove(any())).thenReturn(editor)
        fixture = AliasRepository(context)
    }

    @Test
    fun aliasReturnsEmptyWhenNotFound() {
        whenever(sharedPreferences.getString(bssid, String.EMPTY)).thenReturn(null)
        assertThat(fixture.alias(bssid)).isEmpty()
    }

    @Test
    fun aliasReturnsValueWhenFound() {
        whenever(sharedPreferences.getString(bssid, String.EMPTY)).thenReturn(alias)
        assertThat(fixture.alias(bssid)).isEqualTo(alias)
    }

    @Test
    fun saveStoresValue() {
        fixture.save(bssid, alias)
        verify(editor).putString(bssid, alias)
        verify(editor).apply()
    }

    @Test
    fun saveRemovesValueWhenEmpty() {
        fixture.save(bssid, "")
        verify(editor).remove(bssid)
        verify(editor).apply()
    }
}
