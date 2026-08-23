package com.flosi.app.ui.screens.accounts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun AccountPickerScreen(onBack:()->Unit,onAddAccount:()->Unit){
 val vm:AccountsViewModel=flosiViewModel();val items by vm.accounts.collectAsState();var selected by remember{mutableStateOf<Long?>(null)}
 FlosiPage("اختيار الحساب","حساباتك الحقيقية",onBack){
  SectionTitle("الحسابات","+ حساب",onAddAccount)
  items.forEach{a->CardBox{RadioButton(selected==a.id,{selected=a.id});ActionRow(a.name,a.type,moneyText(a.currentBalance))}}
  Button(onClick=onBack,enabled=selected!=null){Text("اختيار")}
 }
}
