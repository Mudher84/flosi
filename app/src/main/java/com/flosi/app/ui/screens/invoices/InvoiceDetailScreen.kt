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
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    fun statusLabel(status:String)=when(status){"paid"->s("مدفوعة","Paid","Ödendi","Payée","Bezahlt","Pagada");"partial"->s("مدفوعة جزئياً","Partially paid","Kısmen ödendi","Partiellement payée","Teilweise bezahlt","Pagada parcialmente");"unpaid"->s("غير مدفوعة","Unpaid","Ödenmedi","Impayée","Unbezahlt","Sin pagar");"cancelled"->s("ملغاة","Cancelled","İptal edildi","Annulée","Storniert","Cancelada");else->s("مسودة","Draft","Taslak","Brouillon","Entwurf","Borrador")}

    FlosiPage(inv?.let{"${s("فاتورة","Invoice","Fatura","Facture","Rechnung","Factura")} #${it.number}"}?:flosiText("invoices"),s("تفاصيل الفاتورة","Invoice details","Fatura ayrıntıları","Détails de la facture","Rechnungsdetails","Detalles de la factura"),onBack){
        inv?.let{i->
            val currency=i.currency.trim().uppercase().ifBlank{"IQD"};val remaining=(i.total-i.paidAmount).coerceAtLeast(0L);val tone=when(i.status){"paid"->FlosiGreen;"partial"->FlosiOrange;else->FlosiPurple}
            CardBox{
                Text(s("عملة الفاتورة: $currency","Invoice currency: $currency","Fatura para birimi: $currency","Devise de la facture : $currency","Rechnungswährung: $currency","Moneda de la factura: $currency"),color=FlosiMuted)
                Metric(s("الإجمالي النهائي","Grand total","Genel toplam","Total général","Gesamtsumme","Total general"),moneyText(i.total,currency),tone)
                ActionRow(s("الحالة","Status","Durum","Statut","Status","Estado"),statusLabel(i.status),accent=tone)
                ActionRow(s("المجموع الفرعي","Subtotal","Ara toplam","Sous-total","Zwischensumme","Subtotal"),"",moneyText(i.subtotal,currency),FlosiText)
                if(i.discount>0L)ActionRow(s("الخصم","Discount","İndirim","Remise","Rabatt","Descuento"),"",moneyText(i.discount,currency),FlosiRed)
                if(i.taxAmount>0L)ActionRow(s("الضريبة","Tax","Vergi","Taxe","Steuer","Impuesto"),"${quantityText(i.taxPercent)}%",moneyText(i.taxAmount,currency),FlosiOrange)
                ActionRow(s("المدفوع","Paid","Ödenen","Payé","Bezahlt","Pagado"),"",moneyText(i.paidAmount,currency),FlosiGreen)
                ActionRow(s("المتبقي","Remaining","Kalan","Restant","Verbleibend","Restante"),"",moneyText(remaining,currency),if(remaining>0L)FlosiOrange else FlosiGreen)
            }
            CardBox{SectionTitle(s("البنود","Items","Kalemler","Articles","Positionen","Partidas"));if(items.isEmpty())Text(s("لا توجد بنود","No items","Kalem yok","Aucun article","Keine Positionen","No hay partidas"),color=FlosiMuted);items.forEach{item->ActionRow(item.title,"${quantityText(item.quantity)} × ${moneyText(item.unitPrice,currency)}",moneyText(item.lineTotal,currency),FlosiPurple)}}
        }?:CardBox{Text(s("الفاتورة غير موجودة","Invoice not found","Fatura bulunamadı","Facture introuvable","Rechnung nicht gefunden","Factura no encontrada"),color=FlosiMuted)}
        Button(onClick={onPdf(id)},enabled=inv!=null){Text(s("حفظ / مشاركة PDF","Save / share PDF","PDF kaydet / paylaş","Enregistrer / partager le PDF","PDF speichern / teilen","Guardar / compartir PDF"))}
    }
}
