package com.flosi.app.ui.screens.planning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.data.local.entity.BudgetEntity
import com.flosi.app.i18n.FlosiLocales
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun BudgetDetailScreen(onBack:()->Unit){
    val vm:PlanningViewModel=flosiViewModel();val categories by vm.categories.collectAsState();val prefs by vm.preferences.collectAsState();val lang=LocalFlosiLanguage.current
    var title by remember{mutableStateOf("")};var limit by remember{mutableStateOf("")};var categoryId by remember{mutableStateOf<Long?>(null)};var warningPercent by remember{mutableIntStateOf(80)}
    val expenseCategories=categories.filter{it.kind=="expense" || it.kind=="both"};val (periodStart,periodEnd)=remember{currentMonthRange()}
    val locale=FlosiLocales.get(lang).locale();val monthLabel=remember(periodStart,lang){SimpleDateFormat("MMMM yyyy",locale).format(Date(periodStart))}

    FlosiPage(localizedLegacyText("ميزانية جديدة"),localizedLegacyText("شهر تقويمي وصرف فعلي"),onBack){
        CardBox{Text((if(lang=="ar")"الفترة: " else "Period: ")+monthLabel,color=FlosiMuted);Text((if(lang=="ar")"العملة: " else "Currency: ")+prefs.currency,color=FlosiMuted)}
        OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text(if(lang=="ar")"اسم الميزانية" else "Budget name")})
        OutlinedTextField(limit,{limit=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text(if(lang=="ar")"حد الصرف (${prefs.currency})" else "Spending limit (${prefs.currency})")})
        CardBox{
            Text(flosiText("category"),style=MaterialTheme.typography.titleSmall)
            FilterChip(categoryId==null,{categoryId=null},{Text(if(lang=="ar")"كل المصروفات" else "All expenses")})
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){expenseCategories.forEach{category->FilterChip(categoryId==category.id,{categoryId=category.id},{Text(category.name)})}}
        }
        CardBox{
            Text(if(lang=="ar")"نسبة التنبيه" else "Alert threshold",style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(70,80,90).forEach{value->FilterChip(warningPercent==value,{warningPercent=value},{Text("$value%")})}}
        }
        Button(
            onClick={vm.addBudget(BudgetEntity(title=title.trim(),categoryId=categoryId,limitAmount=limit.toLong(),currency=prefs.currency,periodStart=periodStart,periodEnd=periodEnd,warningPercent=warningPercent));onBack()},
            enabled=title.isNotBlank()&&(limit.toLongOrNull()?:0L)>0L,modifier=Modifier.fillMaxWidth()
        ){Text(flosiText("save"))}
    }
}

private fun currentMonthRange():Pair<Long,Long>{
    val start=Calendar.getInstance().apply{set(Calendar.DAY_OF_MONTH,1);set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}
    val end=(start.clone() as Calendar).apply{add(Calendar.MONTH,1)}
    return start.timeInMillis to (end.timeInMillis-1L)
}
