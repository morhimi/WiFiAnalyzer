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

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import androidx.core.graphics.withClip
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.vrem.wifianalyzer.wifi.model.WiFiDetail

typealias CalculateLabelPosition = (CartesianDrawingContext, SeriesData) -> LabelPosition?
typealias ConfigureLabel = (CartesianDrawingContext, LabelPosition, SeriesData, Paint) -> Unit

data class LabelPosition(
    val x: Float,
    val y: Float,
    val textAlign: Paint.Align = Paint.Align.CENTER,
)

internal fun CartesianDrawingContext.canvasY(point: DataPoint): Float {
    val yRange = ranges.getYRange(null)
    return layerBounds.bottom - ((point.y - yRange.minY) / yRange.length).toFloat() * layerBounds.height
}

internal val configureLabel: ConfigureLabel = { context, position, seriesData, paint ->
    paint.textAlign = position.textAlign
    paint.color = seriesData.graphColor.primary
    paint.textSize = with(context.density) { 13.sp.toPx() }
    paint.typeface = Typeface.create(Typeface.DEFAULT, if (seriesData.connected) Typeface.BOLD else Typeface.NORMAL)
    paint.setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
}

class SeriesLabel(
    val calculateLabelPosition: CalculateLabelPosition,
    val configure: ConfigureLabel = configureLabel,
    var seriesSnapshot: List<SeriesData> = emptyList(),
) : Decoration {
    internal val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var lastDrawingContext: CartesianDrawingContext? = null

    override fun drawOverLayers(context: CartesianDrawingContext) {
        lastDrawingContext = context
        if (seriesSnapshot.isEmpty()) return
        with(context) {
            val bounds = layerBounds
            val clipRect = RectF(bounds.left, bounds.top, bounds.right, bounds.bottom)
            val verticalOffset = with(density) { 6.sp.toPx() }
            canvas.nativeCanvas.withClip(clipRect) {
                seriesSnapshot.forEach { seriesData ->
                    calculateLabelPosition(context, seriesData)?.let { position ->
                        configure(context, position, seriesData, paint)
                        drawText(seriesData.title, position.x, position.y - verticalOffset, paint)
                    }
                }
            }
        }
    }

    fun findDetailsAt(
        canvasX: Float,
        canvasY: Float,
        chartWidth: Float = 0f,
        chartHeight: Float = 0f,
    ): List<WiFiDetail> {
        val validSeries = seriesSnapshot.filter { it.wiFiDetail != PLACEHOLDER_DETAIL && it.dataPoints.isNotEmpty() }
        if (validSeries.isEmpty()) return emptyList()

        val context = lastDrawingContext
        val minX: Double
        val maxX: Double
        val minY: Double
        val maxY: Double
        val left: Float
        val top: Float
        val width: Float
        val height: Float

        if (context != null) {
            val yRange = context.ranges.getYRange(null)
            val bounds = context.layerBounds
            minX = context.ranges.minX
            maxX = context.ranges.maxX
            minY = yRange.minY
            maxY = yRange.maxY
            left = bounds.left
            top = bounds.top
            width = bounds.width
            height = bounds.height
        } else {
            minX = 2400.0
            maxX = 2500.0
            minY = -100.0
            maxY = -20.0
            left = 0f
            top = 0f
            width = if (chartWidth > 0f) chartWidth else 1000f
            height = if (chartHeight > 0f) chartHeight else 800f
        }

        if (width <= 0f || height <= 0f || maxX <= minX || maxY <= minY) return emptyList()

        val tappedDataX = minX + ((canvasX - left) / width) * (maxX - minX)
        val tappedDataY = minY + ((top + height - canvasY) / height) * (maxY - minY)

        return validSeries
            .mapNotNull { seriesData ->
                val wiFiDetail = seriesData.wiFiDetail
                val wiFiSignal = wiFiDetail.wiFiSignal
                if (wiFiSignal.primaryFrequency > 0) {
                    val freqTolerance = 5.0
                    val startFreq = wiFiSignal.wiFiChannelStart.frequency - freqTolerance
                    val endFreq = wiFiSignal.wiFiChannelEnd.frequency + freqTolerance
                    val levelTolerance = 15.0
                    val maxLevel = wiFiSignal.level + levelTolerance

                    if (tappedDataX in startFreq..endFreq && tappedDataY <= maxLevel) {
                        val distFreq = kotlin.math.abs(tappedDataX - wiFiSignal.centerFrequency)
                        val distLevel = kotlin.math.abs(tappedDataY - wiFiSignal.level)
                        val dist = distFreq + distLevel * 0.5
                        wiFiDetail to dist
                    } else {
                        null
                    }
                } else {
                    val closestPointDist =
                        seriesData.dataPoints.minOfOrNull { dp ->
                            val distDataX = kotlin.math.abs(tappedDataX - dp.x)
                            val distDataY = kotlin.math.abs(tappedDataY - dp.y)
                            distDataX + distDataY
                        } ?: Double.MAX_VALUE
                    if (closestPointDist <= 20.0) {
                        wiFiDetail to closestPointDist
                    } else {
                        null
                    }
                }
            }.sortedBy { it.second }
            .map { it.first }
            .distinct()
    }
}
