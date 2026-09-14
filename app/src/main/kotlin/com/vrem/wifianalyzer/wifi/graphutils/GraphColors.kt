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
package com.vrem.wifianalyzer.wifi.graphutils

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.vrem.wifianalyzer.R

private fun String.toColor(): Int = this.substring(1).toLong(16).toInt()

data class GraphColor(
    @param:ColorInt val primary: Int,
    @param:ColorInt val background: Int,
)

internal val DEFAULT_GRAPH_COLORS: List<GraphColor> =
    listOf(
        "#FF9E9E9E" to "#339E9E9E",
        "#FFE91E63" to "#33E91E63",
        "#FFFF9800" to "#33FF9800",
        "#FF607D8B" to "#33607D8B",
        "#FF8BC34A" to "#338BC34A",
        "#FF03A9F4" to "#3303A9F4",
        "#FF3F51B5" to "#333F51B5",
        "#FFFF5722" to "#33FF5722",
        "#FFFFC107" to "#33FFC107",
        "#FF673AB7" to "#33673AB7",
        "#FF009688" to "#33009688",
        "#FF00BCD4" to "#3300BCD4",
        "#FFCDDC39" to "#33CDDC39",
        "#FFF44336" to "#33F44336",
        "#FFFFEB3B" to "#33FFEB3B",
        "#FF795548" to "#33795548",
        "#FF9C27B0" to "#339C27B0",
        "#FF4CAF50" to "#334CAF50",
    ).map { GraphColor(it.first.toColor(), it.second.toColor()) }.reversed()

internal val DEFAULT_CONNECTED_COLOR: GraphColor =
    GraphColor("#FF2196F3".toColor(), "#332196F3".toColor())

class GraphColors(
    private val context: Context? = null,
) {
    private val availableGraphColors: List<GraphColor> by lazy {
        val loaded =
            context
                ?.resources
                ?.getStringArray(R.array.graph_colors)
                ?.filterNotNull()
                ?.chunked(2) { GraphColor(it[0].toColor(), it[1].toColor()) }
                ?.reversed()
        if (loaded.isNullOrEmpty()) {
            DEFAULT_GRAPH_COLORS
        } else {
            loaded
        }
    }
    private val currentGraphColors: ArrayDeque<GraphColor> = ArrayDeque()
    val connectedColor: GraphColor by lazy {
        if (context != null) {
            val primary = ContextCompat.getColor(context, R.color.selected)
            val background = ContextCompat.getColor(context, R.color.selected_background)
            GraphColor(primary, background)
        } else {
            DEFAULT_CONNECTED_COLOR
        }
    }

    fun graphColor(): GraphColor {
        if (currentGraphColors.isEmpty()) {
            currentGraphColors.addAll(availableGraphColors)
        }
        return currentGraphColors.removeFirst()
    }

    fun addColor(
        @ColorInt primaryColor: Int,
    ) {
        availableGraphColors.firstOrNull { primaryColor == it.primary }?.let {
            if (!currentGraphColors.contains(it)) {
                currentGraphColors.addFirst(it)
            }
        }
    }
}
