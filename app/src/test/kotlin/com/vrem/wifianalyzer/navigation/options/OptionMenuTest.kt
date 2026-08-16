/*
package com.vrem.wifianalyzer.navigation.options

import android.os.Build
import android.view.Menu
import android.view.MenuItem
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vrem.wifianalyzer.MainActivity
import com.vrem.wifianalyzer.MainContextHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class OptionMenuTest {
    private val mainActivity: MainActivity = mock()
    private val menu: Menu = mock()
    private val menuItem: MenuItem = mock()
    private val scannerService = MainContextHelper.INSTANCE.scannerService
    private val settings = MainContextHelper.INSTANCE.settings

    private val fixture = OptionMenu.create(mainActivity)

    @After
    fun tearDown() {
        verifyNoMoreInteractions(mainActivity)
        verifyNoMoreInteractions(menu)
        verifyNoMoreInteractions(menuItem)
        MainContextHelper.INSTANCE.restore()
    }

    @Test
    fun create() {
        // execute
        val actual = fixture.create(menu)
        // validate
        assertThat(actual).isTrue
        verify(menu).size()
    }
}
*/
