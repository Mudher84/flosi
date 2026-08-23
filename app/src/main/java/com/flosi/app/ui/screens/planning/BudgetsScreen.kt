package com.flosi.app.ui.screens.planning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun BudgetsScreen(onBack:()->Unit,onDetail:()->Unit){
 val vm:PlanningViewModel=flosiViewModel();val items by vm.budgets.collectAsState()
 FlosiPage("الميزانيات","سيطر على الصرف",onBack){
  CardBox{Metric("إجمالي الحدود",moneyText(items.sumOf{it.limitAmount}),FlosiPurple)}
  SectionTitle("ميزانياتك","+ ميزانية",onDetail)
  CardBox{if(items.isEmpty())Text("ماكو ميزانيات بعد",color=FlosiMuted);items.forEach{ActionRow(it.title,"تنبيه ${it.warningPercent}%",moneyText(it.limitAmount),FlosiPurple,onClick=onDetail)}}
 }
}
