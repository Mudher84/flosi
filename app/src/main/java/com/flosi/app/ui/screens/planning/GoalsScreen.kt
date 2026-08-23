package com.flosi.app.ui.screens.planning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.data.local.entity.GoalEntity
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun GoalsScreen(onBack:()->Unit,onEdit:()->Unit){
    val vm:PlanningViewModel=flosiViewModel()
    val items by vm.goals.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val accountMap=remember(accounts){accounts.associateBy{it.id}}

    var selectedGoal by remember{mutableStateOf<GoalEntity?>(null)}
    var reserveAmount by remember{mutableStateOf("")}
    var reserveError by remember{mutableStateOf<String?>(null)}

    FlosiPage("خطتي","الأهداف والادخار المحجوز",onBack){
        SectionTitle("الأهداف","+ هدف",onEdit)

        if(items.isEmpty()){
            CardBox{Text("ابدأ أول هدف ادخار",color=FlosiMuted)}
        }

        items.forEach{goal->
            val account=goal.accountId?.let(accountMap::get)
            val currency=account?.currency?:"IQD"
            val saved=goal.savedAmount.coerceIn(0L,goal.targetAmount.coerceAtLeast(0L))
            val remaining=(goal.targetAmount-saved).coerceAtLeast(0L)
            val progress=if(goal.targetAmount>0L)(saved.toFloat()/goal.targetAmount.toFloat()).coerceIn(0f,1f) else 0f

            CardBox{
                ActionRow(
                    title=goal.title,
                    subtitle=account?.let{"${it.name} • محجوز ${moneyText(saved,currency)}"}?:"هدف قديم غير مربوط بحساب",
                    value=moneyText(goal.targetAmount,currency),
                    accent=if(remaining==0L)FlosiGreen else FlosiPurple
                )
                LinearProgressIndicator(progress={progress})
                Text(
                    if(remaining==0L)"اكتمل الهدف" else "المتبقي ${moneyText(remaining,currency)}",
                    color=if(remaining==0L)FlosiGreen else FlosiMuted
                )
                if(account!=null&&remaining>0L){
                    OutlinedButton(
                        onClick={
                            selectedGoal=goal
                            reserveAmount=""
                            reserveError=null
                        },
                        modifier=Modifier.fillMaxWidth()
                    ){
                        Text("إضافة ادخار للهدف")
                    }
                }else if(account==null){
                    Text("أنشئ هدفاً جديداً مربوطاً بحساب حتى يكون الادخار جزءاً من السجل المالي.",color=FlosiOrange)
                }
            }
        }
    }

    val goal=selectedGoal
    if(goal!=null){
        val account=goal.accountId?.let(accountMap::get)
        val currency=account?.currency?:"IQD"
        val remaining=(goal.targetAmount-goal.savedAmount.coerceAtLeast(0L)).coerceAtLeast(0L)
        AlertDialog(
            onDismissRequest={selectedGoal=null},
            title={Text("إضافة ادخار — ${goal.title}")},
            text={
                Column{
                    Text("هذا المبلغ يُحجز من المتاح للصرف ولا يُسجل كمصروف.",color=FlosiMuted)
                    OutlinedTextField(
                        value=reserveAmount,
                        onValueChange={reserveAmount=it.filter(Char::isDigit);reserveError=null},
                        modifier=Modifier.fillMaxWidth(),
                        label={Text("المبلغ ($currency)")}
                    )
                    Text("المتبقي للهدف ${moneyText(remaining,currency)}",color=FlosiMuted)
                    reserveError?.let{Text(it,color=FlosiRed)}
                }
            },
            confirmButton={
                Button(
                    onClick={
                        val amount=reserveAmount.toLongOrNull()?:0L
                        vm.reserveGoal(goal.id,amount){error->
                            if(error==null){
                                selectedGoal=null
                                reserveAmount=""
                                reserveError=null
                            }else{
                                reserveError=error
                            }
                        }
                    },
                    enabled=(reserveAmount.toLongOrNull()?:0L)>0L
                ){
                    Text("حجز المبلغ")
                }
            },
            dismissButton={
                TextButton(onClick={selectedGoal=null}){Text("إلغاء")}
            }
        )
    }
}
