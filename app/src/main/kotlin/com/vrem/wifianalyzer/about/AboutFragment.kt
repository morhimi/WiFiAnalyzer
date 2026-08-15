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

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.vrem.util.EMPTY
import com.vrem.util.packageInfo
import com.vrem.util.readFile
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.settings.ThemeStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val activity: FragmentActivity = requireActivity()
        val wiFiManagerWrapper = MainContext.INSTANCE.wiFiManagerWrapper
        return ComposeView(requireContext()).apply {
            setContent {
                val settings = MainContext.INSTANCE.settings
                val isDark = when (settings.themeStyle()) {
                    ThemeStyle.DARK, ThemeStyle.BLACK -> true
                    ThemeStyle.LIGHT -> false
                    ThemeStyle.SYSTEM -> isSystemInDarkTheme()
                }
                WiFiAnalyzerTheme(darkTheme = isDark) {
                    AboutScreen(
                        applicationName = getString(R.string.app_full_name),
                        packageName = activity.packageName,
                        versionInfo = version(activity),
                        copyright = copyright(),
                        device = device(),
                        isScanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
                        is5GHzBandSupported = wiFiManagerWrapper.is5GHzBandSupported(),
                        is6GHzBandSupported = wiFiManagerWrapper.is6GHzBandSupported(),
                        onWriteReview = { onWriteReview(activity) },
                        onShowLicense = { titleId, resourceId, isSmallFont ->
                            showLicense(activity, titleId, resourceId, isSmallFont)
                        },
                    )
                }
            }
        }
    }

    private fun onWriteReview(activity: FragmentActivity) {
        val url = "market://details?id=" + activity.applicationContext.packageName
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        runCatching {
            activity.startActivity(intent)
        }.getOrElse {
            Toast.makeText(activity, it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun showLicense(
        activity: FragmentActivity,
        titleId: Int,
        resourceId: Int,
        isSmallFont: Boolean,
    ) {
        if (!activity.isFinishing) {
            val text = readFile(activity.resources, resourceId)
            val alertDialog: AlertDialog =
                AlertDialog
                    .Builder(activity)
                    .setTitle(titleId)
                    .setMessage(text)
                    .setNeutralButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .create()
            alertDialog.show()
            if (isSmallFont) {
                alertDialog.findViewById<TextView>(android.R.id.message).textSize = 8f
            }
        }
    }

    private fun device(): String = Build.MANUFACTURER + " - " + Build.BRAND + " - " + Build.MODEL

    private fun copyright(): String =
        resources.getString(R.string.app_copyright) + SimpleDateFormat(YEAR_FORMAT, Locale.getDefault()).format(Date())

    private fun version(activity: FragmentActivity): String {
        val configuration = MainContext.INSTANCE.configuration
        return applicationVersion(activity) +
            ifElse(configuration.sizeAvailable, "S") +
            ifElse(configuration.largeScreen, "L") +
            " (" + Build.VERSION.RELEASE + "-" + Build.VERSION.SDK_INT + ")"
    }

    private fun applicationVersion(activity: FragmentActivity): String =
        runCatching {
            val packageInfo: PackageInfo = activity.packageInfo()
            packageInfo.versionName + " - " + PackageInfoCompat.getLongVersionCode(packageInfo)
        }.getOrDefault(String.EMPTY)

    private fun ifElse(
        condition: Boolean,
        value: String,
    ) = if (condition) {
        value
    } else {
        String.EMPTY
    }

    companion object {
        private const val YEAR_FORMAT = "yyyy"
    }
}
