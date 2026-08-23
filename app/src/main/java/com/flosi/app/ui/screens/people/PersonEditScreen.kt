package com.flosi.app.ui.screens.people
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun PersonEditScreen(onBack:()->Unit){
 val vm:PeopleViewModel=flosiViewModel();val lang=LocalFlosiLanguage.current
 var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var balance by remember{mutableStateOf("")};var mine by remember{mutableStateOf(true)}
 FlosiPage(localizedLegacyText("إضافة شخص"),localizedLegacyText("حساب جديد"),onBack){
  OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text(if(lang=="ar")"الاسم" else "Name")})
  OutlinedTextField(phone,{phone=it},Modifier.fillMaxWidth(),label={Text(if(lang=="ar")"الهاتف" else "Phone")})
  OutlinedTextField(balance,{balance=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text(if(lang=="ar")"الرصيد الافتتاحي" else "Opening balance")})
  Row{FilterChip(mine,{mine=true},{Text(if(lang=="ar")"لي عنده" else "Owed to me")});FilterChip(!mine,{mine=false},{Text(if(lang=="ar")"عليّ له" else "I owe")})}
  Button(onClick={val b=(balance.toLongOrNull() ?: 0L) * (if(mine) 1L else -1L);vm.add(name,phone,b);onBack()},enabled=name.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text(flosiText("save"))}
 }
}
