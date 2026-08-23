package com.flosi.app.ui.screens.people
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun PeopleScreen(onOpenPerson:(Long)->Unit,onAddPerson:()->Unit){
 val vm:PeopleViewModel=flosiViewModel()
 val people by vm.people.collectAsState()
 FlosiPage("الأشخاص","حساباتك مع الآخرين"){
  val net=people.sumOf{it.currentBalance}
  CardBox{Metric("صافي حسابات الأشخاص",signedMoney(net),if(net>=0)FlosiGreen else FlosiRed)}
  SectionTitle("الأشخاص","+ شخص",onAddPerson)
  CardBox{
   if(people.isEmpty())Text("أضف أول شخص",color=FlosiMuted)
   people.forEach{p->ActionRow(p.name,p.phone,if(p.currentBalance>=0)"لك ${moneyText(p.currentBalance)}" else "عليك ${moneyText(p.currentBalance)}",if(p.currentBalance>=0)FlosiGreen else FlosiRed){onOpenPerson(p.id)}}
  }
 }
}
