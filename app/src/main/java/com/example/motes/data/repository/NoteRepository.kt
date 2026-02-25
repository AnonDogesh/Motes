package com.example.motes.data.repository

import com.example.motes.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeById(id: String): Flow<NoteEntity?>
    fun observeActive(): Flow<List<NoteEntity>>
    fun observePinned(): Flow<List<NoteEntity>>
    fun observeArchived(): Flow<List<NoteEntity>>

    suspend fun upsert(note: NoteEntity)
    suspend fun delete(note: NoteEntity)
    suspend fun restore(id: String)
    suspend fun deletePermanently(id: String)
}
