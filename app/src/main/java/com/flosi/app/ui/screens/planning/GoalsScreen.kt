package com.flosi.app.ui.screens.planning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.data.local.entity.GoalEntity
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun GoalsScreen(onBack:()->Unit,onEdit:()->Unit){
    val vm:PlanningViewModel=flosiViewModel();val items by vm.goals.collectAsState();val accounts by vm.accounts.collectAsState();val lang=LocalFlosiLanguage.current
    val accountMap=remember(accounts){accounts.associateBy{it.id}}
    var selectedGoal by remember{mutableStateOf<GoalEntity?>(null)};var reserveAmount by remember{mutableStateOf("")};var reserveError by remember{mutableStateOf<String?>(null)}

    FlosiPage(flosiText("goals"),localizedLegacyText("الأهداف والادخار المحجوز"),onBack){
        SectionTitle(flosiText("goals"),if(lang=="ar")"+ هدف" else "+ Goal",onEdit)
        if(items.isEmpty()) CardBox{Text(if(lang=="ar")"ابدأ أول هدف ادخار" else "Create your first savings goal",color=FlosiMuted)}
        items.forEach{goal->
            val account=goal.accountId?.let(accountMap::get);val currency=account?.currency?:"IQD";val saved=goal.savedAmount.coerceIn(0L,goal.targetAmount.coerceAtLeast(0L));val remaining=(goal.targetAmount-saved).coerceAtLeast(0L);val progress=if(goal.targetAmount>0L)(saved.toFloat()/goal.targetAmount.toFloat()).coerceIn(0f,1f) else 0f
            CardBox{
                ActionRow(goal.title,account?.let{"${it.name} • ${if(lang=="ar")"محجوز" else "Reserved"} ${moneyText(saved,currency)}"}?:if(lang=="ar")"هدف قديم غير مربوط بحساب" else "Legacy goal not linked to an account",moneyText(goal.targetAmount,currency),if(remaining==0L)FlosiGreen else FlosiPurple)
                LinearProgressIndicator(progress={progress})
                Text(if(remaining==0L){if(lang=="ar")"اكتمل الهدف" else "Goal completed"}else{if(lang=="ar")"المتبقي ${moneyText(remaining,currency)}" else "Remaining ${moneyText(remaining,currency)}"},color=if(remaining==0L)FlosiGreen else FlosiMuted)
                if(account!=null&&remaining>0L){OutlinedButton(onClick={selectedGoal=goal;reserveAmount="";reserveError=null},modifier=Modifier.fillMaxWidth()){Text(if(lang=="ar")"إضافة ادخار للهدف" else "Add savings to goal")}}
                else if(account==null) Text(if(lang=="ar")"أنشئ هدفاً جديداً مربوطاً بحساب حتى يكون الادخار جزءاً من السجل المالي." else "Create a new goal linked to an account so savings become part of the financial ledger.",color=FlosiOrange)
            }
        }
    }

    val goal=selectedGoal
    if(goal!=null){
        val account=goal.accountId?.let(accountMap::get);val currency=account?.currency?:"IQD";val remaining=(goal.targetAmount-goal.savedAmount.coerceAtLeast(0L)).coerceAtLeast(0L)
        AlertDialog(
            onDismissRequest={selectedGoal=null},
            title={Text((if(lang=="ar")"إضافة ادخار — " else "Add savings — ")+goal.title)},
            text={Column{
                Text(if(lang=="ar")"هذا المبلغ يُحجز من المتاح للصرف ولا يُسجل كمصروف." else "This amount is reserved from safe-to-spend and is not recorded as an expense.",color=FlosiMuted)
                OutlinedTextField(reserveAmount,{reserveAmount=it.filter(Char::isDigit);reserveError=null},Modifier.fillMaxWidth(),label={Text("${flosiText("amount")} ($currency)")})
                Text(if(lang=="ar")"المتبقي للهدف ${moneyText(remaining,currency)}" else "Remaining for goal ${moneyText(remaining,currency)}",color=FlosiMuted)
                reserveError?.let{Text(it,color=FlosiRed)}
            }},
            confirmButton={Button(onClick={val amount=reserveAmount.toLongOrNull()?:0L;vm.reserveGoal(goal.id,amount){error->if(error==null){selectedGoal=null;reserveAmount="";reserveError=null}else reserveError=error}},enabled=(reserveAmount.toLongOrNull()?:0L)>0L){Text(if(lang=="ar")"حجز المبلغ" else "Reserve amount")}},
            dismissButton={TextButton(onClick={selectedGoal=null}){Text(flosiText("cancel"))}}
        )
    }
}
