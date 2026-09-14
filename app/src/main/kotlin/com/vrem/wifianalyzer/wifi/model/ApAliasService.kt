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

import com.vrem.util.EMPTY
import com.vrem.wifianalyzer.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ApAliasService(
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
) {
    private val cache = ConcurrentHashMap<BSSID, String>()

    init {
        scope.launch {
            settingsRepository.preferencesFlow.collect { preferences ->
                val newCache = mutableMapOf<String, String>()
                preferences.asMap().forEach { (key, value) ->
                    if (key.name.startsWith(ALIAS_PREFIX) && value is String) {
                        val bssid = key.name.removePrefix(ALIAS_PREFIX)
                        newCache[bssid] = value
                    }
                }
                cache.clear()
                cache.putAll(newCache)
            }
        }
    }

    fun getAlias(bssid: BSSID): String {
        if (bssid.isBlank()) return String.EMPTY
        val key = bssid.uppercase()
        return cache[key].orEmpty()
    }

    fun saveAlias(
        bssid: BSSID,
        alias: String,
    ) {
        if (bssid.isBlank()) return
        val trimmedAlias = alias.trim()
        val key = bssid.uppercase()
        if (trimmedAlias.isEmpty()) {
            cache.remove(key)
        } else {
            cache[key] = trimmedAlias
        }
        scope.launch {
            settingsRepository.saveAlias(key, trimmedAlias)
        }
    }

    fun removeAlias(bssid: BSSID) {
        if (bssid.isBlank()) return
        val key = bssid.uppercase()
        cache.remove(key)
        scope.launch {
            settingsRepository.saveAlias(key, "")
        }
    }

    companion object {
        private const val ALIAS_PREFIX = "ap_alias_"
    }
}
