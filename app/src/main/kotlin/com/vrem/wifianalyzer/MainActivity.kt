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

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.material.navigation.NavigationView
import com.vrem.annotation.OpenClass
import com.vrem.util.createContext
import com.vrem.wifianalyzer.Configuration as WiFiConfiguration
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme
import com.vrem.wifianalyzer.navigation.MainNavigationGraph
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.navigation.NavigationMenuControl
import com.vrem.wifianalyzer.navigation.NavigationMenuController
import com.vrem.wifianalyzer.navigation.options.OptionMenu
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.WiFiScanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    NavigationMenuControl {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var scannerService: ScannerService

    @Inject
    lateinit var permissionService: PermissionService

    @Inject
    lateinit var configuration: WiFiConfiguration

    @Inject
    lateinit var wiFiManagerWrapper: WiFiManagerWrapper

    @Inject
    lateinit var vendorService: VendorService

    @Inject
    lateinit var apAliasService: ApAliasService

    @Inject
    lateinit var filtersAdapter: FiltersAdapter

    private val wiFiScanViewModel: WiFiScanViewModel by viewModels()

    internal lateinit var drawerNavigation: DrawerNavigation
    internal lateinit var mainReload: MainReload
    internal lateinit var navigationMenuController: NavigationMenuController
    internal lateinit var optionMenu: OptionMenu
    internal lateinit var navController: NavHostController

    override fun attachBaseContext(newBase: Context) =
        super.attachBaseContext(newBase.createContext(Settings(Repository(newBase)).languageLocale()))

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        MainContext.INSTANCE.initialize(
            applicationContext,
            settings,
            wiFiManagerWrapper,
            permissionService,
            scannerService,
            vendorService,
            apAliasService,
            configuration,
            filtersAdapter,
        )

        settings.initializeDefaultValues()
        settings.themeStyle().setTheme(this)

        mainReload = MainReload(settings)

        setContentView(R.layout.main_activity)

        val composeView = findViewById<ComposeView>(R.id.main_fragment_compose)
        composeView.setContent {
            val controller = rememberNavController()
            navController = controller

            DisposableEffect(controller) {
                val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                    val menu = NavigationMenu.findByRoute(destination.route)
                    navigationMenuController.currentNavigationMenu(menu)
                    title = getString(menu.title)
                    settings.saveSelectedMenu(menu)
                    updateActionBar()
                }
                controller.addOnDestinationChangedListener(listener)
                onDispose {
                    controller.removeOnDestinationChangedListener(listener)
                }
            }

            LaunchedEffect(Unit) {
                val selectedMenu = settings.selectedMenu()
                if (selectedMenu != NavigationMenu.ACCESS_POINTS) {
                    controller.navigate(selectedMenu.route) {
                        popUpTo(controller.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            WiFiAnalyzerTheme {
                MainNavigationGraph(
                    navController = controller,
                    wiFiScanViewModel = wiFiScanViewModel,
                    settings = settings,
                    wiFiManagerWrapper = wiFiManagerWrapper,
                    permissionService = permissionService,
                    scannerService = scannerService,
                    vendorService = vendorService,
                    configuration = configuration,
                )
            }
        }

        optionMenu = OptionMenu()

        keepScreenOn()

        val toolbar = setupToolbar()
        drawerNavigation = DrawerNavigation(this, toolbar)
        drawerNavigation.create()

        navigationMenuController = NavigationMenuController(this)
        navigationMenuController.currentNavigationMenu(settings.selectedMenu())

        onBackPressedDispatcher.addCallback(this, MainActivityBackPressed(this))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settings.settingsData.collectLatest { _ ->
                    if (mainReload.shouldReload(settings)) {
                        scannerService.stop()
                        recreate()
                    } else {
                        keepScreenOn()
                        update()
                    }
                }
            }
        }
    }

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerNavigation.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerNavigation.onConfigurationChanged(newConfig)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!permissionService.granted(requestCode, grantResults)) {
            finish()
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
        updateActionBar()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        closeDrawer()
        val currentNavigationMenu = NavigationMenu.find(menuItem.itemId)

        if (currentNavigationMenu == NavigationMenu.EXPORT) {
            currentNavigationMenu.activateNavigationMenu(this)
            return true
        }

        if (::navController.isInitialized) {
            navController.navigate(currentNavigationMenu.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        currentNavigationMenu(currentNavigationMenu)
        return true
    }

    fun closeDrawer(): Boolean {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }

    public override fun onPause() {
        scannerService.pause()
        updateActionBar()
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
        updateActionBar()
    }

    public override fun onStop() {
        scannerService.stop()
        updateActionBar()
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
            permissionService.check(this)
        }
        updateActionBar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        optionMenu.create(this, menu)
        updateActionBar()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionMenu.select(item)
        updateActionBar()
        return true
    }

    fun updateActionBar() = currentNavigationMenu().activateOptions(this)

    override fun currentMenuItem(): MenuItem = navigationMenuController.currentMenuItem()

    override fun currentNavigationMenu(): NavigationMenu = navigationMenuController.currentNavigationMenu()

    override fun currentNavigationMenu(navigationMenu: NavigationMenu) {
        navigationMenuController.currentNavigationMenu(navigationMenu)
        settings.saveSelectedMenu(navigationMenu)
    }

    override fun navigationView(): NavigationView = navigationMenuController.drawerNavigationView
}
