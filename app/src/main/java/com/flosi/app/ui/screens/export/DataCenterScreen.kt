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
    var success by remember{mutableStateOf(true)}

    fun ok(message:String){status=message;success=true}
    fun fail(error:Throwable){status=error.message?:"فشل التصدير";success=false}

    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->
        uri?.let{runCatching{CsvExporter.exportTransactions(context,it,items)}.onSuccess{ok("تم تصدير CSV")}.onFailure(::fail)}
    }
    val xlsx=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){uri->
        uri?.let{runCatching{XlsxExporter.exportTransactions(context,it,items)}.onSuccess{ok("تم تصدير Excel بخلايا رقمية وتواريخ حقيقية")}.onFailure(::fail)}
    }
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->
        uri?.let{runCatching{PdfExporter.exportTransactions(context,it,items)}.onSuccess{ok("تم تصدير PDF مع عملة كل حركة")}.onFailure(::fail)}
    }

    FlosiPage("الاستيراد والتصدير","ملفات مالية قابلة للاستخدام",onBack){
        CardBox{
            Metric("الحركات",items.size.toString(),FlosiPurple)
            Text("التصدير يحافظ على عملة كل حركة ولا يفترض IQD أو USD للجميع.",color=FlosiMuted)
        }
        CardBox{
            ActionRow("PDF","تقرير قابل للطباعة • التاريخ والعملة لكل حركة",onClick={pdf.launch("flosi-report.pdf")})
            ActionRow("Excel XLSX","المبلغ رقم فعلي والتاريخ خلية Date قابلة للفرز والمعادلات",onClick={xlsx.launch("flosi-transactions.xlsx")})
            ActionRow("CSV","UTF-8 • تاريخ ISO • عمود مستقل للعملة",onClick={csv.launch("flosi-transactions.csv")})
        }
        if(status.isNotBlank())CardBox{Text(status,color=if(success)FlosiGreen else FlosiRed)}
        Text("يمكن اختيار Google Drive من نافذة الحفظ إذا كان Drive متاحاً على الجهاز.",color=FlosiMuted)
    }
}
