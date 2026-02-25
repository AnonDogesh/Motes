package com.example.motes.data.repository

import com.example.motes.data.dao.NoteDao
import com.example.motes.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class DefaultNoteRepository(
    private val noteDao: NoteDao
) : NoteRepository {
    override fun observeById(id: String): Flow<NoteEntity?> = noteDao.observeById(id)

    override fun observeActive(): Flow<List<NoteEntity>> = noteDao.observeActive()

    override fun observePinned(): Flow<List<NoteEntity>> = noteDao.observePinned()

    override fun observeArchived(): Flow<List<NoteEntity>> = noteDao.observeArchived()

    override suspend fun upsert(note: NoteEntity) {
        noteDao.upsert(note)
    }

    override suspend fun delete(note: NoteEntity) {
        noteDao.delete(note)
    }
}
