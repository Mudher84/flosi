package com.flosi.app.ui.screens.people

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun PeopleScreen(onOpenPerson:(Long)->Unit,onAddPerson:()->Unit){
    val vm:PeopleViewModel=flosiViewModel()
    val people by vm.people.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val lang=LocalFlosiLanguage.current
    val base=CurrencyConverter.normalizeCode(prefs.currency)
    val missing=linkedSetOf<String>()

    fun converted(amount:Long,currency:String):Long?{
        val value=CurrencyConverter.convert(amount,currency,base,prefs.exchangeRates)
        if(value==null) missing+=CurrencyConverter.normalizeCode(currency)
        return value
    }

    var receivable=0L
    var payable=0L
    people.forEach { person ->
        val value=converted(person.currentBalance,person.currency) ?: return@forEach
        if(value>=0L) receivable=runCatching{Math.addExact(receivable,value)}.getOrElse{Long.MAX_VALUE}
        else payable=runCatching{Math.addExact(payable,Math.negateExact(value))}.getOrElse{Long.MAX_VALUE}
    }
    val net=runCatching{Math.subtractExact(receivable,payable)}.getOrElse{if(receivable>=payable)Long.MAX_VALUE else Long.MIN_VALUE}
    val owedToYouCount=people.count{it.currentBalance>0L}
    val youOweCount=people.count{it.currentBalance<0L}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(s("الديوان","Diwan"),s("كل ما لك وما عليك في مكان واحد","Everything you are owed and owe in one place")){
        CardBox{
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                Column(Modifier.weight(1f)){
                    Text(s("لك","Owed to you"),color=FlosiMuted)
                    Text(moneyText(receivable,base),color=FlosiGreen,style=MaterialTheme.typography.titleMedium)
                    Text(s("$owedToYouCount أشخاص","$owedToYouCount people"),color=FlosiMuted,style=MaterialTheme.typography.bodySmall)
                }
                Column(Modifier.weight(1f)){
                    Text(s("عليك","You owe"),color=FlosiMuted)
                    Text(moneyText(payable,base),color=FlosiRed,style=MaterialTheme.typography.titleMedium)
                    Text(s("$youOweCount أشخاص","$youOweCount people"),color=FlosiMuted,style=MaterialTheme.typography.bodySmall)
                }
            }
            HorizontalDivider(Modifier.padding(vertical=8.dp))
            Metric(s("الصافي","Net position"),signedMoney(net,base),if(net>=0)FlosiGreen else FlosiRed)
            if(missing.isNotEmpty())Text(
                s("لم تدخل في الإجمالي عملات بدون سعر تحويل: ${missing.joinToString()}","Currencies without an exchange rate were excluded: ${missing.joinToString()}"),
                color=FlosiOrange
            )
        }

        SectionTitle(s("الحسابات الشخصية","Personal ledgers"),s("+ شخص","+ Person"),onAddPerson)
        CardBox{
            if(people.isEmpty())Text(s("أضف أول شخص وابدأ تسجيل السلف والديون","Add your first person to start tracking debts"),color=FlosiMuted)
            people.sortedWith(compareByDescending<com.flosi.app.data.local.entity.PersonEntity>{kotlin.math.abs(it.currentBalance)}.thenBy{it.name}).forEach{p->
                val value=when{
                    p.currentBalance>0 -> s("لك ${moneyText(p.currentBalance,p.currency)}","Owed to you ${moneyText(p.currentBalance,p.currency)}")
                    p.currentBalance<0 -> s("عليك ${moneyText(kotlin.math.abs(p.currentBalance),p.currency)}","You owe ${moneyText(kotlin.math.abs(p.currentBalance),p.currency)}")
                    else -> s("الحساب مصفّى","Settled")
                }
                val color=when{
                    p.currentBalance>0 -> FlosiGreen
                    p.currentBalance<0 -> FlosiRed
                    else -> FlosiMuted
                }
                ActionRow(
                    p.name,
                    listOf(p.phone,p.currency).filter{it.isNotBlank()}.joinToString(" • "),
                    value,
                    color
                ){onOpenPerson(p.id)}
            }
        }

        CardBox{
            Text(s("طريقة الحساب","How it works"),style=MaterialTheme.typography.titleSmall)
            Text(
                s("الأخضر = مبلغ لك عند شخص. الأحمر = مبلغ عليك له. التسديدات الجزئية تبقى ضمن كشف حساب الشخص حتى يصل الرصيد إلى صفر.","Green means money owed to you. Red means money you owe. Partial repayments stay in the person's statement until the balance reaches zero."),
                color=FlosiMuted,
                style=MaterialTheme.typography.bodySmall
            )
        }
    }
}
