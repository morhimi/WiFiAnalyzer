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
package com.vrem.wifianalyzer.wifi.scanner

import com.vrem.wifianalyzer.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PeriodicScanTest {
    private val settings: Settings = mock()
    private val scanner: ScannerService = mock()

    @Test
    fun startSchedulesImmediateInitialScanAndPeriodicLoops() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val fixture = PeriodicScan(scanner, settings, this, dispatcher)
            val scanSpeed = 10
            whenever(settings.scanSpeed()).thenReturn(scanSpeed)

            // execute
            fixture.start()
            assertThat(fixture.running).isTrue

            // Advance past initial delay (1ms)
            testScheduler.advanceTimeBy(2L)
            verify(scanner, times(1)).update()

            // Advance by one interval (10s)
            testScheduler.advanceTimeBy(10_000L)
            verify(scanner, times(2)).update()

            // Advance by another interval (10s)
            testScheduler.advanceTimeBy(10_000L)
            verify(scanner, times(3)).update()

            // Stop
            fixture.stop()
            assertThat(fixture.running).isFalse

            // Advance time further, no more updates should occur
            testScheduler.advanceTimeBy(20_000L)
            verify(scanner, times(3)).update()
        }

    @Test
    fun startWithDelayWaitsIntervalBeforeFirstScan() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val fixture = PeriodicScan(scanner, settings, this, dispatcher)
            val scanSpeed = 5
            whenever(settings.scanSpeed()).thenReturn(scanSpeed)

            // execute
            fixture.startWithDelay()
            assertThat(fixture.running).isTrue

            // Advance 4.9s - no scan yet
            testScheduler.advanceTimeBy(4_900L)
            verify(scanner, never()).update()

            // Advance remaining 101ms - first scan runs
            testScheduler.advanceTimeBy(101L)
            verify(scanner, times(1)).update()

            // Next interval runs
            testScheduler.advanceTimeBy(5_000L)
            verify(scanner, times(2)).update()

            fixture.stop()
            assertThat(fixture.running).isFalse
        }

    @Test
    fun stopCancelsActiveJob() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val fixture = PeriodicScan(scanner, settings, this, dispatcher)
            val scanSpeed = 10
            whenever(settings.scanSpeed()).thenReturn(scanSpeed)

            fixture.start()
            assertThat(fixture.running).isTrue

            fixture.stop()
            assertThat(fixture.running).isFalse

            testScheduler.advanceTimeBy(20_000L)
            verify(scanner, never()).update()
        }
}
