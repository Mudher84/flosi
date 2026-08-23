package com.flosi.app.ui.screens.invoices
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.data.local.entity.InvoiceEntity
import com.flosi.app.data.local.entity.InvoiceItemEntity
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.InvoicesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun CreateInvoiceScreen(onBack:()->Unit){
 val vm:InvoicesViewModel=flosiViewModel();var title by remember{mutableStateOf("")};var qty by remember{mutableStateOf("1")};var price by remember{mutableStateOf("")}
 FlosiPage("إنشاء فاتورة","تحفظ مع البنود",onBack){
  OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("البند")})
  OutlinedTextField(qty,{qty=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("الكمية")})
  OutlinedTextField(price,{price=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("سعر الوحدة")})
  val total=(qty.toLongOrNull()?:0)*(price.toLongOrNull()?:0)
  CardBox{Metric("الإجمالي",moneyText(total),FlosiPurple)}
  Button(onClick={
   val number="F-${System.currentTimeMillis().toString().takeLast(6)}"
   vm.create(InvoiceEntity(number=number,subtotal=total,total=total,status="unpaid"),listOf(InvoiceItemEntity(invoiceId=0,title=title,quantity=(qty.toDoubleOrNull()?:1.0),unitPrice=price.toLong(),lineTotal=total))){onBack()}
  },enabled=title.isNotBlank()&&total>0){Text("حفظ الفاتورة")}
 }
}
