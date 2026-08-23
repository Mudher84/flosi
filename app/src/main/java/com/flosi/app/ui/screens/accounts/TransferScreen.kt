package com.flosi.app.ui.screens.accounts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun TransferScreen(onBack:()->Unit){
 val vm:AccountsViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState()
 var from by remember{mutableStateOf<Long?>(null)};var to by remember{mutableStateOf<Long?>(null)};var amount by remember{mutableStateOf("")}
 FlosiPage("تحويل بين الحسابات","يحدث الرصيدين فوراً",onBack){
  Text("من");Row{accounts.take(3).forEach{a->FilterChip(from==a.id,{from=a.id},{Text(a.name)})}}
  Text("إلى");Row{accounts.take(3).forEach{a->FilterChip(to==a.id,{to=a.id},{Text(a.name)})}}
  OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("المبلغ")})
  Button(onClick={vm.transfer(from!!,to!!,amount.toLong());onBack()},enabled=from!=null&&to!=null&&from!=to&&(amount.toLongOrNull()?:0)>0,modifier=Modifier.fillMaxWidth()){Text("تنفيذ التحويل")}
 }
}
