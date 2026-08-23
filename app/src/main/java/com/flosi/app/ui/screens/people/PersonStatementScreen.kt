package com.flosi.app.ui.screens.people

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository

@Composable
fun PersonStatementScreen(id:Long,onBack:()->Unit,onAddTx:()->Unit){
    val repo=rememberFlosiRepository();val lang=LocalFlosiLanguage.current
    val person by repo.observePerson(id).collectAsState(initial=null);val txs by repo.observePersonTransactions(id).collectAsState(initial=emptyList())
    fun s(ar:String,en:String)=if(lang=="ar")ar else en
    FlosiPage(person?.name ?: localizedLegacyText("كشف الحساب"),localizedLegacyText("حساب الشخص"),onBack){
        person?.let{p->
            CardBox{
                Metric(if(p.currentBalance>=0)s("لك عنده","Owed to you")else s("عليك له","You owe"),moneyText(p.currentBalance,p.currency),if(p.currentBalance>=0)FlosiGreen else FlosiRed)
                if(p.phone.isNotBlank())Text(p.phone)
                Text(s("عملة حساب الشخص: ${p.currency}","Person balance currency: ${p.currency}"),color=FlosiMuted)
            }
        }
        Button(onClick=onAddTx){Text("+ ${flosiText("activity")}")}
        SectionTitle(flosiText("activity"))
        CardBox{
            if(txs.isEmpty())Text(localizedLegacyText("ماكو حركات مرتبطة"),color=FlosiMuted)
            txs.forEach{t->ActionRow(t.title,listOfNotNull(t.categoryName,t.accountName).joinToString(" • "),moneyText(t.amount,t.accountCurrency),if(t.kind in setOf("income","invoice_payment","debt_received"))FlosiGreen else FlosiRed)}
        }
    }
}
