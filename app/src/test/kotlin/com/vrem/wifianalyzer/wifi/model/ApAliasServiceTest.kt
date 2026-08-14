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
package com.vrem.wifianalyzer.wifi.model

import com.vrem.util.EMPTY
import com.vrem.wifianalyzer.settings.Repository
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class ApAliasServiceTest {
    @Mock
    private lateinit var repository: Repository

    @InjectMocks
    private lateinit var fixture: ApAliasService

    private val bssid = "00:11:22:33:44:55"
    private val key = "ap_alias_00:11:22:33:44:55"
    private val alias = "Living Room"

    @After
    fun tearDown() {
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun getAliasReturnsAliasFromRepository() {
        whenever(repository.string(key, String.EMPTY)).thenReturn(alias)

        val result = fixture.getAlias(bssid)

        assertThat(result).isEqualTo(alias)
        verify(repository).string(key, String.EMPTY)
    }

    @Test
    fun getAliasWithBlankBssidReturnsEmpty() {
        val result = fixture.getAlias("   ")

        assertThat(result).isEqualTo(String.EMPTY)
    }

    @Test
    fun saveAliasSavesTrimmedAlias() {
        fixture.saveAlias(bssid, "  $alias  ")

        verify(repository).save(key, alias)
    }

    @Test
    fun saveAliasRemovesAliasWhenEmpty() {
        fixture.saveAlias(bssid, "   ")

        verify(repository).remove(key)
    }

    @Test
    fun removeAliasRemovesFromRepository() {
        fixture.removeAlias(bssid)

        verify(repository).remove(key)
    }
}
