package com.flosi.app.ui.screens.accounts
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun AccountsScreen(onBack:()->Unit,onOpen:(Long)->Unit,onAdd:()->Unit){
 val vm:AccountsViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState()
 FlosiPage("الحسابات والمحافظ","أموالك موزعة بوضوح",onBack){
  CardBox{Metric("إجمالي السيولة",moneyText(accounts.filter{it.includeInTotal}.sumOf{it.currentBalance}),FlosiPurple)}
  SectionTitle("الحسابات","+ حساب",onAdd)
  CardBox{accounts.forEach{a->ActionRow(a.name,a.type,moneyText(a.currentBalance),FlosiPurple){onOpen(a.id)}}}
 }
}
