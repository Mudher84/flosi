package com.flosi.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.flosi.app.ui.components.*

@Composable
fun BankConnectionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showZainCashInfo by remember { mutableStateOf(false) }

    FlosiPage(
        "ربط البنوك والمحافظ",
        "مكان واحد لإدارة الربط المالي الآمن",
        onBack
    ) {
        CardBox {
            SectionTitle("المحافظ الإلكترونية")
            ActionRow(
                "ZainCash",
                "بوابة الدفع الرسمية جاهزة من جهة Flosi Backend",
                "التفاصيل",
                FlosiPurple,
                onClick = { showZainCashInfo = true }
            )
            Text(
                "واجهة ZainCash العامة الحالية هي بوابة دفع، وليست API لقراءة رصيد وحركات محفظة المستخدم. Flosi لن يدّعي مزامنة الرصيد إلا إذا وفرت ZainCash API رسمية منفصلة لذلك.",
                color = FlosiMuted
            )
        }

        CardBox {
            SectionTitle("الحسابات البنكية")
            ActionRow(
                "ربط بنك",
                "يظهر البنك هنا عندما تتوفر له واجهة Open Banking / API رسمية",
                "قريباً",
                FlosiMuted
            )
            ActionRow(
                "استيراد كشف حساب",
                "استخدم CSV/XLSX/PDF من مركز البيانات للبنوك التي لا توفر API",
                "متاح",
                FlosiGreen
            )
        }

        CardBox {
            SectionTitle("سياسة الأمان")
            Text("• لا نخزن بيانات الدخول المصرفية داخل Flosi.", color = FlosiMuted)
            Text("• لا نستخدم WebView لالتقاط اسم المستخدم أو كلمة المرور.", color = FlosiMuted)
            Text("• كل ربط حقيقي يجب أن يكون عبر OAuth/API رسمي أو مزود Open Banking معتمد.", color = FlosiMuted)
            Text("• أسرار ZainCash وGoogle Play تبقى على الخادم فقط ولا تدخل داخل APK.", color = FlosiMuted)
        }
    }

    if (showZainCashInfo) {
        AlertDialog(
            onDismissRequest = { showZainCashInfo = false },
            title = { Text("ZainCash") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تم تجهيز Flosi Backend لـ ZainCash Payment Gateway v2: OAuth2، إنشاء الدفع، الاستعلام، والتحقق من الـcallback والـwebhook.")
                    Text("التشغيل الإنتاجي يحتاج حساب ZainCash Business/Merchant وclient_id وclient_secret وAPI key وserviceType من ZainCash.")
                    Text("هذه البوابة لا تمنح Flosi صلاحية قراءة رصيد أو حركات محفظة المستخدم.", color = FlosiMuted)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showZainCashInfo = false
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.zaincash.iq/"))
                    runCatching { context.startActivity(intent) }
                }) { Text("فتح وثائق ZainCash") }
            },
            dismissButton = {
                TextButton(onClick = { showZainCashInfo = false }) { Text("إغلاق") }
            }
        )
    }
}
