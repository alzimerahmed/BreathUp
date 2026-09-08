package com.smokingtracker.data.repository

import com.smokingtracker.data.local.SmokingEntryEntity
import kotlinx.coroutines.flow.Flow

interface SmokingRepository {
    val smokingEntries: Flow<List<SmokingEntryEntity>>

    suspend fun getAllEntries(): List<SmokingEntryEntity>
    suspend fun addEntry(timestamp: Long, trigger: String?)
    suspend fun addResistedEntry(timestamp: Long, trigger: String?)
    suspend fun removeEntryById(id: Long)
    suspend fun updateEntryTimestampById(id: Long, newTimestamp: Long)
    suspend fun updateEntryTriggerById(id: Long, trigger: String?)
    suspend fun clearAndInsertEntries(entities: List<SmokingEntryEntity>)
}
