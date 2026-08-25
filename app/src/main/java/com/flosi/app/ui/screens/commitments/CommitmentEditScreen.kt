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
fun CommitmentEditScreen(onBack:()->Unit,initialPersonId:Long?=null){
    val vm:PlanningViewModel=flosiViewModel()
    val accounts by vm.accounts.collectAsState()
    val people by vm.people.collectAsState()
    val categories by vm.categories.collectAsState()
    val lang=LocalFlosiLanguage.current
    var title by remember{mutableStateOf("")}
    var amount by remember{mutableStateOf("")}
    var accountId by remember{mutableStateOf<Long?>(null)}
    var personId by remember{mutableStateOf<Long?>(initialPersonId)}
    var categoryId by remember{mutableStateOf<Long?>(null)}
    var dueDays by remember{mutableIntStateOf(1)}
    var repeatRule by remember{mutableStateOf("none")}
    var remindBeforeDays by remember{mutableIntStateOf(3)}
    var saving by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf<String?>(null)}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    LaunchedEffect(accounts,personId,people){
        val person=people.firstOrNull{it.id==personId}
        if(person!=null){
            val matching=accounts.firstOrNull{it.currency.equals(person.currency,true)}
            if(accountId==null || accounts.firstOrNull{it.id==accountId}?.currency?.equals(person.currency,true)!=true) accountId=matching?.id
        } else if(accountId==null) accountId=accounts.firstOrNull()?.id
    }
    val expenseCategories=categories.filter{it.kind=="expense"||it.kind=="both"}
    val parsedAmount=amount.toLongOrNull()
    val selectedPerson=people.firstOrNull{it.id==personId}
    val selectedAccount=accounts.firstOrNull{it.id==accountId}
    val currencyMismatch=selectedPerson!=null&&selectedAccount!=null&&!selectedPerson.currency.equals(selectedAccount.currency,true)
    val valid=!saving&&title.isNotBlank()&&parsedAmount!=null&&parsedAmount>0L&&accountId!=null&&!currencyMismatch

    FlosiPage(localizedLegacyText("إضافة التزام"),s("قسط أو موعد دفع مربوط بحساب وشخص اختياري","A due payment linked to an account and optional person"),onBack){
        OutlinedTextField(title,{title=it;error=null},Modifier.fillMaxWidth(),label={Text(s("اسم الالتزام أو القسط","Commitment or installment name"))},singleLine=true)
        OutlinedTextField(amount,{amount=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(flosiText("amount"))},singleLine=true)

        CardBox{
            Text(s("الشخص — اختياري","Person — optional"),style=MaterialTheme.typography.titleSmall)
            FilterChip(personId==null,{personId=null;error=null},{Text(s("بدون شخص","No person"))})
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
                people.take(12).forEach{person->FilterChip(personId==person.id,{personId=person.id;error=null},{Text("${person.name} • ${person.currency}")})}
            }
            selectedPerson?.let{Text(s("سيظهر هذا القسط داخل ديوان ${it.name}","This installment will appear in ${it.name}'s Diwan"),color=FlosiPurple)}
        }

        CardBox{
            Text(flosiText("account"),style=MaterialTheme.typography.titleSmall)
            if(accounts.isEmpty()) Text(s("أضف حساباً أولاً؛ لا يمكن تسجيل الدفع بدون حساب.","Add an account first; payment cannot be recorded without an account."),color=FlosiRed)
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
                accounts.filter{selectedPerson==null||it.currency.equals(selectedPerson.currency,true)}.forEach{account->FilterChip(accountId==account.id,{accountId=account.id;error=null},{Text("${account.name} • ${account.currency}")})}
            }
            if(currencyMismatch)Text(s("عملة الحساب يجب أن تطابق عملة الشخص.","Account currency must match the person's currency."),color=FlosiRed)
        }

        CardBox{
            Text(flosiText("category"),style=MaterialTheme.typography.titleSmall)
            FilterChip(categoryId==null,{categoryId=null},{Text(s("بدون تصنيف","No category"))})
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){expenseCategories.take(4).forEach{category->FilterChip(categoryId==category.id,{categoryId=category.id},{Text(category.name)})}}
        }

        CardBox{
            Text(s("موعد الاستحقاق","Due date"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(0 to s("اليوم","Today"),1 to s("غداً","Tomorrow"),7 to s("7 أيام","7 days"),30 to s("30 يوم","30 days")).forEach{(days,label)->FilterChip(dueDays==days,{dueDays=days},{Text(label)})}}
            Text(s("التكرار","Repeat"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("none" to s("مرة واحدة","Once"),"weekly" to s("أسبوعي","Weekly"),"monthly" to s("شهري","Monthly"),"yearly" to s("سنوي","Yearly")).forEach{(rule,label)->FilterChip(repeatRule==rule,{repeatRule=rule},{Text(label)})}}
            Text(s("التذكير قبل الموعد","Remind before"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(0,1,3,7).forEach{days->FilterChip(remindBeforeDays==days,{remindBeforeDays=days},{Text(if(days==0)s("نفس اليوم","Same day") else s("$days يوم","$days days"))})}}
        }

        error?.let{Text(it,color=FlosiRed)}
        Button(onClick={
            val value=parsedAmount?:return@Button;val account=accountId?:return@Button
            val due=System.currentTimeMillis()+dueDays*86_400_000L
            saving=true;error=null
            vm.addCommitment(CommitmentEntity(title=title.trim(),amount=value,accountId=account,personId=personId,categoryId=categoryId,dueAt=due,repeatRule=repeatRule,remindBeforeDays=remindBeforeDays)){message->saving=false;if(message==null)onBack()else error=message}
        },enabled=valid,modifier=Modifier.fillMaxWidth()){
            if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(if(personId!=null)s("حفظ في الديوان","Save to Diwan")else flosiText("save"))
        }
    }
}
