package com.example.motes.data

import android.content.Context
import androidx.room.Room
import com.example.motes.data.repository.ChecklistRepository
import com.example.motes.data.repository.DefaultChecklistRepository
import com.example.motes.data.repository.DefaultDrawingRepository
import com.example.motes.data.repository.DefaultNoteRepository
import com.example.motes.data.repository.DrawingRepository
import com.example.motes.data.repository.NoteRepository

/**
 * Lightweight app-scoped dependency container to avoid recreating Room/database objects
 * during recomposition and to keep lifecycle ownership at application scope.
 */
object AppContainer {
    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var noteRepository: NoteRepository? = null

    @Volatile
    private var checklistRepository: ChecklistRepository? = null

    @Volatile
    private var drawingRepository: DrawingRepository? = null

    fun noteRepository(context: Context): NoteRepository {
        val appContext = context.applicationContext
        return noteRepository ?: synchronized(this) {
            noteRepository ?: DefaultNoteRepository(database(appContext).noteDao()).also {
                noteRepository = it
            }
        }
    }


    fun checklistRepository(context: Context): ChecklistRepository {
        val appContext = context.applicationContext
        return checklistRepository ?: synchronized(this) {
            checklistRepository ?: DefaultChecklistRepository(database(appContext).checklistDao()).also {
                checklistRepository = it
            }
        }
    }

    fun drawingRepository(context: Context): DrawingRepository {
        val appContext = context.applicationContext
        return drawingRepository ?: synchronized(this) {
            drawingRepository ?: DefaultDrawingRepository(database(appContext).drawingDao()).also {
                drawingRepository = it
            }
        }
    }

    private fun database(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(context, AppDatabase::class.java, "motes.db").fallbackToDestructiveMigration().build().also {
                database = it
            }
        }
    }
}
