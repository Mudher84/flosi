package com.flosi.app.ui.screens.people

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.FlosiPeopleCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun PeopleScreen(onOpenPerson:(Long)->Unit,onAddPerson:()->Unit){
    val vm:PeopleViewModel=flosiViewModel();val people by vm.people.collectAsState();val prefs by vm.preferences.collectAsState();val lang=LocalFlosiLanguage.current;fun c(key:String,vararg values:Pair<String,Any?>)=FlosiPeopleCopy.text(lang,key,*values)
    val base=CurrencyConverter.normalizeCode(prefs.currency);val missing=linkedSetOf<String>()
    fun converted(amount:Long,currency:String):Long?{val value=CurrencyConverter.convert(amount,currency,base,prefs.exchangeRates);if(value==null)missing+=CurrencyConverter.normalizeCode(currency);return value}
    var receivable=0L;var payable=0L
    people.forEach{person->val value=converted(person.currentBalance,person.currency)?:return@forEach;if(value>=0L)receivable=runCatching{Math.addExact(receivable,value)}.getOrElse{Long.MAX_VALUE}else payable=runCatching{Math.addExact(payable,Math.negateExact(value))}.getOrElse{Long.MAX_VALUE}}
    val net=runCatching{Math.subtractExact(receivable,payable)}.getOrElse{if(receivable>=payable)Long.MAX_VALUE else Long.MIN_VALUE};val owedToYouCount=people.count{it.currentBalance>0L};val youOweCount=people.count{it.currentBalance<0L}

    FlosiPage(c("title"),c("sub")){
        CardBox{
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                Column(Modifier.weight(1f)){Text(c("owed_to_you"),color=FlosiMuted);Text(moneyText(receivable,base),color=FlosiGreen,style=MaterialTheme.typography.titleMedium);Text(c("people_count","count" to owedToYouCount),color=FlosiMuted,style=MaterialTheme.typography.bodySmall)}
                Column(Modifier.weight(1f)){Text(c("you_owe"),color=FlosiMuted);Text(moneyText(payable,base),color=FlosiRed,style=MaterialTheme.typography.titleMedium);Text(c("people_count","count" to youOweCount),color=FlosiMuted,style=MaterialTheme.typography.bodySmall)}
            }
            HorizontalDivider(Modifier.padding(vertical=8.dp));Metric(c("net"),signedMoney(net,base),if(net>=0)FlosiGreen else FlosiRed)
            if(missing.isNotEmpty())Text(c("missing_rates","currencies" to missing.joinToString()),color=FlosiOrange)
        }
        SectionTitle(c("ledgers"),c("add_person"),onAddPerson)
        if(people.isEmpty()) EmptyState(title=c("no_people"),subtitle=c("empty_sub"),action=c("add_first"),onAction=onAddPerson,symbol="◎")
        else CardBox{people.sortedWith(compareByDescending<com.flosi.app.data.local.entity.PersonEntity>{kotlin.math.abs(it.currentBalance)}.thenBy{it.name}).forEach{p->val value=when{p.currentBalance>0->c("owed_value","amount" to moneyText(p.currentBalance,p.currency));p.currentBalance<0->c("owe_value","amount" to moneyText(kotlin.math.abs(p.currentBalance),p.currency));else->c("settled")};val color=when{p.currentBalance>0->FlosiGreen;p.currentBalance<0->FlosiRed;else->FlosiMuted};ActionRow(p.name,listOf(p.phone,p.currency).filter{it.isNotBlank()}.joinToString(" • "),value,color){onOpenPerson(p.id)}}}
        CardBox{Text(c("how"),style=MaterialTheme.typography.titleSmall);Text(c("how_body"),color=FlosiMuted,style=MaterialTheme.typography.bodySmall)}
    }
}
