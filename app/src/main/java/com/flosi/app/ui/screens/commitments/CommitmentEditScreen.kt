package com.flosi.app.ui.screens.commitments
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.data.local.entity.CommitmentEntity
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
@Composable fun CommitmentEditScreen(onBack:()->Unit){
 val vm:PlanningViewModel=flosiViewModel();var title by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")}
 FlosiPage("إضافة التزام","يحفظ بقاعدة البيانات",onBack){
  OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("الاسم")})
  OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("المبلغ")})
  Button(onClick={vm.addCommitment(CommitmentEntity(title=title,amount=amount.toLong(),dueAt=System.currentTimeMillis()+86400000L));onBack()},enabled=title.isNotBlank()&&(amount.toLongOrNull()?:0)>0){Text("حفظ")}
 }
}
