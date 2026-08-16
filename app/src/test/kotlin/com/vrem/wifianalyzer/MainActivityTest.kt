/*
package com.vrem.wifianalyzer

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.navigation.NavigationView
import com.vrem.wifianalyzer.navigation.NavigationMenu
import com.vrem.wifianalyzer.navigation.NavigationMenuController
import com.vrem.wifianalyzer.navigation.options.OptionMenu
import com.vrem.wifianalyzer.settings.Settings
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class MainActivityTest {
    private val navigationMenuController: NavigationMenuController = mock()
    private val optionMenu: OptionMenu<MainActivity> = mock()
    private val menu: Menu = mock()
    private val menuItem: MenuItem = mock()
    private val drawerLayout: DrawerLayout = mock()
    private val drawerNavigationView: NavigationView = mock()
    private val settings: Settings = MainContextHelper.INSTANCE.settings
    private val scannerService = MainContextHelper.INSTANCE.scannerService
    private val permissionService = MainContextHelper.INSTANCE.permissionService

    private lateinit var fixture: MainActivity

    @Before
    fun setUp() {
        fixture = spy(Robolectric.buildActivity(MainActivity::class.java).get())
        doReturn(navigationMenuController).whenever(fixture).navigationMenuController
        doReturn(optionMenu).whenever(fixture).optionMenu
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(navigationMenuController)
        verifyNoMoreInteractions(optionMenu)
        verifyNoMoreInteractions(menu)
        verifyNoMoreInteractions(menuItem)
        verifyNoMoreInteractions(drawerLayout)
        verifyNoMoreInteractions(drawerNavigationView)
        verifyNoMoreInteractions(settings)
        verifyNoMoreInteractions(scannerService)
        verifyNoMoreInteractions(permissionService)
        MainContextHelper.INSTANCE.restore()
    }

    @Test
    fun onCreate() {
        // setup
        doReturn(drawerLayout).whenever(fixture).findViewById<View>(R.id.drawer_layout)
        doReturn(false).whenever(fixture).largeScreen
        // execute
        fixture.onCreate(null)
        // validate
        verify(fixture).setupToolbar()
        verify(fixture).largeScreen
        verify(fixture).findViewById<View>(R.id.drawer_layout)
        verify(navigationMenuController).create(drawerLayout)
        verify(settings).initializeDefaultValues()
        verify(fixture).onSharedPreferenceChanged(any(), any())
    }

    @Test
    fun onCreateOptionsMenu() {
        // execute
        val actual = fixture.onCreateOptionsMenu(menu)
        // validate
        assertThat(actual).isTrue
        verify(optionMenu).create(menu)
    }

    @Test
    fun onPrepareOptionsMenu() {
        // execute
        val actual = fixture.onPrepareOptionsMenu(menu)
        // validate
        assertThat(actual).isTrue
        verify(optionMenu).select(menu)
    }

    @Test
    fun onOptionsItemSelected() {
        // setup
        whenever(optionMenu.menuItem(menuItem)).thenReturn(true)
        // execute
        val actual = fixture.onOptionsItemSelected(menuItem)
        // validate
        assertThat(actual).isTrue
        verify(optionMenu).menuItem(menuItem)
    }

    @Test
    fun onConfigurationChanged() {
        // setup
        val configuration: android.content.res.Configuration = mock()
        // execute
        fixture.onConfigurationChanged(configuration)
        // validate
        verify(navigationMenuController).onConfigurationChanged(configuration)
    }

    @Test
    fun onPostCreate() {
        // execute
        fixture.onPostCreate(null)
        // validate
        verify(navigationMenuController).syncState()
    }

    @Test
    fun onNavigationItemSelected() {
        // setup
        whenever(navigationMenuController.onNavigationItemSelected(menuItem)).thenReturn(true)
        // execute
        val actual = fixture.onNavigationItemSelected(menuItem)
        // validate
        assertThat(actual).isTrue
        verify(navigationMenuController).onNavigationItemSelected(menuItem)
    }

    @Test
    fun onSharedPreferenceChanged() {
        // setup
        doReturn(null).whenever(fixture).findViewById<View>(R.id.drawer_layout)
        // execute
        fixture.onSharedPreferenceChanged(null, null)
        // validate
        verify(fixture).update()
    }

    @Test
    fun closeDrawer() {
        // setup
        doReturn(drawerLayout).whenever(fixture).findViewById<View>(R.id.drawer_layout)
        whenever(navigationMenuController.closeDrawer()).thenReturn(true)
        // execute
        val actual = fixture.closeDrawer()
        // validate
        assertThat(actual).isTrue
        verify(fixture).findViewById<View>(R.id.drawer_layout)
        verify(navigationMenuController).closeDrawer()
    }

    @Test
    fun optionMenu() {
        // execute
        val actual = fixture.optionMenu
        // validate
        assertThat(actual).isNotNull
    }

    @Test
    fun navigationMenuController() {
        // execute
        val actual = fixture.navigationMenuController
        // validate
        assertThat(actual).isNotNull
    }

    @Test
    fun currentMenuItem() {
        // setup
        whenever(navigationMenuController.currentMenuItem).thenReturn(menuItem)
        // execute
        val actual = fixture.currentMenuItem()
        // validate
        assertThat(actual).isEqualTo(menuItem)
        verify(navigationMenuController).currentMenuItem
    }

    @Test
    fun currentNavigationMenu() {
        // setup
        whenever(navigationMenuController.currentNavigationMenu).thenReturn(NavigationMenu.ACCESS_POINTS)
        // execute
        val actual = fixture.currentNavigationMenu()
        // validate
        assertThat(actual).isEqualTo(NavigationMenu.ACCESS_POINTS)
        verify(navigationMenuController).currentNavigationMenu
    }

    @Test
    fun currentNavigationMenuWithNavigationMenu() {
        // execute
        fixture.currentNavigationMenu(NavigationMenu.ACCESS_POINTS)
        // validate
        verify(navigationMenuController).currentNavigationMenu = NavigationMenu.ACCESS_POINTS
    }

    @Test
    fun navigationView() {
        // setup
        doReturn(drawerNavigationView).whenever(fixture).findViewById<View>(R.id.drawer_layout)
        whenever(navigationMenuController.navigationView).thenReturn(drawerNavigationView)
        // execute
        val actual = fixture.navigationView()
        // validate
        assertThat(actual).isEqualTo(drawerNavigationView)
        verify(navigationMenuController).navigationView
    }
}
*/
