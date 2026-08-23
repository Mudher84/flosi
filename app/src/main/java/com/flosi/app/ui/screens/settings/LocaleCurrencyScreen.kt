package com.flosi.app.ui.screens.settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences
import kotlinx.coroutines.launch
@Composable fun LocaleCurrencyScreen(onBack:()->Unit){
 val prefs=rememberFlosiPreferences();val state by prefs.state.collectAsState(initial=com.flosi.app.settings.FlosiPreferencesState());val scope=rememberCoroutineScope()
 FlosiPage("العملة واللغة","إعدادات محفوظة",onBack){
  CardBox{Text("اللغة");Row{listOf("ar" to "العربية","en" to "English").forEach{(v,l)->FilterChip(state.language==v,{scope.launch{prefs.setLanguage(v)}},{Text(l)})}}}
  CardBox{Text("العملة");Row{listOf("IQD","USD","SAR","AED").forEach{v->FilterChip(state.currency==v,{scope.launch{prefs.setCurrency(v)}},{Text(v)})}}}
 }
}
