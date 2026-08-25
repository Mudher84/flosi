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
    val vm:InvoicesViewModel=flosiViewModel();val items by vm.invoices.collectAsState();val lang=LocalFlosiLanguage.current
    val receivables=items.count{it.type=="sale"&&(it.status=="unpaid"||it.status=="partial")}
    val payables=items.count{it.type=="purchase"&&(it.status=="unpaid"||it.status=="partial")}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en
    FlosiPage(flosiText("invoices"),localizedLegacyText("فواتير محفوظة وحسابات واضحة"),onBack){
        CardBox{
            Metric(s("عدد الفواتير","Invoice count"),items.size.toString(),FlosiPurple)
            Metric(s("مبالغ تحتاج تحصيل","Receivables to collect"),receivables.toString(),if(receivables>0)FlosiOrange else FlosiGreen)
            Metric(s("فواتير شراء تحتاج دفع","Purchase invoices to pay"),payables.toString(),if(payables>0)FlosiRed else FlosiGreen)
        }
        SectionTitle(flosiText("invoices"),s("+ إنشاء","+ Create"),onCreate)
        if(items.isEmpty()){
            EmptyState(
                title=s("ما عندك فواتير بعد","No invoices yet"),
                subtitle=s("أنشئ أول فاتورة بيع أو شراء وخلي Flosi يتابع المدفوع والمتبقي وياك.","Create your first sale or purchase invoice and let Flosi track paid and remaining amounts."),
                action=s("إنشاء أول فاتورة","Create first invoice"),
                onAction=onCreate,
                symbol="▤"
            )
        } else {
            CardBox{
                items.forEach{i->
                    val currency=i.currency.trim().uppercase().ifBlank{"IQD"};val remaining=(i.total-i.paidAmount).coerceAtLeast(0L);val tone=when(i.status){"paid"->FlosiGreen;"partial"->FlosiOrange;else->if(i.type=="purchase")FlosiRed else FlosiPurple}
                    val typeLabel=if(i.type=="purchase")s("شراء","Purchase")else s("بيع","Sale")
                    val status=if(lang=="ar") when(i.status){"paid"->"$typeLabel • مدفوعة بالكامل • $currency";"partial"->"$typeLabel • متبقي ${moneyText(remaining,currency)}";"unpaid"->"$typeLabel • غير مدفوعة • متبقي ${moneyText(remaining,currency)}";"cancelled"->"$typeLabel • ملغاة • $currency";else->"$typeLabel • مسودة • $currency"}
                    else when(i.status){"paid"->"$typeLabel • Paid in full • $currency";"partial"->"$typeLabel • Remaining ${moneyText(remaining,currency)}";"unpaid"->"$typeLabel • Unpaid • remaining ${moneyText(remaining,currency)}";"cancelled"->"$typeLabel • Cancelled • $currency";else->"$typeLabel • Draft • $currency"}
                    ActionRow("#${i.number}",status,moneyText(i.total,currency),tone){onDetail(i.id)}
                }
            }
        }
    }
}
