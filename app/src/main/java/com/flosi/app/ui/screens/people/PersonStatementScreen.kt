package com.flosi.app.ui.screens.people
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository

@Composable fun PersonStatementScreen(id:Long,onBack:()->Unit,onAddTx:()->Unit){
 val repo=rememberFlosiRepository();val lang=LocalFlosiLanguage.current
 val person by repo.observePerson(id).collectAsState(initial=null);val txs by repo.observePersonTransactions(id).collectAsState(initial=emptyList())
 FlosiPage(person?.name ?: localizedLegacyText("كشف الحساب"),localizedLegacyText("حساب الشخص"),onBack){
  person?.let{p->CardBox{Metric(if(p.currentBalance>=0){if(lang=="ar")"لك عنده" else "Owed to you"}else{if(lang=="ar")"عليك له" else "You owe"},moneyText(p.currentBalance),if(p.currentBalance>=0)FlosiGreen else FlosiRed);Text(p.phone)}}
  Button(onClick=onAddTx){Text("+ ${flosiText("activity")}")}
  SectionTitle(flosiText("activity"))
  CardBox{
   if(txs.isEmpty())Text(localizedLegacyText("ماكو حركات مرتبطة"),color=FlosiMuted)
   txs.forEach{t->ActionRow(t.title,t.categoryName ?: t.kind,moneyText(t.amount))}
  }
 }
}
