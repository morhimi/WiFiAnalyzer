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
package com.vrem.wifianalyzer.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vrem.util.applyLocale
import com.vrem.util.buildMinVersionQ
import com.vrem.util.buildMinVersionS
import com.vrem.util.findByLanguageTag
import com.vrem.util.supportedLanguages
import com.vrem.util.toCapitalize
import com.vrem.util.toLanguageTag
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointViewType
import com.vrem.wifianalyzer.wifi.accesspoint.ConnectionViewType
import com.vrem.wifianalyzer.wifi.band.WiFiChannelCountry
import com.vrem.wifianalyzer.wifi.model.GroupBy
import com.vrem.wifianalyzer.wifi.model.SortBy
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: Settings,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val settingsData by settings.settingsData.collectAsStateWithLifecycle()

    var showScanSpeedDialog by remember { mutableStateOf(false) }
    var showSortByDialog by remember { mutableStateOf(false) }
    var showGroupByDialog by remember { mutableStateOf(false) }
    var showConnectionViewDialog by remember { mutableStateOf(false) }
    var showApViewDialog by remember { mutableStateOf(false) }
    var showGraphMaxYDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
        ) {
            // Scanning Section
            SettingsCategoryHeader(title = stringResource(R.string.scan_speed_title))
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_fast_forward),
                title = stringResource(R.string.scan_speed_title),
                summary = stringResource(R.string.scan_speed_summary, settingsData.scanSpeed),
                onClick = { showScanSpeedDialog = true },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Display & Sorting Section
            SettingsCategoryHeader(title = stringResource(R.string.sort_by_title))
            val sortByNames = stringArrayResource(R.array.sort_by_array)
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_sort),
                title = stringResource(R.string.sort_by_title),
                summary = sortByNames.getOrElse(settingsData.sortBy.ordinal) { "" },
                onClick = { showSortByDialog = true },
            )

            val groupByNames = stringArrayResource(R.array.group_by_array)
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_group),
                title = stringResource(R.string.group_by_title),
                summary = groupByNames.getOrElse(settingsData.groupBy.ordinal) { "" },
                onClick = { showGroupByDialog = true },
            )

            val connectionViewNames = stringArrayResource(R.array.connection_view_array)
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_view_carousel),
                title = stringResource(R.string.connection_view_title),
                summary = connectionViewNames.getOrElse(settingsData.connectionViewType.ordinal) { "" },
                onClick = { showConnectionViewDialog = true },
            )

            val apViewNames = stringArrayResource(R.array.ap_view_array)
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_view_carousel),
                title = stringResource(R.string.ap_view_title),
                summary = apViewNames.getOrElse(settingsData.accessPointView.ordinal) { "" },
                onClick = { showApViewDialog = true },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Graph Section
            SettingsCategoryHeader(title = stringResource(R.string.graph_maximum_y_title))
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_show_chart),
                title = stringResource(R.string.graph_maximum_y_title),
                summary = "${settingsData.graphMaximumY}dBm",
                onClick = { showGraphMaxYDialog = true },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Look & Feel Section
            SettingsCategoryHeader(title = stringResource(R.string.theme_title))
            val themeNames = stringArrayResource(R.array.theme_array)
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_color_lens),
                title = stringResource(R.string.theme_title),
                summary = themeNames.getOrElse(settingsData.themeStyle.ordinal) { "" },
                onClick = { showThemeDialog = true },
            )

            if (buildMinVersionS()) {
                SettingsSwitchItem(
                    icon = painterResource(R.drawable.ic_color_lens),
                    title = stringResource(R.string.dynamic_color_title),
                    summary = stringResource(R.string.dynamic_color_summary),
                    checked = settingsData.dynamicColor,
                    onCheckedChange = { settings.updateDynamicColor(it) },
                )
            }

            if (!buildMinVersionQ()) {
                SettingsSwitchItem(
                    icon = painterResource(R.drawable.ic_signal_wifi_off),
                    title = stringResource(R.string.wifi_off_on_exit_title),
                    checked = settingsData.wiFiOffOnExit,
                    onCheckedChange = { settings.updateWiFiOffOnExit(it) },
                )
            }

            SettingsSwitchItem(
                icon = painterResource(R.drawable.ic_brightness_low),
                title = stringResource(R.string.keep_screen_on_title),
                checked = settingsData.keepScreenOn,
                onCheckedChange = { settings.updateKeepScreenOn(it) },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Localization Section
            SettingsCategoryHeader(title = stringResource(R.string.language_title))
            val currentCountry =
                WiFiChannelCountry.findAll().find { it.countryCode == settingsData.countryCode }
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_location_on),
                title = stringResource(R.string.country_code_title),
                summary = currentCountry?.countryName(settingsData.languageLocale) ?: settingsData.countryCode,
                onClick = { showCountryDialog = true },
            )

            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_language),
                title = stringResource(R.string.language_title),
                summary =
                    settingsData.languageLocale
                        .getDisplayName(
                            settingsData.languageLocale,
                        ).toCapitalize(settingsData.languageLocale),
                onClick = { showLanguageDialog = true },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Actions Section
            SettingsCategoryHeader(title = stringResource(R.string.reset_title))
            SettingsPreferenceItem(
                icon = painterResource(R.drawable.ic_reset),
                title = stringResource(R.string.reset_title),
                summary = stringResource(R.string.reset_title),
                onClick = { showResetDialog = true },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Experimental Section
            SettingsCategoryHeader(title = stringResource(R.string.experimental_title))
            SettingsSwitchItem(
                icon = painterResource(R.drawable.ic_settings),
                title = stringResource(R.string.cache_off_title),
                checked = settingsData.cacheOff,
                onCheckedChange = { settings.updateCacheOff(it) },
            )
        }
    }

    // Dialogs
    if (showScanSpeedDialog) {
        val scanSpeeds = listOf(1, 3, 5, 10, 15, 20, 30)
        ListChoiceDialog(
            title = stringResource(R.string.scan_speed_title),
            items = scanSpeeds.map { "$it seconds" },
            selectedIndex = scanSpeeds.indexOf(settingsData.scanSpeed).coerceAtLeast(0),
            onItemSelected = { index ->
                settings.updateScanSpeed(scanSpeeds[index])
                showScanSpeedDialog = false
            },
            onDismiss = { showScanSpeedDialog = false },
        )
    }

    if (showSortByDialog) {
        val sortByNames = stringArrayResource(R.array.sort_by_array).toList()
        ListChoiceDialog(
            title = stringResource(R.string.sort_by_title),
            items = sortByNames,
            selectedIndex = settingsData.sortBy.ordinal,
            onItemSelected = { index ->
                settings.updateSortBy(SortBy.entries[index])
                showSortByDialog = false
            },
            onDismiss = { showSortByDialog = false },
        )
    }

    if (showGroupByDialog) {
        val groupByNames = stringArrayResource(R.array.group_by_array).toList()
        ListChoiceDialog(
            title = stringResource(R.string.group_by_title),
            items = groupByNames,
            selectedIndex = settingsData.groupBy.ordinal,
            onItemSelected = { index ->
                settings.updateGroupBy(GroupBy.entries[index])
                showGroupByDialog = false
            },
            onDismiss = { showGroupByDialog = false },
        )
    }

    if (showConnectionViewDialog) {
        val connectionViewNames = stringArrayResource(R.array.connection_view_array).toList()
        ListChoiceDialog(
            title = stringResource(R.string.connection_view_title),
            items = connectionViewNames,
            selectedIndex = settingsData.connectionViewType.ordinal,
            onItemSelected = { index ->
                settings.updateConnectionView(ConnectionViewType.entries[index])
                showConnectionViewDialog = false
            },
            onDismiss = { showConnectionViewDialog = false },
        )
    }

    if (showApViewDialog) {
        val apViewNames = stringArrayResource(R.array.ap_view_array).toList()
        ListChoiceDialog(
            title = stringResource(R.string.ap_view_title),
            items = apViewNames,
            selectedIndex = settingsData.accessPointView.ordinal,
            onItemSelected = { index ->
                settings.updateAccessPointView(AccessPointViewType.entries[index])
                showApViewDialog = false
            },
            onDismiss = { showApViewDialog = false },
        )
    }

    if (showGraphMaxYDialog) {
        val graphMaxYOptions = listOf(0, -10, -20, -30, -40, -50)
        ListChoiceDialog(
            title = stringResource(R.string.graph_maximum_y_title),
            items = graphMaxYOptions.map { "${it}dBm" },
            selectedIndex = graphMaxYOptions.indexOf(settingsData.graphMaximumY).coerceAtLeast(0),
            onItemSelected = { index ->
                settings.updateGraphMaximumY(graphMaxYOptions[index])
                showGraphMaxYDialog = false
            },
            onDismiss = { showGraphMaxYDialog = false },
        )
    }

    if (showThemeDialog) {
        val themeNames = stringArrayResource(R.array.theme_array).toList()
        ListChoiceDialog(
            title = stringResource(R.string.theme_title),
            items = themeNames,
            selectedIndex = settingsData.themeStyle.ordinal,
            onItemSelected = { index ->
                settings.updateTheme(ThemeStyle.entries[index])
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showCountryDialog) {
        val countries =
            remember(settingsData.languageLocale) {
                WiFiChannelCountry
                    .findAll()
                    .map { Data(it.countryCode, it.countryName(settingsData.languageLocale)) }
                    .sorted()
            }
        SearchableListChoiceDialog(
            title = stringResource(R.string.country_code_title),
            items = countries,
            selectedCode = settingsData.countryCode,
            onItemSelected = { selected ->
                settings.updateCountryCode(selected.code)
                showCountryDialog = false
            },
            onDismiss = { showCountryDialog = false },
        )
    }

    if (showLanguageDialog) {
        val languages =
            remember {
                supportedLanguages()
                    .map {
                        Data(
                            toLanguageTag(it),
                            it.getDisplayName(it).toCapitalize(Locale.getDefault()),
                        )
                    }.sorted()
            }
        SearchableListChoiceDialog(
            title = stringResource(R.string.language_title),
            items = languages,
            selectedCode = toLanguageTag(settingsData.languageLocale),
            onItemSelected = { selected ->
                settings.updateLanguage(selected.code)
                applyLocale(context, findByLanguageTag(selected.code))
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_title)) },
            text = { Text(stringResource(R.string.reset_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        settings.reset()
                        showResetDialog = false
                    },
                ) {
                    Text(stringResource(R.string.filter_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.filter_close))
                }
            },
        )
    }
}

@Composable
private fun SettingsCategoryHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SettingsPreferenceItem(
    icon: Painter,
    title: String,
    summary: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (summary.isNotBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: Painter,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String = "",
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (summary.isNotBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ListChoiceDialog(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                items.forEachIndexed { index, text ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = index == selectedIndex,
                                    onClick = { onItemSelected(index) },
                                    role = Role.RadioButton,
                                ).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = text, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.filter_close))
            }
        },
    )
}

@Composable
private fun SearchableListChoiceDialog(
    title: String,
    items: List<Data>,
    selectedCode: String,
    onItemSelected: (Data) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems =
        remember(searchQuery, items) {
            if (searchQuery.isBlank()) {
                items
            } else {
                items.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                        it.code.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(filteredItems) { item ->
                        val isSelected = item.code == selectedCode
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { onItemSelected(item) },
                                        role = Role.RadioButton,
                                    ).padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.name} (${item.code})",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.filter_close))
            }
        },
    )
}
