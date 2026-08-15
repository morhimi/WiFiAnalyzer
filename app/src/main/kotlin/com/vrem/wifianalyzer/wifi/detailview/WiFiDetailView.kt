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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.vrem.annotation.OpenClass
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.model.WiFiAdditional
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiIdentifier
import com.vrem.wifianalyzer.wifi.model.WiFiSecurity
import com.vrem.wifianalyzer.wifi.model.WiFiSignal

@OpenClass
class WiFiDetailView {
    fun makeView(
        convertView: View?,
        parent: ViewGroup?,
        wiFiDetail: WiFiDetail,
        child: Boolean = false,
        @LayoutRes layout: Int =
            MainContext.INSTANCE.settings
                .accessPointView()
                .layout,
    ): View {
        val inflater =
            parent?.let { LayoutInflater.from(it.context) } ?: LayoutInflater.from(MainContext.INSTANCE.context)
        val view = convertView ?: inflater.inflate(layout, parent, false)
        setViewCompact(view, wiFiDetail, child)
        setViewExtra(view, wiFiDetail)
        setViewVendor(view, R.id.vendorShort, wiFiDetail.wiFiAdditional)
        return view
    }

    fun makeViewDetailed(
        wiFiDetail: WiFiDetail,
        ssidColor: Int? = null,
        context: Context = MainContext.INSTANCE.context,
    ): View {
        val view = LayoutInflater.from(context).inflate(R.layout.wifi_detail_view_popup, null)
        setViewCompact(view, wiFiDetail, false, ssidColor)
        setViewExtra(view, wiFiDetail)
        setViewCapabilitiesLong(view, wiFiDetail.wiFiSecurity)
        setViewSecurityTypes(view, wiFiDetail.wiFiSecurity)
        setViewVendor(view, R.id.vendorLong, wiFiDetail.wiFiAdditional)
        setViewBSSID(view, wiFiDetail.wiFiIdentifier)
        setViewWiFiBand(view, wiFiDetail.wiFiSignal)
        setViewWiFiChannelPair(view, wiFiDetail.wiFiSignal)
        setView80211mc(view, wiFiDetail.wiFiSignal)
        setViewWiFiStandard(view, wiFiDetail.wiFiSignal)
        setViewFastRoaming(view, wiFiDetail.wiFiSignal)
        enableTextSelection(view)
        return view
    }

    private fun enableTextSelection(view: View) {
        view.findViewById<TextView>(R.id.ssid).setTextIsSelectable(true)
        view.findViewById<TextView>(R.id.vendorLong).setTextIsSelectable(true)
        view.findViewById<TextView>(R.id.bssid).setTextIsSelectable(true)
    }

    private fun setViewBSSID(
        view: View,
        wiFiIdentifier: WiFiIdentifier,
    ) {
        val bssid = view.findViewById<TextView>(R.id.bssid)
        if (wiFiIdentifier.bssid.isBlank()) {
            bssid.visibility = View.GONE
        } else {
            bssid.visibility = View.VISIBLE
            bssid.text = wiFiIdentifier.bssid
        }
    }

    private fun setViewCompact(
        view: View,
        wiFiDetail: WiFiDetail,
        child: Boolean,
        ssidColor: Int? = null,
    ) {
        val ssid = view.findViewById<TextView>(R.id.ssid)
        ssid.text = wiFiDetail.wiFiIdentifier.title
        ssidColor?.let { color -> ssid.setTextColor(color) }
        val wiFiSignal = wiFiDetail.wiFiSignal
        view.findViewById<TextView>(R.id.channel).text = wiFiSignal.channelDisplay()
        view.findViewById<TextView>(R.id.primaryFrequency).text =
            "${wiFiSignal.primaryFrequency}${WiFiSignal.FREQUENCY_UNITS}"
        view.findViewById<TextView>(R.id.distance).text = wiFiSignal.distance
        view.findViewById<View>(R.id.tab).visibility = if (child) View.VISIBLE else View.GONE
        setSecurityImage(view, wiFiDetail)
        setLevelText(view, wiFiSignal)
    }

    private fun setSecurityImage(
        view: View,
        wiFiDetail: WiFiDetail,
    ) {
        val securityImage = view.findViewById<ImageView>(R.id.securityImage)
        val security = wiFiDetail.wiFiSecurity.security
        securityImage.tag = security.imageResource
        securityImage.setImageResource(security.imageResource)
    }

    private fun setViewExtra(
        view: View,
        wiFiDetail: WiFiDetail,
    ) = view.findViewById<TextView>(R.id.channel_frequency_range)?.let {
        val wiFiSignal = wiFiDetail.wiFiSignal
        setLevelImage(view, wiFiSignal)
        setWiFiStandardImage(view, wiFiSignal)
        setWiFiWidth(view, wiFiSignal)
        it.text = "${wiFiSignal.wiFiChannelStart.frequency} - ${wiFiSignal.wiFiChannelEnd.frequency}"
        view.findViewById<TextView>(R.id.capabilities).text =
            wiFiDetail.wiFiSecurity.securities
                .toList()
                .joinToString(" ", "[", "]")
    }

    private fun setWiFiWidth(
        view: View,
        wiFiSignal: WiFiSignal,
    ) {
        view.findViewById<TextView>(R.id.width).text =
            ContextCompat.getString(view.context, wiFiSignal.wiFiWidth.textResource)
    }

    private fun setWiFiStandardImage(
        view: View,
        wiFiSignal: WiFiSignal,
    ) {
        view.findViewById<TextView>(R.id.wiFiStandardValue).text =
            ContextCompat.getString(view.context, wiFiSignal.extra.wiFiStandard.valueResource)
    }

    private fun setLevelText(
        view: View,
        wiFiSignal: WiFiSignal,
    ) {
        val levelView = view.findViewById<TextView>(R.id.level)
        levelView.text = "${wiFiSignal.level}dBm"
        levelView.setTextColor(ContextCompat.getColor(view.context, wiFiSignal.strengthColor))
    }

    private fun setLevelImage(
        view: View,
        wiFiSignal: WiFiSignal,
    ) {
        val image = view.findViewById<ImageView>(R.id.levelImage)
        val strength = wiFiSignal.strength
        image.tag = strength.imageResource
        image.setImageResource(strength.imageResource)
        image.setColorFilter(ContextCompat.getColor(view.context, wiFiSignal.strengthColor))
    }

    private fun setViewVendor(
        view: View,
        @IdRes id: Int,
        wiFiAdditional: WiFiAdditional,
    ) = view.findViewById<TextView>(id)?.let {
        if (wiFiAdditional.vendorName.isBlank()) {
            it.visibility = View.GONE
        } else {
            it.visibility = View.VISIBLE
            it.text = wiFiAdditional.vendorName
        }
    }

    private fun setViewCapabilitiesLong(
        view: View,
        wiFiSecurity: WiFiSecurity,
    ) {
        view.findViewById<TextView>(R.id.capabilitiesLong).text = wiFiSecurity.capabilities
    }

    private fun setViewSecurityTypes(
        view: View,
        wiFiSecurity: WiFiSecurity,
    ) {
        view.findViewById<TextView>(R.id.securityTypes).text = wiFiSecurity.wiFiSecurityTypesDisplay(view.context)
    }

    private fun setViewWiFiBand(
        view: View,
        wiFiSignal: WiFiSignal,
    ) {
        view.findViewById<TextView>(R.id.wiFiBand).setText(wiFiSignal.wiFiBand.textResource)
    }

    private fun setViewFastRoaming(
        view: View,
        wiFiSignal: WiFiSignal,
    ) {
        view.findViewById<TextView>(R.id.fastRoaming).text = wiFiSignal.extra.fastRoamingDisplay(view.context)
    }

    private fun setViewWiFiStandard(
        view: View,
        wiFiSignal: WiFiSignal,
    ) {
        view.findViewById<TextView>(R.id.wiFiStandardFull).setText(wiFiSignal.extra.wiFiStandard.fullResource)
    }

    private fun setView80211mc(
        view: View,
        wiFiSignal: WiFiSignal,
    ) {
        view.findViewById<TextView>(R.id.flag80211mc).visibility =
            if (wiFiSignal.extra.is80211mc) View.VISIBLE else View.GONE
    }

    private fun setViewWiFiChannelPair(
        view: View,
        wiFiSignal: WiFiSignal,
    ) = with(wiFiSignal) {
        view.findViewById<TextView>(R.id.channel_start).text = "${wiFiChannelStart.channel}"
        view.findViewById<TextView>(R.id.channel_end).text = "${wiFiChannelEnd.channel}"
        view.findViewById<TextView>(R.id.channel_width).text =
            ContextCompat.getString(view.context, wiFiSignal.wiFiWidth.textResource)
    }
}
