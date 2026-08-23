package com.flosi.app.ui.screens.people
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun PeopleScreen(onOpenPerson:(Long)->Unit,onAddPerson:()->Unit){
 val vm:PeopleViewModel=flosiViewModel();val people by vm.people.collectAsState();val lang=LocalFlosiLanguage.current
 FlosiPage(flosiText("people"),localizedLegacyText("حساباتك مع الآخرين")){
  val net=people.sumOf{it.currentBalance}
  CardBox{Metric(localizedLegacyText("صافي حسابات الأشخاص"),signedMoney(net),if(net>=0)FlosiGreen else FlosiRed)}
  SectionTitle(flosiText("people"),if(lang=="ar")"+ شخص" else "+ Person",onAddPerson)
  CardBox{
   if(people.isEmpty())Text(localizedLegacyText("أضف أول شخص"),color=FlosiMuted)
   people.forEach{p->
    val value=if(p.currentBalance>=0){if(lang=="ar")"لك ${moneyText(p.currentBalance)}" else "Owed to you ${moneyText(p.currentBalance)}"}else{if(lang=="ar")"عليك ${moneyText(p.currentBalance)}" else "You owe ${moneyText(p.currentBalance)}"}
    ActionRow(p.name,p.phone,value,if(p.currentBalance>=0)FlosiGreen else FlosiRed){onOpenPerson(p.id)}
   }
  }
 }
}
