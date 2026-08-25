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
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun BudgetsScreen(onBack:()->Unit,onDetail:()->Unit){
    val vm:PlanningViewModel=flosiViewModel();val items by vm.budgetProgress.collectAsState();val categories by vm.categories.collectAsState();val lang=LocalFlosiLanguage.current
    val categoryNames=remember(categories){categories.associate{it.id to it.name}};val overCount=items.count{it.isOver};val warningCount=items.count{!it.isOver&&it.warningReached};val healthyCount=items.size-overCount-warningCount

    FlosiPage(flosiText("budgets"),if(lang=="ar")"خلي الصرف تحت السيطرة" else "Keep spending under control",onBack){
        PremiumCard{
            Text(if(lang=="ar")"حالة ميزانياتك" else "Budget health",color=Color.White.copy(alpha=.58f),fontSize=11.sp)
            Text(if(items.isEmpty())"—" else "${healthyCount}/${items.size}",color=Color.White,fontSize=32.sp,fontWeight=FontWeight.Black)
            Text(if(lang=="ar")"ميزانيات ضمن الحدود" else "budgets within limits",color=Color.White.copy(alpha=.62f),fontSize=10.sp)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                BudgetBadge(if(lang=="ar")"سليم" else "Healthy",healthyCount,FlosiGreen,Modifier.weight(1f))
                BudgetBadge(if(lang=="ar")"تنبيه" else "Watch",warningCount,FlosiOrange,Modifier.weight(1f))
                BudgetBadge(if(lang=="ar")"متجاوز" else "Over",overCount,FlosiRed,Modifier.weight(1f))
            }
        }
        SectionTitle(flosiText("budgets"),if(lang=="ar")"+ ميزانية" else "+ Budget",onDetail)
        if(items.isEmpty()) CardBox{Text(if(lang=="ar")"ابدأ بميزانية شهرية حتى Flosi يراقب صرفك وياك." else "Create a monthly budget and Flosi will track your spending with you.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        items.forEach{item->
            val budget=item.budget;val tone=when{item.isOver->FlosiRed;item.warningReached->FlosiOrange;else->FlosiGreen};val category=budget.categoryId?.let(categoryNames::get)?:if(lang=="ar")"كل المصروفات" else "All expenses";val percent=item.usagePercent.coerceAtLeast(0f);val progress by animateFloatAsState((percent/100f).coerceIn(0f,1f),tween(700),label="budgetProgress")
            Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(28.dp),elevation=CardDefaults.cardElevation(defaultElevation=2.dp)){
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(budget.title,fontWeight=FontWeight.ExtraBold,fontSize=15.sp);Text(category,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=10.sp)};Surface(color=tone.copy(alpha=.10f),shape=RoundedCornerShape(50)){Text("${percent.toInt()}%",Modifier.padding(horizontal=10.dp,vertical=6.dp),color=tone,fontWeight=FontWeight.ExtraBold,fontSize=10.sp)}}
                    LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth().height(9.dp),color=tone,trackColor=MaterialTheme.colorScheme.surfaceVariant)
                    Row(Modifier.fillMaxWidth()){Column(Modifier.weight(1f)){Text(if(lang=="ar")"المصروف" else "Spent",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=9.sp);Text(moneyText(item.spent,budget.currency),fontWeight=FontWeight.Bold,fontSize=13.sp)};Column(horizontalAlignment=Alignment.End){Text(if(item.isOver)(if(lang=="ar")"التجاوز" else "Over") else (if(lang=="ar")"المتبقي" else "Remaining"),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=9.sp);Text(moneyText(if(item.isOver)item.overAmount else item.remaining,budget.currency),color=tone,fontWeight=FontWeight.Bold,fontSize=13.sp)}}
                    Text(when{item.isOver->if(lang=="ar")"تجاوزت الحد. الأفضل تخفف الصرف بهالتصنيف لباقي الشهر." else "You've exceeded the limit. Consider easing spending in this category.";item.warningReached->if(lang=="ar")"وصلت لمنطقة التنبيه. بعدك تگدر تلحق الميزانية." else "You've reached the warning zone. There's still time to recover.";else->if(lang=="ar")"وضعك زين، بعدك ضمن الخطة." else "You're on track and still within plan."},color=tone,fontSize=10.sp,fontWeight=FontWeight.SemiBold)
                    if(item.missingCurrencies.isNotEmpty())Text(if(lang=="ar")"حركات غير محسوبة بعملات: ${item.missingCurrencies.joinToString()}" else "Excluded currencies: ${item.missingCurrencies.joinToString()}",color=FlosiOrange,fontSize=9.sp)
                }
            }
        }
    }
}

@Composable private fun BudgetBadge(label:String,count:Int,tone:Color,modifier:Modifier){Surface(modifier=modifier,shape=RoundedCornerShape(18.dp),color=Color.White.copy(alpha=.07f)){Column(Modifier.padding(10.dp),horizontalAlignment=Alignment.Start){Box(Modifier.size(7.dp).background(tone,CircleShape));Spacer(Modifier.height(6.dp));Text(count.toString(),color=Color.White,fontWeight=FontWeight.Black,fontSize=16.sp);Text(label,color=Color.White.copy(alpha=.55f),fontSize=9.sp)}}}
