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

import com.vrem.annotation.OpenClass
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.SIZE_MAX
import com.vrem.wifianalyzer.SIZE_MIN
import com.vrem.wifianalyzer.settings.ThemeStyle
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointDetail
import com.vrem.wifianalyzer.wifi.accesspoint.AccessPointPopup
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import info.appdev.charting.charts.LineChart
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener
import java.security.MessageDigest

@OpenClass
class GraphViewWrapper(
    val graphView: LineChart,
    var graphLegend: GraphLegend,
    private val themeStyle: ThemeStyle,
    private val seriesCache: SeriesCache = SeriesCache(),
    private val seriesOptions: SeriesOptions = SeriesOptions(),
) {
    init {
        graphView.data = LineData()
        graphView.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(entryFloat: EntryFloat, highlight: Highlight) {
                popup(entryFloat)
            }

            override fun onNothingSelected() {}
        })
    }

    fun removeSeries(newSeries: Set<WiFiDetail>) =
        seriesCache.remove(differenceSeries(newSeries)).forEach {
            seriesOptions.removeSeriesColor(it)
            graphView.data?.removeDataSet(it)
            graphView.notifyDataSetChanged()
            graphView.invalidate()
        }

    fun differenceSeries(newSeries: Set<WiFiDetail>): List<WiFiDetail> = seriesCache.difference(newSeries)

    fun addSeries(
        wiFiDetail: WiFiDetail,
        series: LineDataSet<EntryFloat>,
        drawBackground: Boolean,
    ): Boolean =
        if (seriesExists(wiFiDetail)) {
            false
        } else {
            seriesCache.put(wiFiDetail, series)
            val alias = runCatching { MainContext.INSTANCE.aliasRepository.alias(wiFiDetail.wiFiIdentifier.bssid) }.getOrNull()
            val secondLine = if (alias.isNullOrEmpty()) wiFiDetail.wiFiIdentifier.bssid else alias
            val label = "${wiFiDetail.wiFiIdentifier.ssid} ${wiFiDetail.wiFiSignal.channelDisplay()}\n($secondLine)"
            series.label = label
            seriesOptions.highlightConnected(series, wiFiDetail.wiFiAdditional.wiFiConnection.connected)
            seriesOptions.setSeriesColor(series)
            seriesOptions.drawBackground(series, drawBackground)
            seriesOptions.setupLabels(series, label)
            graphView.data?.addDataSet(series)
            graphView.notifyDataSetChanged()
            graphView.invalidate()
            true
        }

    fun updateSeries(
        wiFiDetail: WiFiDetail,
        data: Array<GraphDataPoint>,
        drawBackground: Boolean,
    ): Boolean =
        if (seriesExists(wiFiDetail)) {
            val series = seriesCache[wiFiDetail]
            series.clear()
            data.forEach { series.addEntry(it.toEntry()) }
            val alias = runCatching { MainContext.INSTANCE.aliasRepository.alias(wiFiDetail.wiFiIdentifier.bssid) }.getOrNull()
            val secondLine = if (alias.isNullOrEmpty()) wiFiDetail.wiFiIdentifier.bssid else alias
            val label = "${wiFiDetail.wiFiIdentifier.ssid} ${wiFiDetail.wiFiSignal.channelDisplay()}\n($secondLine)"
            series.label = label
            seriesOptions.highlightConnected(series, wiFiAdditional(wiFiDetail))
            seriesOptions.drawBackground(series, drawBackground)
            seriesOptions.setupLabels(series, label)
            graphView.notifyDataSetChanged()
            graphView.invalidate()
            true
        } else {
            false
        }

    private fun wiFiAdditional(wiFiDetail: WiFiDetail) =
        wiFiDetail.wiFiAdditional.wiFiConnection.connected

    fun appendToSeries(
        wiFiDetail: WiFiDetail,
        data: GraphDataPoint,
        count: Int,
        drawBackground: Boolean,
    ): Boolean =
        if (seriesExists(wiFiDetail)) {
            val series = seriesCache[wiFiDetail]
            if (series.entryCount > 0) {
                series.getEntryForIndex(series.entryCount - 1)?.data = null
            }
            series.addEntry(data.toEntry())
            if (series.entryCount > count + 1) {
                series.removeEntry(0)
            }
            seriesOptions.highlightConnected(series, wiFiAdditional(wiFiDetail))
            seriesOptions.drawBackground(series, drawBackground)
            graphView.notifyDataSetChanged()
            graphView.invalidate()
            true
        } else {
            false
        }

    fun newSeries(wiFiDetail: WiFiDetail): Boolean = !seriesExists(wiFiDetail)

    fun setViewport() {
        graphView.setVisibleXRangeMaximum(viewportCntX.toFloat())
        graphView.moveViewToX(0f)
    }

    fun setViewport(
        minX: Int,
        maxX: Int,
    ) {
        graphView.xAxis.axisMinimum = minX.toFloat()
        graphView.xAxis.axisMaximum = maxX.toFloat()
    }

    val viewportCntX: Int get() = graphView.xAxis.labelCount - 1

    fun addSeries(series: LineDataSet<EntryFloat>) {
        graphView.data?.addDataSet(series)
        graphView.notifyDataSetChanged()
        graphView.invalidate()
    }

    fun updateLegend(graphLegend: GraphLegend) {
        this.graphLegend = graphLegend
        graphLegend.display(graphView.legend)
        graphView.legend.textSize = 12f
        graphView.legend.textColor = themeStyle.colorGraphText
        graphView.invalidate()
    }

    fun calculateGraphType(): Int =
        runCatching {
            with(MessageDigest.getInstance("MD5")) {
                update(
                    MainContext.INSTANCE.mainActivity.packageName
                        .toByteArray(),
                )
                val digest: ByteArray = digest()
                digest.contentHashCode()
            }
        }.getOrDefault(TYPE1)

    fun setHorizontalLabelsVisible(horizontalLabelsVisible: Boolean) {
        graphView.xAxis.setDrawLabels(horizontalLabelsVisible)
    }

    fun visibility(visibility: Int) {
        graphView.visibility = visibility
    }

    fun size(value: Int): Int = if (value == TYPE1 || value == TYPE2 || value == TYPE3) SIZE_MAX else SIZE_MIN

    private fun seriesExists(wiFiDetail: WiFiDetail): Boolean = seriesCache.contains(wiFiDetail)

    private fun popup(entry: EntryFloat) {
        val dataSet = graphView.data?.getDataSetForEntry(entry) as? LineDataSet<EntryFloat>
        dataSet?.let {
            seriesCache.find(it)?.let { wiFiDetail ->
                runCatching {
                    AccessPointPopup().show(AccessPointDetail().makeViewDetailed(wiFiDetail), wiFiDetail)
                }
            }
        }
    }
}
