package com.flosi.app.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.FlosiLaunchCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences
import kotlinx.coroutines.launch

@Composable
fun AccountsScreen(onBack:()->Unit,onOpen:(Long)->Unit,onAdd:()->Unit){
 val vm:AccountsViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState();val prefs=rememberFlosiPreferences();val settings by prefs.state.collectAsState(initial=FlosiPreferencesState());val scope=rememberCoroutineScope();val lang=LocalFlosiLanguage.current;val base=settings.currency
 fun c(key:String,vararg values:Pair<String,Any?>)=FlosiLaunchCopy.text(lang,key,*values)
 val included=accounts.filter{it.includeInTotal};val converted=included.map{a->a to CurrencyConverter.convert(a.currentBalance,a.currency,base,settings.exchangeRates)};val missing=converted.filter{it.second==null}.map{it.first.currency}.distinct();val total=converted.mapNotNull{it.second}.sum();val bankAccounts=accounts.filter{it.type.equals("bank",ignoreCase=true)};var showBankConnect by remember{mutableStateOf(false)}
 FlosiPage(flosiText("accounts"),flosiText("accounts_sub"),onBack){
  PremiumCard{
   Text(c("liquid_wealth"),color=Color.White.copy(alpha=.58f),fontSize=11.sp,fontWeight=FontWeight.Medium)
   Text(moneyText(total,base),color=Color.White,fontSize=31.sp,fontWeight=FontWeight.Black,letterSpacing=(-.5).sp)
   Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(7.dp).background(FlosiGreen,CircleShape));Spacer(Modifier.width(7.dp));Text(c("accounts_included","count" to included.size),color=Color.White.copy(alpha=.70f),fontSize=10.sp)}
   if(missing.isNotEmpty())Text(c("excluded","currencies" to missing.joinToString()),color=Color(0xFFFFC57A),fontSize=10.sp)
  }
  SectionTitle(flosiText("accounts"),flosiText("add_account"),onAdd)
  if(accounts.isEmpty()){
   EmptyState(title=c("no_accounts"),subtitle=c("add_first_account_sub"),action=c("add_first_account"),onAction=onAdd,symbol="◈")
  } else accounts.forEachIndexed{index,a->
   val accent=listOf(FlosiPurple,FlosiBlue,FlosiGreen,FlosiOrange)[index%4]
   Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(26.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp),onClick={onOpen(a.id)}){
    Row(Modifier.fillMaxWidth().padding(17.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).background(accent.copy(alpha=.11f),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){Text(a.name.take(1).uppercase(),color=accent,fontWeight=FontWeight.Black,fontSize=18.sp)};Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(a.name,color=MaterialTheme.colorScheme.onSurface,fontWeight=FontWeight.ExtraBold,fontSize=15.sp);Text("${a.type} • ${a.currency}",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp)};Text(moneyText(a.currentBalance,a.currency),color=MaterialTheme.colorScheme.onSurface,fontWeight=FontWeight.Black,fontSize=14.sp)}
   }
  }
  SectionTitle(flosiText("connected_banks"))
  CardBox{
   Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).background(FlosiPurpleSoft,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Text("⌁",color=FlosiPurple,fontSize=21.sp,fontWeight=FontWeight.Black)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(c("bank_connection"),fontWeight=FontWeight.ExtraBold);Text(c("bank_secure_sync"),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp)}}
   Button(onClick={showBankConnect=true},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Text(c("connect_bank_account"))}
   BankOptionRow(flosiText("auto_transactions"),settings.bankSyncEnabled,bankAccounts.isNotEmpty()){scope.launch{prefs.setBankSyncEnabled(it)}}
   BankOptionRow(flosiText("salary_auto"),settings.salaryAutoAdd,settings.bankSyncEnabled&&bankAccounts.isNotEmpty()){scope.launch{prefs.setSalaryAutoAdd(it)}}
   BankOptionRow(flosiText("review_before_add"),settings.bankReviewBeforeAdd,settings.bankSyncEnabled&&bankAccounts.isNotEmpty()){scope.launch{prefs.setBankReviewBeforeAdd(it)}}
   Text(c("bank_password_notice"),color=FlosiGreen,fontSize=10.sp,fontWeight=FontWeight.SemiBold)
  }
 }
 if(showBankConnect)AlertDialog(onDismissRequest={showBankConnect=false},title={Text(c("connect_bank"))},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(c("bank_live_notice"));Text(c("bank_import_notice"),color=FlosiPurple)}},confirmButton={TextButton(onClick={showBankConnect=false}){Text(c("ok"))}})
}
@Composable private fun BankOptionRow(title:String,checked:Boolean,enabled:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(title,modifier=Modifier.weight(1f),color=if(enabled)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp,fontWeight=FontWeight.SemiBold);Switch(checked=checked,onCheckedChange=onChange,enabled=enabled)}}
