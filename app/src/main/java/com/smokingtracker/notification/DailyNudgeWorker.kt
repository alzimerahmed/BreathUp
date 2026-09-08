package com.smokingtracker.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.smokingtracker.MainActivity
import com.smokingtracker.R
import com.smokingtracker.SmokingTrackerApp
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.NotificationPreferencesStore
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyNudgeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        val dataStoreManager: DataStoreManager = get()
        val notificationStore: NotificationPreferencesStore = get()

        val registered = dataStoreManager.isRegistered.first()
        val enabled = notificationStore.dailyNudgeEnabled.first()
        if (!registered || !enabled) return Result.success()

        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val missions = listOf(
            R.string.nudge_mission_1,
            R.string.nudge_mission_2,
            R.string.nudge_mission_3
        )
        val missionRes = missions[dayOfYear % missions.size]

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SmokingTrackerApp.CHANNEL_DAILY_NUDGE)
            .setSmallIcon(R.drawable.ic_cigarettebase)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(missionRes))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(missionRes)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        }
        return Result.success()
    }

    companion object {
        const val NOTIFICATION_ID = 1002
        private const val WORK_NAME = "daily_nudge_work"
        private const val NUDGE_HOUR = 10

        fun schedule(context: Context) {
            val now = java.util.Calendar.getInstance()
            val next = (now.clone() as java.util.Calendar).apply {
                set(java.util.Calendar.HOUR_OF_DAY, NUDGE_HOUR)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            val initialDelay = next.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyNudgeWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
