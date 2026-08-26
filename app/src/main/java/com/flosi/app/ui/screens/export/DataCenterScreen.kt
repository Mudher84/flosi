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
    val vm:TransactionsViewModel=flosiViewModel();val items by vm.transactions.collectAsState();val context=LocalContext.current;val lang=LocalFlosiLanguage.current;val scope=rememberCoroutineScope()
    var status by remember{mutableStateOf("")};var success by remember{mutableStateOf(true)};var busy by remember{mutableStateOf(false)}
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    fun launchExport(block:suspend()->Unit,done:String){busy=true;status="";scope.launch{val result=runCatching{withContext(Dispatchers.IO){block()}};busy=false;result.onSuccess{success=true;status=done}.onFailure{error->success=false;status=error.message?:s("فشل التصدير","Export failed","Dışa aktarma başarısız","Échec de l’export","Export fehlgeschlagen","Error al exportar")}}}
    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri->if(uri!=null)launchExport({CsvExporter.exportTransactions(context,uri,items)},s("تم تصدير CSV","CSV exported","CSV dışa aktarıldı","CSV exporté","CSV exportiert","CSV exportado"))}
    val xlsx=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){uri->if(uri!=null)launchExport({XlsxExporter.exportTransactions(context,uri,items)},s("تم تصدير Excel بخلايا رقمية وتواريخ حقيقية","Excel exported with numeric cells and real dates","Excel sayısal hücreler ve gerçek tarihlerle dışa aktarıldı","Excel exporté avec cellules numériques et vraies dates","Excel mit numerischen Zellen und echten Datumswerten exportiert","Excel exportado con celdas numéricas y fechas reales"))}
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->if(uri!=null)launchExport({PdfExporter.exportTransactions(context,uri,items)},s("تم تصدير PDF مع عملة كل حركة","PDF exported with currency per transaction","PDF her işlem için para birimiyle dışa aktarıldı","PDF exporté avec la devise de chaque opération","PDF mit Währung pro Buchung exportiert","PDF exportado con la moneda de cada movimiento"))}

    FlosiPage(flosiText("data_center"),s("ملفات مالية قابلة للاستخدام","Usable financial files","Kullanılabilir finans dosyaları","Fichiers financiers exploitables","Nutzbare Finanzdateien","Archivos financieros utilizables"),onBack){
        CardBox{Metric(flosiText("activity"),items.size.toString(),FlosiPurple);Text(s("التصدير يحافظ على عملة كل حركة ولا يفترض IQD أو USD للجميع.","Exports preserve each transaction currency and never assume one currency for all data.","Dışa aktarma her işlemin para birimini korur ve tüm veriler için tek para birimi varsaymaz.","Les exports conservent la devise de chaque opération sans supposer une devise unique.","Exporte behalten die Währung jeder Buchung und setzen keine einheitliche Währung voraus.","Las exportaciones conservan la moneda de cada movimiento y no asumen una moneda única."),color=FlosiMuted)}
        CardBox{
            ActionRow("PDF",s("تقرير قابل للطباعة • التاريخ والعملة لكل حركة","Printable report • date and currency per transaction","Yazdırılabilir rapor • işlem başına tarih ve para birimi","Rapport imprimable • date et devise par opération","Druckbarer Bericht • Datum und Währung je Buchung","Informe imprimible • fecha y moneda por movimiento"),onClick={if(!busy)pdf.launch("flosi-report.pdf")})
            ActionRow("Excel XLSX",s("المبلغ رقم فعلي والتاريخ خلية Date قابلة للفرز والمعادلات","Numeric amounts and real Date cells for sorting and formulas","Sayısal tutarlar ve sıralama/formüller için gerçek tarih hücreleri","Montants numériques et vraies cellules Date pour tri et formules","Numerische Beträge und echte Datumszellen für Sortierung und Formeln","Importes numéricos y celdas de fecha reales para ordenar y usar fórmulas"),onClick={if(!busy)xlsx.launch("flosi-transactions.xlsx")})
            ActionRow("CSV",s("UTF-8 • تاريخ ISO • عمود للعملة","UTF-8 • ISO date • currency column","UTF-8 • ISO tarih • para birimi sütunu","UTF-8 • date ISO • colonne devise","UTF-8 • ISO-Datum • Währungsspalte","UTF-8 • fecha ISO • columna de moneda"),onClick={if(!busy)csv.launch("flosi-transactions.csv")})
        }
        if(busy)CardBox{CircularProgressIndicator();Text(s("جارٍ تجهيز الملف…","Preparing file…","Dosya hazırlanıyor…","Préparation du fichier…","Datei wird vorbereitet…","Preparando archivo…"),color=FlosiMuted)}
        if(status.isNotBlank())CardBox{Text(status,color=if(success)FlosiGreen else FlosiRed)}
        Text(s("يمكن اختيار Google Drive من نافذة الحفظ إذا كان Drive متاحاً على الجهاز.","Google Drive can be selected from the system save dialog when available.","Google Drive cihazda varsa sistem kaydetme penceresinden seçilebilir.","Google Drive peut être sélectionné dans la boîte de sauvegarde système s’il est disponible.","Google Drive kann im System-Speicherdialog gewählt werden, wenn verfügbar.","Google Drive puede seleccionarse desde el diálogo del sistema si está disponible."),color=FlosiMuted)
    }
}
