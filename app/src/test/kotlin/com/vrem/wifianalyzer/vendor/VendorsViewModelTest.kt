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
package com.vrem.wifianalyzer.vendor

import com.vrem.wifianalyzer.vendor.model.VendorService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class VendorsViewModelTest {
    private val vendorService: VendorService = mock()
    private val initialVendors = listOf("Apple", "Google", "Cisco")
    private lateinit var fixture: VendorsViewModel

    @Before
    fun setUp() {
        whenever(vendorService.findVendors("")).thenReturn(initialVendors)
        fixture = VendorsViewModel(vendorService)
    }

    @After
    fun tearDown() {
        verify(vendorService).findVendors("")
        verifyNoMoreInteractions(vendorService)
    }

    @Test
    fun shouldInitializeWithEmptySearchQuery() {
        assertThat(fixture.searchQuery.value).isEmpty()
    }

    @Test
    fun shouldInitializeWithDefaultVendors() =
        runTest {
            assertThat(fixture.vendors.value).isEqualTo(initialVendors)
        }

    @Test
    fun shouldUpdateSearchQuery() {
        // execute
        fixture.onSearchQueryChange("Apple")

        // assert
        assertThat(fixture.searchQuery.value).isEqualTo("Apple")
    }

    @Test
    fun shouldFindMacAddresses() {
        // arrange
        val vendorName = "Apple"
        val expectedMacs = listOf("00:11:22", "33:44:55")
        whenever(vendorService.findMacAddresses(vendorName)).thenReturn(expectedMacs)

        // execute
        val actual = fixture.findMacAddresses(vendorName)

        // assert
        assertThat(actual).isEqualTo(expectedMacs)
        verify(vendorService).findMacAddresses(vendorName)
    }
}
