package com.example.motes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.motes.data.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Upsert
    suspend fun upsert(checklist: ChecklistEntity)

    @Delete
    suspend fun delete(checklist: ChecklistEntity)

    @Query("SELECT * FROM checklists WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<ChecklistEntity?>

    @Query("SELECT * FROM checklists WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun observeActive(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklists WHERE isPinned = 1 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun observePinned(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklists WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun observeArchived(): Flow<List<ChecklistEntity>>
}
