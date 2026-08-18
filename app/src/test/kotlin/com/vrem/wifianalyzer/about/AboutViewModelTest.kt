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
package com.vrem.wifianalyzer.about

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import com.vrem.wifianalyzer.Configuration
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class AboutViewModelTest {
    private val wiFiManagerWrapper: WiFiManagerWrapper = mock()
    private val configuration: Configuration = mock()
    private val context: Context = mock()
    private val resources: Resources = mock()
    private val packageManager: PackageManager = mock()
    private val packageInfo =
        PackageInfo().apply {
            versionName = "1.0.0"
            @Suppress("DEPRECATION")
            versionCode = 42
        }

    private lateinit var fixture: AboutViewModel

    private fun setupMocks(largeScreen: Boolean = false) {
        whenever(context.packageName).thenReturn("com.vrem.wifianalyzer")
        whenever(context.resources).thenReturn(resources)
        whenever(context.packageManager).thenReturn(packageManager)
        @Suppress("DEPRECATION")
        whenever(packageManager.getPackageInfo("com.vrem.wifianalyzer", 0)).thenReturn(packageInfo)
        whenever(resources.getString(R.string.app_copyright)).thenReturn("Copyright (C) 2015 - ")
        whenever(wiFiManagerWrapper.isScanThrottleEnabled()).thenReturn(true)
        whenever(wiFiManagerWrapper.is5GHzBandSupported()).thenReturn(true)
        whenever(wiFiManagerWrapper.is6GHzBandSupported()).thenReturn(false)
        whenever(configuration.largeScreen).thenReturn(largeScreen)

        fixture =
            AboutViewModel(
                wiFiManagerWrapper = wiFiManagerWrapper,
                configuration = configuration,
                context = context,
            )
    }

    @After
    fun tearDown() {
        verify(context, times(2)).packageName
        verify(context).resources
        verify(context).packageManager
        @Suppress("DEPRECATION")
        verify(packageManager).getPackageInfo("com.vrem.wifianalyzer", 0)
        verify(resources).getString(R.string.app_copyright)
        verify(wiFiManagerWrapper).isScanThrottleEnabled()
        verify(wiFiManagerWrapper).is5GHzBandSupported()
        verify(wiFiManagerWrapper).is6GHzBandSupported()
        verify(configuration).largeScreen
        verifyNoMoreInteractions(wiFiManagerWrapper, configuration, context, resources, packageManager)
    }

    @Test
    fun shouldPopulateUiState() {
        setupMocks(largeScreen = false)
        val state = fixture.uiState

        assertThat(state.packageName).isEqualTo("com.vrem.wifianalyzer")
        assertThat(state.isScanThrottleEnabled).isTrue
        assertThat(state.is5GHzBandSupported).isTrue
        assertThat(state.is6GHzBandSupported).isFalse
        assertThat(state.copyright).startsWith("Copyright (C) 2015 - ")
        assertThat(state.device).isNotBlank
        assertThat(state.versionInfo).doesNotContain("L (")
    }

    @Test
    fun versionWithLargeScreen() {
        setupMocks(largeScreen = true)
        val state = fixture.uiState

        assertThat(state.versionInfo).contains("L (")
    }
}
