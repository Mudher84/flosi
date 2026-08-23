package com.flosi.app.ui.screens.commitments
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun CommitmentsScreen(onBack:()->Unit,onEdit:()->Unit){
 val vm:PlanningViewModel=flosiViewModel();val items by vm.commitments.collectAsState()
 FlosiPage("الالتزامات والأقساط","القادم عليك",onBack){
  CardBox{Metric("إجمالي الالتزامات",moneyText(items.sumOf{it.amount}),FlosiRed)}
  SectionTitle("القادم","+ التزام",onEdit)
  CardBox{if(items.isEmpty())androidx.compose.material3.Text("ماكو التزامات بعد",color=FlosiMuted);items.forEach{ActionRow(it.title,it.repeatRule,moneyText(it.amount),FlosiOrange,onClick=onEdit)}}
 }
}
