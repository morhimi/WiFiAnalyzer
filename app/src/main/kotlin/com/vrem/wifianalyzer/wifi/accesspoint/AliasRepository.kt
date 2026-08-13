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
package com.vrem.wifianalyzer.wifi.accesspoint

import android.content.Context
import androidx.core.content.edit
import com.vrem.util.EMPTY
import com.vrem.wifianalyzer.wifi.model.BSSID

class AliasRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences(ALIAS_PREFS, Context.MODE_PRIVATE)

    fun alias(bssid: BSSID): String = sharedPreferences.getString(bssid, String.EMPTY) ?: String.EMPTY

    fun save(bssid: BSSID, alias: String) {
        if (alias.isBlank()) {
            sharedPreferences.edit { remove(bssid) }
        } else {
            sharedPreferences.edit { putString(bssid, alias) }
        }
    }

    companion object {
        private const val ALIAS_PREFS = "vrem.wifianalyzer.alias"
    }
}
