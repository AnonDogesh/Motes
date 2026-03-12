package com.example.motes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.motes.data.dao.ChecklistDao
import com.example.motes.data.dao.DrawingDao
import com.example.motes.data.dao.NoteDao
import com.example.motes.data.entity.ChecklistEntity
import com.example.motes.data.entity.DrawingEntity
import com.example.motes.data.entity.NoteEntity

@Database(
    entities = [
        NoteEntity::class,
        ChecklistEntity::class,
        DrawingEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun drawingDao(): DrawingDao
}
