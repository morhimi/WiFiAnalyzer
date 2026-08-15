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
package com.vrem.wifianalyzer.wifi.detailview

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import com.vrem.annotation.OpenClass
import com.vrem.util.findActivity
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@OpenClass
class WiFiDetailPopup {
    fun show(
        view: View,
        wiFiDetail: WiFiDetail? = null,
    ): AlertDialog {
        val targetContext = view.findActivity() ?: view.context
        val builder = AlertDialog.Builder(targetContext).setView(view)
        wiFiDetail?.let { detail ->
            if (detail.wiFiIdentifier.bssid.isNotBlank()) {
                builder.setNeutralButton(R.string.ap_alias_edit) { dialog, _ ->
                    dialog.dismiss()
                    showAliasDialog(targetContext, detail)
                }
            }
        }
        val alertDialog: AlertDialog =
            builder
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.cancel()
                }.create()
        alertDialog.show()
        return alertDialog
    }

    fun showAliasDialog(
        context: Context,
        wiFiDetail: WiFiDetail,
    ): AlertDialog {
        val input = EditText(context)
        input.setSingleLine()
        input.setText(wiFiDetail.wiFiIdentifier.alias)
        input.hint = context.getString(R.string.ap_alias_hint)

        val container = FrameLayout(context)
        val params =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        val margin = context.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)
        params.setMargins(margin, margin / 2, margin, margin / 2)
        input.layoutParams = params
        container.addView(input)

        val builder =
            AlertDialog
                .Builder(context)
                .setTitle(R.string.ap_alias_title)
                .setMessage("${wiFiDetail.wiFiIdentifier.ssid} (${wiFiDetail.wiFiIdentifier.bssid})")
                .setView(container)
                .setPositiveButton(R.string.ap_alias_save) { dialog, _ ->
                    val alias = input.text.toString()
                    MainContext.INSTANCE.apAliasService.saveAlias(wiFiDetail.wiFiIdentifier.bssid, alias)
                    MainContext.INSTANCE.scannerService.update()
                    dialog.dismiss()
                }.setNeutralButton(R.string.ap_alias_clear) { dialog, _ ->
                    MainContext.INSTANCE.apAliasService.removeAlias(wiFiDetail.wiFiIdentifier.bssid)
                    MainContext.INSTANCE.scannerService.update()
                    dialog.dismiss()
                }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
        val alertDialog = builder.create()
        alertDialog.show()
        return alertDialog
    }

    fun showSequence(views: List<View>): AlertDialog {
        if (views.size <= 1) return show(views.first())
        return showAtIndex(views, 0)
    }

    private fun showAtIndex(
        views: List<View>,
        index: Int,
    ): AlertDialog {
        val view = views[index]
        val isLast = index == views.size - 1
        val targetContext = view.findActivity() ?: view.context
        val builder =
            AlertDialog
                .Builder(targetContext)
                .setView(view)
                .setNegativeButton(R.string.filter_close) { dialog, _ -> dialog.cancel() }
        if (!isLast) {
            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                showAtIndex(views, index + 1)
            }
        }
        val alertDialog = builder.create()
        alertDialog.show()
        return alertDialog
    }

    fun attach(
        view: View,
        wiFiDetail: WiFiDetail,
    ) {
        view.setOnClickListener {
            val targetContext = it.findActivity() ?: it.context
            runCatching { show(WiFiDetailView().makeViewDetailed(wiFiDetail, context = targetContext), wiFiDetail) }
        }
    }

    fun attachToRow(
        row: View,
        wiFiDetail: WiFiDetail,
    ) {
        row.findViewById<View>(R.id.attachPopup)?.let {
            attach(it, wiFiDetail)
            attach(row.findViewById(R.id.ssid), wiFiDetail)
        }
    }
}
