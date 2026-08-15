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
package com.vrem.wifianalyzer.wifi.model

import com.vrem.annotation.OpenClass
import com.vrem.util.EMPTY
import com.vrem.wifianalyzer.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@OpenClass
class ApAliasService(
    private val settingsRepository: SettingsRepository,
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun getAlias(bssid: BSSID): String {
        if (bssid.isBlank()) return String.EMPTY
        return settingsRepository.getAlias(bssid)
    }

    fun saveAlias(
        bssid: BSSID,
        alias: String,
    ) {
        if (bssid.isBlank()) return
        val trimmedAlias = alias.trim()
        scope.launch {
            settingsRepository.saveAlias(bssid, trimmedAlias)
        }
    }

    fun removeAlias(bssid: BSSID) {
        if (bssid.isBlank()) return
        scope.launch {
            settingsRepository.saveAlias(bssid, "")
        }
    }
}
