package com.flosi.app.ui.screens.analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.HomeViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun AnalyticsScreen(onBack:()->Unit){
 val vm:HomeViewModel=flosiViewModel();val state by vm.state.collectAsState()
 FlosiPage("التقارير","من بياناتك الفعلية",onBack){
  CardBox{Metric("دخل الشهر",moneyText(state.dashboard.monthIncome),FlosiGreen);Metric("مصروف الشهر",moneyText(state.dashboard.monthExpense),FlosiRed);Metric("الصافي",signedMoney(state.dashboard.monthIncome-state.dashboard.monthExpense),FlosiPurple)}
  SectionTitle("أعلى التصنيفات")
  CardBox{state.topCategories.forEach{ActionRow(it.categoryName,"هذا الشهر",moneyText(it.amount),FlosiOrange)}}
 }
}
