package com.flosi.app.ui.screens.accounts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository

@Composable fun AccountDetailScreen(id:Long,onBack:()->Unit,onTransfer:()->Unit){
 val repo=rememberFlosiRepository();val account by repo.observeAccount(id).collectAsState(initial=null)
 FlosiPage(account?.name ?: "الحساب","تفاصيل الحساب",onBack){
  account?.let{a->CardBox{Metric("الرصيد الحالي",moneyText(a.currentBalance,a.currency),FlosiPurple);Text("${a.type} • ${a.currency}",color=FlosiMuted)}}
  CardBox{ActionRow("تحويل من/إلى الحساب","إدارة التحويلات",onClick=onTransfer)}
 }
}
