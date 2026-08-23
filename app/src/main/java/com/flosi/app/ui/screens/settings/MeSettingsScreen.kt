package com.flosi.app.ui.screens.settings
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences
@Composable fun MeSettingsScreen(onAccounts:()->Unit,onBudgets:()->Unit,onGoals:()->Unit,onCommitments:()->Unit,onAnalytics:()->Unit,onInvoices:()->Unit,onSecurity:()->Unit,onLocale:()->Unit,onData:()->Unit){
 val prefs=rememberFlosiPreferences();val state by prefs.state.collectAsState(initial=com.flosi.app.settings.FlosiPreferencesState())
 FlosiPage("أنا","إعداداتك ومساحتك"){
  CardBox{Metric("فلوسي الشخصي",state.currency,FlosiPurple);androidx.compose.material3.Text(if(state.language=="ar")"العربية" else "English")}
  CardBox{
   ActionRow("الحسابات والمحافظ","أموالك",onClick=onAccounts);ActionRow("الميزانيات","خطط الصرف",onClick=onBudgets);ActionRow("الأهداف","الادخار",onClick=onGoals)
   ActionRow("الالتزامات","القادم عليك",onClick=onCommitments);ActionRow("التحليلات","تقارير",onClick=onAnalytics);ActionRow("الفواتير","بيع وقبض",onClick=onInvoices)
   ActionRow("الاستيراد والتصدير","CSV / PDF",onClick=onData);ActionRow("الأمان والنسخ","حماية",onClick=onSecurity);ActionRow("العملة واللغة","${state.language} • ${state.currency}",onClick=onLocale)
  }
 }
}
