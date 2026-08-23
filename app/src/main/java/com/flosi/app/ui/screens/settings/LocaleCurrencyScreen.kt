package com.flosi.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.FlosiLocales
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

 FlosiPage("العملة واللغة","إعدادات محفوظة ومحرك تحويل واضح",onBack){
  CardBox{
   Text("لغة التطبيق")
   Text("تغيير اللغة يطبق اتجاه RTL/LTR على التطبيق كله فوراً.",color=FlosiMuted)
   Box(Modifier.fillMaxWidth()){
    OutlinedButton(onClick={languageMenu=true},modifier=Modifier.fillMaxWidth()){
     Text("${selectedLocale.label}  •  ${selectedLocale.localeTag}")
    }
    DropdownMenu(expanded=languageMenu,onDismissRequest={languageMenu=false}){
     FlosiLocales.all.forEach{locale->
      DropdownMenuItem(
       text={Text(locale.label)},
       onClick={
        languageMenu=false
        scope.launch{prefs.setLanguage(locale.code)}
       },
       trailingIcon={if(state.language==locale.code){Text("✓",color=FlosiPurple)}}
      )
     }
    }
   }
   Text("اللغات المتاحة: ${FlosiLocales.all.size}",color=FlosiMuted)
  }

  CardBox{
   Text("عملة التقارير الأساسية")
   Text("اختيار العملة هنا لا يغيّر عملة الحساب نفسه؛ فقط يوحّد التقارير بعد التحويل.",color=FlosiMuted)
   currencies.chunked(5).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){row.forEach{v->FilterChip(state.currency==v,{scope.launch{prefs.setCurrency(v)}},{Text(v)})}}}
  }

  CardBox{
   Text("أسعار التحويل")
   Text("Flosi لن يجمع عملتين مختلفتين بدون سعر معروف. أدخل سعر يدوي موثوق عند استخدام أكثر من عملة.",color=FlosiMuted)
   Text("1 من")
   Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){currencies.filter{it!=state.currency}.take(7).forEach{v->FilterChip(rateCurrency==v,{rateCurrency=v},{Text(v)})}}
   OutlinedTextField(rateText,{rateText=it.filter{ch->ch.isDigit()||ch=='.'||ch==','}},Modifier.fillMaxWidth(),label={Text("يساوي كم ${state.currency}")},singleLine=true)
   Button(
    onClick={scope.launch{val ok=prefs.setExchangeRate(rateCurrency,state.currency,rateText);message=if(ok)"تم حفظ سعر $rateCurrency/${state.currency}" else "تحقق من السعر";if(ok)rateText=""}},
    enabled=rateText.isNotBlank(),modifier=Modifier.fillMaxWidth()
   ){Text("حفظ سعر التحويل")}
  }

  val saved=state.exchangeRates.mapNotNull(CurrencyConverter::parseRate).sortedBy{it.from}
  CardBox{
   Text("الأسعار المحفوظة")
   if(saved.isEmpty()) Text("لا توجد أسعار مضافة بعد",color=FlosiMuted)
   saved.forEach{r->
    ActionRow("1 ${r.from}","سعر يدوي", "${r.value.stripTrailingZeros().toPlainString()} ${r.to}",FlosiPurple)
   }
  }
  if(message.isNotBlank()) Text(message,color=FlosiGreen)
 }
}
