package com.example.motes.data

import android.content.Context
import androidx.room.Room
import com.example.motes.data.repository.DefaultNoteRepository
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

    fun noteRepository(context: Context): NoteRepository {
        val appContext = context.applicationContext
        return noteRepository ?: synchronized(this) {
            noteRepository ?: DefaultNoteRepository(database(appContext).noteDao()).also {
                noteRepository = it
            }
        }
    }

    private fun database(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(context, AppDatabase::class.java, "motes.db").build().also {
                database = it
            }
        }
    }
}
