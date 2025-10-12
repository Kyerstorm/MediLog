package com.healthcalendar.app.data.database.entities

enum class FavoriteSortOrder {
    RECENTLY_ADDED,    // Sort by addedAt DESC
    ALPHABETICAL,      // Sort by medicationName ASC
    MOST_VIEWED        // Sort by viewCount DESC
}
