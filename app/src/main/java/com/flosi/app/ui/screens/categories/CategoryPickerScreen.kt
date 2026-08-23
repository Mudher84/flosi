package com.flosi.app.ui.screens.categories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.CategoriesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun CategoryPickerScreen(onBack:()->Unit,onManage:()->Unit){
 val vm:CategoriesViewModel=flosiViewModel();val cats by vm.categories.collectAsState();var selected by remember{mutableStateOf<Long?>(null)}
 FlosiPage("اختيار التصنيف","تصنيفاتك الفعلية",onBack){
  SectionTitle("التصنيفات","إدارة",onManage)
  cats.forEach{c->CardBox{RadioButton(selected==c.id,{selected=c.id});ActionRow(c.name,c.kind)}}
  Button(onClick=onBack,enabled=selected!=null){Text("اختيار")}
 }
}
