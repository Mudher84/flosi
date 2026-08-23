package com.flosi.app.ui.screens.people
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun PersonEditScreen(onBack:()->Unit){
 val vm:PeopleViewModel=flosiViewModel()
 var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var balance by remember{mutableStateOf("")};var mine by remember{mutableStateOf(true)}
 FlosiPage("إضافة شخص","حساب جديد",onBack){
  OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("الاسم")})
  OutlinedTextField(phone,{phone=it},Modifier.fillMaxWidth(),label={Text("الهاتف")})
  OutlinedTextField(balance,{balance=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("الرصيد الافتتاحي")})
  Row{FilterChip(mine,{mine=true},{Text("لي عنده")});FilterChip(!mine,{mine=false},{Text("عليّ له")})}
  Button(onClick={val b=(balance.toLongOrNull() ?: 0L) * (if(mine) 1L else -1L);vm.add(name,phone,b);onBack()},enabled=name.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("حفظ")}
 }
}
