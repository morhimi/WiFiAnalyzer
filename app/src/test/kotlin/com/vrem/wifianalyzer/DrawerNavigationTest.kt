/*
package com.vrem.wifianalyzer

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class DrawerNavigationTest {
    private val mainActivity: MainActivity = mock()
    private val drawerNavigation: DrawerNavigation<MainActivity> = mock()

    @After
    fun tearDown() {
        verifyNoMoreInteractions(mainActivity)
        verifyNoMoreInteractions(drawerNavigation)
    }

    @Test
    fun create() {
        // setup
        doReturn(mock<View>()).whenever(mainActivity).findViewById<View>(R.id.drawer_layout)
        val actual = DrawerNavigation.create(mainActivity)
        // validate
        assertThat(actual).isNotNull
        verify(mainActivity).findViewById<View>(R.id.drawer_layout)
    }
}
*/
