/*
package com.vrem.wifianalyzer.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ApplicationPermissionTest {
    private val context: Context = mock()
    private lateinit var fixture: ApplicationPermission

    @Before
    fun setUp() {
        fixture = ApplicationPermission(context)
    }

    @Test
    fun grantedWithRequestCodeAndGrantResults() {
        // execute
        val actual = fixture.granted(ApplicationPermission.REQUEST_CODE, intArrayOf(PackageManager.PERMISSION_GRANTED))
        // validate
        assertThat(actual).isTrue
    }
}
*/
