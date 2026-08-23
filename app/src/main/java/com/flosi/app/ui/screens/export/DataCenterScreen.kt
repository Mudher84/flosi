package com.flosi.app.ui.screens.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.flosi.app.export.CsvExporter
import com.flosi.app.export.PdfExporter
import com.flosi.app.export.XlsxExporter
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.TransactionsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun DataCenterScreen(onBack:()->Unit){
    val vm:TransactionsViewModel=flosiViewModel();val items by vm.transactions.collectAsState();val context=LocalContext.current;val lang=LocalFlosiLanguage.current
    var status by remember{mutableStateOf("")};var success by remember{mutableStateOf(true)};fun s(ar:String,en:String)=if(lang=="ar")ar else en
    fun ok(message:String){status=message;success=true};fun fail(error:Throwable){status=error.message?:s("فشل التصدير","Export failed");success=false}
    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->uri?.let{runCatching{CsvExporter.exportTransactions(context,it,items)}.onSuccess{ok(s("تم تصدير CSV","CSV exported"))}.onFailure(::fail)}}
    val xlsx=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){uri->uri?.let{runCatching{XlsxExporter.exportTransactions(context,it,items)}.onSuccess{ok(s("تم تصدير Excel بخلايا رقمية وتواريخ حقيقية","Excel exported with numeric cells and real dates"))}.onFailure(::fail)}}
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->uri?.let{runCatching{PdfExporter.exportTransactions(context,it,items)}.onSuccess{ok(s("تم تصدير PDF مع عملة كل حركة","PDF exported with currency per transaction"))}.onFailure(::fail)}}

    FlosiPage(flosiText("data_center"),s("ملفات مالية قابلة للاستخدام","Usable financial files"),onBack){
        CardBox{Metric(flosiText("activity"),items.size.toString(),FlosiPurple);Text(s("التصدير يحافظ على عملة كل حركة ولا يفترض IQD أو USD للجميع.","Exports preserve each transaction currency and never assume one currency for all data."),color=FlosiMuted)}
        CardBox{
            ActionRow("PDF",s("تقرير قابل للطباعة • التاريخ والعملة لكل حركة","Printable report • date and currency per transaction"),onClick={pdf.launch("flosi-report.pdf")})
            ActionRow("Excel XLSX",s("المبلغ رقم فعلي والتاريخ خلية Date قابلة للفرز والمعادلات","Numeric amounts and real Date cells for sorting and formulas"),onClick={xlsx.launch("flosi-transactions.xlsx")})
            ActionRow("CSV","UTF-8 • ISO date • currency column",onClick={csv.launch("flosi-transactions.csv")})
        }
        if(status.isNotBlank())CardBox{Text(status,color=if(success)FlosiGreen else FlosiRed)}
        Text(s("يمكن اختيار Google Drive من نافذة الحفظ إذا كان Drive متاحاً على الجهاز.","Google Drive can be selected from the system save dialog when available."),color=FlosiMuted)
    }
}
