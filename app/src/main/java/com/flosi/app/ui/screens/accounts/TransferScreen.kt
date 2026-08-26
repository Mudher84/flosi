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
    val vm:AccountsViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState();val lang=LocalFlosiLanguage.current
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    val prefs=rememberFlosiPreferences();val settings by prefs.state.collectAsState(initial=FlosiPreferencesState())
    var from by remember{mutableStateOf<Long?>(null)};var to by remember{mutableStateOf<Long?>(null)};var amount by remember{mutableStateOf("")};var fee by remember{mutableStateOf("0")};var note by remember{mutableStateOf("")};var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    LaunchedEffect(initialFromAccountId,accounts){if(from==null&&initialFromAccountId!=null&&accounts.any{it.id==initialFromAccountId})from=initialFromAccountId}
    LaunchedEffect(from,accounts){if(from!=null&&accounts.none{it.id==from})from=null;if(to!=null&&accounts.none{it.id==to})to=null;if(from!=null&&to==from)to=null}
    val fromAccount=accounts.firstOrNull{it.id==from};val toAccount=accounts.firstOrNull{it.id==to};val amountLong=amount.toLongOrNull()?:0L;val feeLong=fee.toLongOrNull()?:0L;val quoted=if(fromAccount!=null&&toAccount!=null&&amountLong>0)CurrencyConverter.convert(amountLong,fromAccount.currency,toAccount.currency,settings.exchangeRates)else null;val debit=runCatching{Math.addExact(amountLong,feeLong)}.getOrNull()

    FlosiPage(localizedLegacyText("تحويل بين الحسابات"),localizedLegacyText("يحدث الرصيدين فوراً"),onBack){
        if(accounts.size<2)CardBox{Text(s("تحتاج حسابين على الأقل لإجراء تحويل.","You need at least two accounts to make a transfer.","Transfer için en az iki hesaba ihtiyacın var.","Vous avez besoin d’au moins deux comptes pour effectuer un virement.","Du brauchst mindestens zwei Konten für eine Überweisung.","Necesitas al menos dos cuentas para hacer una transferencia."),color=FlosiOrange)}
        Text(s("من","From","Kimden","Depuis","Von","Desde"));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){accounts.take(6).forEach{a->FilterChip(from==a.id,{from=a.id;if(to==a.id)to=null;error=null},{Text("${a.name} ${a.currency}")})}}
        Text(s("إلى","To","Kime","Vers","An","Hacia"));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){accounts.filter{it.id!=from}.take(6).forEach{a->FilterChip(to==a.id,{to=a.id;error=null},{Text("${a.name} ${a.currency}")})}}
        OutlinedTextField(amount,{amount=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(flosiText("amount"))},singleLine=true)
        OutlinedTextField(fee,{fee=it.filter(Char::isDigit).ifBlank{"0"};error=null},Modifier.fillMaxWidth(),label={Text(s("رسوم التحويل — اختياري","Transfer fee — optional","Transfer ücreti — isteğe bağlı","Frais de virement — facultatif","Überweisungsgebühr — optional","Comisión de transferencia — opcional"))},singleLine=true)
        OutlinedTextField(note,{note=it;error=null},Modifier.fillMaxWidth(),label={Text(s("ملاحظة — اختياري","Note — optional","Not — isteğe bağlı","Note — facultatif","Notiz — optional","Nota — opcional"))})
        if(fromAccount!=null&&toAccount!=null&&amountLong>0)CardBox{if(quoted!=null){Metric(localizedLegacyText("سيصل للحساب الآخر"),moneyText(quoted,toAccount.currency),FlosiGreen);if(feeLong>0L)ActionRow(s("رسوم التحويل","Transfer fee","Transfer ücreti","Frais de virement","Überweisungsgebühr","Comisión de transferencia"),"",moneyText(feeLong,fromAccount.currency),FlosiOrange);debit?.let{ActionRow(s("إجمالي الخصم من المصدر","Total debit from source","Kaynak hesaptan toplam çıkış","Débit total du compte source","Gesamtabbuchung vom Quellkonto","Débito total de la cuenta origen"),"",moneyText(it,fromAccount.currency),FlosiPurple)}}else Text(s("لا يوجد سعر تحويل من ${fromAccount.currency} إلى ${toAccount.currency}. أضف السعر من إعدادات العملة أولاً.","No exchange rate from ${fromAccount.currency} to ${toAccount.currency}. Add the rate in currency settings first.","${fromAccount.currency} → ${toAccount.currency} için kur yok. Önce para birimi ayarlarından kuru ekle.","Aucun taux de change de ${fromAccount.currency} vers ${toAccount.currency}. Ajoutez-le d’abord dans les paramètres de devise.","Kein Wechselkurs von ${fromAccount.currency} nach ${toAccount.currency}. Füge ihn zuerst in den Währungseinstellungen hinzu.","No hay tipo de cambio de ${fromAccount.currency} a ${toAccount.currency}. Añádelo primero en los ajustes de moneda."),color=FlosiOrange)}
        error?.let{Text(it,color=FlosiRed)}
        Button(onClick={val source=from?:return@Button;val target=to?:return@Button;saving=true;error=null;vm.transfer(source,target,amountLong,feeLong,note){message->saving=false;if(message==null)onBack()else error=message}},enabled=!saving&&accounts.size>=2&&from!=null&&to!=null&&from!=to&&amountLong>0&&feeLong>=0&&debit!=null&&quoted!=null,modifier=Modifier.fillMaxWidth()){if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(flosiText("transfer"))}
    }
}
