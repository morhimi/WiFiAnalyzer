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

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class WiFiConnectionCallbackTest {
    private val connectivityManager: ConnectivityManager = mock()
    private val callback: () -> Unit = mock()
    private val network: Network = mock()
    private val networkCapabilities: NetworkCapabilities = mock()
    private val linkProperties: LinkProperties = mock()
    private val fixture = WiFiConnectionCallback(connectivityManager, callback)

    @After
    fun tearDown() {
        verifyNoMoreInteractions(connectivityManager)
        verifyNoMoreInteractions(callback)
    }

    @Test
    fun registerRegistersCallbackOnce() {
        // execute
        fixture.register()
        fixture.register()

        // validate
        assertThat(fixture.registered).isTrue
        verify(connectivityManager, times(1)).registerNetworkCallback(any(), eq(fixture))
    }

    @Test
    fun registerWhenExceptionThrownCatchesAndRemainsUnregistered() {
        // setup
        whenever(connectivityManager.registerNetworkCallback(any(), eq(fixture))).thenThrow(SecurityException())

        // execute
        fixture.register()

        // validate
        assertThat(fixture.registered).isFalse
        verify(connectivityManager).registerNetworkCallback(any(), eq(fixture))
    }

    @Test
    fun unregisterWhenExceptionThrownCatchesAndMarksUnregistered() {
        // setup
        fixture.register()
        whenever(connectivityManager.unregisterNetworkCallback(fixture)).thenThrow(IllegalArgumentException())

        // execute
        fixture.unregister()

        // validate
        assertThat(fixture.registered).isFalse
        verify(connectivityManager).registerNetworkCallback(any(), eq(fixture))
        verify(connectivityManager).unregisterNetworkCallback(fixture)
    }

    @Test
    fun unregisterUnregistersCallbackOnce() {
        // setup
        fixture.register()

        // execute
        fixture.unregister()
        fixture.unregister()

        // validate
        assertThat(fixture.registered).isFalse
        verify(connectivityManager).registerNetworkCallback(any(), eq(fixture))
        verify(connectivityManager, times(1)).unregisterNetworkCallback(fixture)
    }

    @Test
    fun unregisterWhenNotRegisteredDoesNothing() {
        // execute
        fixture.unregister()

        // validate
        assertThat(fixture.registered).isFalse
    }

    @Test
    fun onAvailableTriggersCallback() {
        // execute
        fixture.onAvailable(network)

        // validate
        verify(callback).invoke()
    }

    @Test
    fun onCapabilitiesChangedTriggersCallback() {
        // execute
        fixture.onCapabilitiesChanged(network, networkCapabilities)

        // validate
        verify(callback).invoke()
    }

    @Test
    fun onLinkPropertiesChangedTriggersCallback() {
        // execute
        fixture.onLinkPropertiesChanged(network, linkProperties)

        // validate
        verify(callback).invoke()
    }

    @Test
    fun onLostTriggersCallback() {
        // execute
        fixture.onLost(network)

        // validate
        verify(callback).invoke()
    }
}
