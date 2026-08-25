package com.flosi.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.flosi.app.MainActivity
import com.flosi.app.data.local.FlosiDatabase
import com.flosi.app.data.repository.FinanceRepository
import com.flosi.app.settings.FlosiPreferences
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class DailyFinanceWorker(appContext: Context,params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (!FlosiWorkScheduler.isDailySummaryEnabled(applicationContext)) return Result.success()
        ensureChannel(applicationContext)
        val preferences=FlosiPreferences(applicationContext)
        val prefs=runCatching{preferences.state.first()}.getOrNull()
        val language=prefs?.language ?: "ar";val arabic=language=="ar"
        val notificationManager=NotificationManagerCompat.from(applicationContext)
        val notificationAllowed=notificationManager.areNotificationsEnabled() && (android.os.Build.VERSION.SDK_INT<33 || ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)
        if(!notificationAllowed)return Result.success()

        val repo=FinanceRepository(FlosiDatabase.get(applicationContext),preferences)
        val dashboard=runCatching{repo.dashboard.first()}.getOrNull()
        val commitments=runCatching{repo.commitments.first()}.getOrDefault(emptyList())
        val now=System.currentTimeMillis();val next7=now+7L*86_400_000L
        val dueCount=commitments.count{it.dueAt in now..next7}
        val title=if(arabic)"لمحة Flosi اليوم ✦" else "Your Flosi snapshot ✦"
        val text=if(dashboard==null){if(arabic)"افتح Flosi وراجع وضعك المالي اليوم." else "Open Flosi to review your finances today."}else{
            val nf=NumberFormat.getNumberInstance(if(arabic)Locale("ar","IQ") else Locale.US)
            val spent=nf.format(dashboard.todayExpense)
            val base=dashboard.baseCurrency
            when{
                dueCount>0&&arabic->"صرفت اليوم $spent $base • عندك $dueCount استحقاق قريب"
                dueCount>0->"Spent $spent $base today • $dueCount upcoming due item${if(dueCount>1)"s" else ""}"
                arabic->"صرفت اليوم $spent $base • ماكو استحقاقات خلال 7 أيام"
                else->"Spent $spent $base today • no dues in the next 7 days"
            }
        }
        val openIntent=Intent(applicationContext,MainActivity::class.java).apply{flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP}
        val pending=PendingIntent.getActivity(applicationContext,7001,openIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification=NotificationCompat.Builder(applicationContext,CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text)).setContentIntent(pending).setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).build()
        notificationManager.notify(NOTIFICATION_ID,notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL="flosi_finance";const val NOTIFICATION_ID=1001
        fun ensureChannel(context:Context){if(android.os.Build.VERSION.SDK_INT>=26){context.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,"Flosi insights",NotificationManager.IMPORTANCE_DEFAULT).apply{description="Daily financial insights and upcoming commitments"})}}
    }
}

object FlosiWorkScheduler {
    private const val WORK_NAME="flosi_daily_summary";private const val PREFS="flosi_notification_settings";private const val KEY_DAILY_SUMMARY="daily_summary_enabled"
    fun isDailySummaryEnabled(context:Context)=context.applicationContext.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getBoolean(KEY_DAILY_SUMMARY,true)
    fun setDailySummaryEnabled(context:Context,enabled:Boolean){val app=context.applicationContext;app.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putBoolean(KEY_DAILY_SUMMARY,enabled).apply();if(enabled)ensureScheduled(app) else{WorkManager.getInstance(app).cancelUniqueWork(WORK_NAME);NotificationManagerCompat.from(app).cancel(DailyFinanceWorker.NOTIFICATION_ID)}}
    fun ensureScheduled(context:Context){val app=context.applicationContext;if(!isDailySummaryEnabled(app)){WorkManager.getInstance(app).cancelUniqueWork(WORK_NAME);return};val constraints=Constraints.Builder().setRequiresBatteryNotLow(true).build();val daily=PeriodicWorkRequestBuilder<DailyFinanceWorker>(24,TimeUnit.HOURS).setConstraints(constraints).build();WorkManager.getInstance(app).enqueueUniquePeriodicWork(WORK_NAME,ExistingPeriodicWorkPolicy.KEEP,daily)}
}
