package com.flosi.app.ui.screens.invoices
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.InvoicesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun InvoicesScreen(onBack:()->Unit,onCreate:()->Unit,onDetail:(Long)->Unit){
 val vm:InvoicesViewModel=flosiViewModel();val items by vm.invoices.collectAsState()
 FlosiPage("الفواتير والوصولات","فواتير محفوظة",onBack){
  CardBox{Metric("الإجمالي",moneyText(items.sumOf{it.total}),FlosiPurple)}
  SectionTitle("الفواتير","+ إنشاء",onCreate)
  CardBox{if(items.isEmpty())androidx.compose.material3.Text("ماكو فواتير بعد",color=FlosiMuted);items.forEach{i->ActionRow("#${i.number}",i.status,moneyText(i.total),FlosiPurple){onDetail(i.id)}}}
 }
}
