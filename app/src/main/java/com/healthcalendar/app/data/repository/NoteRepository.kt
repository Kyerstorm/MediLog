package com.healthcalendar.app.data.repository

import com.healthcalendar.app.data.database.dao.NoteDao
import com.healthcalendar.app.data.database.entities.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(noteId: Long): Note? = noteDao.getNoteById(noteId)
    
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    
    fun getPinnedNotes(): Flow<List<Note>> = noteDao.getPinnedNotes()
}
