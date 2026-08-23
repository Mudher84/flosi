package com.flosi.app.ui.screens.activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.TransactionsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun ActivityScreen(onOpenDetail:(Long)->Unit,onAdd:()->Unit){
 val vm:TransactionsViewModel=flosiViewModel()
 val items by vm.transactions.collectAsState()
 val query by vm.query.collectAsState()
 FlosiPage("الحركات","كل شيء مسجل"){
  OutlinedTextField(query,vm::setSearch,Modifier.fillMaxWidth(),placeholder={Text("ابحث بالحركة أو الشخص أو الحساب...")})
  CardBox{
   val net=items.sumOf{ if(it.kind in listOf("income","transfer_in","invoice_payment")) it.amount else -it.amount }
   Metric("صافي الحركات المعروضة",signedMoney(net),if(net>=0)FlosiGreen else FlosiRed)
  }
  SectionTitle("السجل","+ حركة",onAdd)
  CardBox{
   if(items.isEmpty()) Text("ماكو حركات مطابقة",color=FlosiMuted)
   items.forEach{
    val pos=it.kind in listOf("income","transfer_in","invoice_payment")
    ActionRow(it.title,listOfNotNull(it.categoryName,it.personName,it.accountName).joinToString(" • "),moneyText(it.amount),if(pos)FlosiGreen else FlosiRed){onOpenDetail(it.id)}
   }
  }
 }
}
