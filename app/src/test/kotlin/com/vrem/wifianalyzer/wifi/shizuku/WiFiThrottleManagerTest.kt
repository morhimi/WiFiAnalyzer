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
package com.vrem.wifianalyzer.wifi.shizuku

import android.content.ContentResolver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.settings.Settings
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [android.os.Build.VERSION_CODES.BAKLAVA])
class WiFiThrottleManagerTest {
    private val settings: Settings = mock()
    private val shizukuRunner: ShizukuRunner = mock()
    private lateinit var contentResolver: ContentResolver
    private var mockSystemThrottleEnabled: Boolean = true
    private lateinit var fixture: WiFiThrottleManager

    @Before
    fun setUp() {
        contentResolver = ApplicationProvider.getApplicationContext<android.content.Context>().contentResolver
        fixture =
            WiFiThrottleManager(
                settings = settings,
                shizukuRunner = shizukuRunner,
                contentResolver = contentResolver,
                systemThrottleReader = { mockSystemThrottleEnabled },
            )
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(settings)
        verifyNoMoreInteractions(shizukuRunner)
    }

    @Test
    fun onAppStartWhenSettingDisabledDoesNothing() {
        whenever(settings.shizukuThrottle()).thenReturn(false)

        fixture.onAppStart()

        assertThat(fixture.wasThrottlingOriginallyEnabled).isNull()
        assertThat(fixture.throttledByApp).isFalse()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner, never()).isAvailable()
    }

    @Test
    fun onAppStartWhenShizukuNotAvailableDoesNothing() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(false)

        fixture.onAppStart()

        assertThat(fixture.wasThrottlingOriginallyEnabled).isNull()
        assertThat(fixture.throttledByApp).isFalse()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner, never()).hasPermission()
    }

    @Test
    fun onAppStartWhenShizukuNoPermissionDoesNothing() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(false)

        fixture.onAppStart()

        assertThat(fixture.wasThrottlingOriginallyEnabled).isNull()
        assertThat(fixture.throttledByApp).isFalse()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner, never()).execute(any())
    }

    @Test
    fun onAppStartWhenOriginallyEnabledDisablesThrottling() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(true)
        mockSystemThrottleEnabled = true

        fixture.onAppStart()

        assertThat(fixture.wasThrottlingOriginallyEnabled).isTrue()
        assertThat(fixture.throttledByApp).isTrue()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner).execute(CMD_DISABLE_THROTTLE)
    }

    @Test
    fun onAppStartWhenAlreadyThrottledByAppDoesNothing() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(true)
        mockSystemThrottleEnabled = true

        fixture.onAppStart()
        fixture.onAppStart()

        assertThat(fixture.throttledByApp).isTrue()
        verify(settings, times(2)).shizukuThrottle()
        verify(shizukuRunner, times(2)).isAvailable()
        verify(shizukuRunner, times(2)).hasPermission()
        verify(shizukuRunner, times(1)).execute(CMD_DISABLE_THROTTLE)
    }

    @Test
    fun onAppStartWhenOriginallyEnabledAndExecuteFailsDoesNotSetThrottledByApp() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(false)
        mockSystemThrottleEnabled = true

        fixture.onAppStart()

        assertThat(fixture.wasThrottlingOriginallyEnabled).isTrue()
        assertThat(fixture.throttledByApp).isFalse()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner).execute(CMD_DISABLE_THROTTLE)
    }

    @Test
    fun onAppStartWhenOriginallyDisabledInDevOptionsDoesNotTouchSetting() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        mockSystemThrottleEnabled = false

        fixture.onAppStart()

        assertThat(fixture.wasThrottlingOriginallyEnabled).isFalse()
        assertThat(fixture.throttledByApp).isFalse()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner, never()).execute(any())
    }

    @Test
    fun onAppStopWhenThrottledByAppRestoresThrottling() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_ENABLE_THROTTLE)).thenReturn(true)
        mockSystemThrottleEnabled = true

        fixture.onAppStart()
        fixture.onAppStop()

        assertThat(fixture.throttledByApp).isFalse()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner).execute(CMD_DISABLE_THROTTLE)
        verify(shizukuRunner).execute(CMD_ENABLE_THROTTLE)
    }

    @Test
    fun onAppStopWhenNotThrottledByAppDoesNothing() {
        fixture.onAppStop()

        assertThat(fixture.throttledByApp).isFalse()
        verify(shizukuRunner, never()).execute(any())
    }

    @Test
    fun onAppStopWhenRestoreFailsKeepsThrottledByApp() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_ENABLE_THROTTLE)).thenReturn(false)
        mockSystemThrottleEnabled = true

        fixture.onAppStart()
        fixture.onAppStop()

        assertThat(fixture.throttledByApp).isTrue()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner).execute(CMD_DISABLE_THROTTLE)
        verify(shizukuRunner).execute(CMD_ENABLE_THROTTLE)
    }

    @Test
    fun onAppExitWhenThrottledByAppRestoresThrottlingAndResetsOriginalState() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_ENABLE_THROTTLE)).thenReturn(true)
        mockSystemThrottleEnabled = true

        fixture.onAppStart()
        fixture.onAppExit()

        assertThat(fixture.throttledByApp).isFalse()
        assertThat(fixture.wasThrottlingOriginallyEnabled).isNull()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner).execute(CMD_DISABLE_THROTTLE)
        verify(shizukuRunner).execute(CMD_ENABLE_THROTTLE)
    }

    @Test
    fun onSettingChangedTrueAppliesThrottling() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(true)
        mockSystemThrottleEnabled = true

        fixture.onSettingChanged(true)

        assertThat(fixture.throttledByApp).isTrue()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner).execute(CMD_DISABLE_THROTTLE)
    }

    @Test
    fun onSettingChangedFalseRestoresThrottlingAndResetsOriginalState() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_ENABLE_THROTTLE)).thenReturn(true)
        mockSystemThrottleEnabled = true

        fixture.onAppStart()
        fixture.onSettingChanged(false)

        assertThat(fixture.throttledByApp).isFalse()
        assertThat(fixture.wasThrottlingOriginallyEnabled).isNull()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner).execute(CMD_DISABLE_THROTTLE)
        verify(shizukuRunner).execute(CMD_ENABLE_THROTTLE)
    }

    @Test
    fun defaultConstructorInitializesWithDefaultSystemThrottleReader() {
        whenever(settings.shizukuThrottle()).thenReturn(true)
        whenever(shizukuRunner.isAvailable()).thenReturn(true)
        whenever(shizukuRunner.hasPermission()).thenReturn(true)
        whenever(shizukuRunner.execute(CMD_DISABLE_THROTTLE)).thenReturn(true)

        val manager = WiFiThrottleManager(settings, shizukuRunner, contentResolver)
        assertThat(manager.wasThrottlingOriginallyEnabled).isNull()
        assertThat(manager.throttledByApp).isFalse()

        manager.onAppStart()

        assertThat(manager.wasThrottlingOriginallyEnabled).isTrue()
        assertThat(manager.throttledByApp).isTrue()
        verify(settings).shizukuThrottle()
        verify(shizukuRunner).isAvailable()
        verify(shizukuRunner).hasPermission()
        verify(shizukuRunner).execute(CMD_DISABLE_THROTTLE)
    }
}
