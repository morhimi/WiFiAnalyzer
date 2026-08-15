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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.vrem.annotation.OpenClass
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

@OpenClass
class WiFiDetailPopup : DialogFragment() {

    companion object {
        private var wiFiDetailsList: List<WiFiDetail> = emptyList()
        private var currentIndex: Int = 0

        fun show(
            fragmentManager: FragmentManager,
            wiFiDetail: WiFiDetail,
        ) {
            wiFiDetailsList = listOf(wiFiDetail)
            currentIndex = 0
            WiFiDetailPopup().show(fragmentManager, "WiFiDetailPopup")
        }

        fun showSequence(
            fragmentManager: FragmentManager,
            wiFiDetails: List<WiFiDetail>,
        ) {
            wiFiDetailsList = wiFiDetails
            currentIndex = 0
            WiFiDetailPopup().show(fragmentManager, "WiFiDetailPopup")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val detail = wiFiDetailsList.getOrNull(currentIndex) ?: return super.onCreateDialog(savedInstanceState)

        val composeView =
            ComposeView(requireContext()).apply {
                setContent {
                    val settings = MainContext.INSTANCE.settings
                    val isDark = when (settings.themeStyle()) {
                        ThemeStyle.DARK, ThemeStyle.BLACK -> true
                        ThemeStyle.LIGHT -> false
                        ThemeStyle.SYSTEM -> isSystemInDarkTheme()
                    }
                    WiFiAnalyzerTheme(darkTheme = isDark) {
                        WiFiDetailContent(wiFiDetail = detail)
                    }
                }
            }

        val builder = AlertDialog.Builder(requireContext()).setView(composeView)

        val isSequence = wiFiDetailsList.size > 1
        val isLast = currentIndex == (wiFiDetailsList.size - 1)

        if (isSequence) {
            builder.setNegativeButton(R.string.filter_close) { _, _ -> dismiss() }
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                dismiss()
                if (!isLast) {
                    currentIndex++
                    WiFiDetailPopup().show(parentFragmentManager, "WiFiDetailPopup")
                }
            }
        } else {
            if (detail.wiFiIdentifier.bssid.isNotBlank()) {
                builder.setNeutralButton(R.string.ap_alias_edit) { _, _ ->
                    dismiss()
                    showAliasDialog(requireContext(), detail)
                }
            }
            builder.setPositiveButton(android.R.string.ok) { _, _ -> dismiss() }
        }

        return builder.create()
    }

    private fun showAliasDialog(
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
}
