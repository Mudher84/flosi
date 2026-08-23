package com.flosi.app.ui.screens.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.flosi.app.i18n.FlosiLocales
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
 val prefs=rememberFlosiPreferences();val state by prefs.state.collectAsState(initial=FlosiPreferencesState())
 val locale=FlosiLocales.get(state.language)
 FlosiPage(flosiText("me"),localizedLegacyText("إعداداتك ومساحتك")){
  CardBox{Metric(localizedLegacyText("فلوسي الشخصي"),state.currency,FlosiPurple);Text(locale.label)}
  CardBox{
   ActionRow(flosiText("accounts"),localizedLegacyText("أموالك"),onClick=onAccounts)
   ActionRow(flosiText("budgets"),localizedLegacyText("خطط الصرف"),onClick=onBudgets)
   ActionRow(flosiText("goals"),flosiText("savings"),onClick=onGoals)
   ActionRow(flosiText("commitments"),localizedLegacyText("القادم عليك"),onClick=onCommitments)
   ActionRow(flosiText("analytics"),flosiText("reports"),onClick=onAnalytics)
   ActionRow(flosiText("invoices"),localizedLegacyText("بيع وقبض"),onClick=onInvoices)
   ActionRow(flosiText("data_center"),"CSV / PDF",onClick=onData)
   ActionRow(flosiText("security"),localizedLegacyText("حماية"),onClick=onSecurity)
   ActionRow(flosiText("language_currency"),"${locale.label} • ${state.currency}",onClick=onLocale)
  }
 }
}
