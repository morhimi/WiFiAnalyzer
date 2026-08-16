/*
package com.vrem.wifianalyzer.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.MainContextHelper
import com.vrem.wifianalyzer.R
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationMenuTest {
    private val scannerService = MainContextHelper.INSTANCE.scannerService

    @After
    fun tearDown() {
        MainContextHelper.INSTANCE.restore()
    }

    @Test
    fun navigationOptions() {
        // validate
        assertThat(NavigationMenu.ACCESS_POINTS.navigationItem).isEqualTo(navigationOptionAp)
        assertThat(NavigationMenu.CHANNEL_RATING.navigationItem).isEqualTo(navigationOptionRating)
        assertThat(NavigationMenu.CHANNEL_GRAPH.navigationItem).isEqualTo(navigationOptionOther)
        assertThat(NavigationMenu.TIME_GRAPH.navigationItem).isEqualTo(navigationOptionOther)
        assertThat(NavigationMenu.EXPORT.navigationItem).isEqualTo(navigationOptionOff)
        assertThat(NavigationMenu.CHANNEL_AVAILABLE.navigationItem).isEqualTo(navigationOptionOff)
        assertThat(NavigationMenu.VENDORS.navigationItem).isEqualTo(navigationOptionOff)
        assertThat(NavigationMenu.SETTINGS.navigationItem).isEqualTo(navigationOptionOff)
        assertThat(NavigationMenu.ABOUT.navigationItem).isEqualTo(navigationOptionOff)
    }
}
*/
