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

import android.app.AlertDialog
import android.view.View
import android.widget.EditText
import com.vrem.annotation.OpenClass
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@OpenClass
class AccessPointPopup {
    fun show(
        view: View,
        wiFiDetail: WiFiDetail,
    ): AlertDialog {
        val alertDialog: AlertDialog =
            AlertDialog
                .Builder(view.context)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.cancel()
                }.setNeutralButton(R.string.action_rename) { _, _ ->
                    rename(view, wiFiDetail)
                }.create()
        alertDialog.show()
        return alertDialog
    }

    private fun rename(
        view: View,
        wiFiDetail: WiFiDetail,
    ) {
        val input = EditText(view.context)
        input.setHint(R.string.alias_hint)
        val currentAlias = MainContext.INSTANCE.aliasRepository.alias(wiFiDetail.wiFiIdentifier.bssid)
        input.setText(currentAlias)

        AlertDialog
            .Builder(view.context)
                .setTitle(wiFiDetail.wiFiIdentifier.ssid)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val newAlias = input.text.toString()
                    MainContext.INSTANCE.aliasRepository.save(wiFiDetail.wiFiIdentifier.bssid, newAlias)
                    MainContext.INSTANCE.mainActivity.update()
                }.setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    fun attach(
        view: View,
        wiFiDetail: WiFiDetail,
    ) {
        view.setOnClickListener {
            runCatching { show(AccessPointDetail().makeViewDetailed(wiFiDetail), wiFiDetail) }
        }
    }
}
