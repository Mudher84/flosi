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
 val included=accounts.filter{it.includeInTotal};val converted=included.map{a->a to CurrencyConverter.convert(a.currentBalance,a.currency,base,settings.exchangeRates)};val missing=converted.filter{it.second==null}.map{it.first.currency}.distinct();val total=converted.mapNotNull{it.second}.sum();val bankAccounts=accounts.filter{it.type.equals("bank",ignoreCase=true)};var showBankConnect by remember{mutableStateOf(false)}
 FlosiPage(flosiText("accounts"),flosiText("accounts_sub"),onBack){
  PremiumCard{
   Text(if(lang=="ar")"ثروتك السائلة" else "Liquid wealth",color=Color.White.copy(alpha=.58f),fontSize=11.sp,fontWeight=FontWeight.Medium)
   Text(moneyText(total,base),color=Color.White,fontSize=31.sp,fontWeight=FontWeight.Black,letterSpacing=(-.5).sp)
   Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(7.dp).background(FlosiGreen,CircleShape));Spacer(Modifier.width(7.dp));Text(if(lang=="ar")"${included.size} حساب داخل الإجمالي" else "${included.size} accounts included",color=Color.White.copy(alpha=.70f),fontSize=10.sp)}
   if(missing.isNotEmpty())Text(if(lang=="ar")"غير محتسب: ${missing.joinToString()}" else "Excluded: ${missing.joinToString()}",color=Color(0xFFFFC57A),fontSize=10.sp)
  }
  SectionTitle(flosiText("accounts"),flosiText("add_account"),onAdd)
  if(accounts.isEmpty()){
   EmptyState(
    title=if(lang=="ar")"بعد ما عندك حسابات" else "No accounts yet",
    subtitle=if(lang=="ar")"أضف أول حساب حتى يبدأ Flosi يحسب رصيدك وصرفك بصورة صحيحة." else "Add your first account so Flosi can track balances and spending correctly.",
    action=if(lang=="ar")"إضافة أول حساب" else "Add first account",
    onAction=onAdd,
    symbol="◈"
   )
  } else accounts.forEachIndexed{index,a->
   val accent=listOf(FlosiPurple,FlosiBlue,FlosiGreen,FlosiOrange)[index%4]
   Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(26.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp),onClick={onOpen(a.id)}){
    Row(Modifier.fillMaxWidth().padding(17.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).background(accent.copy(alpha=.11f),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){Text(a.name.take(1).uppercase(),color=accent,fontWeight=FontWeight.Black,fontSize=18.sp)};Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(a.name,color=MaterialTheme.colorScheme.onSurface,fontWeight=FontWeight.ExtraBold,fontSize=15.sp);Text("${a.type} • ${a.currency}",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp)};Text(moneyText(a.currentBalance,a.currency),color=MaterialTheme.colorScheme.onSurface,fontWeight=FontWeight.Black,fontSize=14.sp)}
   }
  }
  SectionTitle(flosiText("connected_banks"))
  CardBox{
   Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).background(FlosiPurpleSoft,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Text("⌁",color=FlosiPurple,fontSize=21.sp,fontWeight=FontWeight.Black)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(if(lang=="ar")"الربط المصرفي" else "Bank connection",fontWeight=FontWeight.ExtraBold);Text(if(lang=="ar")"مزامنة آمنة عند توفر API رسمي" else "Secure sync when an official API is available",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp)}}
   Button(onClick={showBankConnect=true},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Text(if(lang=="ar")"ربط حساب مصرفي" else "Connect bank account")}
   BankOptionRow(flosiText("auto_transactions"),settings.bankSyncEnabled,bankAccounts.isNotEmpty()){scope.launch{prefs.setBankSyncEnabled(it)}}
   BankOptionRow(flosiText("salary_auto"),settings.salaryAutoAdd,settings.bankSyncEnabled&&bankAccounts.isNotEmpty()){scope.launch{prefs.setSalaryAutoAdd(it)}}
   BankOptionRow(flosiText("review_before_add"),settings.bankReviewBeforeAdd,settings.bankSyncEnabled&&bankAccounts.isNotEmpty()){scope.launch{prefs.setBankReviewBeforeAdd(it)}}
   Text(if(lang=="ar")"Flosi ما يطلب ولا يخزن كلمة مرور حسابك المصرفي." else "Flosi never asks for or stores your bank password.",color=FlosiGreen,fontSize=10.sp,fontWeight=FontWeight.SemiBold)
  }
 }
 if(showBankConnect)AlertDialog(onDismissRequest={showBankConnect=false},title={Text(if(lang=="ar")"ربط المصرف" else "Connect bank")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(if(lang=="ar")"الربط الحي يتم فقط عبر API / OAuth رسمي من المصرف أو مزود Open Banking معتمد. Flosi لا يطلب ولا يخزن كلمة مرور حسابك المصرفي." else "Live connection is available only through an official bank API/OAuth or an approved Open Banking provider. Flosi never asks for or stores your bank password.");Text(if(lang=="ar")"بعد تفعيل قناة رسمية، يقرأ Flosi الحركات الجديدة فقط ويمنع التكرار." else "Once an official connection is configured, Flosi imports only new transactions and prevents duplicates.",color=FlosiPurple)}},confirmButton={TextButton(onClick={showBankConnect=false}){Text(if(lang=="ar")"حسناً" else "OK")}})
}
@Composable private fun BankOptionRow(title:String,checked:Boolean,enabled:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(title,modifier=Modifier.weight(1f),color=if(enabled)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp,fontWeight=FontWeight.SemiBold);Switch(checked=checked,onCheckedChange=onChange,enabled=enabled)}}
