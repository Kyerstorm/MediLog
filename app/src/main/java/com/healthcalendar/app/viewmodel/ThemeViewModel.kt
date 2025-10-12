package com.healthcalendar.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcalendar.app.data.repository.AppSettingsRepository
import com.healthcalendar.app.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing app theme settings
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {
    
    private val _currentTheme = MutableStateFlow(AppTheme.DEFAULT)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _useDynamicColors = MutableStateFlow(false)
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()

    private val _useCustomTheme = MutableStateFlow(false)
    val useCustomTheme: StateFlow<Boolean> = _useCustomTheme.asStateFlow()

    private val _customThemeJson = MutableStateFlow<String?>(null)
    val customThemeJson: StateFlow<String?> = _customThemeJson.asStateFlow()
    
    private val _notificationSoundUri = MutableStateFlow<String?>(null)
    val notificationSoundUri: StateFlow<String?> = _notificationSoundUri.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _currentTheme.value = settings.getTheme()
                _isDarkMode.value = settings.isDarkMode
                _useDynamicColors.value = settings.useDynamicColors
                _useCustomTheme.value = settings.useCustomTheme
                _customThemeJson.value = settings.customThemeJson
                _notificationSoundUri.value = settings.notificationSoundUri
            }
        }
    }
    
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            settingsRepository.updateDarkMode(!_isDarkMode.value)
        }
    }
    
    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDarkMode(isDark)
        }
    }
    
    fun toggleDynamicColors() {
        viewModelScope.launch {
            settingsRepository.updateDynamicColors(!_useDynamicColors.value)
        }
    }

    fun setUseCustomTheme(useCustom: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateUseCustomTheme(useCustom)
        }
    }

    fun setCustomThemeJson(json: String?) {
        viewModelScope.launch {
            settingsRepository.updateCustomThemeJson(json)
        }
    }
    
    fun setNotificationSound(soundUri: String?) {
        viewModelScope.launch {
            settingsRepository.updateNotificationSound(soundUri)
        }
    }
}
