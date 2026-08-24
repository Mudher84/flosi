package com.flosi.app.ui.screens.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.flosi.app.export.PdfExporter
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private fun previewQuantity(value:Double):String =
    if(value%1.0==0.0) String.format(Locale.US,"%.0f",value)
    else String.format(Locale.US,"%.3f",value).trimEnd('0').trimEnd('.')

@Composable
fun PdfPreviewScreen(invoiceId:Long,onBack:()->Unit){
    val repo=rememberFlosiRepository()
    val inv by repo.observeInvoice(invoiceId).collectAsState(initial=null)
    val items by repo.observeInvoiceItems(invoiceId).collectAsState(initial=emptyList())
    val context=LocalContext.current
    val lang=LocalFlosiLanguage.current
    val scope=rememberCoroutineScope()
    var status by remember{mutableStateOf("")}
    var success by remember{mutableStateOf(true)}
    var busy by remember{mutableStateOf(false)}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en
    fun statusLabel(raw:String)=if(lang=="ar")when(raw){"paid"->"مدفوعة";"partial"->"مدفوعة جزئياً";"unpaid"->"غير مدفوعة";"cancelled"->"ملغاة";else->"مسودة"}else when(raw){"paid"->"Paid";"partial"->"Partially paid";"unpaid"->"Unpaid";"cancelled"->"Cancelled";else->"Draft"}

    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->
        val invoice=inv
        if(uri!=null && invoice!=null){
            busy=true;status=""
            scope.launch{
                val result=runCatching{withContext(Dispatchers.IO){PdfExporter.exportInvoice(context,uri,invoice,items)}}
                busy=false
                result.onSuccess{success=true;status=s("تم حفظ PDF","PDF saved")}.onFailure{e->success=false;status=e.message?:s("فشل الحفظ","Save failed")}
            }
        }
    }
    FlosiPage(s("معاينة PDF","PDF preview"),s("فاتورة جاهزة للحفظ والمشاركة","Invoice ready to save and share"),onBack){
        inv?.let{i->
            val currency=i.currency.trim().uppercase()
            CardBox{
                Metric("${s("فاتورة","Invoice")} #${i.number}",moneyText(i.total,currency),FlosiPurple)
                Text("${s("الحالة","Status")}: ${statusLabel(i.status)} • ${s("العملة","Currency")}: $currency",color=FlosiMuted)
            }
            CardBox{
                items.forEach{item->
                    ActionRow(item.title,"${previewQuantity(item.quantity)} × ${moneyText(item.unitPrice,currency)}",moneyText(item.lineTotal,currency))
                }
            }
            Button(onClick={launcher.launch("invoice-${i.number}.pdf")},enabled=!busy){
                if(busy)CircularProgressIndicator(strokeWidth=2.dp)else Text(s("حفظ PDF","Save PDF"))
            }
        } ?: CardBox{Text(s("الفاتورة غير موجودة","Invoice not found"))}
        if(status.isNotBlank())Text(status,color=if(success)FlosiGreen else FlosiRed)
    }
}
