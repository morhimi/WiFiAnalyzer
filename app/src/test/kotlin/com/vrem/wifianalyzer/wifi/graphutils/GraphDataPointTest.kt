package com.vrem.wifianalyzer.wifi.graphutils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GraphDataPointTest {
    private val fixture: GraphDataPoint = GraphDataPoint(111, 222)

    @Test
    fun getX() {
        // Validate
        assertThat(fixture.xValue).isEqualTo(111)
    }

    @Test
    fun getY() {
        // Validate
        assertThat(fixture.yValue).isEqualTo(222)
    }
}
