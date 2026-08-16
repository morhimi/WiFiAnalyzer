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
import com.vrem.wifianalyzer.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ApAliasServiceTest {
    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fixture: ApAliasService

    private val bssid = "00:11:22:33:44:55"
    private val key = "00:11:22:33:44:55"
    private val alias = "Living Room"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fixture = ApAliasService(settingsRepository)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(settingsRepository)
        Dispatchers.resetMain()
    }

    @Test
    fun getAliasWithBlankBssidReturnsEmpty() {
        val result = fixture.getAlias("   ")

        assertThat(result).isEqualTo(String.EMPTY)
    }

    @Test
    fun saveAliasAndGetAlias() =
        runTest(testDispatcher) {
            fixture.saveAlias(bssid, "  $alias  ")
            testScheduler.advanceUntilIdle()

            val result = fixture.getAlias(bssid)

            assertThat(result).isEqualTo(alias)
            verify(settingsRepository).saveAlias(key, alias)
        }

    @Test
    fun saveAliasRemovesAliasWhenEmpty() =
        runTest(testDispatcher) {
            fixture.saveAlias(bssid, "   ")
            testScheduler.advanceUntilIdle()

            verify(settingsRepository).saveAlias(key, "")
        }

    @Test
    fun removeAliasRemovesFromRepository() =
        runTest(testDispatcher) {
            fixture.removeAlias(bssid)
            testScheduler.advanceUntilIdle()

            verify(settingsRepository).saveAlias(key, "")
        }
}
