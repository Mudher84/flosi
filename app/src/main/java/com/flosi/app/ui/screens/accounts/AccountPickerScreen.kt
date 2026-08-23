package com.flosi.app.ui.screens.accounts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun AccountPickerScreen(onBack:()->Unit,onAddAccount:()->Unit){
 val vm:AccountsViewModel=flosiViewModel();val items by vm.accounts.collectAsState();var selected by remember{mutableStateOf<Long?>(null)}
 FlosiPage(localizedLegacyText("اختيار الحساب"),localizedLegacyText("حساباتك الحقيقية"),onBack){
  SectionTitle(flosiText("accounts"),flosiText("add_account"),onAddAccount)
  items.forEach{a->CardBox{RadioButton(selected==a.id,{selected=a.id});ActionRow(a.name,a.type,moneyText(a.currentBalance,a.currency))}}
  Button(onClick=onBack,enabled=selected!=null){Text(localizedLegacyText("اختيار"))}
 }
}
