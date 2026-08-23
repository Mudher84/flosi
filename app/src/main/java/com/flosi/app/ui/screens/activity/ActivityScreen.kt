package com.flosi.app.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.TransactionsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun ActivityScreen(onOpenDetail:(Long)->Unit,onAdd:()->Unit){
    val vm:TransactionsViewModel=flosiViewModel()
    val items by vm.transactions.collectAsState()
    val query by vm.query.collectAsState()

    FlosiPage("الحركات","كل شيء مسجل"){
        OutlinedTextField(
            value=query,
            onValueChange=vm::setSearch,
            modifier=Modifier.fillMaxWidth(),
            placeholder={Text("ابحث بالحركة أو الشخص أو الحساب...")}
        )

        CardBox{
            val currencies=items.map{it.accountCurrency}.distinct()
            if(currencies.size<=1){
                val currency=currencies.firstOrNull()?:"IQD"
                val net=items.sumOf{tx->
                    when(tx.kind){
                        "income","invoice_payment","debt_received"->tx.amount
                        "expense","debt_given"->-tx.amount
                        else->0L
                    }
                }
                Metric("صافي الحركات المعروضة",signedMoney(net,currency),if(net>=0)FlosiGreen else FlosiRed)
            }else{
                Metric("صافي الحركات المعروضة","متعدد العملات",FlosiPurple)
                Text("لا يتم جمع عملات مختلفة كأنها وحدة واحدة.",color=FlosiMuted)
            }
        }

        SectionTitle("السجل","+ حركة",onAdd)
        CardBox{
            if(items.isEmpty()) Text("ماكو حركات مطابقة",color=FlosiMuted)
            items.forEach{tx->
                val positive=tx.kind in listOf("income","invoice_payment","debt_received")
                val neutral=tx.kind in listOf("transfer_in","transfer_out","goal_saving")
                val accent=when{
                    neutral->FlosiPurple
                    positive->FlosiGreen
                    else->FlosiRed
                }
                val value=when(tx.kind){
                    "goal_saving"->"حجز ${moneyText(tx.amount,tx.accountCurrency)}"
                    else->moneyText(tx.amount,tx.accountCurrency)
                }
                ActionRow(
                    title=tx.title,
                    subtitle=listOfNotNull(tx.categoryName,tx.personName,tx.accountName).joinToString(" • "),
                    value=value,
                    accent=accent
                ){
                    onOpenDetail(tx.id)
                }
            }
        }
    }
}
