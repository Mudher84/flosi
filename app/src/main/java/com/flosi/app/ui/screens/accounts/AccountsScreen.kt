package com.flosi.app.ui.screens.accounts

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences

@Composable
fun AccountsScreen(onBack:()->Unit,onOpen:(Long)->Unit,onAdd:()->Unit){
 val vm:AccountsViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState()
 val prefs=rememberFlosiPreferences();val settings by prefs.state.collectAsState(initial=FlosiPreferencesState())
 val base=settings.currency
 val included=accounts.filter{it.includeInTotal}
 val converted=included.map{a->a to CurrencyConverter.convert(a.currentBalance,a.currency,base,settings.exchangeRates)}
 val missing=converted.filter{it.second==null}.map{it.first.currency}.distinct()
 val total=converted.mapNotNull{it.second}.sum()
 val bankAccounts=accounts.filter{it.type.equals("bank",ignoreCase=true)}

 FlosiPage("الحسابات والمحافظ","أموالك موزعة بوضوح",onBack){
  CardBox{
   Metric("إجمالي السيولة",moneyText(total,base),FlosiPurple)
   if(missing.isNotEmpty()) Text("غير محتسب: ${missing.joinToString()} — أضف سعر تحويل حتى يدخل بالإجمالي",color=FlosiOrange)
  }

  SectionTitle("الحسابات","+ حساب",onAdd)
  CardBox{
   if(accounts.isEmpty()) Text("لا توجد حسابات بعد",color=FlosiMuted)
   accounts.forEach{a->ActionRow(a.name,"${a.type} • ${a.currency}",moneyText(a.currentBalance,a.currency),FlosiPurple){onOpen(a.id)}}
  }

  SectionTitle("الحسابات المصرفية المتصلة")
  CardBox{
   Metric("المزامنة المصرفية",if(bankAccounts.isEmpty()) "أضف حساباً مصرفياً" else "جاهزة للربط",if(bankAccounts.isEmpty()) FlosiOrange else FlosiGreen)
   Text(
    "عند ربط API رسمي للمصرف سيستقبل Flosi الحركات الجديدة فقط، يمنع تكرارها بمعرّف الحركة، ويميّز الراتب تلقائياً عن التحويلات والإيداعات الأخرى.",
    color=FlosiMuted
   )
   Text(
    "لا يتم إنشاء دخل وهمي قبل وصول حركة مصرفية مؤكدة من مزود رسمي.",
    color=FlosiPurple
   )
  }
 }
}
