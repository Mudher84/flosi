package com.flosi.app.ui.screens.analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.HomeViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun AnalyticsScreen(onBack:()->Unit){
 val vm:HomeViewModel=flosiViewModel();val state by vm.state.collectAsState();val currency=state.dashboard.baseCurrency
 FlosiPage("التقارير","من بياناتك الفعلية",onBack){
  if(state.dashboard.hasUnconvertedCurrencies) CardBox{Text("بعض العملات غير محتسبة: ${state.dashboard.unconvertedCurrencies.joinToString()}",color=FlosiOrange)}
  CardBox{Metric("دخل الشهر",moneyText(state.dashboard.monthIncome,currency),FlosiGreen);Metric("مصروف الشهر",moneyText(state.dashboard.monthExpense,currency),FlosiRed);Metric("الصافي",signedMoney(state.dashboard.monthIncome-state.dashboard.monthExpense,currency),FlosiPurple)}
  SectionTitle("أعلى التصنيفات")
  CardBox{state.topCategories.forEach{ActionRow(it.categoryName,"هذا الشهر",moneyText(it.amount,currency),FlosiOrange)}}
 }
}
