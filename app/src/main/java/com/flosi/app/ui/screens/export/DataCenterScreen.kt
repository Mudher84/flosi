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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DataCenterScreen(onBack:()->Unit){
    val vm:TransactionsViewModel=flosiViewModel()
    val items by vm.transactions.collectAsState()
    val context=LocalContext.current
    val lang=LocalFlosiLanguage.current
    val scope=rememberCoroutineScope()
    var status by remember{mutableStateOf("")}
    var success by remember{mutableStateOf(true)}
    var busy by remember{mutableStateOf(false)}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    fun launchExport(block:suspend()->Unit,done:String){
        busy=true;status=""
        scope.launch{
            val result=runCatching{withContext(Dispatchers.IO){block()}}
            busy=false
            result.onSuccess{success=true;status=done}.onFailure{error->success=false;status=error.message?:s("فشل التصدير","Export failed")}
        }
    }

    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->
        if(uri!=null)launchExport({CsvExporter.exportTransactions(context,uri,items)},s("تم تصدير CSV","CSV exported"))
    }
    val xlsx=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){uri->
        if(uri!=null)launchExport({XlsxExporter.exportTransactions(context,uri,items)},s("تم تصدير Excel بخلايا رقمية وتواريخ حقيقية","Excel exported with numeric cells and real dates"))
    }
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->
        if(uri!=null)launchExport({PdfExporter.exportTransactions(context,uri,items)},s("تم تصدير PDF مع عملة كل حركة","PDF exported with currency per transaction"))
    }

    FlosiPage(flosiText("data_center"),s("ملفات مالية قابلة للاستخدام","Usable financial files"),onBack){
        CardBox{
            Metric(flosiText("activity"),items.size.toString(),FlosiPurple)
            Text(s("التصدير يحافظ على عملة كل حركة ولا يفترض IQD أو USD للجميع.","Exports preserve each transaction currency and never assume one currency for all data."),color=FlosiMuted)
        }
        CardBox{
            ActionRow("PDF",s("تقرير قابل للطباعة • التاريخ والعملة لكل حركة","Printable report • date and currency per transaction"),onClick={if(!busy)pdf.launch("flosi-report.pdf")})
            ActionRow("Excel XLSX",s("المبلغ رقم فعلي والتاريخ خلية Date قابلة للفرز والمعادلات","Numeric amounts and real Date cells for sorting and formulas"),onClick={if(!busy)xlsx.launch("flosi-transactions.xlsx")})
            ActionRow("CSV","UTF-8 • ISO date • currency column",onClick={if(!busy)csv.launch("flosi-transactions.csv")})
        }
        if(busy)CardBox{CircularProgressIndicator();Text(s("جارٍ تجهيز الملف…","Preparing file…"),color=FlosiMuted)}
        if(status.isNotBlank())CardBox{Text(status,color=if(success)FlosiGreen else FlosiRed)}
        Text(s("يمكن اختيار Google Drive من نافذة الحفظ إذا كان Drive متاحاً على الجهاز.","Google Drive can be selected from the system save dialog when available."),color=FlosiMuted)
    }
}
