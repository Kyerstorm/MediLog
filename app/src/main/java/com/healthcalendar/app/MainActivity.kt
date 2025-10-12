package com.healthcalendar.app

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import com.healthcalendar.app.data.preferences.UserPreferencesRepository
import com.healthcalendar.app.ui.navigation.AppNavigation
import com.healthcalendar.app.ui.theme.HealthCalendarTheme
import com.healthcalendar.app.ui.theme.ThemeProvider
import com.healthcalendar.app.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.SharedPreferences

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository
    
    private val themeViewModel: ThemeViewModel by viewModels()
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            // You can show a message if critical permissions are denied
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure widgets are refreshed once after install/update. Some OEM launchers
        // (notably some Vivo launchers) don't refresh widget previews automatically.
        try {
            // Use simple SharedPreferences file to avoid adding androidx.preference dependency
            val prefs: SharedPreferences = getSharedPreferences("medilog_prefs", MODE_PRIVATE)
            val refreshed = prefs.getBoolean("widgets_refreshed_after_update", false)
            if (!refreshed) {
                val appWidgetManager = AppWidgetManager.getInstance(this)
                val cn = ComponentName(this, com.healthcalendar.app.widget.MedicationWidget::class.java)
                val ids = appWidgetManager.getAppWidgetIds(cn)
                if (ids != null && ids.isNotEmpty()) {
                    for (id in ids) {
                        com.healthcalendar.app.widget.updateAppWidget(this, appWidgetManager, id)
                    }
                }
                prefs.edit().putBoolean("widgets_refreshed_after_update", true).apply()
            }
        } catch (t: Throwable) {
            // non-fatal; ignore
        }

        // Request necessary permissions on startup
        requestAppPermissions()
        
        setContent {
            // Get theme settings from ThemeViewModel
            val appTheme by themeViewModel.currentTheme.collectAsState()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val useDynamicColors by themeViewModel.useDynamicColors.collectAsState()
            val useCustomTheme by themeViewModel.useCustomTheme.collectAsState()
            val customThemeJson by themeViewModel.customThemeJson.collectAsState()
            
            // Get other preferences
            val fontScale by preferencesRepository.fontScale.collectAsState(initial = 1.0f)
            val language by preferencesRepository.language.collectAsState(
                initial = UserPreferencesRepository.Language.ENGLISH
            )
            
            // Apply language
            val locale = when (language) {
                UserPreferencesRepository.Language.ENGLISH -> Locale.ENGLISH
                UserPreferencesRepository.Language.SPANISH -> Locale("es")
                UserPreferencesRepository.Language.FRENCH -> Locale.FRENCH
                UserPreferencesRepository.Language.GERMAN -> Locale.GERMAN
                UserPreferencesRepository.Language.ITALIAN -> Locale.ITALIAN
                UserPreferencesRepository.Language.PORTUGUESE -> Locale("pt")
                UserPreferencesRepository.Language.CHINESE -> Locale.CHINESE
                UserPreferencesRepository.Language.JAPANESE -> Locale.JAPANESE
                UserPreferencesRepository.Language.KOREAN -> Locale.KOREAN
            }
            
            val configuration = Configuration(resources.configuration).apply {
                setLocale(locale)
            }
            createConfigurationContext(configuration)
            
            // Apply font scale through density
            val defaultDensity = LocalDensity.current
            val scaledDensity = Density(
                density = defaultDensity.density,
                fontScale = defaultDensity.fontScale * fontScale
            )
            
            val customColors = if (useCustomTheme && !customThemeJson.isNullOrBlank()) {
                try {
                    ThemeProvider.parseThemeColorsFromJson(customThemeJson!!, appTheme, isDarkMode)
                } catch (e: Exception) {
                    null
                }
            } else null

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                HealthCalendarTheme(
                    darkTheme = isDarkMode,
                    appTheme = appTheme,
                    dynamicColor = useDynamicColors,
                    customColors = customColors
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
    
    private fun requestAppPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        
        // Read media images permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Read external storage for older versions
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        // Request all missing permissions
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
