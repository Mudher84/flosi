package com.flosi.app.ui.screens.planning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.data.local.entity.GoalEntity
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun GoalEditScreen(onBack:()->Unit){
    val vm:PlanningViewModel=flosiViewModel()
    val accounts by vm.accounts.collectAsState()
    var title by remember{mutableStateOf("")}
    var target by remember{mutableStateOf("")}
    var accountId by remember{mutableStateOf<Long?>(null)}

    LaunchedEffect(accounts){
        if(accountId==null) accountId=accounts.firstOrNull()?.id
    }

    val selectedAccount=accounts.firstOrNull{it.id==accountId}

    FlosiPage("هدف جديد","ادخار محجوز ومربوط بحساب",onBack){
        OutlinedTextField(
            value=title,
            onValueChange={title=it},
            modifier=Modifier.fillMaxWidth(),
            label={Text("اسم الهدف")}
        )
        OutlinedTextField(
            value=target,
            onValueChange={target=it.filter(Char::isDigit)},
            modifier=Modifier.fillMaxWidth(),
            label={Text("المبلغ المطلوب${selectedAccount?.let{" (${it.currency})"}.orEmpty()}")}
        )

        CardBox{
            Text("الحساب المرتبط",style=MaterialTheme.typography.titleSmall)
            Text("الادخار يحجز جزءاً من رصيد هذا الحساب ولا يُحسب كمصروف.",color=FlosiMuted)
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
                accounts.forEach{account->
                    FilterChip(
                        selected=accountId==account.id,
                        onClick={accountId=account.id},
                        label={Text("${account.name} • ${account.currency} • ${moneyText(account.currentBalance,account.currency)}")}
                    )
                }
            }
            if(accounts.isEmpty()) Text("أضف حساباً أولاً حتى تنشئ هدفاً مالياً.",color=FlosiRed)
        }

        Button(
            onClick={
                vm.addGoal(
                    GoalEntity(
                        title=title.trim(),
                        targetAmount=target.toLong(),
                        savedAmount=0L,
                        accountId=accountId
                    )
                )
                onBack()
            },
            enabled=title.isNotBlank()&&(target.toLongOrNull()?:0L)>0L&&accountId!=null,
            modifier=Modifier.fillMaxWidth()
        ){
            Text("إنشاء الهدف")
        }
    }
}
