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
package com.vrem.wifianalyzer

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.vrem.wifianalyzer.compose.WiFiAnalyzerApp
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.permission.PermissionRationaleDialog
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.shizuku.WiFiThrottleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var permissionService: PermissionService

    @Inject
    lateinit var scannerService: ScannerService

    @Inject
    lateinit var wiFiThrottleManager: WiFiThrottleManager

    private val shizukuPermissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == WiFiThrottleManager.SHIZUKU_REQUEST_CODE &&
                grantResult == PackageManager.PERMISSION_GRANTED
            ) {
                wiFiThrottleManager.onAppStart()
            }
        }

    private val shizukuBinderReceivedListener =
        Shizuku.OnBinderReceivedListener {
            if (settings.shizukuThrottle()) {
                wiFiThrottleManager.onAppStart()
            }
        }

    private var showPermissionRationale by mutableStateOf(false)

    internal val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                if (!permissionService.locationEnabled()) {
                    startLocationSettings()
                }
                scannerService.resume()
                update()
            } else {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsData by settings.settingsData.collectAsStateWithLifecycle()
            val isDark =
                when (settingsData.themeStyle) {
                    ThemeStyle.DARK, ThemeStyle.BLACK -> true
                    ThemeStyle.LIGHT -> false
                    ThemeStyle.SYSTEM -> isSystemInDarkTheme()
                }

            DisposableEffect(settingsData.keepScreenOn) {
                keepScreenOn()
                onDispose {}
            }

            if (showPermissionRationale) {
                PermissionRationaleDialog(
                    onConfirm = {
                        showPermissionRationale = false
                        permissionLauncher.launch(permissionService.permissions())
                    },
                    onDismiss = {
                        showPermissionRationale = false
                        finish()
                    },
                )
            }

            WiFiAnalyzerTheme(
                themeStyle = settingsData.themeStyle,
                darkTheme = isDark,
                dynamicColor = settingsData.dynamicColor,
            ) {
                val controller = rememberNavController()
                WiFiAnalyzerApp(navController = controller)
            }
        }

        runCatching {
            Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
            Shizuku.addBinderReceivedListenerSticky(shizukuBinderReceivedListener)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settings.settingsData.collectLatest { settingsData ->
                    wiFiThrottleManager.onSettingChanged(settingsData.shizukuThrottle)
                    update()
                }
            }
        }
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
        if (permissionService.enabled()) {
            scannerService.resume()
        } else {
            scannerService.pause()
        }
    }

    public override fun onStop() {
        wiFiThrottleManager.onAppStop()
        scannerService.stop()
        super.onStop()
    }

    public override fun onStart() {
        super.onStart()
        wiFiThrottleManager.onAppStart()
        if (permissionService.permissionGranted()) {
            if (permissionService.locationEnabled()) {
                scannerService.resume()
            } else {
                scannerService.pause()
            }
        } else {
            showPermissionRationale = true
        }
    }

    override fun onDestroy() {
        runCatching {
            Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
            Shizuku.removeBinderReceivedListener(shizukuBinderReceivedListener)
        }
        wiFiThrottleManager.onAppExit()
        super.onDestroy()
    }
}
