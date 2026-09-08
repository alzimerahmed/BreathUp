package com.smokingtracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationPreferencesStore(private val context: Context) {

    val ongoingNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { it[DataStoreManager.NOTIFICATION_ENABLED] ?: false }
    val notificationLowPriority: Flow<Boolean> = context.dataStore.data.map { it[DataStoreManager.NOTIFICATION_LOW_PRIORITY] ?: true }
    val notificationShowTimer: Flow<Boolean> = context.dataStore.data.map { it[DataStoreManager.NOTIFICATION_SHOW_TIMER] ?: true }
    val notificationShowProgress: Flow<Boolean> = context.dataStore.data.map { it[DataStoreManager.NOTIFICATION_SHOW_PROGRESS] ?: true }
    val notificationShowAddButton: Flow<Boolean> = context.dataStore.data.map { it[DataStoreManager.NOTIFICATION_SHOW_ADD_BUTTON] ?: true }
    val notificationShowResistButton: Flow<Boolean> = context.dataStore.data.map { it[DataStoreManager.NOTIFICATION_SHOW_RESIST_BUTTON] ?: false }

    suspend fun saveOngoingNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[DataStoreManager.NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun saveNotificationLowPriority(lowPriority: Boolean) {
        context.dataStore.edit { it[DataStoreManager.NOTIFICATION_LOW_PRIORITY] = lowPriority }
    }

    suspend fun saveNotificationShowTimer(show: Boolean) {
        context.dataStore.edit { it[DataStoreManager.NOTIFICATION_SHOW_TIMER] = show }
    }

    suspend fun saveNotificationShowProgress(show: Boolean) {
        context.dataStore.edit { it[DataStoreManager.NOTIFICATION_SHOW_PROGRESS] = show }
    }

    suspend fun saveNotificationShowAddButton(show: Boolean) {
        context.dataStore.edit { it[DataStoreManager.NOTIFICATION_SHOW_ADD_BUTTON] = show }
    }

    suspend fun saveNotificationShowResistButton(show: Boolean) {
        context.dataStore.edit { it[DataStoreManager.NOTIFICATION_SHOW_RESIST_BUTTON] = show }
    }
}
