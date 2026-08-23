package com.flosi.app.ui.screens.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.CardBox
import com.flosi.app.ui.components.FlosiPage
import com.flosi.app.ui.viewmodel.EntryViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun AddTransactionScreen(onBack:()->Unit,onPickAccount:()->Unit,onPickPerson:()->Unit,onPickCategory:()->Unit) {
    val vm:EntryViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState();val people by vm.people.collectAsState();val categories by vm.categories.collectAsState();val lang=LocalFlosiLanguage.current
    var amount by remember{mutableStateOf("")};var title by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var kind by remember{mutableStateOf("expense")};var accountId by remember(accounts){mutableStateOf(accounts.firstOrNull()?.id?:0L)};var personId by remember{mutableStateOf<Long?>(null)};var categoryId by remember{mutableStateOf<Long?>(null)}
    LaunchedEffect(accounts){if(accountId==0L)accountId=accounts.firstOrNull()?.id?:0L}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(localizedLegacyText("إضافة حركة"),s("تسجل وتحدث الأرصدة مباشرة","Records the transaction and updates balances immediately"),onBack){
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf("expense" to flosiText("expense"),"income" to flosiText("income"),"debt_given" to s("سلفة","Loan given"),"debt_received" to s("استلام دين","Debt received")).forEach{(key,label)->FilterChip(kind==key,{kind=key},{Text(label)})}
        }
        OutlinedTextField(amount,{value->amount=value.filter{it.isDigit()}},Modifier.fillMaxWidth(),label={Text(flosiText("amount"))})
        OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text(flosiText("description"))})
        OutlinedTextField(note,{note=it},Modifier.fillMaxWidth(),label={Text(s("ملاحظة","Note"))})
        CardBox{
            Text(flosiText("account"));accounts.forEach{account->FilterChip(accountId==account.id,{accountId=account.id},{Text(account.name)})}
            Text(s("الشخص — اختياري","Person — optional"))
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(personId==null,{personId=null},{Text(s("بدون","None"))});people.take(3).forEach{person->FilterChip(personId==person.id,{personId=person.id},{Text(person.name)})}}
            Text(flosiText("category"));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){categories.take(4).forEach{category->FilterChip(categoryId==category.id,{categoryId=category.id},{Text(category.name)})}}
        }
        val validAmount=amount.toLongOrNull()?.let{it>0L}==true
        Button(onClick={vm.save(kind,amount.toLong(),title,note,accountId,personId,categoryId,onBack)},enabled=validAmount&&title.isNotBlank()&&accountId>0L,modifier=Modifier.fillMaxWidth()){Text(flosiText("save"))}
    }
}
