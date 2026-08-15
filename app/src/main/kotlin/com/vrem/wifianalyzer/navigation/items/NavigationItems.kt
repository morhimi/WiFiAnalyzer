/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2026 VREMSoftwareDevelopment@gmail.com
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
package com.vrem.wifianalyzer.navigation.items

import com.vrem.wifianalyzer.export.Export

val navigationItemAccessPoints: NavigationItem = ComposeItem()
val navigationItemChannelRating: NavigationItem = ComposeItem()
val navigationItemChannelGraph: NavigationItem = ComposeItem()
val navigationItemTimeGraph: NavigationItem = ComposeItem()
val navigationItemExport: NavigationItem = ExportItem(Export())
val navigationItemChannelAvailable: NavigationItem = ComposeItem(false)
val navigationItemVendors: NavigationItem = ComposeItem(false)
val navigationItemSettings: NavigationItem = ComposeItem(false)
val navigationItemAbout: NavigationItem = ComposeItem(false)
