package com.flosi.app.ui.screens.activity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository

@Composable fun TransactionDetailScreen(id:Long,onBack:()->Unit){
 val repo=rememberFlosiRepository();val item by repo.observeTransaction(id).collectAsState(initial=null);val lang=LocalFlosiLanguage.current
 FlosiPage(localizedLegacyText("تفاصيل الحركة"),if(lang=="ar")"حركة محفوظة" else "Saved transaction",onBack){
  val tx=item
  if(tx==null){CardBox{Text(if(lang=="ar")"الحركة غير موجودة" else "Transaction not found")}}
  else{
   val positive=tx.kind in listOf("income","transfer_in","invoice_payment")
   CardBox{Metric(flosiText("amount"),moneyText(tx.amount,tx.accountCurrency),if(positive)FlosiGreen else FlosiRed);Text(tx.title);Text(tx.kind,color=FlosiMuted)}
   CardBox{
    ActionRow(flosiText("account"),tx.accountName)
    ActionRow(if(lang=="ar")"الشخص المرتبط" else "Linked person",tx.personName ?: if(lang=="ar")"بدون شخص" else "No person")
    ActionRow(flosiText("category"),tx.categoryName ?: if(lang=="ar")"بدون تصنيف" else "No category")
    if(tx.note.isNotBlank())ActionRow(if(lang=="ar")"ملاحظة" else "Note",tx.note)
   }
  }
 }
}
