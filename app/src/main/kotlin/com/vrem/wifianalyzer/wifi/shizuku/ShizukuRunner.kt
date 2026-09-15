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
package com.vrem.wifianalyzer.wifi.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

interface ShizukuRunner {
    fun isAvailable(): Boolean

    fun hasPermission(): Boolean

    fun requestPermission(requestCode: Int)

    fun execute(command: String): Boolean
}

internal val defaultBinderPinger: () -> Boolean = {
    runCatching { Shizuku.pingBinder() }.getOrDefault(false)
}

internal val defaultPermissionChecker: () -> Int = {
    runCatching { Shizuku.checkSelfPermission() }.getOrDefault(PackageManager.PERMISSION_DENIED)
}

internal val defaultPermissionRequester: (Int) -> Unit = { requestCode ->
    runCatching { Shizuku.requestPermission(requestCode) }
}

internal val defaultProcessLauncher: (Array<String>) -> Process? = { args ->
    runCatching {
        val method =
            Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java,
            )
        method.isAccessible = true
        method.invoke(null, args, null, null) as? Process
    }.getOrNull()
}

@Singleton
class DefaultShizukuRunner
    internal constructor(
        private val binderPinger: () -> Boolean = defaultBinderPinger,
        private val permissionChecker: () -> Int = defaultPermissionChecker,
        private val permissionRequester: (Int) -> Unit = defaultPermissionRequester,
        private val processLauncher: (Array<String>) -> Process? = defaultProcessLauncher,
    ) : ShizukuRunner {
        @Inject
        constructor() : this(
            binderPinger = defaultBinderPinger,
            permissionChecker = defaultPermissionChecker,
            permissionRequester = defaultPermissionRequester,
            processLauncher = defaultProcessLauncher,
        )

        override fun isAvailable(): Boolean = binderPinger()

        override fun hasPermission(): Boolean =
            if (!isAvailable()) {
                false
            } else {
                permissionChecker() == PackageManager.PERMISSION_GRANTED
            }

        override fun requestPermission(requestCode: Int) {
            if (isAvailable() && !hasPermission()) {
                permissionRequester(requestCode)
            }
        }

        override fun execute(command: String): Boolean =
            runCatching {
                if (!hasPermission()) return false
                val args = command.trim().split("\\s+".toRegex()).toTypedArray()
                val process = processLauncher(args)
                process?.waitFor() == 0
            }.getOrDefault(false)
    }
