package com.smokingtracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoricalBaselineStore(private val context: Context, private val gson: Gson = Gson()) {

    val hasHistoricalBaseline: Flow<Boolean> = context.dataStore.data.map { it[DataStoreManager.HAS_HISTORICAL_BASELINE] ?: false }
    val historicalStartDate: Flow<Long> = context.dataStore.data.map { it[DataStoreManager.HISTORICAL_START_DATE] ?: 0L }
    val historicalDailyAvg: Flow<Int> = context.dataStore.data.map { it[DataStoreManager.HISTORICAL_DAILY_AVG] ?: 0 }
    val historicalPackPrice: Flow<Float> = context.dataStore.data.map { it[DataStoreManager.HISTORICAL_PACK_PRICE] ?: 0f }
    val historicalPackSize: Flow<Int> = context.dataStore.data.map { it[DataStoreManager.HISTORICAL_PACK_SIZE] ?: 20 }
    val historicalTriggerPriorities: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[DataStoreManager.HISTORICAL_TRIGGER_PRIORITIES] ?: "[]"
        val listType = object : TypeToken<List<String>>() {}.type
        gson.fromJson(json, listType) ?: emptyList()
    }

    suspend fun saveHistoricalBaseline(
        startDate: Long,
        dailyAvg: Int,
        packPrice: Float,
        packSize: Int,
        triggerPriorities: List<String>
    ) {
        context.dataStore.edit { prefs ->
            prefs[DataStoreManager.HAS_HISTORICAL_BASELINE] = true
            prefs[DataStoreManager.HISTORICAL_START_DATE] = startDate
            prefs[DataStoreManager.HISTORICAL_DAILY_AVG] = dailyAvg
            prefs[DataStoreManager.HISTORICAL_PACK_PRICE] = packPrice
            prefs[DataStoreManager.HISTORICAL_PACK_SIZE] = packSize
            prefs[DataStoreManager.HISTORICAL_TRIGGER_PRIORITIES] = gson.toJson(triggerPriorities)
        }
    }

    suspend fun clearHistoricalBaseline() {
        context.dataStore.edit { prefs ->
            prefs[DataStoreManager.HAS_HISTORICAL_BASELINE] = false
            prefs.remove(DataStoreManager.HISTORICAL_START_DATE)
            prefs.remove(DataStoreManager.HISTORICAL_DAILY_AVG)
            prefs.remove(DataStoreManager.HISTORICAL_PACK_PRICE)
            prefs.remove(DataStoreManager.HISTORICAL_PACK_SIZE)
            prefs.remove(DataStoreManager.HISTORICAL_TRIGGER_PRIORITIES)
        }
    }
}
