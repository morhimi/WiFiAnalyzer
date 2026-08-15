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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.detailview.WiFiDetailPopup
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.predicate.makeAccessPointsPredicate
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.WiFiScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class AccessPointsFragment : Fragment() {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var wiFiManagerWrapper: WiFiManagerWrapper

    @Inject
    lateinit var permissionService: PermissionService

    @Inject
    lateinit var scannerService: ScannerService

    internal val wiFiScanViewModel: WiFiScanViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                val isDark =
                    when (settings.themeStyle()) {
                        ThemeStyle.DARK, ThemeStyle.BLACK -> true
                        ThemeStyle.LIGHT -> false
                        ThemeStyle.SYSTEM -> isSystemInDarkTheme()
                    }
                val wiFiData by wiFiScanViewModel.wiFiData.collectAsStateWithLifecycle()
                val wiFiBand = settings.wiFiBand()
                var isRefreshing by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                WiFiAnalyzerTheme(darkTheme = isDark) {
                    AccessPointsScreen(
                        wiFiData = wiFiData,
                        wiFiDetails =
                            wiFiData.wiFiDetails(
                                makeAccessPointsPredicate(settings),
                                settings.sortBy(),
                                settings.groupBy(),
                            ),
                        viewType = settings.accessPointView(),
                        wiFiBandAvailable = wiFiBand.available(),
                        wiFiBandName = getString(wiFiBand.textResource),
                        scanThrottleEnabled = wiFiManagerWrapper.isScanThrottleEnabled(),
                        permissionEnabled = permissionService.enabled(),
                        isScanning = scannerService.running(),
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            scope.launch {
                                isRefreshing = true
                                wiFiScanViewModel.update()
                                delay(1.seconds) // Visual feedback
                                isRefreshing = false
                            }
                        },
                        onDetailClick = { detail ->
                            WiFiDetailPopup.show(parentFragmentManager, detail)
                        },
                    )
                }
            }
        }
}
