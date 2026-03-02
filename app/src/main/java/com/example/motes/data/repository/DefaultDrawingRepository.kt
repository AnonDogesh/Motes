package com.example.motes.data.repository

import com.example.motes.data.dao.DrawingDao
import com.example.motes.data.entity.DrawingEntity
import kotlinx.coroutines.flow.Flow

class DefaultDrawingRepository(
    private val drawingDao: DrawingDao
) : DrawingRepository {
    override fun observeById(id: String): Flow<DrawingEntity?> = drawingDao.observeById(id)

    override fun observeActive(): Flow<List<DrawingEntity>> = drawingDao.observeActive()

    override fun observePinned(): Flow<List<DrawingEntity>> = drawingDao.observePinned()

    override fun observeArchived(): Flow<List<DrawingEntity>> = drawingDao.observeArchived()

    override suspend fun upsert(drawing: DrawingEntity) {
        drawingDao.upsert(drawing)
    }

    override suspend fun delete(drawing: DrawingEntity) {
        drawingDao.delete(drawing)
    }

    override suspend fun restore(id: String) {
        drawingDao.setArchived(id = id, archived = false, updatedAt = System.currentTimeMillis())
    }

    override suspend fun deletePermanently(id: String) {
        drawingDao.deleteById(id)
    }
}
