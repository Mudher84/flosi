package com.flosi.app.ui.screens.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.flosi.app.export.CsvExporter
import com.flosi.app.export.PdfExporter
import com.flosi.app.export.XlsxExporter
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.TransactionsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun DataCenterScreen(onBack:()->Unit){
    val vm:TransactionsViewModel=flosiViewModel()
    val items by vm.transactions.collectAsState()
    val context=LocalContext.current
    var status by remember{mutableStateOf("")}

    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->
        uri?.let{runCatching{CsvExporter.exportTransactions(context,it,items)}.onSuccess{status="تم تصدير CSV"}.onFailure{e->status=e.message?:"فشل التصدير"}}
    }
    val xlsx=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){uri->
        uri?.let{runCatching{XlsxExporter.exportTransactions(context,it,items)}.onSuccess{status="تم تصدير Excel"}.onFailure{e->status=e.message?:"فشل التصدير"}}
    }
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->
        uri?.let{runCatching{PdfExporter.exportTransactions(context,it,items)}.onSuccess{status="تم تصدير PDF"}.onFailure{e->status=e.message?:"فشل التصدير"}}
    }

    FlosiPage("الاستيراد والتصدير","ملفات حقيقية",onBack){
        CardBox{Metric("الحركات",items.size.toString(),FlosiPurple)}
        CardBox{
            ActionRow("PDF","تقرير قابل للطباعة",onClick={pdf.launch("flosi-report.pdf")})
            ActionRow("Excel XLSX","ملف Excel حقيقي",onClick={xlsx.launch("flosi-transactions.xlsx")})
            ActionRow("CSV","للنقل والتحليل",onClick={csv.launch("flosi-transactions.csv")})
        }
        if(status.isNotBlank()) Text(status,color=FlosiGreen)
        Text("يمكن اختيار Google Drive من نافذة الحفظ إذا كان Drive متاحاً على الجهاز.",color=FlosiMuted)
    }
}
