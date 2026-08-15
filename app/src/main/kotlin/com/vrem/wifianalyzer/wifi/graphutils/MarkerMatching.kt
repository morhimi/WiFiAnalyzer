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

import com.patrykandpatrick.vico.views.common.Point
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import kotlin.math.hypot

internal data class MarkerPoint(
    val entry: DataPoint,
    val canvasY: Float,
)

internal fun Point.distanceTo(
    canvasX: Float,
    canvasY: Float,
): Float {
    val distX = if (x.isNaN()) 0f else x - canvasX
    val distY = if (y.isNaN()) 0f else y - canvasY
    return hypot(distX, distY)
}

internal fun Point.withinProximity(
    canvasX: Float,
    canvasY: Float,
    thresholdPx: Float,
): Boolean {
    val distX = if (x.isNaN()) 0f else kotlin.math.abs(x - canvasX)
    if (distX > thresholdPx) return false
    val distY = if (y.isNaN()) 0f else y - canvasY
    if (distY >= -thresholdPx) return true
    return distanceTo(canvasX, canvasY) < thresholdPx
}

internal fun matchDetails(
    points: List<MarkerPoint>,
    canvasX: Float,
    touch: Point,
    thresholdPx: Float,
    pointMap: Map<Long, List<WiFiDetail>>,
): List<WiFiDetail> =
    points
        .asSequence()
        .filter { touch.withinProximity(canvasX, it.canvasY, thresholdPx) }
        .filter { pointMap.containsKey(it.entry.key) }
        .map {
            val dist =
                if (touch.y.isNaN() || touch.y < it.canvasY) {
                    touch.distanceTo(canvasX, it.canvasY)
                } else {
                    val distX = if (touch.x.isNaN()) 0f else kotlin.math.abs(touch.x - canvasX)
                    distX + (touch.y - it.canvasY) * 0.1f
                }
            it to dist
        }.flatMap { (point, dist) ->
            pointMap.getValue(point.entry.key).map { it to dist }
        }.sortedBy { it.second }
        .map { it.first }
        .distinct()
        .toList()
