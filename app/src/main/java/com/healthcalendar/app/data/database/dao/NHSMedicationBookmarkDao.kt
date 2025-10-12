package com.healthcalendar.app.data.database.dao

import androidx.room.*
import com.healthcalendar.app.data.database.entities.NHSMedicationBookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface NHSMedicationBookmarkDao {
    
    @Query("SELECT * FROM nhs_medication_bookmarks WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<NHSMedicationBookmark>>
    
    @Query("SELECT * FROM nhs_medication_bookmarks WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoritesRecentlyAdded(): Flow<List<NHSMedicationBookmark>>
    
    @Query("SELECT * FROM nhs_medication_bookmarks WHERE isFavorite = 1 ORDER BY medicationName ASC")
    fun getFavoritesAlphabetical(): Flow<List<NHSMedicationBookmark>>
    
    @Query("SELECT * FROM nhs_medication_bookmarks WHERE isFavorite = 1 ORDER BY viewCount DESC")
    fun getFavoritesMostViewed(): Flow<List<NHSMedicationBookmark>>
    
    @Query("SELECT * FROM nhs_medication_bookmarks WHERE viewedAt IS NOT NULL ORDER BY viewedAt DESC LIMIT 10")
    fun getRecentlyViewed(): Flow<List<NHSMedicationBookmark>>
    
    @Query("SELECT * FROM nhs_medication_bookmarks WHERE medicationName = :name LIMIT 1")
    suspend fun getBookmarkByName(name: String): NHSMedicationBookmark?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: NHSMedicationBookmark): Long
    
    @Update
    suspend fun updateBookmark(bookmark: NHSMedicationBookmark)
    
    @Delete
    suspend fun deleteBookmark(bookmark: NHSMedicationBookmark)
    
    @Query("UPDATE nhs_medication_bookmarks SET isFavorite = :isFavorite WHERE medicationName = :name")
    suspend fun setFavorite(name: String, isFavorite: Boolean)
    
    @Query("UPDATE nhs_medication_bookmarks SET viewedAt = :viewedAt, viewCount = viewCount + 1 WHERE medicationName = :name")
    suspend fun recordView(name: String, viewedAt: kotlinx.datetime.LocalDateTime)
}
