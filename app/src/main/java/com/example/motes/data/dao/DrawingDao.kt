package com.example.motes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.motes.data.entity.DrawingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Upsert
    suspend fun upsert(drawing: DrawingEntity)

    @Delete
    suspend fun delete(drawing: DrawingEntity)

    @Query("SELECT * FROM drawings WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<DrawingEntity?>

    @Query("SELECT * FROM drawings WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun observeActive(): Flow<List<DrawingEntity>>

    @Query("SELECT * FROM drawings WHERE isPinned = 1 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun observePinned(): Flow<List<DrawingEntity>>

    @Query("SELECT * FROM drawings WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun observeArchived(): Flow<List<DrawingEntity>>
}
