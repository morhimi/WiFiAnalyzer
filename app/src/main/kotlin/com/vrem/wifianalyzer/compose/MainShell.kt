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
package com.vrem.wifianalyzer.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vrem.wifianalyzer.Configuration
import com.vrem.wifianalyzer.MainActivity
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.navigation.MAIN_NAVIGATION
import com.vrem.wifianalyzer.navigation.MainNavigationGraph
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.WiFiScanViewModel
import kotlinx.coroutines.launch

@Composable
fun WiFiAnalyzerApp(
    navController: NavHostController,
    wiFiScanViewModel: WiFiScanViewModel,
    settings: Settings,
    wiFiManagerWrapper: WiFiManagerWrapper,
    permissionService: PermissionService,
    scannerService: ScannerService,
    vendorService: VendorService,
    configuration: Configuration,
    onFilterClick: () -> Unit,
) {
    val settingsData by settings.settingsData.collectAsStateWithLifecycle()
    val isScanning by wiFiScanViewModel.isScanning.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: NavigationMenu.ACCESS_POINTS.route
    val currentMenu = NavigationMenu.findByRoute(currentRoute)
    val context = LocalContext.current
    val mainActivity = context as? MainActivity

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            WiFiAnalyzerDrawer(
                currentMenu = currentMenu,
                onMenuSelected = { menu ->
                    scope.launch { drawerState.close() }
                    if (menu == NavigationMenu.EXPORT) {
                        mainActivity?.let { menu.activateNavigationMenu(it) }
                    } else {
                        navController.navigate(menu.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                WiFiAnalyzerTopBar(
                    currentMenu = currentMenu,
                    settingsData = settingsData,
                    isScanning = isScanning,
                    onToggleScanner = { scannerService.toggle() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onBandSelected = { band -> settings.wiFiBand(band) },
                    onFilterClick = onFilterClick,
                )
            },
            bottomBar = {
                WiFiAnalyzerBottomBar(
                    currentDestination = currentDestination,
                    onMenuSelected = { menu ->
                        navController.navigate(menu.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            },
        ) { innerPadding ->
            MainNavigationGraph(
                navController = navController,
                wiFiScanViewModel = wiFiScanViewModel,
                settings = settings,
                wiFiManagerWrapper = wiFiManagerWrapper,
                permissionService = permissionService,
                scannerService = scannerService,
                vendorService = vendorService,
                configuration = configuration,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiAnalyzerTopBar(
    currentMenu: NavigationMenu,
    settingsData: SettingsData,
    isScanning: Boolean,
    onToggleScanner: () -> Unit,
    onOpenDrawer: () -> Unit,
    onBandSelected: (WiFiBand) -> Unit,
    onFilterClick: () -> Unit,
) {
    var showBandMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = { Text(stringResource(currentMenu.title)) },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.navigation_drawer_open))
            }
        },
        actions = {
            if (currentMenu.showWiFiBandSelector) {
                Box {
                    IconButton(onClick = { showBandMenu = true }) {
                        val bandTitle = stringResource(settingsData.wiFiBand.textResource).replace(" ", "\n")
                        Text(
                            text = bandTitle,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    DropdownMenu(
                        expanded = showBandMenu,
                        onDismissRequest = { showBandMenu = false },
                    ) {
                        WiFiBand.entries.forEach { band ->
                            DropdownMenuItem(
                                text = { Text(stringResource(band.textResource)) },
                                onClick = {
                                    onBandSelected(band)
                                    showBandMenu = false
                                },
                            )
                        }
                    }
                }
            }

            if (currentMenu.showFilter) {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        painterResource(R.drawable.ic_filter_list),
                        contentDescription = stringResource(R.string.filter_title),
                    )
                }
            }

            if (currentMenu.showScanner) {
                IconButton(onClick = onToggleScanner) {
                    Icon(
                        painter = painterResource(if (isScanning) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                        contentDescription =
                            stringResource(
                                if (isScanning) R.string.scanner_pause else R.string.scanner_play,
                            ),
                    )
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

@Composable
fun WiFiAnalyzerDrawer(
    currentMenu: NavigationMenu,
    onMenuSelected: (NavigationMenu) -> Unit,
) {
    ModalDrawerSheet {
        DrawerHeader()
        HorizontalDivider()

        Text(
            text = "Features",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.labelMedium,
        )
        MAIN_NAVIGATION.forEach { menu ->
            NavigationDrawerItem(
                label = { Text(stringResource(menu.title)) },
                selected = menu == currentMenu,
                onClick = { onMenuSelected(menu) },
                icon = { Icon(painterResource(menu.icon), contentDescription = null) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Other",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.labelMedium,
        )
        listOf(NavigationMenu.EXPORT, NavigationMenu.CHANNEL_AVAILABLE, NavigationMenu.VENDORS).forEach { menu ->
            NavigationDrawerItem(
                label = { Text(stringResource(menu.title)) },
                selected = menu == currentMenu,
                onClick = { onMenuSelected(menu) },
                icon = { Icon(painterResource(menu.icon), contentDescription = null) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        listOf(NavigationMenu.SETTINGS, NavigationMenu.ABOUT).forEach { menu ->
            NavigationDrawerItem(
                label = { Text(stringResource(menu.title)) },
                selected = menu == currentMenu,
                onClick = { onMenuSelected(menu) },
                icon = { Icon(painterResource(menu.icon), contentDescription = null) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
private fun DrawerHeader() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_network_wifi),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun WiFiAnalyzerBottomBar(
    currentDestination: NavDestination?,
    onMenuSelected: (NavigationMenu) -> Unit,
) {
    NavigationBar {
        MAIN_NAVIGATION.forEach { menu ->
            NavigationBarItem(
                icon = { Icon(painterResource(menu.icon), contentDescription = null) },
                label = { Text(stringResource(menu.title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                selected = currentDestination?.hierarchy?.any { it.route == menu.route } == true,
                onClick = { onMenuSelected(menu) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun WiFiAnalyzerTopBarPreview() {
    WiFiAnalyzerTheme {
        WiFiAnalyzerTopBar(
            currentMenu = NavigationMenu.ACCESS_POINTS,
            settingsData = SettingsData(),
            isScanning = true,
            onToggleScanner = {},
            onOpenDrawer = {},
            onBandSelected = {},
            onFilterClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WiFiAnalyzerBottomBarPreview() {
    WiFiAnalyzerTheme {
        WiFiAnalyzerBottomBar(
            currentDestination = null,
            onMenuSelected = {},
        )
    }
}
