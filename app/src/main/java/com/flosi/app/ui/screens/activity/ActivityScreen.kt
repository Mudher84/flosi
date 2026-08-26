package com.flosi.app.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.i18n.FlosiActivityCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.TransactionsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun ActivityScreen(onOpenDetail:(Long)->Unit,onAdd:()->Unit){
 val vm:TransactionsViewModel=flosiViewModel();val items by vm.transactions.collectAsState();val query by vm.query.collectAsState();val lang=LocalFlosiLanguage.current;var filter by remember{mutableStateOf("all")};fun c(key:String,vararg values:Pair<String,Any?>)=FlosiActivityCopy.text(lang,key,*values)
 val filtered=remember(items,filter){when(filter){"income"->items.filter{it.kind in listOf("income","invoice_payment","debt_received")};"expense"->items.filter{it.kind in listOf("expense","debt_given")};"transfer"->items.filter{it.kind.startsWith("transfer")||it.kind=="goal_saving"};else->items}}
 FlosiPage(flosiText("activity"),localizedLegacyText("كل شيء مسجل")){
  OutlinedTextField(query,vm::setSearch,Modifier.fillMaxWidth(),placeholder={Text(c("search"))},singleLine=true,shape=RoundedCornerShape(18.dp))
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("all" to c("all"),"income" to flosiText("income"),"expense" to flosiText("expense"),"transfer" to flosiText("transfer")).forEach{(key,label)->FilterChip(filter==key,{filter=key},{Text(label)},modifier=Modifier.weight(1f))}}
  if(filtered.isNotEmpty()){
   CardBox{val currencies=filtered.map{it.accountCurrency}.distinct();if(currencies.size<=1){val currency=currencies.firstOrNull()?:"IQD";val net=filtered.sumOf{tx->when(tx.kind){"income","invoice_payment","debt_received"->tx.amount;"expense","debt_given"->-tx.amount;else->0L}};Metric(localizedLegacyText("صافي الحركات المعروضة"),signedMoney(net,currency),if(net>=0)FlosiGreen else FlosiRed)}else{Metric(localizedLegacyText("صافي الحركات المعروضة"),localizedLegacyText("متعدد العملات"),FlosiPurple);Text(localizedLegacyText("لا يتم جمع عملات مختلفة كأنها وحدة واحدة."),color=MaterialTheme.colorScheme.onSurfaceVariant)}}
  }
  SectionTitle(localizedLegacyText("السجل"),"+ ${flosiText("activity")}",onAdd)
  if(filtered.isEmpty()) EmptyState(title=localizedLegacyText("ماكو حركات مطابقة"),subtitle=c("empty_sub"),action=localizedLegacyText("إضافة حركة"),onAction=onAdd)
  else CardBox{filtered.forEach{tx->val positive=tx.kind in listOf("income","invoice_payment","debt_received");val neutral=tx.kind in listOf("transfer_in","transfer_out","goal_saving");val accent=when{neutral->FlosiPurple;positive->FlosiGreen;else->FlosiRed};val value=if(tx.kind=="goal_saving")c("reserved","amount" to moneyText(tx.amount,tx.accountCurrency)) else moneyText(tx.amount,tx.accountCurrency);ActionRow(tx.title,listOfNotNull(tx.categoryName,tx.personName,tx.accountName).joinToString(" • "),value,accent){onOpenDetail(tx.id)}}}
 }
}
