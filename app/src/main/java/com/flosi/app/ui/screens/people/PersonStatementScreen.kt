package com.flosi.app.ui.screens.people
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository

@Composable fun PersonStatementScreen(id:Long,onBack:()->Unit,onAddTx:()->Unit){
 val repo=rememberFlosiRepository()
 val person by repo.observePerson(id).collectAsState(initial=null)
 val txs by repo.observePersonTransactions(id).collectAsState(initial=emptyList())
 FlosiPage(person?.name ?: "كشف الحساب","حساب الشخص",onBack){
  person?.let{p->CardBox{Metric(if(p.currentBalance>=0)"لك عنده" else "عليك له",moneyText(p.currentBalance),if(p.currentBalance>=0)FlosiGreen else FlosiRed);Text(p.phone)}}
  Button(onClick=onAddTx){Text("+ حركة")}
  SectionTitle("الحركات")
  CardBox{
   if(txs.isEmpty())Text("ماكو حركات مرتبطة",color=FlosiMuted)
   txs.forEach{t->ActionRow(t.title,t.categoryName ?: t.kind,moneyText(t.amount))}
  }
 }
}
