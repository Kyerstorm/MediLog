package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.NHSMedicationBookmarkDao
import com.healthcalendar.app.data.database.entities.NHSMedicationBookmark
import com.healthcalendar.app.data.database.entities.FavoriteSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NHSMedicationBookmarkRepository @Inject constructor(
    private val bookmarkDao: NHSMedicationBookmarkDao
) {
    fun getFavorites(): Flow<List<NHSMedicationBookmark>> {
        return bookmarkDao.getFavorites()
    }
    
    fun getFavorites(sortOrder: FavoriteSortOrder): Flow<List<NHSMedicationBookmark>> {
        return when (sortOrder) {
            FavoriteSortOrder.RECENTLY_ADDED -> bookmarkDao.getFavoritesRecentlyAdded()
            FavoriteSortOrder.ALPHABETICAL -> bookmarkDao.getFavoritesAlphabetical()
            FavoriteSortOrder.MOST_VIEWED -> bookmarkDao.getFavoritesMostViewed()
        }
    }
    
    fun getRecentlyViewed(): Flow<List<NHSMedicationBookmark>> {
        return bookmarkDao.getRecentlyViewed()
    }
    
    suspend fun toggleFavorite(medicationName: String, nhsUrl: String) {
        val existing = bookmarkDao.getBookmarkByName(medicationName)
        if (existing != null) {
            // Toggle existing bookmark
            bookmarkDao.setFavorite(medicationName, !existing.isFavorite)
        } else {
            // Create new bookmark with favorite = true
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            bookmarkDao.insertBookmark(
                NHSMedicationBookmark(
                    medicationName = medicationName,
                    nhsUrl = nhsUrl,
                    isFavorite = true,
                    addedAt = now
                )
            )
        }
    }
    
    suspend fun recordView(medicationName: String, nhsUrl: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val existing = bookmarkDao.getBookmarkByName(medicationName)
        
        if (existing != null) {
            // Update existing bookmark
            bookmarkDao.recordView(medicationName, now)
        } else {
            // Create new bookmark with view recorded
            bookmarkDao.insertBookmark(
                NHSMedicationBookmark(
                    medicationName = medicationName,
                    nhsUrl = nhsUrl,
                    viewedAt = now,
                    viewCount = 1,
                    addedAt = now
                )
            )
        }
    }
    
    suspend fun isFavorite(medicationName: String): Boolean {
        return bookmarkDao.getBookmarkByName(medicationName)?.isFavorite ?: false
    }
    
    suspend fun getViewCount(medicationName: String): Int {
        return bookmarkDao.getBookmarkByName(medicationName)?.viewCount ?: 0
    }
}
