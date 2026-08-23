package com.flosi.app.ui.screens.invoices

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.InvoicesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun InvoicesScreen(onBack:()->Unit,onCreate:()->Unit,onDetail:(Long)->Unit){
    val vm:InvoicesViewModel=flosiViewModel();val items by vm.invoices.collectAsState();val pending=items.count{it.status=="unpaid"||it.status=="partial"};val lang=LocalFlosiLanguage.current
    FlosiPage(flosiText("invoices"),localizedLegacyText("فواتير محفوظة وحسابات واضحة"),onBack){
        CardBox{
            Metric(if(lang=="ar")"عدد الفواتير" else "Invoice count",items.size.toString(),FlosiPurple)
            Metric(if(lang=="ar")"تحتاج تحصيل" else "Needs collection",pending.toString(),if(pending>0)FlosiOrange else FlosiGreen)
        }
        SectionTitle(flosiText("invoices"),if(lang=="ar")"+ إنشاء" else "+ Create",onCreate)
        CardBox{
            if(items.isEmpty())Text(if(lang=="ar")"ماكو فواتير بعد" else "No invoices yet",color=FlosiMuted)
            items.forEach{i->
                val currency=i.currency.trim().uppercase().ifBlank{"IQD"};val remaining=(i.total-i.paidAmount).coerceAtLeast(0L);val tone=when(i.status){"paid"->FlosiGreen;"partial"->FlosiOrange;else->FlosiPurple}
                val status=if(lang=="ar") when(i.status){"paid"->"مدفوعة بالكامل • $currency";"partial"->"متبقي ${moneyText(remaining,currency)}";"unpaid"->"غير مدفوعة • متبقي ${moneyText(remaining,currency)}";"cancelled"->"ملغاة • $currency";else->"مسودة • $currency"}
                else when(i.status){"paid"->"Paid in full • $currency";"partial"->"Remaining ${moneyText(remaining,currency)}";"unpaid"->"Unpaid • remaining ${moneyText(remaining,currency)}";"cancelled"->"Cancelled • $currency";else->"Draft • $currency"}
                ActionRow("#${i.number}",status,moneyText(i.total,currency),tone){onDetail(i.id)}
            }
        }
    }
}
