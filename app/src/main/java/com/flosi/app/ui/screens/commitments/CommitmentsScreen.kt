package com.flosi.app.ui.screens.commitments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun CommitmentsScreen(onBack:()->Unit,onEdit:()->Unit){
    val vm:PlanningViewModel=flosiViewModel()
    val items by vm.commitments.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val prefs by vm.preferences.collectAsState()
    var message by remember{mutableStateOf<String?>(null)}
    val base=CurrencyConverter.normalizeCode(prefs.currency)
    val accountMap=accounts.associateBy{it.id}
    val missing=linkedSetOf<String>()
    val total=items.sumOf{item->
        val source=item.accountId?.let(accountMap::get)?.currency?:base
        val converted=CurrencyConverter.convert(item.amount,source,base,prefs.exchangeRates)
        if(converted==null)missing+=CurrencyConverter.normalizeCode(source)
        converted?:0L
    }

    FlosiPage("الالتزامات والأقساط","القادم عليك",onBack){
        CardBox{
            Metric("إجمالي الالتزامات",moneyText(total,base),FlosiRed)
            if(missing.isNotEmpty())Text("غير محسوب لعدم وجود سعر تحويل: ${missing.joinToString()}",color=FlosiOrange)
        }
        SectionTitle("القادم","+ التزام",onEdit)
        CardBox{
            if(items.isEmpty())Text("ماكو التزامات بعد",color=FlosiMuted)
            items.forEach{item->
                val account=accountMap[item.accountId]
                Column(Modifier.fillMaxWidth()){
                    ActionRow(
                        item.title,
                        listOfNotNull(item.repeatRule,account?.name).joinToString(" • "),
                        moneyText(item.amount,account?.currency?:base),
                        FlosiOrange,
                        onClick=onEdit
                    )
                    TextButton(
                        onClick={vm.payCommitment(item.id){message=it?:"تم تسجيل الدفع وتحديث الموعد"}},
                        enabled=item.accountId!=null
                    ){Text(if(item.accountId==null)"اربط حساباً قبل الدفع" else "تسجيل كمدفوع")}
                }
            }
        }
        message?.let{Text(it,color=if(it.startsWith("تم"))FlosiGreen else FlosiRed)}
    }
}
