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
package com.vrem.wifianalyzer.di

import android.content.Context
import android.content.res.Configuration
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.vrem.wifianalyzer.permission.PermissionService
import com.vrem.wifianalyzer.settings.Repository
import com.vrem.wifianalyzer.settings.Settings
import com.vrem.wifianalyzer.settings.SettingsRepository
import com.vrem.wifianalyzer.vendor.model.VendorService
import com.vrem.wifianalyzer.wifi.filter.adapter.FiltersAdapter
import com.vrem.wifianalyzer.wifi.manager.WiFiManagerWrapper
import com.vrem.wifianalyzer.wifi.model.ApAliasService
import com.vrem.wifianalyzer.wifi.scanner.ScannerService
import com.vrem.wifianalyzer.wifi.scanner.makeScannerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.vrem.wifianalyzer.Configuration as WiFiConfiguration

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") },
        )

    @Provides
    @Singleton
    fun provideSettings(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository,
    ): Settings = Settings(Repository(context), settingsRepository)

    @Provides
    @Singleton
    fun provideWiFiManager(
        @ApplicationContext context: Context,
    ): WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @Provides
    @Singleton
    fun provideWiFiManagerWrapper(wifiManager: WifiManager): WiFiManagerWrapper = WiFiManagerWrapper(wifiManager)

    @Provides
    @Singleton
    fun providePermissionService(
        @ApplicationContext context: Context,
    ): PermissionService = PermissionService(context)

    @Provides
    @Singleton
    fun provideVendorService(
        @ApplicationContext context: Context,
    ): VendorService = VendorService(context.resources)

    @Provides
    @Singleton
    fun provideApAliasService(settingsRepository: SettingsRepository): ApAliasService =
        ApAliasService(settingsRepository)

    @Provides
    @Singleton
    fun provideConfiguration(
        @ApplicationContext context: Context,
    ): WiFiConfiguration {
        val configuration = context.resources.configuration
        val screenLayoutSize = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val largeScreen =
            screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE
        return WiFiConfiguration(largeScreen)
    }

    @Provides
    @Singleton
    fun provideScannerService(
        @ApplicationContext context: Context,
        wiFiManagerWrapper: WiFiManagerWrapper,
        permissionService: PermissionService,
        settings: Settings,
    ): ScannerService =
        makeScannerService(
            context,
            wiFiManagerWrapper,
            permissionService,
            Handler(Looper.getMainLooper()),
            settings,
        )

    @Provides
    @Singleton
    fun provideFiltersAdapter(settings: Settings): FiltersAdapter = FiltersAdapter(settings)
}
