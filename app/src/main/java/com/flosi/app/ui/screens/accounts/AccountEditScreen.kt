package com.flosi.app.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun AccountEditScreen(onBack:()->Unit){
    val vm:AccountsViewModel=flosiViewModel()
    var name by remember{mutableStateOf("")}
    var balance by remember{mutableStateOf("")}
    var type by remember{mutableStateOf("cash")}
    var currency by remember{mutableStateOf("IQD")}

    FlosiPage("إضافة حساب","حساب أو محفظة بعملة مستقلة",onBack){
        OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("الاسم")})
        OutlinedTextField(balance,{balance=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("الرصيد الافتتاحي")})
        Text("نوع الحساب")
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf("cash" to "نقدي","bank" to "مصرف","wallet" to "محفظة").forEach{(k,l)->
                FilterChip(type==k,{type=k},{Text(l)})
            }
        }
        Text("عملة الحساب")
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf("IQD","USD","EUR","GBP").forEach{c->
                FilterChip(currency==c,{currency=c},{Text(c)})
            }
        }
        Button(
            onClick={vm.add(name,type,balance.toLongOrNull()?:0,currency);onBack()},
            enabled=name.isNotBlank(),
            modifier=Modifier.fillMaxWidth()
        ){
            Text("حفظ")
        }
    }
}
