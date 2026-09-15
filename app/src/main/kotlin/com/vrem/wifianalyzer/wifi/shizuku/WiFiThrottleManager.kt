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
import com.vrem.wifianalyzer.settings.Settings
import javax.inject.Inject
import javax.inject.Singleton
import android.provider.Settings as AndroidSettings

internal const val SETTING_WIFI_SCAN_THROTTLE_ENABLED = "wifi_scan_throttle_enabled"
internal const val CMD_DISABLE_THROTTLE =
    "cmd wifi set-scan-throttle-enabled disabled; settings put global wifi_scan_throttle_enabled 0"
internal const val CMD_ENABLE_THROTTLE =
    "cmd wifi set-scan-throttle-enabled enabled; settings put global wifi_scan_throttle_enabled 1"

@Singleton
class WiFiThrottleManager
    @Inject
    constructor(
        private val settings: Settings,
        private val shizukuRunner: ShizukuRunner,
        private val contentResolver: ContentResolver,
        private val systemThrottleReader: (ContentResolver) -> Boolean = { cr ->
            runCatching {
                AndroidSettings.Global.getInt(cr, SETTING_WIFI_SCAN_THROTTLE_ENABLED, 1) == 1
            }.getOrDefault(true)
        },
    ) {
        internal var wasThrottlingOriginallyEnabled: Boolean? = null
            private set

        internal var throttledByApp: Boolean = false
            private set

        fun onAppStart() {
            if (!settings.shizukuThrottle()) return
            if (!shizukuRunner.isAvailable() || !shizukuRunner.hasPermission()) return
            if (throttledByApp) return

            if (wasThrottlingOriginallyEnabled == null) {
                wasThrottlingOriginallyEnabled = systemThrottleReader(contentResolver)
            }

            if (wasThrottlingOriginallyEnabled == false) return

            if (shizukuRunner.execute(CMD_DISABLE_THROTTLE)) {
                throttledByApp = true
            }
        }

        fun onAppStop() {
            restoreThrottling()
        }

        fun onAppExit() {
            restoreThrottling()
            wasThrottlingOriginallyEnabled = null
        }

        fun onSettingChanged(enabled: Boolean) {
            if (enabled) {
                onAppStart()
            } else {
                restoreThrottling()
                wasThrottlingOriginallyEnabled = null
            }
        }

        private fun restoreThrottling() {
            if (throttledByApp) {
                if (shizukuRunner.execute(CMD_ENABLE_THROTTLE)) {
                    throttledByApp = false
                }
            }
        }

        companion object {
            const val SHIZUKU_REQUEST_CODE = 4201
        }
    }
