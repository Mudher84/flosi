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

@Composable
fun PersonEditScreen(onBack:()->Unit){
    val vm:PeopleViewModel=flosiViewModel();val prefs by vm.preferences.collectAsState();val lang=LocalFlosiLanguage.current
    var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var balance by remember{mutableStateOf("")};var mine by remember{mutableStateOf(true)}
    var currency by remember(prefs.currency){mutableStateOf(prefs.currency)};var currencyMenu by remember{mutableStateOf(false)}
    var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    val currencies=listOf("IQD","USD","EUR","GBP","SAR","AED","KWD","QAR","JOD","EGP","TRY","INR","CNY","JPY","KRW","CAD","AUD","CHF","SEK","RUB")
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(localizedLegacyText("إضافة شخص"),s("رصيد شخصي افتتاحي بعملة واضحة","A personal opening balance with an explicit currency"),onBack){
        OutlinedTextField(name,{name=it;error=null},Modifier.fillMaxWidth(),label={Text(s("الاسم","Name"))},singleLine=true)
        OutlinedTextField(phone,{phone=it.filter{ch->ch.isDigit()||ch=='+'||ch==' '};error=null},Modifier.fillMaxWidth(),label={Text(s("الهاتف — اختياري","Phone — optional"))},singleLine=true)
        OutlinedTextField(balance,{balance=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(s("الرصيد الافتتاحي","Opening balance"))},singleLine=true)
        Box(Modifier.fillMaxWidth()){
            OutlinedButton(onClick={currencyMenu=true},modifier=Modifier.fillMaxWidth()){Text("${s("عملة حساب الشخص","Person balance currency")}: $currency")}
            DropdownMenu(expanded=currencyMenu,onDismissRequest={currencyMenu=false}){
                currencies.forEach{code->DropdownMenuItem(text={Text(code)},onClick={currency=code;currencyMenu=false;error=null})}
            }
        }
        Row{
            FilterChip(mine,{mine=true;error=null},{Text(s("لي عنده","Owed to me"))})
            FilterChip(!mine,{mine=false;error=null},{Text(s("عليّ له","I owe"))})
        }
        Text(s("إذا ربطت حركة بهذا الشخص، يجب أن تكون الحركة من حساب بنفس العملة حتى يبقى رصيد الشخص دقيقاً ولا يتغير تاريخياً بتغير سعر الصرف.","A transaction linked to this person must use an account in the same currency so the person's balance stays exact and does not change historically with exchange rates."),color=FlosiMuted)
        error?.let{Text(it,color=FlosiRed)}
        Button(
            onClick={
                val raw=balance.toLongOrNull()?:0L
                val signed=runCatching{Math.multiplyExact(raw,if(mine)1L else -1L)}.getOrElse{error=s("الرصيد أكبر من الحد المسموح","Balance is too large");return@Button}
                saving=true;error=null
                vm.add(name,phone,signed,currency){message->saving=false;if(message==null)onBack()else error=message}
            },
            enabled=!saving&&name.isNotBlank(),
            modifier=Modifier.fillMaxWidth()
        ){
            if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(flosiText("save"))
        }
    }
}
