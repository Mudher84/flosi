package com.flosi.app.ui.screens.commitments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.data.local.entity.CommitmentEntity
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun CommitmentEditScreen(onBack:()->Unit){
    val vm:PlanningViewModel=flosiViewModel()
    val accounts by vm.accounts.collectAsState()
    val categories by vm.categories.collectAsState()
    val lang=LocalFlosiLanguage.current
    var title by remember{mutableStateOf("")}
    var amount by remember{mutableStateOf("")}
    var accountId by remember{mutableStateOf<Long?>(null)}
    var categoryId by remember{mutableStateOf<Long?>(null)}
    var dueDays by remember{mutableIntStateOf(1)}
    var repeatRule by remember{mutableStateOf("none")}
    var remindBeforeDays by remember{mutableIntStateOf(3)}
    var saving by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf<String?>(null)}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    LaunchedEffect(accounts){if(accountId==null)accountId=accounts.firstOrNull()?.id}
    val expenseCategories=categories.filter{it.kind=="expense"||it.kind=="both"}
    val parsedAmount=amount.toLongOrNull()
    val valid=!saving&&title.isNotBlank()&&parsedAmount!=null&&parsedAmount>0L&&accountId!=null

    FlosiPage(localizedLegacyText("إضافة التزام"),s("موعد ودفع متكرر مربوط بحساب فعلي","A due payment linked to a real account"),onBack){
        OutlinedTextField(title,{title=it;error=null},Modifier.fillMaxWidth(),label={Text(s("اسم الالتزام","Commitment name"))},singleLine=true)
        OutlinedTextField(amount,{amount=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(flosiText("amount"))},singleLine=true)

        CardBox{
            Text(flosiText("account"),style=MaterialTheme.typography.titleSmall)
            if(accounts.isEmpty()) Text(s("أضف حساباً أولاً؛ لا يمكن تسجيل دفع الالتزام بدون حساب.","Add an account first; a commitment cannot be paid without an account."),color=FlosiRed)
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
                accounts.forEach{account->FilterChip(accountId==account.id,{accountId=account.id;error=null},{Text("${account.name} • ${account.currency}")})}
            }
        }

        CardBox{
            Text(flosiText("category"),style=MaterialTheme.typography.titleSmall)
            FilterChip(categoryId==null,{categoryId=null},{Text(s("بدون تصنيف","No category"))})
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                expenseCategories.take(4).forEach{category->FilterChip(categoryId==category.id,{categoryId=category.id},{Text(category.name)})}
            }
        }

        CardBox{
            Text(s("موعد الاستحقاق","Due date"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                listOf(0 to s("اليوم","Today"),1 to s("غداً","Tomorrow"),7 to s("7 أيام","7 days"),30 to s("30 يوم","30 days")).forEach{(days,label)->
                    FilterChip(dueDays==days,{dueDays=days},{Text(label)})
                }
            }
            Text(s("التكرار","Repeat"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                listOf("none" to s("مرة واحدة","Once"),"weekly" to s("أسبوعي","Weekly"),"monthly" to s("شهري","Monthly"),"yearly" to s("سنوي","Yearly")).forEach{(rule,label)->
                    FilterChip(repeatRule==rule,{repeatRule=rule},{Text(label)})
                }
            }
            Text(s("التذكير قبل الموعد","Remind before"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                listOf(0,1,3,7).forEach{days->FilterChip(remindBeforeDays==days,{remindBeforeDays=days},{Text(if(days==0)s("نفس اليوم","Same day") else s("$days يوم","$days days"))})}
            }
        }

        error?.let{Text(it,color=FlosiRed)}
        Button(
            onClick={
                val value=parsedAmount ?: return@Button
                val account=accountId ?: return@Button
                val due=System.currentTimeMillis()+dueDays*86_400_000L
                saving=true;error=null
                vm.addCommitment(
                    CommitmentEntity(
                        title=title.trim(),amount=value,accountId=account,categoryId=categoryId,
                        dueAt=due,repeatRule=repeatRule,remindBeforeDays=remindBeforeDays
                    )
                ){message->
                    saving=false
                    if(message==null)onBack()else error=message
                }
            },
            enabled=valid,
            modifier=Modifier.fillMaxWidth()
        ){
            if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(flosiText("save"))
        }
    }
}
