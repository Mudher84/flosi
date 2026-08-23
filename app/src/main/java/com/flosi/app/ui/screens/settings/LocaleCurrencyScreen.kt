package com.flosi.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.FlosiLocales
import com.flosi.app.i18n.flosiText
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences
import kotlinx.coroutines.launch

@Composable
fun LocaleCurrencyScreen(onBack:()->Unit){
 val prefs=rememberFlosiPreferences()
 val state by prefs.state.collectAsState(initial=FlosiPreferencesState())
 val scope=rememberCoroutineScope()
 val currencies=listOf("IQD","USD","EUR","GBP","SAR","AED","KWD","QAR","JOD","EGP","TRY","INR","CNY","JPY","KRW","CAD","AUD","CHF","SEK","RUB")
 var rateCurrency by remember(state.currency){mutableStateOf(currencies.firstOrNull{it!=state.currency}?:"USD")}
 var rateText by remember{mutableStateOf("")}
 var message by remember{mutableStateOf("")}
 var languageMenu by remember{mutableStateOf(false)}
 val selectedLocale=FlosiLocales.get(state.language)
 val doneText=flosiText("done")

 FlosiPage(flosiText("language_currency"),flosiText("all_languages_ready"),onBack){
  CardBox{
   Text(flosiText("choose_language"))
   Box(Modifier.fillMaxWidth()){
    OutlinedButton(onClick={languageMenu=true},modifier=Modifier.fillMaxWidth()){
     Text("${selectedLocale.label}  •  ${selectedLocale.localeTag}")
    }
    DropdownMenu(expanded=languageMenu,onDismissRequest={languageMenu=false}){
     FlosiLocales.all.forEach{locale->
      DropdownMenuItem(
       text={Text(locale.label)},
       onClick={languageMenu=false;scope.launch{prefs.setLanguage(locale.code)}},
       trailingIcon={if(state.language==locale.code){Text("✓",color=FlosiPurple)}}
      )
     }
    }
   }
   Text("${FlosiLocales.all.size} languages",color=FlosiMuted)
  }

  CardBox{
   Text(flosiText("base_currency"))
   currencies.chunked(5).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){row.forEach{v->FilterChip(state.currency==v,{scope.launch{prefs.setCurrency(v)}},{Text(v)})}}}
  }

  CardBox{
   Text(flosiText("exchange_rates"))
   Text("1 ${rateCurrency}",color=FlosiMuted)
   Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){currencies.filter{it!=state.currency}.take(7).forEach{v->FilterChip(rateCurrency==v,{rateCurrency=v},{Text(v)})}}
   OutlinedTextField(rateText,{rateText=it.filter{ch->ch.isDigit()||ch=='.'||ch==','}},Modifier.fillMaxWidth(),label={Text("${rateCurrency} → ${state.currency}")},singleLine=true)
   Button(
    onClick={scope.launch{val ok=prefs.setExchangeRate(rateCurrency,state.currency,rateText);message=if(ok)doneText else "Invalid rate";if(ok)rateText=""}},
    enabled=rateText.isNotBlank(),modifier=Modifier.fillMaxWidth()
   ){Text(flosiText("save_rate"))}
  }

  val saved=state.exchangeRates.mapNotNull(CurrencyConverter::parseRate).sortedBy{it.from}
  CardBox{
   Text(flosiText("saved_rates"))
   if(saved.isEmpty()) Text(flosiText("no_data"),color=FlosiMuted)
   saved.forEach{r->ActionRow("1 ${r.from}","FX", "${r.value.stripTrailingZeros().toPlainString()} ${r.to}",FlosiPurple)}
  }
  if(message.isNotBlank()) Text(message,color=FlosiGreen)
 }
}
