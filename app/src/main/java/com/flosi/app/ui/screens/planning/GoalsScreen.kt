package com.flosi.app.ui.screens.planning

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flosi.app.data.local.entity.GoalEntity
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.FlosiGoalCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun GoalsScreen(onBack:()->Unit,onEdit:()->Unit){
    val vm:PlanningViewModel=flosiViewModel();val items by vm.goals.collectAsState();val accounts by vm.accounts.collectAsState();val prefs by vm.preferences.collectAsState();val lang=LocalFlosiLanguage.current
    fun c(key:String,vararg values:Pair<String,Any?>)=FlosiGoalCopy.text(lang,key,*values)
    val accountMap=remember(accounts){accounts.associateBy{it.id}};val baseCurrency=CurrencyConverter.normalizeCode(prefs.currency)
    var selectedGoal by remember{mutableStateOf<GoalEntity?>(null)};var reserveAmount by remember{mutableStateOf("")};var reserveError by remember{mutableStateOf<String?>(null)};var reserving by remember{mutableStateOf(false)}
    val completed=items.count{it.targetAmount>0&&it.savedAmount>=it.targetAmount};val active=items.count{it.active&&it.savedAmount<it.targetAmount}
    LaunchedEffect(selectedGoal,items,accounts){val current=selectedGoal?:return@LaunchedEffect;val live=items.firstOrNull{it.id==current.id};val account=live?.accountId?.let(accountMap::get);if(live==null||!live.active||account==null||live.savedAmount>=live.targetAmount){selectedGoal=null;reserveAmount="";reserveError=null;reserving=false}}

    FlosiPage(flosiText("goals"),c("sub"),onBack){
        PremiumCard{
            Text(c("journey"),color=Color.White.copy(alpha=.58f),fontSize=11.sp)
            Text(if(items.isEmpty())"—" else "$completed/${items.size}",color=Color.White,fontSize=32.sp,fontWeight=FontWeight.Black)
            Text(c("completed"),color=Color.White.copy(alpha=.62f),fontSize=10.sp)
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){GoalBadge(c("active"),active,FlosiPurple,Modifier.weight(1f));GoalBadge(c("done"),completed,FlosiGreen,Modifier.weight(1f))}
        }
        SectionTitle(flosiText("goals"),c("add_goal"),onEdit)
        if(items.isEmpty()) EmptyState(title=c("start_first"),subtitle=c("empty_sub"),action=c("create"),onAction=onEdit,symbol="◎")
        items.forEach{goal->
            val account=goal.accountId?.let(accountMap::get);val currency=account?.currency?.let(CurrencyConverter::normalizeCode)?:baseCurrency;val target=goal.targetAmount.coerceAtLeast(0L);val saved=goal.savedAmount.coerceIn(0L,target);val remaining=(target-saved).coerceAtLeast(0L);val rawProgress=if(target>0L)(saved.toFloat()/target).coerceIn(0f,1f) else 0f;val progress by animateFloatAsState(rawProgress,tween(800),label="goalProgress")
            Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(28.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)){
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(46.dp).background((if(remaining==0L)FlosiGreen else FlosiPurple).copy(alpha=.10f),RoundedCornerShape(15.dp)),contentAlignment=Alignment.Center){Text(if(remaining==0L)"✓" else "◎",color=if(remaining==0L)FlosiGreen else FlosiPurple,fontSize=20.sp,fontWeight=FontWeight.Black)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(goal.title,fontWeight=FontWeight.ExtraBold,fontSize=15.sp);Text(account?.let{"${it.name} • $currency"}?:c("not_linked"),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp)};Text("${(rawProgress*100).toInt()}%",color=if(remaining==0L)FlosiGreen else FlosiPurple,fontWeight=FontWeight.Black,fontSize=13.sp)}
                    LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth().height(9.dp),color=if(remaining==0L)FlosiGreen else FlosiPurple,trackColor=MaterialTheme.colorScheme.surfaceVariant)
                    Row(Modifier.fillMaxWidth()){Column(Modifier.weight(1f)){Text(c("saved"),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=9.sp);Text(moneyText(saved,currency),fontWeight=FontWeight.Bold,fontSize=13.sp)};Column(horizontalAlignment=Alignment.End){Text(c("remaining"),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=9.sp);Text(moneyText(remaining,currency),color=if(remaining==0L)FlosiGreen else FlosiPurple,fontWeight=FontWeight.Bold,fontSize=13.sp)}}
                    Text(if(remaining==0L)c("congrats") else if(rawProgress>=.75f)c("very_close") else if(rawProgress>=.5f)c("halfway") else c("small_step"),color=if(remaining==0L)FlosiGreen else FlosiPurple,fontSize=10.sp,fontWeight=FontWeight.SemiBold)
                    if(account!=null&&remaining>0L&&goal.active)Button(onClick={selectedGoal=goal;reserveAmount="";reserveError=null;reserving=false},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Text(c("add_to_goal"))}
                    else if(account==null)Text(c("link_notice"),color=FlosiOrange,fontSize=10.sp)
                }
            }
        }
    }

    val goal=selectedGoal
    if(goal!=null){
        val liveGoal=items.firstOrNull{it.id==goal.id}?:goal;val account=liveGoal.accountId?.let(accountMap::get);val currency=account?.currency?.let(CurrencyConverter::normalizeCode)?:baseCurrency;val target=liveGoal.targetAmount.coerceAtLeast(0L);val saved=liveGoal.savedAmount.coerceIn(0L,target);val remaining=(target-saved).coerceAtLeast(0L);val rawAmount=reserveAmount.toLongOrNull()?:0L;val canReserve=!reserving&&account!=null&&rawAmount>0L&&rawAmount<=remaining
        AlertDialog(onDismissRequest={if(!reserving)selectedGoal=null},title={Text(c("dialog_title","title" to liveGoal.title))},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(c("reserve_notice"),color=MaterialTheme.colorScheme.onSurfaceVariant);OutlinedTextField(reserveAmount,{reserveAmount=it.filter(Char::isDigit);reserveError=null},Modifier.fillMaxWidth(),label={Text("${flosiText("amount")} ($currency)")},singleLine=true);Text(c("remaining_amount","amount" to moneyText(remaining,currency)),color=MaterialTheme.colorScheme.onSurfaceVariant);if(rawAmount>remaining&&remaining>0L)Text(c("too_large"),color=FlosiRed);reserveError?.let{Text(it,color=FlosiRed)}}},confirmButton={Button(onClick={val amount=reserveAmount.toLongOrNull()?:return@Button;reserving=true;reserveError=null;vm.reserveGoal(liveGoal.id,amount){error->reserving=false;if(error==null){selectedGoal=null;reserveAmount="";reserveError=null}else reserveError=error}},enabled=canReserve){if(reserving)CircularProgressIndicator(strokeWidth=2.dp,modifier=Modifier.size(20.dp))else Text(c("reserve"))}},dismissButton={TextButton(onClick={selectedGoal=null},enabled=!reserving){Text(flosiText("cancel"))}})
    }
}

@Composable private fun GoalBadge(label:String,count:Int,tone:Color,modifier:Modifier){Surface(modifier=modifier,shape=RoundedCornerShape(18.dp),color=Color.White.copy(alpha=.07f)){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(8.dp).background(tone,CircleShape));Spacer(Modifier.width(7.dp));Text("$count",color=Color.White,fontWeight=FontWeight.Black);Spacer(Modifier.width(5.dp));Text(label,color=Color.White.copy(alpha=.58f),fontSize=9.sp)}}}
