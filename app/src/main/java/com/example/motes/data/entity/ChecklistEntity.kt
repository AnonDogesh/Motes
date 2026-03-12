package com.example.motes.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val items: List<ChecklistItem>,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val cardColor: Long? = null,
    val reminderAt: Long? = null
)
