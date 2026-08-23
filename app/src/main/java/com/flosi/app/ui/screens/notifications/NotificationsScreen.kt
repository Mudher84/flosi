package com.flosi.app.ui.screens.notifications
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun NotificationsScreen(onBack:()->Unit){
 val vm:PlanningViewModel=flosiViewModel();val items by vm.commitments.collectAsState()
 FlosiPage("الإشعارات","من التزاماتك الفعلية",onBack){
  CardBox{if(items.isEmpty())androidx.compose.material3.Text("ماكو التزامات تحتاج تنبيه",color=FlosiMuted);items.take(6).forEach{ActionRow(it.title,"موعد استحقاق محفوظ",moneyText(it.amount),FlosiOrange)}}
 }
}
