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
    val vm:PlanningViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState();val people by vm.people.collectAsState();val categories by vm.categories.collectAsState();val lang=LocalFlosiLanguage.current
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    var title by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var accountId by remember{mutableStateOf<Long?>(null)};var personId by remember{mutableStateOf<Long?>(initialPersonId)};var categoryId by remember{mutableStateOf<Long?>(null)};var dueDays by remember{mutableIntStateOf(1)};var repeatRule by remember{mutableStateOf("none")};var remindBeforeDays by remember{mutableIntStateOf(3)};var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    LaunchedEffect(accounts,personId,people){val person=people.firstOrNull{it.id==personId};if(person!=null){val matching=accounts.firstOrNull{it.currency.equals(person.currency,true)};if(accountId==null||accounts.firstOrNull{it.id==accountId}?.currency?.equals(person.currency,true)!=true)accountId=matching?.id}else if(accountId==null)accountId=accounts.firstOrNull()?.id}
    val expenseCategories=categories.filter{it.kind=="expense"||it.kind=="both"};val parsedAmount=amount.toLongOrNull();val selectedPerson=people.firstOrNull{it.id==personId};val selectedAccount=accounts.firstOrNull{it.id==accountId};val currencyMismatch=selectedPerson!=null&&selectedAccount!=null&&!selectedPerson.currency.equals(selectedAccount.currency,true);val valid=!saving&&title.isNotBlank()&&parsedAmount!=null&&parsedAmount>0L&&accountId!=null&&!currencyMismatch

    FlosiPage(localizedLegacyText("إضافة التزام"),s("قسط أو موعد دفع مربوط بحساب وشخص اختياري","A due payment linked to an account and optional person","Hesaba ve isteğe bağlı kişiye bağlı bir ödeme","Un paiement dû lié à un compte et éventuellement à une personne","Eine fällige Zahlung mit Konto und optionaler Person","Un pago pendiente vinculado a una cuenta y persona opcional"),onBack){
        OutlinedTextField(title,{title=it;error=null},Modifier.fillMaxWidth(),label={Text(s("اسم الالتزام أو القسط","Commitment or installment name","Yükümlülük veya taksit adı","Nom de l’engagement ou du versement","Name der Verpflichtung oder Rate","Nombre del compromiso o cuota"))},singleLine=true)
        OutlinedTextField(amount,{amount=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(flosiText("amount"))},singleLine=true)
        CardBox{
            Text(s("الشخص — اختياري","Person — optional","Kişi — isteğe bağlı","Personne — facultatif","Person — optional","Persona — opcional"),style=MaterialTheme.typography.titleSmall)
            FilterChip(personId==null,{personId=null;error=null},{Text(s("بدون شخص","No person","Kişi yok","Aucune personne","Keine Person","Sin persona"))})
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){people.take(12).forEach{person->FilterChip(personId==person.id,{personId=person.id;error=null},{Text("${person.name} • ${person.currency}")})}}
            selectedPerson?.let{Text(s("سيظهر هذا القسط داخل ديوان ${it.name}","This installment will appear in ${it.name}'s Diwan","Bu taksit ${it.name} defterinde görünecek","Cette échéance apparaîtra dans le registre de ${it.name}","Diese Rate erscheint im Kontobuch von ${it.name}","Esta cuota aparecerá en el libro de ${it.name}"),color=FlosiPurple)}
        }
        CardBox{
            Text(flosiText("account"),style=MaterialTheme.typography.titleSmall)
            if(accounts.isEmpty())Text(s("أضف حساباً أولاً؛ لا يمكن تسجيل الدفع بدون حساب.","Add an account first; payment cannot be recorded without an account.","Önce hesap ekle; hesapsız ödeme kaydedilemez.","Ajoutez d’abord un compte ; le paiement ne peut pas être enregistré sans compte.","Füge zuerst ein Konto hinzu; ohne Konto kann keine Zahlung erfasst werden.","Añade primero una cuenta; no se puede registrar el pago sin cuenta."),color=FlosiRed)
            Column(verticalArrangement=Arrangement.spacedBy(4.dp)){accounts.filter{selectedPerson==null||it.currency.equals(selectedPerson.currency,true)}.forEach{account->FilterChip(accountId==account.id,{accountId=account.id;error=null},{Text("${account.name} • ${account.currency}")})}}
            if(currencyMismatch)Text(s("عملة الحساب يجب أن تطابق عملة الشخص.","Account currency must match the person's currency.","Hesap para birimi kişinin para birimiyle eşleşmeli.","La devise du compte doit correspondre à celle de la personne.","Die Kontowährung muss der Währung der Person entsprechen.","La moneda de la cuenta debe coincidir con la de la persona."),color=FlosiRed)
        }
        CardBox{
            Text(flosiText("category"),style=MaterialTheme.typography.titleSmall);FilterChip(categoryId==null,{categoryId=null},{Text(s("بدون تصنيف","No category","Kategori yok","Aucune catégorie","Keine Kategorie","Sin categoría"))});Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){expenseCategories.take(4).forEach{category->FilterChip(categoryId==category.id,{categoryId=category.id},{Text(category.name)})}}
        }
        CardBox{
            Text(s("موعد الاستحقاق","Due date","Vade tarihi","Date d’échéance","Fälligkeitsdatum","Fecha de vencimiento"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(0 to s("اليوم","Today","Bugün","Aujourd’hui","Heute","Hoy"),1 to s("غداً","Tomorrow","Yarın","Demain","Morgen","Mañana"),7 to s("7 أيام","7 days","7 gün","7 jours","7 Tage","7 días"),30 to s("30 يوم","30 days","30 gün","30 jours","30 Tage","30 días")).forEach{(days,label)->FilterChip(dueDays==days,{dueDays=days},{Text(label)})}}
            Text(s("التكرار","Repeat","Tekrar","Répétition","Wiederholung","Repetición"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("none" to s("مرة واحدة","Once","Bir kez","Une fois","Einmal","Una vez"),"weekly" to s("أسبوعي","Weekly","Haftalık","Hebdomadaire","Wöchentlich","Semanal"),"monthly" to s("شهري","Monthly","Aylık","Mensuel","Monatlich","Mensual"),"yearly" to s("سنوي","Yearly","Yıllık","Annuel","Jährlich","Anual")).forEach{(rule,label)->FilterChip(repeatRule==rule,{repeatRule=rule},{Text(label)})}}
            Text(s("التذكير قبل الموعد","Remind before","Önceden hatırlat","Rappeler avant","Vorher erinnern","Recordar antes"),style=MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(0,1,3,7).forEach{days->val label=if(days==0)s("نفس اليوم","Same day","Aynı gün","Le jour même","Am selben Tag","El mismo día")else when(lang){"ar"->"$days يوم";"tr"->"$days gün";"fr"->"$days jours";"de"->"$days Tage";"es"->"$days días";else->"$days days"};FilterChip(remindBeforeDays==days,{remindBeforeDays=days},{Text(label)})}}
        }
        error?.let{Text(it,color=FlosiRed)}
        Button(onClick={val value=parsedAmount?:return@Button;val account=accountId?:return@Button;val due=System.currentTimeMillis()+dueDays*86_400_000L;saving=true;error=null;vm.addCommitment(CommitmentEntity(title=title.trim(),amount=value,accountId=account,personId=personId,categoryId=categoryId,dueAt=due,repeatRule=repeatRule,remindBeforeDays=remindBeforeDays)){message->saving=false;if(message==null)onBack()else error=message}},enabled=valid,modifier=Modifier.fillMaxWidth()){if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(if(personId!=null)s("حفظ في الديوان","Save to Diwan","Deftere kaydet","Enregistrer dans le registre","Im Kontobuch speichern","Guardar en el libro")else flosiText("save"))}
    }
}
