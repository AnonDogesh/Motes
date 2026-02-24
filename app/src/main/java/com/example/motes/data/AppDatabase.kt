package com.example.motes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.motes.data.dao.NoteDao
import com.example.motes.data.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
