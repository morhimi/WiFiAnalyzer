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
 * along with this program.  See the  GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.vrem.wifianalyzer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.vrem.wifianalyzer.compose.WiFiAnalyzerApp
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.permission.ApplicationPermission
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.WiFiScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.vrem.wifianalyzer.Configuration as WiFiConfiguration

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var wiFiManagerWrapper: WiFiManagerWrapper

    @Inject
    lateinit var permissionService: PermissionService

    @Inject
    lateinit var scannerService: ScannerService

    @Inject
    lateinit var configuration: WiFiConfiguration

    @Inject
    lateinit var vendorService: VendorService

    @Inject
    lateinit var apAliasService: ApAliasService

    @Inject
    lateinit var filtersAdapter: FiltersAdapter

    private val wiFiScanViewModel: WiFiScanViewModel by viewModels()

    internal lateinit var mainReload: MainReload
    internal lateinit var navController: NavHostController
    internal var showWiFiDetailsCallback: ((List<WiFiDetail>) -> Unit)? = null

    internal val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                if (!permissionService.locationEnabled()) {
                    startLocationSettings()
                }
                scannerService.resume()
                update()
            } else {
                finish()
            }
        }

    fun showWiFiDetails(details: List<WiFiDetail>) {
        showWiFiDetailsCallback?.invoke(details)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settings.initializeDefaultValues()
        settings.themeStyle().setTheme(this)
        mainReload = MainReload(settings)

        setContent {
            val settingsData by settings.settingsData.collectAsStateWithLifecycle()
            val isDark =
                when (settingsData.themeStyle) {
                    ThemeStyle.DARK, ThemeStyle.BLACK -> true
                    ThemeStyle.LIGHT -> false
                    ThemeStyle.SYSTEM -> isSystemInDarkTheme()
                }

            WiFiAnalyzerTheme(darkTheme = isDark) {
                val controller = rememberNavController()
                navController = controller

                WiFiAnalyzerApp(
                    navController = controller,
                    wiFiScanViewModel = wiFiScanViewModel,
                    settings = settings,
                    wiFiManagerWrapper = wiFiManagerWrapper,
                    permissionService = permissionService,
                    scannerService = scannerService,
                    vendorService = vendorService,
                    apAliasService = apAliasService,
                    filtersAdapter = filtersAdapter,
                    configuration = configuration,
                )
            }
        }

        onBackPressedDispatcher.addCallback(this, MainActivityBackPressed(this))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settings.settingsData.collectLatest { _ ->
                    if (mainReload.shouldReload(settings)) {
                        scannerService.stop()
                        recreate()
                    } else {
                        update()
                    }
                }
            }
        }
    }

    internal val largeScreen: Boolean
        get() {
            val configuration = resources.configuration
            val screenLayoutSize = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
            return screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

    fun update() {
        scannerService.update()
    }

    public override fun onPause() {
        scannerService.pause()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        if (permissionService.permissionGranted()) {
            if (!permissionService.locationEnabled()) {
                startLocationSettings()
            }
            scannerService.resume()
        } else {
            scannerService.pause()
        }
    }

    public override fun onStop() {
        scannerService.stop()
        super.onStop()
    }

    public override fun onStart() {
        super.onStart()
        if (permissionService.permissionGranted()) {
            if (!permissionService.locationEnabled()) {
                startLocationSettings()
            }
            scannerService.resume()
        } else {
            permissionService.check(
                context = this,
                onOk = { permissionLauncher.launch(ApplicationPermission.PERMISSION) },
                onCancel = { finish() },
            )
        }
    }
}
