package com.flosi.app.ui.screens.planning
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.data.local.entity.GoalEntity
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun GoalEditScreen(onBack:()->Unit){
 val vm:PlanningViewModel=flosiViewModel();var title by remember{mutableStateOf("")};var target by remember{mutableStateOf("")};var saved by remember{mutableStateOf("")}
 FlosiPage("هدف جديد","خطة ادخار حقيقية",onBack){
  OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("الهدف")})
  OutlinedTextField(target,{target=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("المبلغ المطلوب")})
  OutlinedTextField(saved,{saved=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("المحفوظ الآن")})
  Button(onClick={vm.addGoal(GoalEntity(title=title,targetAmount=target.toLong(),savedAmount=saved.toLongOrNull()?:0));onBack()},enabled=title.isNotBlank()&&(target.toLongOrNull()?:0)>0){Text("حفظ")}
 }
}
