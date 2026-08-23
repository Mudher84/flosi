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
    var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    val expenseCategories=categories.filter{it.kind=="expense" || it.kind=="both"};val (periodStart,periodEnd)=remember{currentMonthRange()}
    val locale=FlosiLocales.get(lang).locale();val monthLabel=remember(periodStart,lang){SimpleDateFormat("MMMM yyyy",locale).format(Date(periodStart))}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(localizedLegacyText("ميزانية جديدة"),localizedLegacyText("شهر تقويمي وصرف فعلي"),onBack){
        CardBox{Text(s("الفترة: $monthLabel","Period: $monthLabel"),color=FlosiMuted);Text(s("العملة: ${prefs.currency}","Currency: ${prefs.currency}"),color=FlosiMuted)}
        OutlinedTextField(title,{title=it;error=null},Modifier.fillMaxWidth(),label={Text(s("اسم الميزانية","Budget name"))},singleLine=true)
        OutlinedTextField(limit,{limit=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(s("حد الصرف (${prefs.currency})","Spending limit (${prefs.currency})"))},singleLine=true)
        CardBox{
            Text(flosiText("category"),style=MaterialTheme.typography.titleSmall)
            FilterChip(categoryId==null,{categoryId=null},{Text(s("كل المصروفات","All expenses"))})
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){expenseCategories.forEach{category->FilterChip(categoryId==category.id,{categoryId=category.id},{Text(category.name)})}}
        }
        CardBox{
            Text(s("نسبة التنبيه","Alert threshold"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(70,80,90).forEach{value->FilterChip(warningPercent==value,{warningPercent=value},{Text("$value%")})}}
        }
        error?.let{Text(it,color=FlosiRed)}
        Button(
            onClick={
                val value=limit.toLongOrNull()?:return@Button
                saving=true;error=null
                vm.addBudget(BudgetEntity(title=title.trim(),categoryId=categoryId,limitAmount=value,currency=prefs.currency,periodStart=periodStart,periodEnd=periodEnd,warningPercent=warningPercent)){message->
                    saving=false
                    if(message==null)onBack()else error=message
                }
            },
            enabled=!saving&&title.isNotBlank()&&(limit.toLongOrNull()?:0L)>0L,
            modifier=Modifier.fillMaxWidth()
        ){if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(flosiText("save"))}
    }
}

private fun currentMonthRange():Pair<Long,Long>{
    val start=Calendar.getInstance().apply{set(Calendar.DAY_OF_MONTH,1);set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}
    val end=(start.clone() as Calendar).apply{add(Calendar.MONTH,1)}
    return start.timeInMillis to (end.timeInMillis-1L)
}
