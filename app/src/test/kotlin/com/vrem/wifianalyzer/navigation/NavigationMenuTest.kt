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
package com.vrem.wifianalyzer.navigation

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NavigationMenuTest {
    @Test
    fun mainNavigationContainsExpectedMenus() {
        // validate
        assertThat(MAIN_NAVIGATION).containsExactly(
            NavigationMenu.ACCESS_POINTS,
            NavigationMenu.CHANNEL_RATING,
            NavigationMenu.CHANNEL_GRAPH,
            NavigationMenu.TIME_GRAPH,
        )
    }

    @Test
    fun accessPointsScreenMapping() {
        assertThat(NavigationMenu.ACCESS_POINTS.screen).isEqualTo(Screen.AccessPoints)
        assertThat(NavigationMenu.ACCESS_POINTS.showFilter).isTrue
        assertThat(NavigationMenu.ACCESS_POINTS.showScanner).isTrue
        assertThat(NavigationMenu.ACCESS_POINTS.showWiFiBandSelector).isFalse
    }

    @Test
    fun channelRatingScreenMapping() {
        assertThat(NavigationMenu.CHANNEL_RATING.screen).isEqualTo(Screen.ChannelRating)
        assertThat(NavigationMenu.CHANNEL_RATING.showFilter).isFalse
        assertThat(NavigationMenu.CHANNEL_RATING.showScanner).isTrue
        assertThat(NavigationMenu.CHANNEL_RATING.showWiFiBandSelector).isTrue
    }

    @Test
    fun channelGraphScreenMapping() {
        assertThat(NavigationMenu.CHANNEL_GRAPH.screen).isEqualTo(Screen.ChannelGraph)
        assertThat(NavigationMenu.CHANNEL_GRAPH.showFilter).isTrue
        assertThat(NavigationMenu.CHANNEL_GRAPH.showScanner).isTrue
        assertThat(NavigationMenu.CHANNEL_GRAPH.showWiFiBandSelector).isTrue
    }

    @Test
    fun timeGraphScreenMapping() {
        assertThat(NavigationMenu.TIME_GRAPH.screen).isEqualTo(Screen.TimeGraph)
        assertThat(NavigationMenu.TIME_GRAPH.showFilter).isTrue
        assertThat(NavigationMenu.TIME_GRAPH.showScanner).isTrue
        assertThat(NavigationMenu.TIME_GRAPH.showWiFiBandSelector).isTrue
    }

    @Test
    fun exportScreenMapping() {
        assertThat(NavigationMenu.EXPORT.screen).isNull()
    }

    @Test
    fun otherScreensMapping() {
        assertThat(NavigationMenu.CHANNEL_AVAILABLE.screen).isEqualTo(Screen.ChannelAvailable)
        assertThat(NavigationMenu.VENDORS.screen).isEqualTo(Screen.Vendors)
        assertThat(NavigationMenu.SETTINGS.screen).isEqualTo(Screen.Settings)
        assertThat(NavigationMenu.PING.screen).isEqualTo(Screen.Ping)
        assertThat(NavigationMenu.ABOUT.screen).isEqualTo(Screen.About)
    }

    @Test
    fun findByDestinationWithNullReturnsAccessPoints() {
        val actual = NavigationMenu.findByDestination(null)
        assertThat(actual).isEqualTo(NavigationMenu.ACCESS_POINTS)
    }
}
