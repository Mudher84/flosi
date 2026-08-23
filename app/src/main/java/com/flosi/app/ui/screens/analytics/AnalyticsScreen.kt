package com.flosi.app.ui.screens.analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.HomeViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun AnalyticsScreen(onBack:()->Unit){
 val vm:HomeViewModel=flosiViewModel();val state by vm.state.collectAsState();val currency=state.dashboard.baseCurrency;val lang=LocalFlosiLanguage.current
 FlosiPage(flosiText("reports"),localizedLegacyText("من بياناتك الفعلية"),onBack){
  if(state.dashboard.hasUnconvertedCurrencies) CardBox{Text(if(lang=="ar")"بعض العملات غير محتسبة: ${state.dashboard.unconvertedCurrencies.joinToString()}" else "Some currencies are excluded: ${state.dashboard.unconvertedCurrencies.joinToString()}",color=FlosiOrange)}
  CardBox{Metric(flosiText("monthly_income"),moneyText(state.dashboard.monthIncome,currency),FlosiGreen);Metric(flosiText("monthly_expense"),moneyText(state.dashboard.monthExpense,currency),FlosiRed);Metric(flosiText("net"),signedMoney(state.dashboard.monthIncome-state.dashboard.monthExpense,currency),FlosiPurple)}
  SectionTitle(localizedLegacyText("أعلى التصنيفات"))
  CardBox{state.topCategories.forEach{ActionRow(it.categoryName,localizedLegacyText("هذا الشهر"),moneyText(it.amount,currency),FlosiOrange)}}
 }
}
