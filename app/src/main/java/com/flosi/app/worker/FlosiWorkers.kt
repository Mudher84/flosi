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
import java.util.concurrent.TimeUnit

class DailyFinanceWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        ensureChannel(applicationContext)

        val notificationAllowed =
            android.os.Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

        if (notificationAllowed) {
            val notification = NotificationCompat.Builder(
                applicationContext,
                CHANNEL
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("ملخص فلوسي")
                .setContentText("راجع مصروفات اليوم والالتزامات القادمة.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            NotificationManagerCompat
                .from(applicationContext)
                .notify(1001, notification)
        }

        return Result.success()
    }

    companion object {
        const val CHANNEL = "flosi_finance"

        fun ensureChannel(context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                val manager =
                    context.getSystemService(NotificationManager::class.java)

                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL,
                        "فلوسي",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }
}

object FlosiWorkScheduler {

    fun ensureScheduled(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val daily =
            PeriodicWorkRequestBuilder<DailyFinanceWorker>(
                24,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                "flosi_daily_summary",
                ExistingPeriodicWorkPolicy.KEEP,
                daily
            )
    }
}
