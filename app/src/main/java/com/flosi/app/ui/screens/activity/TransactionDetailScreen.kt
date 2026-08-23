package com.flosi.app.ui.screens.activity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository

@Composable fun TransactionDetailScreen(id:Long,onBack:()->Unit){
 val repo=rememberFlosiRepository()
 val item by repo.observeTransaction(id).collectAsState(initial=null)
 FlosiPage("تفاصيل الحركة","حركة محفوظة",onBack){
  val tx=item
  if(tx==null){CardBox{Text("الحركة غير موجودة")}}
  else{
   val positive=tx.kind in listOf("income","transfer_in","invoice_payment")
   CardBox{Metric("المبلغ",moneyText(tx.amount),if(positive)FlosiGreen else FlosiRed);Text(tx.title);Text(tx.kind,color=FlosiMuted)}
   CardBox{
    ActionRow("الحساب",tx.accountName)
    ActionRow("الشخص المرتبط",tx.personName ?: "بدون شخص")
    ActionRow("التصنيف",tx.categoryName ?: "بدون تصنيف")
    if(tx.note.isNotBlank())ActionRow("ملاحظة",tx.note)
   }
  }
 }
}
