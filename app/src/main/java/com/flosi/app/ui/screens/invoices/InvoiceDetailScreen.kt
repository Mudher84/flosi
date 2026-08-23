package com.flosi.app.ui.screens.invoices

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository
import java.util.Locale

private fun quantityText(value:Double):String = if(value%1.0==0.0) String.format(Locale.getDefault(),"%.0f",value) else String.format(Locale.getDefault(),"%.3f",value).trimEnd('0').trimEnd('.')

@Composable
fun InvoiceDetailScreen(id:Long,onBack:()->Unit,onPdf:(Long)->Unit){
    val repo=rememberFlosiRepository();val inv by repo.observeInvoice(id).collectAsState(initial=null);val items by repo.observeInvoiceItems(id).collectAsState(initial=emptyList());val lang=LocalFlosiLanguage.current
    fun s(ar:String,en:String)=if(lang=="ar")ar else en
    fun statusLabel(status:String)=if(lang=="ar")when(status){"paid"->"مدفوعة";"partial"->"مدفوعة جزئياً";"unpaid"->"غير مدفوعة";"cancelled"->"ملغاة";else->"مسودة"}else when(status){"paid"->"Paid";"partial"->"Partially paid";"unpaid"->"Unpaid";"cancelled"->"Cancelled";else->"Draft"}

    FlosiPage(inv?.let{"${s("فاتورة","Invoice")} #${it.number}"}?:flosiText("invoices"),s("تفاصيل الفاتورة","Invoice details"),onBack){
        inv?.let{i->
            val currency=i.currency.trim().uppercase().ifBlank{"IQD"};val remaining=(i.total-i.paidAmount).coerceAtLeast(0L);val tone=when(i.status){"paid"->FlosiGreen;"partial"->FlosiOrange;else->FlosiPurple}
            CardBox{
                Text(s("عملة الفاتورة: $currency","Invoice currency: $currency"),color=FlosiMuted)
                Metric(s("الإجمالي النهائي","Grand total"),moneyText(i.total,currency),tone)
                ActionRow(s("الحالة","Status"),statusLabel(i.status),accent=tone)
                ActionRow(s("المجموع الفرعي","Subtotal"),"",moneyText(i.subtotal,currency),FlosiText)
                if(i.discount>0L)ActionRow(s("الخصم","Discount"),"",moneyText(i.discount,currency),FlosiRed)
                if(i.taxAmount>0L)ActionRow(s("الضريبة","Tax"),"${quantityText(i.taxPercent)}%",moneyText(i.taxAmount,currency),FlosiOrange)
                ActionRow(s("المدفوع","Paid"),"",moneyText(i.paidAmount,currency),FlosiGreen)
                ActionRow(s("المتبقي","Remaining"),"",moneyText(remaining,currency),if(remaining>0L)FlosiOrange else FlosiGreen)
            }
            CardBox{
                SectionTitle(s("البنود","Items"));if(items.isEmpty())Text(s("لا توجد بنود","No items"),color=FlosiMuted)
                items.forEach{item->ActionRow(item.title,"${quantityText(item.quantity)} × ${moneyText(item.unitPrice,currency)}",moneyText(item.lineTotal,currency),FlosiPurple)}
            }
        }?:CardBox{Text(s("الفاتورة غير موجودة","Invoice not found"),color=FlosiMuted)}
        Button(onClick={onPdf(id)},enabled=inv!=null){Text(s("حفظ / مشاركة PDF","Save / share PDF"))}
    }
}
