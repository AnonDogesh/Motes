package com.example.motes.data.repository

import com.example.motes.data.dao.ChecklistDao
import com.example.motes.data.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow

class DefaultChecklistRepository(
    private val checklistDao: ChecklistDao
) : ChecklistRepository {
    override fun observeById(id: String): Flow<ChecklistEntity?> = checklistDao.observeById(id)

    override fun observeActive(): Flow<List<ChecklistEntity>> = checklistDao.observeActive()

    override fun observePinned(): Flow<List<ChecklistEntity>> = checklistDao.observePinned()

    override fun observeArchived(): Flow<List<ChecklistEntity>> = checklistDao.observeArchived()

    override suspend fun upsert(checklist: ChecklistEntity) {
        checklistDao.upsert(checklist)
    }

    override suspend fun delete(checklist: ChecklistEntity) {
        checklistDao.delete(checklist)
    }
}
