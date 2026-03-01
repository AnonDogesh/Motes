package com.example.motes.data.repository

import com.example.motes.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeById(id: Long): Flow<NoteEntity?>
    fun observeActive(): Flow<List<NoteEntity>>
    fun observePinned(): Flow<List<NoteEntity>>
    fun observeArchived(): Flow<List<NoteEntity>>

    suspend fun insert(note: NoteEntity): Long
    suspend fun update(note: NoteEntity)
    suspend fun delete(note: NoteEntity)
    suspend fun restore(id: Long)
    suspend fun deletePermanently(id: Long)
}
