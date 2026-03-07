package com.example.motes.data.repository

import com.example.motes.data.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {
    fun observeById(id: String): Flow<ChecklistEntity?>
    fun observeActive(): Flow<List<ChecklistEntity>>
    fun observePinned(): Flow<List<ChecklistEntity>>
    fun observeArchived(): Flow<List<ChecklistEntity>>

    suspend fun upsert(checklist: ChecklistEntity)
    suspend fun delete(checklist: ChecklistEntity)
    suspend fun restore(id: String)
    suspend fun deletePermanently(id: String)
}
