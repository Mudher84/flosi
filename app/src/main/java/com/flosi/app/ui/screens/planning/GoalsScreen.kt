package com.flosi.app.ui.screens.planning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun GoalsScreen(onBack:()->Unit,onEdit:()->Unit){
 val vm:PlanningViewModel=flosiViewModel();val items by vm.goals.collectAsState()
 FlosiPage("خطتي","الأهداف والادخار",onBack){
  SectionTitle("الأهداف","+ هدف",onEdit)
  items.forEach{g->CardBox{ActionRow(g.title,"${g.savedAmount} محفوظ",moneyText(g.targetAmount),FlosiPurple,onClick=onEdit);LinearProgressIndicator(progress={if(g.targetAmount>0)(g.savedAmount.toFloat()/g.targetAmount).coerceIn(0f,1f) else 0f})}}
  if(items.isEmpty())CardBox{Text("ابدأ أول هدف ادخار",color=FlosiMuted)}
 }
}
