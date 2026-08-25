package com.flosi.app.ui.screens.people

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository
import java.text.DateFormat

@Composable
fun PersonStatementScreen(id:Long,onBack:()->Unit,onAddTx:()->Unit,onAddCommitment:(Long)->Unit){
    val repo=rememberFlosiRepository();val lang=LocalFlosiLanguage.current
    val person by repo.observePerson(id).collectAsState(initial=null)
    val txs by repo.observePersonTransactions(id).collectAsState(initial=emptyList())
    val commitments by repo.commitments.collectAsState(initial=emptyList())
    val accounts by repo.accounts.collectAsState(initial=emptyList())
    val linkedCommitments=commitments.filter{it.personId==id}.sortedBy{it.dueAt}
    val accountMap=accounts.associateBy{it.id}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(person?.name ?: localizedLegacyText("كشف الحساب"),s("الديوان • ديون وسلف وأقساط","Diwan • debts, loans and installments"),onBack){
        person?.let{p->
            CardBox{
                Metric(if(p.currentBalance>=0)s("لك عنده","Owed to you")else s("عليك له","You owe"),moneyText(kotlin.math.abs(p.currentBalance),p.currency),if(p.currentBalance>=0)FlosiGreen else FlosiRed)
                if(p.phone.isNotBlank())Text(p.phone)
                Text(s("عملة حساب الشخص: ${p.currency}","Person balance currency: ${p.currency}"),color=FlosiMuted)
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            Button(onClick=onAddTx,modifier=Modifier.weight(1f)){Text(s("+ حركة","+ Entry"))}
            OutlinedButton(onClick={onAddCommitment(id)},modifier=Modifier.weight(1f)){Text(s("+ قسط / استحقاق","+ Installment"))}
        }

        if(linkedCommitments.isNotEmpty()){
            SectionTitle(s("الأقساط والاستحقاقات","Installments & due items"))
            CardBox{
                linkedCommitments.forEach{item->
                    val account=accountMap[item.accountId]
                    val overdue=item.dueAt<System.currentTimeMillis()
                    ActionRow(item.title,DateFormat.getDateInstance(DateFormat.MEDIUM).format(item.dueAt),moneyText(item.amount,account?.currency?:person?.currency?:"IQD"),if(overdue)FlosiRed else FlosiOrange)
                    if(overdue)Text(s("متأخر عن موعده","Overdue"),color=FlosiRed)
                }
            }
        }

        SectionTitle(flosiText("activity"))
        CardBox{
            if(txs.isEmpty())Text(localizedLegacyText("ماكو حركات مرتبطة"),color=FlosiMuted)
            txs.forEach{t->ActionRow(t.title,listOfNotNull(t.categoryName,t.accountName).joinToString(" • "),moneyText(t.amount,t.accountCurrency),if(t.kind in setOf("income","invoice_payment","debt_received"))FlosiGreen else FlosiRed)}
        }
    }
}
