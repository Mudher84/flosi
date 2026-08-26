package com.flosi.app.ui.screens.notifications
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun NotificationsScreen(onBack:()->Unit){
 val vm:PlanningViewModel=flosiViewModel();val items by vm.commitments.collectAsState();val lang=LocalFlosiLanguage.current
 fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
 FlosiPage(flosiText("notifications"),s("من التزاماتك الفعلية","From your real commitments","Gerçek yükümlülüklerinden","À partir de vos engagements réels","Aus deinen tatsächlichen Verpflichtungen","Desde tus compromisos reales"),onBack){
  CardBox{if(items.isEmpty())androidx.compose.material3.Text(s("ماكو التزامات تحتاج تنبيه","No commitments need an alert","Uyarı gerektiren yükümlülük yok","Aucun engagement ne nécessite d’alerte","Keine Verpflichtungen benötigen eine Erinnerung","No hay compromisos que necesiten aviso"),color=FlosiMuted);items.take(6).forEach{ActionRow(it.title,s("موعد استحقاق محفوظ","Saved due date","Kaydedilmiş vade tarihi","Échéance enregistrée","Gespeichertes Fälligkeitsdatum","Fecha de vencimiento guardada"),moneyText(it.amount),FlosiOrange)}}
 }
}
