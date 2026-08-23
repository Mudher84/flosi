package com.flosi.app.ui.screens.accounts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences

@Composable fun TransferScreen(onBack:()->Unit){
 val vm:AccountsViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState()
 val prefs=rememberFlosiPreferences();val settings by prefs.state.collectAsState(initial=FlosiPreferencesState())
 var from by remember{mutableStateOf<Long?>(null)};var to by remember{mutableStateOf<Long?>(null)};var amount by remember{mutableStateOf("")}
 val fromAccount=accounts.firstOrNull{it.id==from};val toAccount=accounts.firstOrNull{it.id==to};val amountLong=amount.toLongOrNull()?:0L
 val quoted=if(fromAccount!=null&&toAccount!=null&&amountLong>0) CurrencyConverter.convert(amountLong,fromAccount.currency,toAccount.currency,settings.exchangeRates) else null
 FlosiPage("تحويل بين الحسابات","يحدث الرصيدين فوراً",onBack){
  Text("من");Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){accounts.take(4).forEach{a->FilterChip(from==a.id,{from=a.id},{Text("${a.name} ${a.currency}")})}}
  Text("إلى");Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){accounts.take(4).forEach{a->FilterChip(to==a.id,{to=a.id},{Text("${a.name} ${a.currency}")})}}
  OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("المبلغ")})
  if(fromAccount!=null&&toAccount!=null&&amountLong>0){
   CardBox{
    if(quoted!=null) Metric("سيصل للحساب الآخر",moneyText(quoted,toAccount.currency),FlosiGreen)
    else Text("لا يوجد سعر تحويل من ${fromAccount.currency} إلى ${toAccount.currency}. أضف السعر من إعدادات العملة أولاً.",color=FlosiOrange)
   }
  }
  Button(onClick={vm.transfer(from!!,to!!,amountLong);onBack()},enabled=from!=null&&to!=null&&from!=to&&amountLong>0&&quoted!=null,modifier=Modifier.fillMaxWidth()){Text("تنفيذ التحويل")}
 }
}
