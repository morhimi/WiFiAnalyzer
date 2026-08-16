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
package com.vrem.wifianalyzer.permission

import android.content.Context
import com.vrem.annotation.OpenClass

@OpenClass
class PermissionService(
    private val context: Context,
    private val locationPermission: LocationPermission = LocationPermission(context),
    private val applicationPermission: ApplicationPermission = ApplicationPermission(context),
) {
    fun enabled(): Boolean = locationEnabled() && permissionGranted()

    fun locationEnabled(): Boolean = locationPermission.enabled()

    fun check(
        context: Context? = null,
        onOk: () -> Unit = {},
        onCancel: () -> Unit = {},
    ): Unit =
        applicationPermission.check(
            targetContext = context ?: this.context,
            onOk = onOk,
            onCancel = onCancel,
        )

    fun permissionGranted(): Boolean = applicationPermission.granted()
}
