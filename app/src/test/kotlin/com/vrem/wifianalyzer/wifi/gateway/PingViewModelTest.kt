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
package com.vrem.wifianalyzer.wifi.gateway

import kotlinx.coroutines.flow.MutableStateFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class PingViewModelTest {
    private val pingService: PingService = mock()
    private val pingStateFlow = MutableStateFlow(PingUiState.INITIAL)

    private lateinit var fixture: PingViewModel

    @Before
    fun setUp() {
        whenever(pingService.pingState).thenReturn(pingStateFlow)
        fixture = PingViewModel(pingService)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(pingService)
    }

    @Test
    fun initCallsStart() {
        assertThat(fixture.uiState.value).isEqualTo(PingUiState.INITIAL)
        verify(pingService).pingState
        verify(pingService).start(any(), any())
    }

    @Test
    fun pauseCallsPingServicePause() {
        fixture.pause()
        verify(pingService).pingState
        verify(pingService).start(any(), any())
        verify(pingService).pause()
    }

    @Test
    fun resetCallsPingServiceReset() {
        fixture.reset()
        verify(pingService).pingState
        verify(pingService).start(any(), any())
        verify(pingService).reset()
    }

    @Test
    fun toggleWhenRunningCallsPause() {
        whenever(pingService.isRunning()).thenReturn(true)
        fixture.toggle()

        verify(pingService).pingState
        verify(pingService).start(any(), any())
        verify(pingService).isRunning()
        verify(pingService).pause()
    }

    @Test
    fun toggleWhenNotRunningCallsStart() {
        whenever(pingService.isRunning()).thenReturn(false)
        fixture.toggle()

        verify(pingService).pingState
        verify(pingService).isRunning()
        verify(pingService, org.mockito.kotlin.times(2)).start(any(), any())
    }

    @Test
    fun onClearedCallsPause() {
        val method = androidx.lifecycle.ViewModel::class.java.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(fixture)

        verify(pingService).pingState
        verify(pingService).start(any(), any())
        verify(pingService).pause()
    }
}
