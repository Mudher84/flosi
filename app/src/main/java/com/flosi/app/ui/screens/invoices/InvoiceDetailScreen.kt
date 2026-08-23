package com.flosi.app.ui.screens.invoices

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository
import java.util.Locale

private fun invoiceStatusLabel(status:String):String=when(status){
    "paid"->"مدفوعة"
    "partial"->"مدفوعة جزئياً"
    "unpaid"->"غير مدفوعة"
    "cancelled"->"ملغاة"
    else->"مسودة"
}

private fun quantityText(value:Double):String =
    if(value%1.0==0.0) String.format(Locale.US,"%.0f",value)
    else String.format(Locale.US,"%.3f",value).trimEnd('0').trimEnd('.')

@Composable
fun InvoiceDetailScreen(id:Long,onBack:()->Unit,onPdf:(Long)->Unit){
    val repo=rememberFlosiRepository()
    val inv by repo.observeInvoice(id).collectAsState(initial=null)
    val items by repo.observeInvoiceItems(id).collectAsState(initial=emptyList())

    FlosiPage(inv?.let{"فاتورة #${it.number}"} ?: "الفاتورة","تفاصيل الفاتورة",onBack){
        inv?.let{i->
            val currency=i.currency.trim().uppercase().ifBlank{"IQD"}
            val taxable=(i.subtotal-i.discount).coerceAtLeast(0L)
            val tax=(i.total-taxable).coerceAtLeast(0L)
            val remaining=(i.total-i.paidAmount).coerceAtLeast(0L)
            val tone=when(i.status){"paid"->FlosiGreen;"partial"->FlosiOrange;else->FlosiPurple}

            CardBox{
                Text("عملة الفاتورة: $currency",color=FlosiMuted)
                Metric("الإجمالي النهائي",moneyText(i.total,currency),tone)
                ActionRow("الحالة",invoiceStatusLabel(i.status),accent=tone)
                ActionRow("المجموع الفرعي","",moneyText(i.subtotal,currency),FlosiText)
                if(i.discount>0L)ActionRow("الخصم","",moneyText(i.discount,currency),FlosiRed)
                if(tax>0L)ActionRow("الضريبة","",moneyText(tax,currency),FlosiOrange)
                ActionRow("المدفوع","",moneyText(i.paidAmount,currency),FlosiGreen)
                ActionRow("المتبقي","",moneyText(remaining,currency),if(remaining>0L)FlosiOrange else FlosiGreen)
            }

            CardBox{
                SectionTitle("البنود")
                if(items.isEmpty())Text("لا توجد بنود",color=FlosiMuted)
                items.forEach{item->
                    ActionRow(
                        item.title,
                        "${quantityText(item.quantity)} × ${moneyText(item.unitPrice,currency)}",
                        moneyText(item.lineTotal,currency),
                        FlosiPurple
                    )
                }
            }
        } ?: CardBox{Text("الفاتورة غير موجودة",color=FlosiMuted)}

        Button(onClick={onPdf(id)},enabled=inv!=null){Text("حفظ / مشاركة PDF")}
    }
}
