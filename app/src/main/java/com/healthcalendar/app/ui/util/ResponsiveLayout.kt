package com.healthcalendar.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size class for responsive design
 */
enum class WindowSizeClass {
    COMPACT,    // Phone in portrait
    MEDIUM,     // Phone in landscape, small tablet
    EXPANDED    // Large tablet, foldable unfolded
}

/**
 * Get the current window size class based on screen width
 */
@Composable
fun getWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 600.dp -> WindowSizeClass.COMPACT
        screenWidth < 840.dp -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/**
 * Responsive padding values
 */
@Composable
fun responsivePadding(): Dp {
    return when (getWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 16.dp
        WindowSizeClass.MEDIUM -> 24.dp
        WindowSizeClass.EXPANDED -> 32.dp
    }
}

/**
 * Responsive content padding
 */
@Composable
fun contentPadding(): Dp {
    return when (getWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 16.dp
        WindowSizeClass.MEDIUM -> 32.dp
        WindowSizeClass.EXPANDED -> 48.dp
    }
}

/**
 * Number of columns for grid layouts
 * Adjusted for landscape orientation
 */
@Composable
fun gridColumns(): Int {
    val windowSize = getWindowSizeClass()
    val landscape = isLandscape()
    
    return when {
        windowSize == WindowSizeClass.COMPACT && landscape -> 2
        windowSize == WindowSizeClass.COMPACT -> 1
        windowSize == WindowSizeClass.MEDIUM && landscape -> 3
        windowSize == WindowSizeClass.MEDIUM -> 2
        windowSize == WindowSizeClass.EXPANDED && landscape -> 4
        else -> 3
    }
}

/**
 * Check if device is in landscape mode
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

/**
 * Get column weights for two-pane layouts in landscape
 * Returns Pair(leftWeight, rightWeight)
 */
@Composable
fun getLandscapeColumnWeights(): Pair<Float, Float> {
    return when (getWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 0.4f to 0.6f
        WindowSizeClass.MEDIUM -> 0.35f to 0.65f
        WindowSizeClass.EXPANDED -> 0.3f to 0.7f
    }
}

/**
 * Determine if should use two-pane layout
 */
@Composable
fun shouldUseTwoPane(): Boolean {
    val windowSize = getWindowSizeClass()
    val landscape = isLandscape()
    return (windowSize != WindowSizeClass.COMPACT) && landscape
}

/**
 * Get list item height based on orientation
 */
@Composable
fun listItemHeight(): Dp {
    return if (isLandscape()) 72.dp else 88.dp
}

/**
 * Get dialog width based on screen size
 */
@Composable
fun dialogWidth(): Dp {
    val windowSize = getWindowSizeClass()
    val landscape = isLandscape()
    
    return when {
        windowSize == WindowSizeClass.COMPACT && landscape -> 500.dp
        windowSize == WindowSizeClass.COMPACT -> 340.dp
        windowSize == WindowSizeClass.MEDIUM && landscape -> 600.dp
        windowSize == WindowSizeClass.MEDIUM -> 520.dp
        else -> 640.dp
    }
}

/**
 * Check if device is tablet or foldable
 */
@Composable
fun isTabletOrFoldable(): Boolean {
    return getWindowSizeClass() != WindowSizeClass.COMPACT
}
