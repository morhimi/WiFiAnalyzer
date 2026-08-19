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

import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget

class MarkerControllerWrapper(
    private val thresholdPx: Float,
    private val onAccepted: (Interaction, List<CartesianMarker.Target>) -> Unit,
) : CartesianMarkerController {
    override val acceptsLongPress: Boolean get() = false

    override fun shouldAcceptInteraction(
        interaction: Interaction,
        targets: List<CartesianMarker.Target>,
    ): Boolean {
        if (interaction is Interaction.Release || interaction is Interaction.Move) {
            return true
        }
        if (interaction !is Interaction.Press && interaction !is Interaction.Tap) return false
        val accepted =
            targets
                .filterIsInstance<LineCartesianLayerMarkerTarget>()
                .any { target ->
                    target.points.any { point ->
                        interaction.point.withinProximity(target.canvasX, point.canvasY, thresholdPx)
                    }
                }
        if (accepted) {
            onAccepted(interaction, targets)
        }
        return accepted
    }

    override fun shouldShowMarker(
        interaction: Interaction,
        targets: List<CartesianMarker.Target>,
    ): Boolean = interaction !is Interaction.Release
}
