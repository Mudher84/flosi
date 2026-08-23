package com.flosi.app.ui.screens.categories
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.CategoriesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable fun CategoryManagementScreen(onBack:()->Unit){
 val vm:CategoriesViewModel=flosiViewModel();val cats by vm.categories.collectAsState();var name by remember{mutableStateOf("")};val lang=LocalFlosiLanguage.current
 FlosiPage(localizedLegacyText("إدارة التصنيفات"),localizedLegacyText("أضف أو أخفِ التصنيف"),onBack){
  OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text(if(lang=="ar")"تصنيف جديد" else "New category")})
  Button(onClick={vm.add(name,"both");name=""},enabled=name.isNotBlank()){Text(flosiText("add"))}
  cats.forEach{c->CardBox{ActionRow(c.name,c.kind,flosiText("delete"),FlosiRed){if(!c.system)vm.archive(c.id)}}}
 }
}
