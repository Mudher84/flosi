package com.flosi.app.ui.screens.people

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PeopleViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun PersonPickerScreen(onBack:()->Unit,onAddPerson:()->Unit,onSelect:(Long)->Unit,currency:String?=null){
    val vm:PeopleViewModel=flosiViewModel();val all by vm.people.collectAsState();var selected by remember{mutableStateOf<Long?>(null)};val lang=LocalFlosiLanguage.current
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    val items=all.filter{currency==null||it.currency.equals(currency,ignoreCase=true)}
    FlosiPage(localizedLegacyText("اختيار شخص"),localizedLegacyText("من دفتر الأشخاص"),onBack){
        SectionTitle(flosiText("people"),s("+ شخص","+ Person","+ Kişi","+ Personne","+ Person","+ Persona"),onAddPerson)
        if(items.isEmpty())Text(s("لا يوجد شخص بنفس عملة الحساب المختار.","No person uses the selected account currency.","Seçilen hesap para birimini kullanan kişi yok.","Aucune personne n’utilise la devise du compte sélectionné.","Keine Person verwendet die Währung des ausgewählten Kontos.","Ninguna persona usa la moneda de la cuenta seleccionada."),color=FlosiMuted)
        items.forEach{p->CardBox{RadioButton(selected==p.id,{selected=p.id});ActionRow(p.name,"${p.phone} • ${p.currency}",onClick={selected=p.id})}}
        Button(onClick={selected?.let(onSelect)},enabled=selected!=null){Text(localizedLegacyText("اختيار"))}
    }
}
