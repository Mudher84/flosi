package com.flosi.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker.Result
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.flosi.app.settings.FlosiPreferences
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailyFinanceWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!FlosiWorkScheduler.isDailySummaryEnabled(applicationContext)) return Result.success()

        ensureChannel(applicationContext)
        val prefs=runCatching{FlosiPreferences(applicationContext).state.first()}.getOrNull()
        val language=prefs?.language ?: "ar"
        val arabic=language=="ar"

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val notificationAllowed =
            notificationManager.areNotificationsEnabled() &&
                (android.os.Build.VERSION.SDK_INT < 33 ||
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED)

        if (notificationAllowed) {
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(if(arabic)"ملخص Flosi" else "Flosi daily summary")
                .setContentText(if(arabic)"راجع مصروفات اليوم والالتزامات القادمة." else "Review today's spending and upcoming commitments.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        return Result.success()
    }

    companion object {
        const val CHANNEL = "flosi_finance"
        const val NOTIFICATION_ID = 1001

        fun ensureChannel(context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL,
                        "Flosi",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }
}

object FlosiWorkScheduler {
    private const val WORK_NAME = "flosi_daily_summary"
    private const val PREFS = "flosi_notification_settings"
    private const val KEY_DAILY_SUMMARY = "daily_summary_enabled"

    fun isDailySummaryEnabled(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DAILY_SUMMARY, true)

    fun setDailySummaryEnabled(context: Context, enabled: Boolean) {
        val app = context.applicationContext
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DAILY_SUMMARY, enabled)
            .apply()

        if (enabled) {
            ensureScheduled(app)
        } else {
            WorkManager.getInstance(app).cancelUniqueWork(WORK_NAME)
            NotificationManagerCompat.from(app).cancel(DailyFinanceWorker.NOTIFICATION_ID)
        }
    }

    fun ensureScheduled(context: Context) {
        val app = context.applicationContext
        if (!isDailySummaryEnabled(app)) {
            WorkManager.getInstance(app).cancelUniqueWork(WORK_NAME)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val daily = PeriodicWorkRequestBuilder<DailyFinanceWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(app).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            daily
        )
    }
}
