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
package com.vrem.wifianalyzer

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class ActivityUtilsTest {
    private val window: Window = mock()
    private val intent: Intent = mock()
    private val intentArgumentCaptor = argumentCaptor<Intent>()
    private val mainActivity: MainActivity = mock()
    private val settings: com.vrem.wifianalyzer.settings.Settings = mock()

    @After
    fun tearDown() {
        verifyNoMoreInteractions(mainActivity)
        verifyNoMoreInteractions(window)
        verifyNoMoreInteractions(settings)
        verifyNoMoreInteractions(intent)
    }

    @Test
    fun keepScreenOnSwitchOn() {
        // setup
        doReturn(settings).whenever(mainActivity).settings
        doReturn(true).whenever(settings).keepScreenOn()
        doReturn(window).whenever(mainActivity).window
        // execute
        mainActivity.keepScreenOn()
        // validate
        verify(mainActivity).settings
        verify(settings).keepScreenOn()
        verify(mainActivity).window
        verify(window).addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @Test
    fun keepScreenOnSwitchOff() {
        // setup
        doReturn(settings).whenever(mainActivity).settings
        doReturn(false).whenever(settings).keepScreenOn()
        doReturn(window).whenever(mainActivity).window
        // execute
        mainActivity.keepScreenOn()
        // validate
        verify(mainActivity).settings
        verify(settings).keepScreenOn()
        verify(mainActivity).window
        verify(window).clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @Test
    fun startWiFiSettings() {
        // execute
        mainActivity.startWiFiSettings()
        // validate
        verify(mainActivity).startActivity(intentArgumentCaptor.capture())
        assertThat(intentArgumentCaptor.firstValue.action).isEqualTo(Settings.Panel.ACTION_WIFI)
    }

    @Test
    fun startLocationSettings() {
        // execute
        mainActivity.startLocationSettings()
        // validate
        verify(mainActivity).startActivity(intentArgumentCaptor.capture())
        assertThat(intentArgumentCaptor.firstValue.action).isEqualTo(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }
}
