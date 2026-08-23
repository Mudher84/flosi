package com.flosi.app.ui.screens.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.flosi.app.export.PdfExporter
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository

@Composable
fun PdfPreviewScreen(invoiceId:Long,onBack:()->Unit){
    val repo=rememberFlosiRepository()
    val inv by repo.observeInvoice(invoiceId).collectAsState(initial=null)
    val items by repo.observeInvoiceItems(invoiceId).collectAsState(initial=emptyList())
    val context=LocalContext.current
    var status by remember{mutableStateOf("")}
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->
        val invoice=inv
        if(uri!=null && invoice!=null){
            runCatching{PdfExporter.exportInvoice(context,uri,invoice,items)}
                .onSuccess{status="تم حفظ PDF"}
                .onFailure{e->status=e.message?:"فشل الحفظ"}
        }
    }
    FlosiPage("معاينة PDF","فاتورة حقيقية",onBack){
        inv?.let{i->
            CardBox{Metric("فاتورة #${i.number}",moneyText(i.total),FlosiPurple);Text("الحالة: ${i.status}")}
            CardBox{items.forEach{ActionRow(it.title,"${it.quantity} × ${moneyText(it.unitPrice)}",moneyText(it.lineTotal))}}
            Button(onClick={launcher.launch("invoice-${i.number}.pdf")}){Text("حفظ PDF")}
        } ?: CardBox{Text("الفاتورة غير موجودة")}
        if(status.isNotBlank())Text(status,color=FlosiGreen)
    }
}
