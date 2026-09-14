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
package com.vrem.wifianalyzer.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ApplicationPermissionTest {
    private val context: Context = mock()
    private lateinit var fixture: ApplicationPermission

    @Before
    fun setUp() {
        fixture = ApplicationPermission(context)
    }

    @Test
    fun permissionsOnTiramisuIncludesNearbyWifiDevices() {
        assertThat(fixture.permissions()).containsExactly(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.NEARBY_WIFI_DEVICES,
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2])
    fun permissionsBeforeTiramisuOnlyIncludesFineLocation() {
        assertThat(fixture.permissions()).containsExactly(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    @Test
    fun grantedReturnsTrueWhenAllPermissionsGranted() {
        whenever(context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(context.checkSelfPermission(android.Manifest.permission.NEARBY_WIFI_DEVICES))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        assertThat(fixture.granted()).isTrue()
    }

    @Test
    fun grantedReturnsFalseWhenFineLocationDenied() {
        whenever(context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(context.checkSelfPermission(android.Manifest.permission.NEARBY_WIFI_DEVICES))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        assertThat(fixture.granted()).isFalse()
    }

    @Test
    fun grantedReturnsFalseWhenNearbyDevicesDenied() {
        whenever(context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(context.checkSelfPermission(android.Manifest.permission.NEARBY_WIFI_DEVICES))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        assertThat(fixture.granted()).isFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2])
    fun grantedReturnsTrueBeforeTiramisuWhenLocationGranted() {
        whenever(context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        assertThat(fixture.granted()).isTrue()
    }
}
