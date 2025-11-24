package com.healthcalendar.app

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.healthcalendar.app.data.repository.AppointmentRepository
import com.healthcalendar.app.util.HolidayService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HealthCalendarApplication : Application() {
	override fun onCreate() {
		super.onCreate()

		try {
			val entryPoint = EntryPointAccessors.fromApplication(this, AppointmentRepositoryEntryPoint::class.java)
			val repo = entryPoint.appointmentRepository()
			CoroutineScope(Dispatchers.IO).launch {
				HolidayService.seedUkBankHolidaysIfNeeded(this@HealthCalendarApplication, repo)
			}
		} catch (ex: Exception) {
			// Fail silently; seeding is best-effort. If Hilt not ready, skip seeding.
		}
	}
}


@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppointmentRepositoryEntryPoint {
	fun appointmentRepository(): AppointmentRepository
}
