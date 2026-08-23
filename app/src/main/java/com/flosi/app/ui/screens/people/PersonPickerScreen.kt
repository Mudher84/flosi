package com.flosi.app.ui.screens.people
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun PersonPickerScreen(onBack:()->Unit,onAddPerson:()->Unit){
 val vm:PeopleViewModel=flosiViewModel();val items by vm.people.collectAsState();var selected by remember{mutableStateOf<Long?>(null)}
 FlosiPage("اختيار شخص","من دفتر الأشخاص",onBack){
  SectionTitle("الأشخاص","+ شخص",onAddPerson)
  items.forEach{p->CardBox{RadioButton(selected==p.id,{selected=p.id});ActionRow(p.name,p.phone)}}
  Button(onClick=onBack,enabled=selected!=null){Text("اختيار")}
 }
}
