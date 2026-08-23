package com.flosi.app.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.TransactionsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun ActivityScreen(onOpenDetail:(Long)->Unit,onAdd:()->Unit){
    val vm:TransactionsViewModel=flosiViewModel()
    val items by vm.transactions.collectAsState()
    val query by vm.query.collectAsState()
    val lang=LocalFlosiLanguage.current

    FlosiPage(flosiText("activity"),localizedLegacyText("كل شيء مسجل")){
        OutlinedTextField(
            value=query,
            onValueChange=vm::setSearch,
            modifier=Modifier.fillMaxWidth(),
            placeholder={Text(if(lang=="ar")"ابحث بالحركة أو الشخص أو الحساب..." else "Search by transaction, person, or account…")}
        )

        CardBox{
            val currencies=items.map{it.accountCurrency}.distinct()
            if(currencies.size<=1){
                val currency=currencies.firstOrNull()?:"IQD"
                val net=items.sumOf{tx->when(tx.kind){"income","invoice_payment","debt_received"->tx.amount;"expense","debt_given"->-tx.amount;else->0L}}
                Metric(localizedLegacyText("صافي الحركات المعروضة"),signedMoney(net,currency),if(net>=0)FlosiGreen else FlosiRed)
            }else{
                Metric(localizedLegacyText("صافي الحركات المعروضة"),localizedLegacyText("متعدد العملات"),FlosiPurple)
                Text(localizedLegacyText("لا يتم جمع عملات مختلفة كأنها وحدة واحدة."),color=FlosiMuted)
            }
        }

        SectionTitle(localizedLegacyText("السجل"),"+ ${flosiText("activity")}",onAdd)
        CardBox{
            if(items.isEmpty()) Text(localizedLegacyText("ماكو حركات مطابقة"),color=FlosiMuted)
            items.forEach{tx->
                val positive=tx.kind in listOf("income","invoice_payment","debt_received")
                val neutral=tx.kind in listOf("transfer_in","transfer_out","goal_saving")
                val accent=when{neutral->FlosiPurple;positive->FlosiGreen;else->FlosiRed}
                val value=if(tx.kind=="goal_saving") (if(lang=="ar")"حجز " else "Reserved ")+moneyText(tx.amount,tx.accountCurrency) else moneyText(tx.amount,tx.accountCurrency)
                ActionRow(tx.title,listOfNotNull(tx.categoryName,tx.personName,tx.accountName).joinToString(" • "),value,accent){onOpenDetail(tx.id)}
            }
        }
    }
}
