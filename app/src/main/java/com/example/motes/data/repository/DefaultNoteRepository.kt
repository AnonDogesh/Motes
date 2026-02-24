package com.example.motes.data.repository

import com.example.motes.data.dao.NoteDao

class DefaultNoteRepository(
    private val noteDao: NoteDao
) : NoteRepository
