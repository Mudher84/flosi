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
import com.flosi.app.ui.components.*
import com.flosi.app.worker.DailyFinanceWorker
import com.flosi.app.worker.FlosiWorkScheduler

@Composable
fun FlosiNotificationCenterScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var dailyEnabled by remember { mutableStateOf(FlosiWorkScheduler.isDailySummaryEnabled(context)) }
    var message by remember { mutableStateOf<String?>(null) }

    fun permissionGranted(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            FlosiWorkScheduler.setDailySummaryEnabled(context, true)
            dailyEnabled = true
            message = "تم تفعيل الملخص اليومي"
        } else {
            FlosiWorkScheduler.setDailySummaryEnabled(context, false)
            dailyEnabled = false
            message = "لم يتم منح إذن الإشعارات، لذلك بقي الملخص اليومي متوقفاً."
        }
    }

    val osNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

    FlosiPage("الإشعارات", "تنبيهات تحت سيطرتك", onBack) {
        CardBox {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    Text("الملخص المالي اليومي", color = FlosiText)
                    Text("WorkManager كل 24 ساعة، ويعمل فقط إذا كان هذا الخيار مفعّلاً.", color = FlosiMuted)
                }
                Switch(
                    checked = dailyEnabled,
                    onCheckedChange = { enabled ->
                        if (!enabled) {
                            FlosiWorkScheduler.setDailySummaryEnabled(context, false)
                            dailyEnabled = false
                            message = "تم إيقاف الملخص اليومي وإلغاء العمل المجدول."
                        } else if (permissionGranted()) {
                            FlosiWorkScheduler.setDailySummaryEnabled(context, true)
                            dailyEnabled = true
                            message = "تم تفعيل الملخص اليومي"
                        } else if (Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }

        if (!osNotificationsEnabled) {
            Surface(color = FlosiPurpleSoft, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "إشعارات Flosi متوقفة من إعدادات النظام. لن يرسل التطبيق إشعاراً حتى تسمح بها من Android.",
                    modifier = Modifier.padding(12.dp),
                    color = FlosiOrange
                )
            }
        }

        CardBox {
            Text("سلوك موثوق", color = FlosiText)
            Text(
                "حتى لو بقي WorkManager موجوداً من نسخة أقدم، العامل نفسه يفحص إعداد الملخص قبل إرسال أي إشعار. عند إيقاف الخيار يتم أيضاً إلغاء العمل الفريد flosi_daily_summary.",
                color = FlosiMuted
            )
        }

        OutlinedButton(
            onClick = {
                if (!dailyEnabled) {
                    message = "فعّل الملخص اليومي أولاً."
                    return@OutlinedButton
                }
                if (!permissionGranted() || !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    message = "الإشعارات غير مسموحة من النظام."
                    return@OutlinedButton
                }
                DailyFinanceWorker.ensureChannel(context)
                val notification = NotificationCompat.Builder(context, DailyFinanceWorker.CHANNEL)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("اختبار Flosi")
                    .setContentText("الإشعارات المالية تعمل بصورة صحيحة.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()
                NotificationManagerCompat.from(context).notify(1002, notification)
                message = "تم إرسال إشعار اختباري."
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("إرسال إشعار اختباري") }

        message?.let {
            Text(it, color = FlosiPurple)
        }
    }
}
