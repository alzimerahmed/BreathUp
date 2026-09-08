package com.smokingtracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaperingPreferencesStore(private val context: Context) {

    val taperingPlanEnabled: Flow<Boolean> = context.dataStore.data.map { it[DataStoreManager.TAPERING_PLAN_ENABLED] ?: false }
    val taperingIntervalDays: Flow<Int> = context.dataStore.data.map { it[DataStoreManager.TAPERING_INTERVAL_DAYS] ?: 7 }
    val lastTaperingCheckinDate: Flow<Long> = context.dataStore.data.map { it[DataStoreManager.LAST_TAPERING_CHECKIN_DATE] ?: 0L }

    suspend fun setTaperingPlanEnabled(enabled: Boolean) {
        context.dataStore.edit { it[DataStoreManager.TAPERING_PLAN_ENABLED] = enabled }
    }

    suspend fun setTaperingIntervalDays(days: Int) {
        context.dataStore.edit { it[DataStoreManager.TAPERING_INTERVAL_DAYS] = days }
    }

    suspend fun updateLastTaperingCheckinDate(timestamp: Long) {
        context.dataStore.edit { it[DataStoreManager.LAST_TAPERING_CHECKIN_DATE] = timestamp }
    }
}
