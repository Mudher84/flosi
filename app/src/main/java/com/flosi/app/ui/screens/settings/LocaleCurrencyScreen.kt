package com.flosi.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.FlosiLocales
import com.flosi.app.i18n.LocalFlosiLanguage
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
    val lang=LocalFlosiLanguage.current
    val currencies=listOf("IQD","USD","EUR","GBP","SAR","AED","KWD","QAR","JOD","EGP","TRY","INR","CNY","JPY","KRW","CAD","AUD","CHF","SEK","RUB")
    var rateCurrency by remember(state.currency){mutableStateOf(currencies.firstOrNull{it!=state.currency}?:"USD")}
    var rateText by remember{mutableStateOf("")}
    var message by remember{mutableStateOf("")}
    var messageError by remember{mutableStateOf(false)}
    var languageMenu by remember{mutableStateOf(false)}
    var currencyMenu by remember{mutableStateOf(false)}
    var rateCurrencyMenu by remember{mutableStateOf(false)}
    val selectedLocale=FlosiLocales.get(state.language)
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(flosiText("language_currency"),flosiText("all_languages_ready"),onBack){
        CardBox{
            Text(flosiText("choose_language"))
            Box(Modifier.fillMaxWidth()){
                OutlinedButton(onClick={languageMenu=true},modifier=Modifier.fillMaxWidth()){
                    Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Text(selectedLocale.label)}
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
            Text(s("${FlosiLocales.all.size} لغة متاحة","${FlosiLocales.all.size} languages available"),color=FlosiMuted)
        }

        CardBox{
            Text(flosiText("base_currency"))
            Text(s("هذه العملة تستخدم للتقارير والإجماليات فقط؛ كل حساب يحتفظ بعملته الأصلية.","This currency is used for reports and totals only; each account keeps its own currency."),color=FlosiMuted)
            Box(Modifier.fillMaxWidth()){
                OutlinedButton(onClick={currencyMenu=true},modifier=Modifier.fillMaxWidth()){
                    Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Text(state.currency)}
                }
                DropdownMenu(expanded=currencyMenu,onDismissRequest={currencyMenu=false}){
                    currencies.forEach{currency->
                        DropdownMenuItem(
                            text={Text(currency)},
                            onClick={currencyMenu=false;scope.launch{runCatching{prefs.setCurrency(currency)}.onFailure{messageError=true;message=it.message.orEmpty()}}},
                            trailingIcon={if(state.currency==currency){Text("✓",color=FlosiPurple)}}
                        )
                    }
                }
            }
        }

        CardBox{
            Text(flosiText("exchange_rates"))
            Text(s("أدخل سعر 1 وحدة من العملة المختارة مقابل ${state.currency}.","Enter the value of 1 unit of the selected currency in ${state.currency}."),color=FlosiMuted)
            Box(Modifier.fillMaxWidth()){
                OutlinedButton(onClick={rateCurrencyMenu=true},modifier=Modifier.fillMaxWidth()){
                    Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Text(rateCurrency)}
                }
                DropdownMenu(expanded=rateCurrencyMenu,onDismissRequest={rateCurrencyMenu=false}){
                    currencies.filter{it!=state.currency}.forEach{currency->
                        DropdownMenuItem(text={Text(currency)},onClick={rateCurrency=currency;rateCurrencyMenu=false;message=""})
                    }
                }
            }
            OutlinedTextField(
                rateText,
                {raw->
                    var seenSeparator=false
                    rateText=buildString {
                        raw.forEach{ch->when{
                            ch.isDigit()->append(ch)
                            (ch=='.'||ch==',')&&!seenSeparator->{append('.');seenSeparator=true}
                        }}
                    }
                    message=""
                },
                Modifier.fillMaxWidth(),
                label={Text("1 $rateCurrency = ? ${state.currency}")},
                singleLine=true
            )
            Button(
                onClick={scope.launch{
                    val ok=prefs.setExchangeRate(rateCurrency,state.currency,rateText)
                    messageError=!ok
                    message=if(ok)s("تم حفظ سعر التحويل","Exchange rate saved") else s("سعر التحويل غير صالح","Invalid exchange rate")
                    if(ok)rateText=""
                }},
                enabled=rateText.toBigDecimalOrNull()?.let{it.signum()>0}==true,
                modifier=Modifier.fillMaxWidth()
            ){Text(flosiText("save_rate"))}
        }

        val saved=state.exchangeRates.mapNotNull(CurrencyConverter::parseRate).sortedWith(compareBy({it.from},{it.to}))
        CardBox{
            Text(flosiText("saved_rates"))
            if(saved.isEmpty()) Text(flosiText("no_data"),color=FlosiMuted)
            saved.forEach{r->
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                    Column(Modifier.weight(1f)){
                        Text("1 ${r.from} = ${r.value.stripTrailingZeros().toPlainString()} ${r.to}",color=FlosiText)
                        Text(s("يُستخدم مباشرة أو بالعكس تلقائياً","Used directly or reciprocally when needed"),color=FlosiMuted)
                    }
                    TextButton(onClick={scope.launch{prefs.removeExchangeRate(r.from,r.to);messageError=false;message=s("تم حذف السعر","Rate removed")}}){Text(flosiText("delete"),color=FlosiRed)}
                }
            }
        }
        if(message.isNotBlank()) Text(message,color=if(messageError)FlosiRed else FlosiGreen)
    }
}
