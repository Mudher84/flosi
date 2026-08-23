package com.flosi.app.ui.screens.planning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.data.local.entity.BudgetEntity
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BudgetDetailScreen(onBack:()->Unit){
    val vm:PlanningViewModel=flosiViewModel()
    val categories by vm.categories.collectAsState()
    val prefs by vm.preferences.collectAsState()
    var title by remember{mutableStateOf("")}
    var limit by remember{mutableStateOf("")}
    var categoryId by remember{mutableStateOf<Long?>(null)}
    var warningPercent by remember{mutableIntStateOf(80)}

    val expenseCategories=categories.filter{it.kind=="expense" || it.kind=="both"}
    val (periodStart,periodEnd)=remember{currentMonthRange()}
    val monthLabel=remember(periodStart){SimpleDateFormat("MMMM yyyy",Locale.US).format(Date(periodStart))}

    FlosiPage("ميزانية جديدة","شهر تقويمي وصرف فعلي",onBack){
        CardBox{
            Text("الفترة: $monthLabel",color=FlosiMuted)
            Text("العملة: ${prefs.currency}",color=FlosiMuted)
        }

        OutlinedTextField(
            value=title,
            onValueChange={title=it},
            modifier=Modifier.fillMaxWidth(),
            label={Text("اسم الميزانية")}
        )
        OutlinedTextField(
            value=limit,
            onValueChange={limit=it.filter(Char::isDigit)},
            modifier=Modifier.fillMaxWidth(),
            label={Text("حد الصرف (${prefs.currency})")}
        )

        CardBox{
            Text("التصنيف",style=MaterialTheme.typography.titleSmall)
            FilterChip(
                selected=categoryId==null,
                onClick={categoryId=null},
                label={Text("كل المصروفات")}
            )
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
                expenseCategories.forEach{category->
                    FilterChip(
                        selected=categoryId==category.id,
                        onClick={categoryId=category.id},
                        label={Text(category.name)}
                    )
                }
            }
        }

        CardBox{
            Text("نسبة التنبيه",style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                listOf(70,80,90).forEach{value->
                    FilterChip(
                        selected=warningPercent==value,
                        onClick={warningPercent=value},
                        label={Text("$value%")}
                    )
                }
            }
        }

        Button(
            onClick={
                vm.addBudget(
                    BudgetEntity(
                        title=title.trim(),
                        categoryId=categoryId,
                        limitAmount=limit.toLong(),
                        currency=prefs.currency,
                        periodStart=periodStart,
                        periodEnd=periodEnd,
                        warningPercent=warningPercent
                    )
                )
                onBack()
            },
            enabled=title.isNotBlank()&&(limit.toLongOrNull()?:0L)>0L,
            modifier=Modifier.fillMaxWidth()
        ){
            Text("حفظ الميزانية")
        }
    }
}

private fun currentMonthRange():Pair<Long,Long>{
    val start=Calendar.getInstance().apply{
        set(Calendar.DAY_OF_MONTH,1)
        set(Calendar.HOUR_OF_DAY,0)
        set(Calendar.MINUTE,0)
        set(Calendar.SECOND,0)
        set(Calendar.MILLISECOND,0)
    }
    val end=(start.clone() as Calendar).apply{add(Calendar.MONTH,1)}
    return start.timeInMillis to (end.timeInMillis-1L)
}
