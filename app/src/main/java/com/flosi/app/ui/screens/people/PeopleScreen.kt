package com.flosi.app.ui.screens.people

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun PeopleScreen(onOpenPerson:(Long)->Unit,onAddPerson:()->Unit){
    val vm:PeopleViewModel=flosiViewModel();val people by vm.people.collectAsState();val prefs by vm.preferences.collectAsState();val lang=LocalFlosiLanguage.current
    val base=CurrencyConverter.normalizeCode(prefs.currency);val missing=linkedSetOf<String>()
    val net=people.fold(0L){acc,p->
        val converted=CurrencyConverter.convert(p.currentBalance,p.currency,base,prefs.exchangeRates)
        if(converted==null){missing+=CurrencyConverter.normalizeCode(p.currency);acc}else runCatching{Math.addExact(acc,converted)}.getOrElse{if(converted>=0)Long.MAX_VALUE else Long.MIN_VALUE}
    }
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(flosiText("people"),localizedLegacyText("حساباتك مع الآخرين")){
        CardBox{
            Metric(localizedLegacyText("صافي حسابات الأشخاص"),signedMoney(net,base),if(net>=0)FlosiGreen else FlosiRed)
            if(missing.isNotEmpty())Text(s("لم تدخل في الإجمالي عملات بدون سعر تحويل: ${missing.joinToString()}","Currencies without an exchange rate were excluded: ${missing.joinToString()}"),color=FlosiOrange)
        }
        SectionTitle(flosiText("people"),s("+ شخص","+ Person"),onAddPerson)
        CardBox{
            if(people.isEmpty())Text(localizedLegacyText("أضف أول شخص"),color=FlosiMuted)
            people.forEach{p->
                val value=if(p.currentBalance>=0){s("لك ${moneyText(p.currentBalance,p.currency)}","Owed to you ${moneyText(p.currentBalance,p.currency)}")}else{s("عليك ${moneyText(p.currentBalance,p.currency)}","You owe ${moneyText(p.currentBalance,p.currency)}")}
                ActionRow(p.name,listOf(p.phone,p.currency).filter{it.isNotBlank()}.joinToString(" • "),value,if(p.currentBalance>=0)FlosiGreen else FlosiRed){onOpenPerson(p.id)}
            }
        }
    }
}
