package com.flosi.app.ui.screens.planning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.data.local.entity.GoalEntity
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun GoalsScreen(onBack:()->Unit,onEdit:()->Unit){
    val vm:PlanningViewModel=flosiViewModel()
    val items by vm.goals.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val lang=LocalFlosiLanguage.current
    val accountMap=remember(accounts){accounts.associateBy{it.id}}
    val baseCurrency=CurrencyConverter.normalizeCode(prefs.currency)
    var selectedGoal by remember{mutableStateOf<GoalEntity?>(null)}
    var reserveAmount by remember{mutableStateOf("")}
    var reserveError by remember{mutableStateOf<String?>(null)}
    var reserving by remember{mutableStateOf(false)}

    LaunchedEffect(selectedGoal,items,accounts){
        val current=selectedGoal ?: return@LaunchedEffect
        val live=items.firstOrNull{it.id==current.id}
        val account=live?.accountId?.let(accountMap::get)
        if(live==null || !live.active || account==null || live.savedAmount>=live.targetAmount){
            selectedGoal=null
            reserveAmount=""
            reserveError=null
            reserving=false
        }
    }

    FlosiPage(flosiText("goals"),localizedLegacyText("الأهداف والادخار المحجوز"),onBack){
        SectionTitle(flosiText("goals"),if(lang=="ar")"+ هدف" else "+ Goal",onEdit)
        if(items.isEmpty()) CardBox{Text(if(lang=="ar")"ابدأ أول هدف ادخار" else "Create your first savings goal",color=FlosiMuted)}
        items.forEach{goal->
            val account=goal.accountId?.let(accountMap::get)
            val currency=account?.currency?.let(CurrencyConverter::normalizeCode)?:baseCurrency
            val target=goal.targetAmount.coerceAtLeast(0L)
            val saved=goal.savedAmount.coerceIn(0L,target)
            val remaining=(target-saved).coerceAtLeast(0L)
            val progress=if(target>0L)(saved.toFloat()/target.toFloat()).coerceIn(0f,1f) else 0f
            CardBox{
                ActionRow(goal.title,account?.let{"${it.name} • ${if(lang=="ar")"محجوز" else "Reserved"} ${moneyText(saved,currency)}"}?:if(lang=="ar")"هدف قديم غير مربوط بحساب" else "Legacy goal not linked to an account",moneyText(target,currency),if(remaining==0L)FlosiGreen else FlosiPurple)
                LinearProgressIndicator(progress={progress})
                Text(if(remaining==0L){if(lang=="ar")"اكتمل الهدف" else "Goal completed"}else{if(lang=="ar")"المتبقي ${moneyText(remaining,currency)}" else "Remaining ${moneyText(remaining,currency)}"},color=if(remaining==0L)FlosiGreen else FlosiMuted)
                if(account!=null&&remaining>0L&&goal.active){
                    OutlinedButton(onClick={selectedGoal=goal;reserveAmount="";reserveError=null;reserving=false},modifier=Modifier.fillMaxWidth()){Text(if(lang=="ar")"إضافة ادخار للهدف" else "Add savings to goal")}
                } else if(account==null) {
                    Text(if(lang=="ar")"أنشئ هدفاً جديداً مربوطاً بحساب حتى يكون الادخار جزءاً من السجل المالي." else "Create a new goal linked to an account so savings become part of the financial ledger.",color=FlosiOrange)
                }
            }
        }
    }

    val goal=selectedGoal
    if(goal!=null){
        val liveGoal=items.firstOrNull{it.id==goal.id}?:goal
        val account=liveGoal.accountId?.let(accountMap::get)
        val currency=account?.currency?.let(CurrencyConverter::normalizeCode)?:baseCurrency
        val target=liveGoal.targetAmount.coerceAtLeast(0L)
        val saved=liveGoal.savedAmount.coerceIn(0L,target)
        val remaining=(target-saved).coerceAtLeast(0L)
        val rawAmount=reserveAmount.toLongOrNull()?:0L
        val canReserve=!reserving && account!=null && rawAmount>0L && rawAmount<=remaining
        AlertDialog(
            onDismissRequest={if(!reserving)selectedGoal=null},
            title={Text((if(lang=="ar")"إضافة ادخار — " else "Add savings — ")+liveGoal.title)},
            text={Column{
                Text(if(lang=="ar")"هذا المبلغ يُحجز من المتاح للصرف ولا يُسجل كمصروف." else "This amount is reserved from safe-to-spend and is not recorded as an expense.",color=FlosiMuted)
                OutlinedTextField(reserveAmount,{reserveAmount=it.filter(Char::isDigit);reserveError=null},Modifier.fillMaxWidth(),label={Text("${flosiText("amount")} ($currency)")},singleLine=true)
                Text(if(lang=="ar")"المتبقي للهدف ${moneyText(remaining,currency)}" else "Remaining for goal ${moneyText(remaining,currency)}",color=FlosiMuted)
                if(rawAmount>remaining&&remaining>0L) Text(if(lang=="ar")"المبلغ أكبر من المتبقي للهدف" else "Amount exceeds the goal remainder",color=FlosiRed)
                reserveError?.let{Text(it,color=FlosiRed)}
            }},
            confirmButton={Button(onClick={
                val amount=reserveAmount.toLongOrNull()?:return@Button
                reserving=true;reserveError=null
                vm.reserveGoal(liveGoal.id,amount){error->
                    reserving=false
                    if(error==null){selectedGoal=null;reserveAmount="";reserveError=null}else reserveError=error
                }
            },enabled=canReserve){if(reserving)CircularProgressIndicator(strokeWidth=2.dp)else Text(if(lang=="ar")"حجز المبلغ" else "Reserve amount")}},
            dismissButton={TextButton(onClick={selectedGoal=null},enabled=!reserving){Text(flosiText("cancel"))}}
        )
    }
}
