package com.flosi.app.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences

@Composable
fun TransferScreen(onBack:()->Unit,initialFromAccountId:Long?=null){
    val vm:AccountsViewModel=flosiViewModel()
    val accounts by vm.accounts.collectAsState()
    val lang=LocalFlosiLanguage.current
    val prefs=rememberFlosiPreferences()
    val settings by prefs.state.collectAsState(initial=FlosiPreferencesState())
    var from by remember{mutableStateOf<Long?>(null)}
    var to by remember{mutableStateOf<Long?>(null)}
    var amount by remember{mutableStateOf("")}
    var fee by remember{mutableStateOf("0")}
    var note by remember{mutableStateOf("")}
    var saving by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf<String?>(null)}

    LaunchedEffect(initialFromAccountId,accounts){
        if(from==null && initialFromAccountId!=null && accounts.any{it.id==initialFromAccountId}) from=initialFromAccountId
    }
    LaunchedEffect(from,accounts){
        if(from!=null && accounts.none{it.id==from}) from=null
        if(to!=null && accounts.none{it.id==to}) to=null
        if(from!=null && to==from) to=null
    }

    val fromAccount=accounts.firstOrNull{it.id==from}
    val toAccount=accounts.firstOrNull{it.id==to}
    val amountLong=amount.toLongOrNull()?:0L
    val feeLong=fee.toLongOrNull()?:0L
    val quoted=if(fromAccount!=null&&toAccount!=null&&amountLong>0) CurrencyConverter.convert(amountLong,fromAccount.currency,toAccount.currency,settings.exchangeRates) else null
    val debit=runCatching{Math.addExact(amountLong,feeLong)}.getOrNull()
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(localizedLegacyText("تحويل بين الحسابات"),localizedLegacyText("يحدث الرصيدين فوراً"),onBack){
        if(accounts.size<2){
            CardBox{Text(s("تحتاج حسابين على الأقل لإجراء تحويل.","You need at least two accounts to make a transfer."),color=FlosiOrange)}
        }
        Text(s("من","From"))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){accounts.take(6).forEach{a->FilterChip(from==a.id,{from=a.id;if(to==a.id)to=null;error=null},{Text("${a.name} ${a.currency}")})}}
        Text(s("إلى","To"))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){accounts.filter{it.id!=from}.take(6).forEach{a->FilterChip(to==a.id,{to=a.id;error=null},{Text("${a.name} ${a.currency}")})}}
        OutlinedTextField(amount,{amount=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(flosiText("amount"))},singleLine=true)
        OutlinedTextField(fee,{fee=it.filter(Char::isDigit).ifBlank{"0"};error=null},Modifier.fillMaxWidth(),label={Text(s("رسوم التحويل — اختياري","Transfer fee — optional"))},singleLine=true)
        OutlinedTextField(note,{note=it;error=null},Modifier.fillMaxWidth(),label={Text(s("ملاحظة — اختياري","Note — optional"))})

        if(fromAccount!=null&&toAccount!=null&&amountLong>0){
            CardBox{
                if(quoted!=null) {
                    Metric(localizedLegacyText("سيصل للحساب الآخر"),moneyText(quoted,toAccount.currency),FlosiGreen)
                    if(feeLong>0L) ActionRow(s("رسوم التحويل","Transfer fee"),"",moneyText(feeLong,fromAccount.currency),FlosiOrange)
                    debit?.let{ActionRow(s("إجمالي الخصم من المصدر","Total debit from source"),"",moneyText(it,fromAccount.currency),FlosiPurple)}
                } else Text(s("لا يوجد سعر تحويل من ${fromAccount.currency} إلى ${toAccount.currency}. أضف السعر من إعدادات العملة أولاً.","No exchange rate from ${fromAccount.currency} to ${toAccount.currency}. Add the rate in currency settings first."),color=FlosiOrange)
            }
        }
        error?.let{Text(it,color=FlosiRed)}
        Button(
            onClick={
                val source=from ?: return@Button
                val target=to ?: return@Button
                saving=true;error=null
                vm.transfer(source,target,amountLong,feeLong,note){message->
                    saving=false
                    if(message==null) onBack() else error=message
                }
            },
            enabled=!saving&&accounts.size>=2&&from!=null&&to!=null&&from!=to&&amountLong>0&&feeLong>=0&&debit!=null&&quoted!=null,
            modifier=Modifier.fillMaxWidth()
        ){
            if(saving) CircularProgressIndicator(strokeWidth=2.dp) else Text(flosiText("transfer"))
        }
    }
}
