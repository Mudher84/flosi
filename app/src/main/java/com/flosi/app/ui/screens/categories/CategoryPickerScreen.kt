package com.flosi.app.ui.screens.categories

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.CategoriesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun CategoryPickerScreen(onBack:()->Unit,onManage:()->Unit,onSelect:(Long)->Unit,kind:String?=null){
    val vm:CategoriesViewModel=flosiViewModel();val all by vm.categories.collectAsState();var selected by remember{mutableStateOf<Long?>(null)};val lang=LocalFlosiLanguage.current
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    val cats=all.filter{kind==null||it.kind=="both"||it.kind==kind}
    FlosiPage(localizedLegacyText("اختيار التصنيف"),localizedLegacyText("تصنيفاتك الفعلية"),onBack){
        SectionTitle(flosiText("category"),s("إدارة","Manage","Yönet","Gérer","Verwalten","Gestionar"),onManage)
        cats.forEach{c->CardBox{RadioButton(selected==c.id,{selected=c.id});ActionRow(c.name,c.kind,onClick={selected=c.id})}}
        Button(onClick={selected?.let(onSelect)},enabled=selected!=null){Text(localizedLegacyText("اختيار"))}
    }
}
