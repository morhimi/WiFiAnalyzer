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

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [android.os.Build.VERSION_CODES.BAKLAVA])
class ShizukuRunnerTest {
    private var mockAvailable: Boolean = false
    private var mockPermission: Int = PackageManager.PERMISSION_DENIED
    private var requestedPermissionCode: Int? = null
    private var capturedArgs: Array<String>? = null
    private var mockProcess: Process? = null
    private var mockProcessThrows: Boolean = false
    private lateinit var fixture: DefaultShizukuRunner

    @Before
    fun setUp() {
        mockAvailable = false
        mockPermission = PackageManager.PERMISSION_DENIED
        requestedPermissionCode = null
        capturedArgs = null
        mockProcess = mock(Process::class.java)
        mockProcessThrows = false
        fixture =
            DefaultShizukuRunner(
                binderPinger = { mockAvailable },
                permissionChecker = { mockPermission },
                permissionRequester = { code -> requestedPermissionCode = code },
                processLauncher = { args ->
                    capturedArgs = args
                    if (mockProcessThrows) throw RuntimeException("Process failed")
                    mockProcess
                },
            )
    }

    @Test
    fun isAvailableReturnsFalseWhenBinderPingerReturnsFalse() {
        mockAvailable = false

        assertThat(fixture.isAvailable()).isFalse()
    }

    @Test
    fun isAvailableReturnsTrueWhenBinderPingerReturnsTrue() {
        mockAvailable = true

        assertThat(fixture.isAvailable()).isTrue()
    }

    @Test
    fun hasPermissionReturnsFalseWhenNotAvailable() {
        mockAvailable = false
        mockPermission = PackageManager.PERMISSION_GRANTED

        assertThat(fixture.hasPermission()).isFalse()
    }

    @Test
    fun hasPermissionReturnsFalseWhenPermissionDenied() {
        mockAvailable = true
        mockPermission = PackageManager.PERMISSION_DENIED

        assertThat(fixture.hasPermission()).isFalse()
    }

    @Test
    fun hasPermissionReturnsTrueWhenPermissionGranted() {
        mockAvailable = true
        mockPermission = PackageManager.PERMISSION_GRANTED

        assertThat(fixture.hasPermission()).isTrue()
    }

    @Test
    fun requestPermissionDoesNothingWhenNotAvailable() {
        mockAvailable = false
        mockPermission = PackageManager.PERMISSION_DENIED

        fixture.requestPermission(123)

        assertThat(requestedPermissionCode).isNull()
    }

    @Test
    fun requestPermissionDoesNothingWhenAlreadyHasPermission() {
        mockAvailable = true
        mockPermission = PackageManager.PERMISSION_GRANTED

        fixture.requestPermission(123)

        assertThat(requestedPermissionCode).isNull()
    }

    @Test
    fun requestPermissionCallsRequesterWhenAvailableAndNoPermission() {
        mockAvailable = true
        mockPermission = PackageManager.PERMISSION_DENIED

        fixture.requestPermission(123)

        assertThat(requestedPermissionCode).isEqualTo(123)
    }

    @Test
    fun executeReturnsFalseWhenNoPermission() {
        mockAvailable = false

        assertThat(fixture.execute("cmd")).isFalse()
    }

    @Test
    fun executeReturnsTrueWhenProcessExitsZero() {
        mockAvailable = true
        mockPermission = PackageManager.PERMISSION_GRANTED
        whenever(mockProcess!!.waitFor()).thenReturn(0)

        assertThat(fixture.execute("settings put global test 0")).isTrue()
        assertThat(capturedArgs).containsExactly("sh", "-c", "settings put global test 0")
    }

    @Test
    fun executeReturnsFalseWhenProcessExitsNonZero() {
        mockAvailable = true
        mockPermission = PackageManager.PERMISSION_GRANTED
        whenever(mockProcess!!.waitFor()).thenReturn(1)

        assertThat(fixture.execute("settings put global test 0")).isFalse()
    }

    @Test
    fun executeReturnsFalseWhenProcessLauncherReturnsNull() {
        mockAvailable = true
        mockPermission = PackageManager.PERMISSION_GRANTED
        mockProcess = null

        assertThat(fixture.execute("settings put global test 0")).isFalse()
    }

    @Test
    fun executeReturnsFalseWhenProcessLauncherThrows() {
        mockAvailable = true
        mockPermission = PackageManager.PERMISSION_GRANTED
        mockProcessThrows = true

        assertThat(fixture.execute("settings put global test 0")).isFalse()
    }

    @Test
    fun defaultConstructorInitializesAndGracefullyHandlesUnavailable() {
        val runner = DefaultShizukuRunner()
        assertThat(runner.isAvailable()).isFalse()
        assertThat(runner.hasPermission()).isFalse()
        runner.requestPermission(123)
        assertThat(runner.execute("settings put global test 0")).isFalse()
    }

    @Test
    fun defaultDelegatesRunSafelyWhenShizukuNotRunning() {
        assertThat(defaultBinderPinger()).isFalse()
        assertThat(defaultPermissionChecker()).isEqualTo(PackageManager.PERMISSION_DENIED)
        defaultPermissionRequester(123)
        assertThat(defaultProcessLauncher(arrayOf("cmd"))).isNull()
    }
}
