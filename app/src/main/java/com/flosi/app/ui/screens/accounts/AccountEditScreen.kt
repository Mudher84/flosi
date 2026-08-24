package com.flosi.app.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences

@Composable
fun AccountEditScreen(onBack:()->Unit){
    val vm:AccountsViewModel=flosiViewModel();val lang=LocalFlosiLanguage.current
    val prefs=rememberFlosiPreferences();val settings by prefs.state.collectAsState(initial=FlosiPreferencesState())
    var name by remember{mutableStateOf("")};var balance by remember{mutableStateOf("")};var type by remember{mutableStateOf("cash")}
    var currency by remember{mutableStateOf("")}
    var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    val currencies=listOf("IQD","USD","EUR","GBP","SAR","AED","KWD","QAR","JOD","EGP","TRY","INR","CNY","JPY","KRW","CAD","AUD","CHF","SEK","RUB")
    var currencyMenu by remember{mutableStateOf(false)}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    LaunchedEffect(settings.currency){
        if(currency.isBlank()) currency=settings.currency.takeIf{it in currencies}?:"USD"
    }

    FlosiPage(localizedLegacyText("إضافة حساب"),localizedLegacyText("حساب أو محفظة بعملة مستقلة"),onBack){
        OutlinedTextField(name,{name=it;error=null},Modifier.fillMaxWidth(),label={Text(s("الاسم","Name"))},singleLine=true)
        OutlinedTextField(balance,{balance=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(s("الرصيد الافتتاحي","Opening balance"))},singleLine=true)
        Text(localizedLegacyText("نوع الحساب"))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf("cash" to flosiText("cash"),"bank" to flosiText("bank"),"wallet" to flosiText("wallet")).forEach{(k,l)->FilterChip(type==k,{type=k;error=null},{Text(l)})}
        }
        Text(localizedLegacyText("عملة الحساب"))
        Box(Modifier.fillMaxWidth()){
            OutlinedButton(onClick={currencyMenu=true},modifier=Modifier.fillMaxWidth()){Text(currency.ifBlank{settings.currency})}
            DropdownMenu(expanded=currencyMenu,onDismissRequest={currencyMenu=false}){
                currencies.forEach{code->DropdownMenuItem(text={Text(code)},onClick={currency=code;currencyMenu=false;error=null},trailingIcon={if(currency==code)Text("✓",color=FlosiPurple)})}
            }
        }
        error?.let{Text(it,color=FlosiRed)}
        Button(
            onClick={
                saving=true;error=null
                vm.add(name,type,balance.toLongOrNull()?:0L,currency.ifBlank{settings.currency}){message->
                    saving=false
                    if(message==null)onBack()else error=message
                }
            },
            enabled=!saving&&name.isNotBlank()&&currency.isNotBlank(),
            modifier=Modifier.fillMaxWidth()
        ){
            if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(flosiText("save"))
        }
    }
}
