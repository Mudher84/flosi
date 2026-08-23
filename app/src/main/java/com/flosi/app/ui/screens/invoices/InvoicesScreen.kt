package com.flosi.app.ui.screens.invoices

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.InvoicesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences

@Composable
fun InvoicesScreen(onBack:()->Unit,onCreate:()->Unit,onDetail:(Long)->Unit){
    val vm:InvoicesViewModel=flosiViewModel()
    val items by vm.invoices.collectAsState()
    val preferences=rememberFlosiPreferences()
    val prefs by preferences.state.collectAsState(initial=FlosiPreferencesState())
    val currency=prefs.currency
    val pending=items.count{it.status=="unpaid"||it.status=="partial"}

    FlosiPage("الفواتير والوصولات","فواتير محفوظة وحسابات واضحة",onBack){
        CardBox{
            Metric("عدد الفواتير",items.size.toString(),FlosiPurple)
            Metric("تحتاج تحصيل",pending.toString(),if(pending>0)FlosiOrange else FlosiGreen)
        }
        SectionTitle("الفواتير","+ إنشاء",onCreate)
        CardBox{
            if(items.isEmpty())Text("ماكو فواتير بعد",color=FlosiMuted)
            items.forEach{i->
                val remaining=(i.total-i.paidAmount).coerceAtLeast(0L)
                val tone=when(i.status){
                    "paid"->FlosiGreen
                    "partial"->FlosiOrange
                    else->FlosiPurple
                }
                ActionRow(
                    title="#${i.number}",
                    subtitle=when(i.status){
                        "paid"->"مدفوعة بالكامل"
                        "partial"->"متبقي ${moneyText(remaining,currency)}"
                        "unpaid"->"غير مدفوعة • متبقي ${moneyText(remaining,currency)}"
                        "cancelled"->"ملغاة"
                        else->"مسودة"
                    },
                    value=moneyText(i.total,currency),
                    accent=tone
                ){
                    onDetail(i.id)
                }
            }
        }
    }
}
