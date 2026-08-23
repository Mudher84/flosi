package com.flosi.app.ui.screens.planning
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.data.local.entity.BudgetEntity
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun BudgetDetailScreen(onBack:()->Unit){
 val vm:PlanningViewModel=flosiViewModel();var title by remember{mutableStateOf("")};var limit by remember{mutableStateOf("")}
 FlosiPage("ميزانية جديدة","حد صرف فعلي",onBack){
  OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("الاسم")})
  OutlinedTextField(limit,{limit=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("الحد")})
  Button(onClick={val now=System.currentTimeMillis();vm.addBudget(BudgetEntity(title=title,limitAmount=limit.toLong(),periodStart=now,periodEnd=now+30L*86400000L));onBack()},enabled=title.isNotBlank()&&(limit.toLongOrNull()?:0)>0){Text("حفظ")}
 }
}
