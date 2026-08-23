package com.flosi.app.ui.screens.planning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.data.local.entity.GoalEntity
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun GoalEditScreen(onBack:()->Unit){
    val vm:PlanningViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState();val lang=LocalFlosiLanguage.current
    var title by remember{mutableStateOf("")};var target by remember{mutableStateOf("")};var accountId by remember{mutableStateOf<Long?>(null)}
    var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    LaunchedEffect(accounts){if(accountId==null) accountId=accounts.firstOrNull()?.id}
    val selectedAccount=accounts.firstOrNull{it.id==accountId}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(localizedLegacyText("هدف جديد"),localizedLegacyText("ادخار محجوز ومربوط بحساب"),onBack){
        OutlinedTextField(title,{title=it;error=null},Modifier.fillMaxWidth(),label={Text(s("اسم الهدف","Goal name"))},singleLine=true)
        OutlinedTextField(target,{target=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(s("المبلغ المطلوب","Target amount")+selectedAccount?.let{" (${it.currency})"}.orEmpty())},singleLine=true)
        CardBox{
            Text(s("الحساب المرتبط","Linked account"),style=MaterialTheme.typography.titleSmall)
            Text(s("الادخار يحجز جزءاً من رصيد هذا الحساب ولا يُحسب كمصروف.","Savings reserve part of this account balance and are not counted as an expense."),color=FlosiMuted)
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){accounts.forEach{account->FilterChip(accountId==account.id,{accountId=account.id;error=null},{Text("${account.name} • ${account.currency} • ${moneyText(account.currentBalance,account.currency)}")})}}
            if(accounts.isEmpty()) Text(s("أضف حساباً أولاً حتى تنشئ هدفاً مالياً.","Add an account before creating a financial goal."),color=FlosiRed)
        }
        error?.let{Text(it,color=FlosiRed)}
        Button(
            onClick={
                val value=target.toLongOrNull()?:return@Button
                saving=true;error=null
                vm.addGoal(GoalEntity(title=title.trim(),targetAmount=value,savedAmount=0L,accountId=accountId)){message->
                    saving=false
                    if(message==null)onBack()else error=message
                }
            },
            enabled=!saving&&title.isNotBlank()&&(target.toLongOrNull()?:0L)>0L&&accountId!=null,
            modifier=Modifier.fillMaxWidth()
        ){if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(s("إنشاء الهدف","Create goal"))}
    }
}
