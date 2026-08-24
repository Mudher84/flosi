package com.flosi.app.ui.screens.accounts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository

@Composable fun AccountDetailScreen(id:Long,onBack:()->Unit,onTransfer:(Long)->Unit){
 val repo=rememberFlosiRepository();val account by repo.observeAccount(id).collectAsState(initial=null)
 FlosiPage(account?.name ?: flosiText("account"),localizedLegacyText("تفاصيل الحساب"),onBack){
  account?.let{a->
   CardBox{Metric(localizedLegacyText("الرصيد الحالي"),moneyText(a.currentBalance,a.currency),FlosiPurple);Text("${a.type} • ${a.currency}",color=FlosiMuted)}
   CardBox{ActionRow(localizedLegacyText("تحويل من/إلى الحساب"),localizedLegacyText("إدارة التحويلات"),onClick={onTransfer(a.id)})}
  }
  if(account==null) CardBox{Text(localizedLegacyText("تعذر العثور على الحساب"),color=FlosiRed)}
 }
}
