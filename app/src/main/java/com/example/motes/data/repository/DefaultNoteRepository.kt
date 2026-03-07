package com.example.motes.data.repository

import com.example.motes.data.dao.NoteDao
import com.example.motes.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class DefaultNoteRepository(
    private val noteDao: NoteDao
) : NoteRepository {
    override fun observeById(id: Long): Flow<NoteEntity?> = noteDao.observeById(id)

    override fun observeActive(): Flow<List<NoteEntity>> = noteDao.observeActive()

    override fun observePinned(): Flow<List<NoteEntity>> = noteDao.observePinned()

    override fun observeArchived(): Flow<List<NoteEntity>> = noteDao.observeArchived()

    override suspend fun insert(note: NoteEntity): Long = noteDao.insert(note)

    override suspend fun update(note: NoteEntity) {
        noteDao.update(note)
    }

    override suspend fun delete(note: NoteEntity) {
        noteDao.delete(note)
    }

    override suspend fun restore(id: Long) {
        noteDao.setArchived(id = id, archived = false, updatedAt = System.currentTimeMillis())
    }

    override suspend fun deletePermanently(id: Long) {
        noteDao.deleteById(id)
    }
}
