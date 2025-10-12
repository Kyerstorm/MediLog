package com.healthcalendar.app.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.healthcalendar.app.data.api.DrugsDotComService
import com.healthcalendar.app.data.api.LocalNHSMedicationService
import com.healthcalendar.app.data.api.NHSMedicationService
import com.healthcalendar.app.data.preferences.UserPreferencesRepository
import com.healthcalendar.app.util.network.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setPrettyPrinting()
            .create()
    }
    
    @Provides
    @Singleton
    fun provideNHSMedicationService(
        @ApplicationContext context: Context,
        gson: Gson
    ): NHSMedicationService {
        return LocalNHSMedicationService(context, gson)
    }
    
    @Provides
    @Singleton
    fun provideDrugsDotComService(
        @ApplicationContext context: Context,
        gson: Gson
    ): DrugsDotComService {
        return DrugsDotComService(context, gson)
    }
    
    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor {
        return NetworkMonitor(context)
    }
    
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }
}
