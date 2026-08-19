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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.navigation.MAIN_NAVIGATION
import com.vrem.wifianalyzer.navigation.MainNavigationGraph
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.navigation.Screen
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.detailview.ApAliasDialog
import com.vrem.wifianalyzer.wifi.detailview.WiFiDetailDialog
import com.vrem.wifianalyzer.wifi.filter.FilterDialog
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import kotlinx.coroutines.launch

@Composable
fun WiFiAnalyzerApp(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val settingsData by mainViewModel.settingsData.collectAsStateWithLifecycle()
    val isScanning by mainViewModel.isScanning.collectAsStateWithLifecycle()
    val settings = mainViewModel.settings
    val filtersAdapter = mainViewModel.filtersAdapter
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentMenu = NavigationMenu.findByDestination(currentDestination)
    val isAccessPoints = currentDestination?.hasRoute(Screen.AccessPoints::class) == true
    val context = LocalContext.current
    var showFilterDialog by remember { mutableStateOf(false) }
    var activeDetailList by remember { mutableStateOf<List<WiFiDetail>?>(null) }
    var aliasEditDetail by remember { mutableStateOf<WiFiDetail?>(null) }

    BackHandler(enabled = !isAccessPoints) {
        navController.navigate(Screen.AccessPoints) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            filtersAdapter = filtersAdapter,
            settings = settings,
            onApply = {
                showFilterDialog = false
                mainViewModel.updateScan()
            },
            onReset = {
                mainViewModel.updateScan()
            },
            onDismiss = {
                showFilterDialog = false
            },
        )
    }

    activeDetailList?.let { details ->
        WiFiDetailDialog(
            wiFiDetails = details,
            onDismiss = { activeDetailList = null },
            onEditAlias = { detail ->
                activeDetailList = null
                aliasEditDetail = detail
            },
        )
    }

    aliasEditDetail?.let { detail ->
        ApAliasDialog(
            wiFiDetail = detail,
            onSave = { alias ->
                mainViewModel.saveAlias(detail.wiFiIdentifier.bssid, alias)
                aliasEditDetail = null
            },
            onClear = {
                mainViewModel.removeAlias(detail.wiFiIdentifier.bssid)
                aliasEditDetail = null
            },
            onDismiss = {
                aliasEditDetail = null
            },
        )
    }

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
                        mainViewModel.export(context)
                    } else if (menu.screen != null) {
                        navController.navigate(menu.screen) {
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
                    onToggleScanner = { mainViewModel.toggleScanning() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onBandSelected = { band -> settings.wiFiBand(band) },
                    onFilterClick = { showFilterDialog = true },
                )
            },
            bottomBar = {
                WiFiAnalyzerBottomBar(
                    currentDestination = currentDestination,
                    onMenuSelected = { menu ->
                        if (menu.screen != null) {
                            navController.navigate(menu.screen) {
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
        ) { innerPadding ->
            MainNavigationGraph(
                navController = navController,
                onDetailClick = { detail -> activeDetailList = listOf(detail) },
                onShowWiFiDetails = { details -> activeDetailList = details },
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
                selected =
                    menu.screen != null &&
                        currentDestination?.hierarchy?.any { it.hasRoute(menu.screen::class) } == true,
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
