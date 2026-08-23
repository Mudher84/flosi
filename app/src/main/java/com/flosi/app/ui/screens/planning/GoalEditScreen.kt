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
    LaunchedEffect(accounts){if(accountId==null) accountId=accounts.firstOrNull()?.id}
    val selectedAccount=accounts.firstOrNull{it.id==accountId}
    FlosiPage(localizedLegacyText("هدف جديد"),localizedLegacyText("ادخار محجوز ومربوط بحساب"),onBack){
        OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text(if(lang=="ar")"اسم الهدف" else "Goal name")})
        OutlinedTextField(target,{target=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text((if(lang=="ar")"المبلغ المطلوب" else "Target amount")+selectedAccount?.let{" (${it.currency})"}.orEmpty())})
        CardBox{
            Text(if(lang=="ar")"الحساب المرتبط" else "Linked account",style=MaterialTheme.typography.titleSmall)
            Text(if(lang=="ar")"الادخار يحجز جزءاً من رصيد هذا الحساب ولا يُحسب كمصروف." else "Savings reserve part of this account balance and are not counted as an expense.",color=FlosiMuted)
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){accounts.forEach{account->FilterChip(accountId==account.id,{accountId=account.id},{Text("${account.name} • ${account.currency} • ${moneyText(account.currentBalance,account.currency)}")})}}
            if(accounts.isEmpty()) Text(if(lang=="ar")"أضف حساباً أولاً حتى تنشئ هدفاً مالياً." else "Add an account before creating a financial goal.",color=FlosiRed)
        }
        Button(onClick={vm.addGoal(GoalEntity(title=title.trim(),targetAmount=target.toLong(),savedAmount=0L,accountId=accountId));onBack()},enabled=title.isNotBlank()&&(target.toLongOrNull()?:0L)>0L&&accountId!=null,modifier=Modifier.fillMaxWidth()){Text(if(lang=="ar")"إنشاء الهدف" else "Create goal")}
    }
}
