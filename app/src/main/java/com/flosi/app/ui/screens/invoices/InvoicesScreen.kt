package com.flosi.app.ui.screens.invoices

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.InvoicesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun InvoicesScreen(onBack:()->Unit,onCreate:()->Unit,onDetail:(Long)->Unit){
    val vm:InvoicesViewModel=flosiViewModel();val items by vm.invoices.collectAsState();val lang=LocalFlosiLanguage.current
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    val receivables=items.count{it.type=="sale"&&(it.status=="unpaid"||it.status=="partial")};val payables=items.count{it.type=="purchase"&&(it.status=="unpaid"||it.status=="partial")}
    FlosiPage(flosiText("invoices"),localizedLegacyText("فواتير محفوظة وحسابات واضحة"),onBack){
        CardBox{
            Metric(s("عدد الفواتير","Invoice count","Fatura sayısı","Nombre de factures","Anzahl Rechnungen","Número de facturas"),items.size.toString(),FlosiPurple)
            Metric(s("مبالغ تحتاج تحصيل","Receivables to collect","Tahsil edilecek alacaklar","Créances à encaisser","Offene Forderungen","Importes por cobrar"),receivables.toString(),if(receivables>0)FlosiOrange else FlosiGreen)
            Metric(s("فواتير شراء تحتاج دفع","Purchase invoices to pay","Ödenecek alış faturaları","Factures d’achat à payer","Zu zahlende Einkaufsrechnungen","Facturas de compra por pagar"),payables.toString(),if(payables>0)FlosiRed else FlosiGreen)
        }
        SectionTitle(flosiText("invoices"),s("+ إنشاء","+ Create","+ Oluştur","+ Créer","+ Erstellen","+ Crear"),onCreate)
        if(items.isEmpty()) EmptyState(title=s("ما عندك فواتير بعد","No invoices yet","Henüz fatura yok","Aucune facture pour le moment","Noch keine Rechnungen","Aún no hay facturas"),subtitle=s("أنشئ أول فاتورة بيع أو شراء وخلي Flosi يتابع المدفوع والمتبقي وياك.","Create your first sale or purchase invoice and let Flosi track paid and remaining amounts.","İlk satış veya alış faturası oluştur; Flosi ödenen ve kalan tutarı takip etsin.","Créez votre première facture de vente ou d’achat et laissez Flosi suivre les montants payés et restants.","Erstelle deine erste Verkaufs- oder Einkaufsrechnung und lass Flosi bezahlte und offene Beträge verfolgen.","Crea tu primera factura de venta o compra y deja que Flosi controle lo pagado y lo pendiente."),action=s("إنشاء أول فاتورة","Create first invoice","İlk faturayı oluştur","Créer la première facture","Erste Rechnung erstellen","Crear primera factura"),onAction=onCreate,symbol="▤")
        else CardBox{items.forEach{i->
            val currency=i.currency.trim().uppercase().ifBlank{"IQD"};val remaining=(i.total-i.paidAmount).coerceAtLeast(0L);val tone=when(i.status){"paid"->FlosiGreen;"partial"->FlosiOrange;else->if(i.type=="purchase")FlosiRed else FlosiPurple};val typeLabel=if(i.type=="purchase")s("شراء","Purchase","Alış","Achat","Einkauf","Compra")else s("بيع","Sale","Satış","Vente","Verkauf","Venta")
            val status=when(i.status){
                "paid"->"$typeLabel • ${s("مدفوعة بالكامل","Paid in full","Tam ödendi","Payée intégralement","Vollständig bezahlt","Pagada por completo")} • $currency"
                "partial"->"$typeLabel • ${s("متبقي","Remaining","Kalan","Restant","Verbleibend","Restante")} ${moneyText(remaining,currency)}"
                "unpaid"->"$typeLabel • ${s("غير مدفوعة","Unpaid","Ödenmedi","Impayée","Unbezahlt","Sin pagar")} • ${s("متبقي","remaining","kalan","restant","verbleibend","restante")} ${moneyText(remaining,currency)}"
                "cancelled"->"$typeLabel • ${s("ملغاة","Cancelled","İptal edildi","Annulée","Storniert","Cancelada")} • $currency"
                else->"$typeLabel • ${s("مسودة","Draft","Taslak","Brouillon","Entwurf","Borrador")} • $currency"
            }
            ActionRow("#${i.number}",status,moneyText(i.total,currency),tone){onDetail(i.id)}
        }}
    }
}
