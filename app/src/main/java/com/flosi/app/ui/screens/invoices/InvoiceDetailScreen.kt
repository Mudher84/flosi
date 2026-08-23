package com.flosi.app.ui.screens.invoices
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository
@Composable fun InvoiceDetailScreen(id:Long,onBack:()->Unit,onPdf:(Long)->Unit){
 val repo=rememberFlosiRepository();val inv by repo.observeInvoice(id).collectAsState(initial=null);val items by repo.observeInvoiceItems(id).collectAsState(initial=emptyList())
 FlosiPage(inv?.let{"فاتورة #${it.number}"} ?: "الفاتورة","تفاصيل الفاتورة",onBack){
  inv?.let{i->CardBox{Metric("الإجمالي",moneyText(i.total),FlosiPurple);ActionRow("الحالة",i.status);ActionRow("المدفوع","",moneyText(i.paidAmount))}}
  CardBox{items.forEach{ActionRow(it.title,"${it.quantity} × ${moneyText(it.unitPrice)}",moneyText(it.lineTotal))}}
  androidx.compose.material3.Button(onClick={onPdf(id)}){androidx.compose.material3.Text("حفظ / مشاركة PDF")}
 }
}
