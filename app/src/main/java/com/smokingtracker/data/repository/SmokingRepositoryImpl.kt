package com.smokingtracker.data.repository

import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.local.SmokingDao
import com.smokingtracker.data.local.SmokingEntryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmokingRepositoryImpl(
    private val smokingDao: SmokingDao,
    private val dataStoreManager: DataStoreManager,
    private val applicationScope: CoroutineScope
) : SmokingRepository {

    override val smokingEntries: Flow<List<SmokingEntryEntity>> = smokingDao.getAllEntriesFlow()

    init {
        applicationScope.launch {
            if (dataStoreManager.hasOldData.first()) {
                val (oldEntries, oldTriggers) = dataStoreManager.getOldEntriesAndClear()
                if (oldEntries.isNotEmpty()) {
                    val entities = oldEntries.map { ts ->
                        SmokingEntryEntity(timestamp = ts, trigger = oldTriggers[ts])
                    }
                    smokingDao.insertEntries(entities)
                }
            }
        }
    }

    override suspend fun getAllEntries(): List<SmokingEntryEntity> {
        return smokingDao.getAllEntriesList()
    }

    override suspend fun addEntry(timestamp: Long, trigger: String?) {
        smokingDao.insertEntry(SmokingEntryEntity(timestamp = timestamp, trigger = trigger, isResisted = false))
    }

    override suspend fun addResistedEntry(timestamp: Long, trigger: String?, cravingIntensity: Int?, outcomeNote: String?) {
        smokingDao.insertEntry(
            SmokingEntryEntity(
                timestamp = timestamp,
                trigger = trigger,
                isResisted = true,
                cravingIntensity = cravingIntensity,
                outcomeNote = outcomeNote
            )
        )
    }

    override suspend fun removeEntryById(id: Long) {
        smokingDao.deleteEntryById(id)
    }

    override suspend fun updateEntryTimestampById(id: Long, newTimestamp: Long) {
        smokingDao.updateEntryTimestampById(id, newTimestamp)
    }

    override suspend fun updateEntryTriggerById(id: Long, trigger: String?) {
        smokingDao.updateEntryTriggerById(id, trigger)
    }

    override suspend fun clearAndInsertEntries(entities: List<SmokingEntryEntity>) {
        smokingDao.clearAllEntries()
        smokingDao.insertEntries(entities)
    }
}
