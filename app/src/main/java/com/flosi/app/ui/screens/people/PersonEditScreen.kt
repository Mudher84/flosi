package com.flosi.app.ui.screens.people

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.i18n.FlosiPersonEditCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun PersonEditScreen(onBack:()->Unit){
    val vm:PeopleViewModel=flosiViewModel();val prefs by vm.preferences.collectAsState();val lang=LocalFlosiLanguage.current;fun c(key:String)=FlosiPersonEditCopy.text(lang,key)
    var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var balance by remember{mutableStateOf("")};var mine by remember{mutableStateOf(true)}
    var currency by remember(prefs.currency){mutableStateOf(prefs.currency)};var currencyMenu by remember{mutableStateOf(false)};var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    val currencies=listOf("IQD","USD","EUR","GBP","SAR","AED","KWD","QAR","JOD","EGP","TRY","INR","CNY","JPY","KRW","CAD","AUD","CHF","SEK","RUB")

    FlosiPage(localizedLegacyText("إضافة شخص"),c("sub"),onBack){
        OutlinedTextField(name,{name=it;error=null},Modifier.fillMaxWidth(),label={Text(c("name"))},singleLine=true)
        OutlinedTextField(phone,{phone=it.filter{ch->ch.isDigit()||ch=='+'||ch==' '};error=null},Modifier.fillMaxWidth(),label={Text(c("phone"))},singleLine=true)
        OutlinedTextField(balance,{balance=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(c("opening_balance"))},singleLine=true)
        Box(Modifier.fillMaxWidth()){OutlinedButton(onClick={currencyMenu=true},modifier=Modifier.fillMaxWidth()){Text("${c("currency")}: $currency")};DropdownMenu(expanded=currencyMenu,onDismissRequest={currencyMenu=false}){currencies.forEach{code->DropdownMenuItem(text={Text(code)},onClick={currency=code;currencyMenu=false;error=null})}}}
        Row{FilterChip(mine,{mine=true;error=null},{Text(c("owed_to_me"))});FilterChip(!mine,{mine=false;error=null},{Text(c("i_owe"))})}
        Text(c("currency_notice"),color=FlosiMuted)
        error?.let{Text(it,color=FlosiRed)}
        Button(onClick={val raw=balance.toLongOrNull()?:0L;val signed=runCatching{Math.multiplyExact(raw,if(mine)1L else -1L)}.getOrElse{error=c("too_large");return@Button};saving=true;error=null;vm.add(name,phone,signed,currency){message->saving=false;if(message==null)onBack()else error=message}},enabled=!saving&&name.isNotBlank(),modifier=Modifier.fillMaxWidth()){if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(flosiText("save"))}
    }
}
