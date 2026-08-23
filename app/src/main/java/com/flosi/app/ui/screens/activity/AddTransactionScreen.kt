package com.flosi.app.ui.screens.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.flosi.app.ui.components.FlosiMuted
import com.flosi.app.ui.components.FlosiPage
import com.flosi.app.ui.components.FlosiRed
import com.flosi.app.ui.viewmodel.EntryViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun AddTransactionScreen(
    onBack:()->Unit,
    onPickAccount:()->Unit,
    onPickPerson:()->Unit,
    onPickCategory:()->Unit
) {
    val vm:EntryViewModel=flosiViewModel()
    val accounts by vm.accounts.collectAsState()
    val people by vm.people.collectAsState()
    val categories by vm.categories.collectAsState()
    val lang=LocalFlosiLanguage.current
    var amount by remember{mutableStateOf("")}
    var title by remember{mutableStateOf("")}
    var note by remember{mutableStateOf("")}
    var kind by remember{mutableStateOf("expense")}
    var accountId by remember(accounts){mutableStateOf(accounts.firstOrNull()?.id?:0L)}
    var personId by remember{mutableStateOf<Long?>(null)}
    var categoryId by remember{mutableStateOf<Long?>(null)}
    var saving by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf<String?>(null)}

    LaunchedEffect(accounts){if(accountId==0L)accountId=accounts.firstOrNull()?.id?:0L}
    LaunchedEffect(kind,categories){
        val selected=categories.firstOrNull{it.id==categoryId}
        if(selected!=null && selected.kind!="both" && selected.kind!=kind) categoryId=null
    }
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    val eligibleCategories=categories.filter { category ->
        category.kind=="both" || when(kind){
            "income" -> category.kind=="income"
            "expense" -> category.kind=="expense"
            "debt_given","debt_received" -> category.kind in setOf("both","expense","income")
            else -> true
        }
    }
    val parsedAmount=amount.toLongOrNull()
    val canSave=!saving && parsedAmount!=null && parsedAmount>0L && title.isNotBlank() && accountId>0L

    FlosiPage(localizedLegacyText("إضافة حركة"),s("تسجل وتحدث الأرصدة مباشرة","Records the transaction and updates balances immediately"),onBack){
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf(
                "expense" to flosiText("expense"),
                "income" to flosiText("income"),
                "debt_given" to s("سلفة","Loan given"),
                "debt_received" to s("استلام دين","Debt received")
            ).forEach{(key,label)->FilterChip(kind==key,{kind=key;error=null},{Text(label)})}
        }
        OutlinedTextField(amount,{value->amount=value.filter{it.isDigit()};error=null},Modifier.fillMaxWidth(),label={Text(flosiText("amount"))},singleLine=true)
        OutlinedTextField(title,{title=it;error=null},Modifier.fillMaxWidth(),label={Text(flosiText("description"))},singleLine=true)
        OutlinedTextField(note,{note=it;error=null},Modifier.fillMaxWidth(),label={Text(s("ملاحظة","Note"))})

        CardBox{
            Text(flosiText("account"))
            if(accounts.isEmpty()) Text(s("لا يوجد حساب. أضف حساباً أولاً.","No account exists. Add an account first."),color=FlosiRed)
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
                accounts.take(4).forEach{account->FilterChip(accountId==account.id,{accountId=account.id;error=null},{Text("${account.name} • ${account.currency}")})}
            }
            if(accounts.size>4) TextButton(onClick=onPickAccount){Text(s("عرض كل الحسابات","View all accounts"))}

            Text(s("الشخص — اختياري","Person — optional"))
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                FilterChip(personId==null,{personId=null;error=null},{Text(s("بدون","None"))})
                people.take(3).forEach{person->FilterChip(personId==person.id,{personId=person.id;error=null},{Text(person.name)})}
            }
            if(people.size>3) TextButton(onClick=onPickPerson){Text(s("عرض كل الأشخاص","View all people"))}

            Text(flosiText("category"))
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                eligibleCategories.take(4).forEach{category->FilterChip(categoryId==category.id,{categoryId=category.id;error=null},{Text(category.name)})}
            }
            if(eligibleCategories.size>4) TextButton(onClick=onPickCategory){Text(s("عرض كل التصنيفات","View all categories"))}
            if(eligibleCategories.isEmpty()) Text(s("يمكن الحفظ بدون تصنيف.","You can save without a category."),color=FlosiMuted)
        }

        error?.let{Text(it,color=FlosiRed)}
        Button(
            onClick={
                val value=parsedAmount ?: return@Button
                saving=true;error=null
                vm.save(kind,value,title.trim(),note.trim(),accountId,personId,categoryId){message->
                    saving=false
                    if(message==null) onBack() else error=message
                }
            },
            enabled=canSave,
            modifier=Modifier.fillMaxWidth()
        ){
            if(saving) CircularProgressIndicator(strokeWidth=2.dp) else Text(flosiText("save"))
        }
    }
}
