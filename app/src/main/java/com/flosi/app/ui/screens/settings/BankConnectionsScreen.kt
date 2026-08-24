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
                "جاهز للربط الرسمي عبر بوابة Flosi الآمنة",
                "ربط",
                FlosiPurple,
                onClick = { showZainCashInfo = true }
            )
            Text(
                "Flosi لا يخزن كلمة مرور ZainCash أو أسرار التاجر داخل الهاتف. الربط الإنتاجي يتم عبر خادم آمن بعد تفعيل بيانات التاجر الرسمية.",
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
            Text("• يمكن فصل أي اتصال بدون حذف السجل المالي المحلي.", color = FlosiMuted)
        }
    }

    if (showZainCashInfo) {
        AlertDialog(
            onDismissRequest = { showZainCashInfo = false },
            title = { Text("ربط ZainCash") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الطبقة داخل Flosi جاهزة كبوابة اتصال، لكن تفعيل الإنتاج يحتاج حساب ZainCash Business/Merchant وبيانات API الرسمية.")
                    Text("أسرار client_secret وAPI key يجب أن تبقى على خادم Flosi ولا تُضمَّن داخل APK.", color = FlosiMuted)
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
