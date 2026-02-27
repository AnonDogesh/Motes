package com.example.motes.data.repository

import com.example.motes.data.entity.DrawingEntity
import kotlinx.coroutines.flow.Flow

interface DrawingRepository {
    fun observeById(id: String): Flow<DrawingEntity?>
    fun observeActive(): Flow<List<DrawingEntity>>
    fun observePinned(): Flow<List<DrawingEntity>>
    fun observeArchived(): Flow<List<DrawingEntity>>

    suspend fun upsert(drawing: DrawingEntity)
    suspend fun delete(drawing: DrawingEntity)
}
