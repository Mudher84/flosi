package com.flosi.app.ui.screens.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences

@Composable
fun MeSettingsScreen(
 onAccounts:()->Unit,onBudgets:()->Unit,onGoals:()->Unit,onCommitments:()->Unit,onAnalytics:()->Unit,
 onInvoices:()->Unit,onSecurity:()->Unit,onLocale:()->Unit,onData:()->Unit
){
 var connectionsOpen by remember{mutableStateOf(false)}
 var aboutOpen by remember{mutableStateOf(false)}
 if(connectionsOpen){
  BankConnectionsScreen{connectionsOpen=false}
  return
 }
 if(aboutOpen){
  AboutFlosiScreen{aboutOpen=false}
  return
 }
 val prefs=rememberFlosiPreferences();val state by prefs.state.collectAsState(initial=FlosiPreferencesState())
 FlosiPage(flosiText("me"),localizedLegacyText("إعداداتك ومساحتك")){
  CardBox{Metric(localizedLegacyText("فلوسي الشخصي"),state.currency,FlosiPurple);Text("العربية")}
  CardBox{
   ActionRow(flosiText("accounts"),localizedLegacyText("أموالك"),onClick=onAccounts)
   ActionRow("ربط البنوك والمحافظ","ZainCash • Open Banking",onClick={connectionsOpen=true})
   ActionRow(flosiText("budgets"),localizedLegacyText("خطط الصرف"),onClick=onBudgets)
   ActionRow(flosiText("goals"),flosiText("savings"),onClick=onGoals)
   ActionRow(flosiText("commitments"),localizedLegacyText("القادم عليك"),onClick=onCommitments)
   ActionRow(flosiText("analytics"),flosiText("reports"),onClick=onAnalytics)
   ActionRow(flosiText("invoices"),localizedLegacyText("بيع وقبض"),onClick=onInvoices)
   ActionRow(flosiText("data_center"),"CSV / PDF",onClick=onData)
   ActionRow(flosiText("security"),localizedLegacyText("حماية"),onClick=onSecurity)
   ActionRow(flosiText("language_currency"),"العربية • ${state.currency}",onClick=onLocale)
   ActionRow(localizedLegacyText("حول Flosi"),"Yam Studio • Wana84.com",onClick={aboutOpen=true})
  }
 }
}
