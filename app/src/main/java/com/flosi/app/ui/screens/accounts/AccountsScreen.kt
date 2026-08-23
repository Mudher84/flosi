package com.flosi.app.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 val vm:AccountsViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState()
 val prefs=rememberFlosiPreferences();val settings by prefs.state.collectAsState(initial=FlosiPreferencesState())
 val scope=rememberCoroutineScope();val lang=LocalFlosiLanguage.current
 val base=settings.currency
 val included=accounts.filter{it.includeInTotal}
 val converted=included.map{a->a to CurrencyConverter.convert(a.currentBalance,a.currency,base,settings.exchangeRates)}
 val missing=converted.filter{it.second==null}.map{it.first.currency}.distinct()
 val total=converted.mapNotNull{it.second}.sum()
 val bankAccounts=accounts.filter{it.type.equals("bank",ignoreCase=true)}
 var showBankConnect by remember{mutableStateOf(false)}

 FlosiPage(flosiText("accounts"),flosiText("accounts_sub"),onBack){
  CardBox{
   Metric(flosiText("total_liquidity"),moneyText(total,base),FlosiPurple)
   if(missing.isNotEmpty()) Text(if(lang=="ar")"غير محتسب: ${missing.joinToString()} — أضف سعر تحويل حتى يدخل بالإجمالي" else "Excluded: ${missing.joinToString()} — add an exchange rate to include it in totals",color=FlosiOrange)
  }

  SectionTitle(flosiText("accounts"),flosiText("add_account"),onAdd)
  CardBox{
   if(accounts.isEmpty()) Text(flosiText("no_data"),color=FlosiMuted)
   accounts.forEach{a->ActionRow(a.name,"${a.type} • ${a.currency}",moneyText(a.currentBalance,a.currency),FlosiPurple){onOpen(a.id)}}
  }

  SectionTitle(flosiText("connected_banks"))
  CardBox{
   Button(onClick={showBankConnect=true},modifier=Modifier.fillMaxWidth()){
    Text(if(lang=="ar")"ربط حساب مصرفي" else "Connect bank account")
   }
   Metric(flosiText("bank_sync"),if(settings.bankSyncEnabled)flosiText("enabled") else flosiText("disabled"),if(settings.bankSyncEnabled)FlosiGreen else FlosiOrange)
   BankOptionRow(flosiText("auto_transactions"),settings.bankSyncEnabled,bankAccounts.isNotEmpty()){
    scope.launch{prefs.setBankSyncEnabled(it)}
   }
   BankOptionRow(flosiText("salary_auto"),settings.salaryAutoAdd,settings.bankSyncEnabled&&bankAccounts.isNotEmpty()){
    scope.launch{prefs.setSalaryAutoAdd(it)}
   }
   BankOptionRow(flosiText("review_before_add"),settings.bankReviewBeforeAdd,settings.bankSyncEnabled&&bankAccounts.isNotEmpty()){
    scope.launch{prefs.setBankReviewBeforeAdd(it)}
   }
   Text(
    if(bankAccounts.isEmpty()) {
     if(lang=="ar") "أضف حساباً من نوع مصرف لتظهر خيارات الربط عند توفر API رسمي للمصرف." else "Add a bank-type account. Connection options become active when an official bank API is available."
    } else flosiText("bank_sync_sub"),
    color=FlosiMuted
   )
   Text(
    if(lang=="ar") "لا ينشئ Flosi أي دخل قبل وصول حركة مصرفية مؤكدة. معرّف الحركة يمنع التكرار، والراتب يُصنف فقط بعد المطابقة." else "Flosi never creates income before a confirmed bank transaction arrives. Transaction IDs prevent duplicates and salary is classified only after matching.",
    color=FlosiPurple
   )
  }
 }

 if(showBankConnect){
  AlertDialog(
   onDismissRequest={showBankConnect=false},
   title={Text(if(lang=="ar")"ربط المصرف" else "Connect bank")},
   text={Column(verticalArrangement=Arrangement.spacedBy(androidx.compose.ui.unit.dp(8))){
    Text(if(lang=="ar")"الربط الحي يتم فقط عبر API / OAuth رسمي من المصرف أو مزود Open Banking معتمد. Flosi لا يطلب ولا يخزن كلمة مرور حسابك المصرفي." else "Live connection is available only through an official bank API/OAuth or an approved Open Banking provider. Flosi never asks for or stores your bank password.")
    Text(if(lang=="ar")"مصارف مقترحة: الرافدين، الرشيد، TBI، أو مصرف آخر." else "Suggested banks: Rafidain, Rasheed, TBI, or another bank.",color=FlosiMuted)
    Text(if(lang=="ar")"بعد تفعيل قناة رسمية، سيقرأ Flosi الحركات الجديدة فقط، يمنع التكرار، ويستطيع إضافة الراتب تلقائياً حسب اختيارك." else "Once an official connection is configured, Flosi imports only new transactions, prevents duplicates, and can add salary automatically if you enable it.",color=FlosiPurple)
   }},
   confirmButton={TextButton(onClick={showBankConnect=false}){Text(if(lang=="ar")"حسناً" else "OK")}}
  )
 }
}

@Composable
private fun BankOptionRow(title:String,checked:Boolean,enabled:Boolean,onChange:(Boolean)->Unit){
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
  Text(title,modifier=Modifier.weight(1f),color=if(enabled)FlosiText else FlosiMuted)
  Switch(checked=checked,onCheckedChange=onChange,enabled=enabled)
 }
}
