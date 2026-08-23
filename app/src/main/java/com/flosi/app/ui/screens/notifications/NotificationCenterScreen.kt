package com.flosi.app.ui.screens.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.ui.components.*
import com.flosi.app.worker.DailyFinanceWorker
import com.flosi.app.worker.FlosiWorkScheduler

@Composable
fun FlosiNotificationCenterScreen(onBack:()->Unit){
    val context=androidx.compose.ui.platform.LocalContext.current;val lang=LocalFlosiLanguage.current;fun s(ar:String,en:String)=if(lang=="ar")ar else en
    var dailyEnabled by remember{mutableStateOf(FlosiWorkScheduler.isDailySummaryEnabled(context))};var message by remember{mutableStateOf<String?>(null)}
    fun permissionGranted()=Build.VERSION.SDK_INT<33||ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED
    val permissionLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(granted){FlosiWorkScheduler.setDailySummaryEnabled(context,true);dailyEnabled=true;message=s("تم تفعيل الملخص اليومي","Daily summary enabled")}else{FlosiWorkScheduler.setDailySummaryEnabled(context,false);dailyEnabled=false;message=s("لم يتم منح إذن الإشعارات، لذلك بقي الملخص اليومي متوقفاً.","Notification permission was not granted, so the daily summary remains off.")}}
    val osNotificationsEnabled=NotificationManagerCompat.from(context).areNotificationsEnabled()

    FlosiPage(flosiText("notifications"),s("تنبيهات تحت سيطرتك","Notifications under your control"),onBack){
        CardBox{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(s("الملخص المالي اليومي","Daily financial summary"),color=FlosiText);Text(s("WorkManager كل 24 ساعة، ويعمل فقط إذا كان هذا الخيار مفعّلاً.","Runs with WorkManager every 24 hours only when enabled."),color=FlosiMuted)};Switch(dailyEnabled,{enabled->if(!enabled){FlosiWorkScheduler.setDailySummaryEnabled(context,false);dailyEnabled=false;message=s("تم إيقاف الملخص اليومي وإلغاء العمل المجدول.","Daily summary disabled and scheduled work cancelled.")}else if(permissionGranted()){FlosiWorkScheduler.setDailySummaryEnabled(context,true);dailyEnabled=true;message=s("تم تفعيل الملخص اليومي","Daily summary enabled")}else if(Build.VERSION.SDK_INT>=33)permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)})}}
        if(!osNotificationsEnabled)Surface(color=FlosiPurpleSoft,shape=MaterialTheme.shapes.large,modifier=Modifier.fillMaxWidth()){Text(s("إشعارات Flosi متوقفة من إعدادات النظام. لن يرسل التطبيق إشعاراً حتى تسمح بها من Android.","Flosi notifications are disabled in system settings. Android must allow them before the app can notify you."),Modifier.padding(12.dp),color=FlosiOrange)}
        CardBox{Text(s("سلوك موثوق","Reliable behavior"),color=FlosiText);Text(s("حتى لو بقي WorkManager موجوداً من نسخة أقدم، العامل نفسه يفحص إعداد الملخص قبل إرسال أي إشعار. عند إيقاف الخيار يتم أيضاً إلغاء العمل الفريد flosi_daily_summary.","Even if older scheduled work exists, the worker checks the setting before sending. Disabling this option also cancels flosi_daily_summary."),color=FlosiMuted)}
        OutlinedButton(onClick={if(!dailyEnabled){message=s("فعّل الملخص اليومي أولاً.","Enable the daily summary first.");return@OutlinedButton};if(!permissionGranted()||!NotificationManagerCompat.from(context).areNotificationsEnabled()){message=s("الإشعارات غير مسموحة من النظام.","Notifications are not allowed by the system.");return@OutlinedButton};DailyFinanceWorker.ensureChannel(context);val notification=NotificationCompat.Builder(context,DailyFinanceWorker.CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(s("اختبار Flosi","Flosi test")).setContentText(s("الإشعارات المالية تعمل بصورة صحيحة.","Financial notifications are working correctly.")).setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).build();NotificationManagerCompat.from(context).notify(1002,notification);message=s("تم إرسال إشعار اختباري.","Test notification sent.")},modifier=Modifier.fillMaxWidth()){Text(s("إرسال إشعار اختباري","Send test notification"))}
        message?.let{Text(it,color=FlosiPurple)}
    }
}
