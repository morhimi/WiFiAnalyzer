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
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModel
import com.vrem.util.packageInfo
import com.vrem.wifianalyzer.Configuration
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AboutUiState(
    val packageName: String,
    val versionInfo: String,
    val copyright: String,
    val device: String,
    val isScanThrottleEnabled: Boolean,
    val is5GHzBandSupported: Boolean,
    val is6GHzBandSupported: Boolean,
)

@HiltViewModel
class AboutViewModel
    @Inject
    constructor(
        private val wiFiManagerWrapper: WiFiManagerWrapper,
        private val configuration: Configuration,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        val uiState: AboutUiState =
            AboutUiState(
                packageName = context.packageName,
                versionInfo = version(context, configuration),
                copyright = copyright(context),
                device = device(),
                isScanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
                is5GHzBandSupported = wiFiManagerWrapper.is5GHzBandSupported(),
                is6GHzBandSupported = wiFiManagerWrapper.is6GHzBandSupported(),
            )

        companion object {
            fun device(): String = Build.MANUFACTURER + " - " + Build.BRAND + " - " + Build.MODEL

            fun copyright(context: Context): String =
                context.resources.getString(R.string.app_copyright) +
                    SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

            fun version(
                context: Context,
                configuration: Configuration,
            ): String =
                applicationVersion(context) +
                    (if (configuration.sizeAvailable) "S" else "") +
                    (if (configuration.largeScreen) "L" else "") +
                    " (" + Build.VERSION.RELEASE + "-" + Build.VERSION.SDK_INT + ")"

            private fun applicationVersion(context: Context): String =
                runCatching {
                    val packageInfo = context.packageInfo()
                    packageInfo.versionName + " - " + PackageInfoCompat.getLongVersionCode(packageInfo)
                }.getOrDefault("")
        }
    }
