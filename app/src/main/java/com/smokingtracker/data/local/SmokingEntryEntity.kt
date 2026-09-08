package com.smokingtracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "smoking_entries", indices = [androidx.room.Index("timestamp")])
data class SmokingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val trigger: String? = null,
    val isResisted: Boolean = false,
    val cravingIntensity: Int? = null,
    val outcomeNote: String? = null
)

