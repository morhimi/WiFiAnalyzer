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
package com.vrem.wifianalyzer.wifi.timegraph

import android.content.Context
import android.graphics.Paint
import android.view.View
import com.patrykandpatrick.vico.views.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import com.patrykandpatrick.vico.views.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.views.cartesian.Scroll
import com.patrykandpatrick.vico.views.cartesian.ScrollHandler
import com.patrykandpatrick.vico.views.cartesian.Zoom
import com.patrykandpatrick.vico.views.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.views.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.settings.SettingsData
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.band.WiFiBand
import com.vrem.wifianalyzer.wifi.graphutils.DataPoint
import com.vrem.wifianalyzer.wifi.graphutils.GraphBuilder
import com.vrem.wifianalyzer.wifi.graphutils.GraphNotifier
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewport
import com.vrem.wifianalyzer.wifi.graphutils.GraphWrapper
import com.vrem.wifianalyzer.wifi.graphutils.LabelPosition
import com.vrem.wifianalyzer.wifi.graphutils.MAX_SCAN_COUNT
import com.vrem.wifianalyzer.wifi.graphutils.MIN_Y
import com.vrem.wifianalyzer.wifi.graphutils.SeriesData
import com.vrem.wifianalyzer.wifi.graphutils.SeriesLabel
import com.vrem.wifianalyzer.wifi.graphutils.canvasY
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.predicate.Predicate
import com.vrem.wifianalyzer.wifi.predicate.makeOtherPredicate

private const val NUM_X_TIME = 21

internal class TimeLayerRangeProvider(
    private val graphMaximumY: Int = -20,
) : CartesianLayerRangeProvider {
    override fun getMinX(
        minX: Double,
        maxX: Double,
        extraStore: ExtraStore,
    ): Double {
        val clamped = minX.coerceAtLeast(maxX - MAX_SCAN_COUNT)
        return clamped - (clamped % 2)
    }

    override fun getMaxX(
        minX: Double,
        maxX: Double,
        extraStore: ExtraStore,
    ) = maxX.coerceAtLeast(NUM_X_TIME.toDouble())

    override fun getMinY(
        minY: Double,
        maxY: Double,
        extraStore: ExtraStore,
    ) = MIN_Y.toDouble()

    override fun getMaxY(
        minY: Double,
        maxY: Double,
        extraStore: ExtraStore,
    ) = graphMaximumY.toDouble()
}

internal fun calculateLabelPosition(
    context: CartesianDrawingContext,
    seriesData: SeriesData,
): LabelPosition? {
    if (seriesData.dataPoints.isEmpty() || seriesData.title.isEmpty()) return null
    val point = seriesData.dataPoints.last()
    if (point.y <= MIN_Y) return null
    return with(context) {
        val canvasX = layerBounds.right - spToPx(2f)
        LabelPosition(canvasX, canvasY(point), Paint.Align.RIGHT)
    }
}

internal fun makeGraph(
    graphMaximumY: Int,
    themeStyle: ThemeStyle,
    context: Context,
): CartesianChartView =
    GraphBuilder(graphMaximumY, themeStyle)
        .setItemPlacer(HorizontalAxis.ItemPlacer.aligned(spacing = { 2 }, shiftExtremeLines = false))
        .setVerticalTitle(context.getString(R.string.graph_axis_y))
        .setHorizontalTitle(context.getString(R.string.graph_time_axis_x))
        .build(context, false)

internal fun makeGraphWrapper(
    context: Context,
    graphMaximumY: Int = -20,
    themeStyle: ThemeStyle = ThemeStyle.DARK,
    onShowWiFiDetails: (List<WiFiDetail>) -> Unit = {},
): GraphWrapper {
    val chartView = makeGraph(graphMaximumY, themeStyle, context)
    val seriesLabel = SeriesLabel(::calculateLabelPosition)
    val scrollHandler = ScrollHandler(true, Scroll.Absolute.End, Scroll.Absolute.End, AutoScrollCondition.OnModelGrowth)
    val graphViewport =
        GraphViewport(
            rangeProvider = TimeLayerRangeProvider(graphMaximumY),
            scrollHandler = scrollHandler,
            placeholderDataPoints = (0..NUM_X_TIME).map { DataPoint(it, MIN_Y) },
            initialZoom = Zoom.x(NUM_X_TIME.toDouble()),
        )
    return GraphWrapper(
        graphViewport = graphViewport,
        chartView = chartView,
        seriesLabel = seriesLabel,
        onShowWiFiDetails = onShowWiFiDetails,
    )
}

internal class TimeGraph(
    private val wiFiBand: WiFiBand,
    private val dataManager: DataManager = DataManager(),
    private val graphWrapper: GraphWrapper,
) : GraphNotifier {
    constructor(
        wiFiBand: WiFiBand,
        context: Context,
        onShowWiFiDetails: (List<WiFiDetail>) -> Unit = {},
    ) : this(
        wiFiBand = wiFiBand,
        dataManager = DataManager(),
        graphWrapper = makeGraphWrapper(context, onShowWiFiDetails = onShowWiFiDetails),
    )

    private var wasSelected: Boolean = false

    override fun update(
        wiFiData: WiFiData,
        settingsData: SettingsData,
    ) {
        if (!selected(settingsData)) {
            wasSelected = false
            graphWrapper.gone()
            return
        }
        if (!wasSelected) {
            dataManager.reset(graphWrapper)
        }
        wasSelected = true
        val sortBy = settingsData.sortBy
        val levelMax = settingsData.graphMaximumY
        val predicate = predicate(settingsData)
        val wiFiDetails = wiFiData.wiFiDetails(predicate, sortBy)
        val newSeries = dataManager.addSeriesData(graphWrapper, wiFiDetails, levelMax)
        graphWrapper.removeSeries(newSeries)
        graphWrapper.show()
    }

    fun predicate(settingsData: SettingsData): Predicate = makeOtherPredicate(settingsData)

    private fun selected(settingsData: SettingsData): Boolean = wiFiBand == settingsData.wiFiBand

    override fun graph(): View = graphWrapper.chartView

    override fun destroy() = graphWrapper.destroy()
}
