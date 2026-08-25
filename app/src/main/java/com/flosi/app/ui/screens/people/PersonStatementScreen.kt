package com.flosi.app.ui.screens.people

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.data.local.entity.TransactionEntity
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun PersonStatementScreen(id:Long,onBack:()->Unit,onAddTx:()->Unit){
    val repo=rememberFlosiRepository()
    val lang=LocalFlosiLanguage.current
    val scope=rememberCoroutineScope()
    val person by repo.observePerson(id).collectAsState(initial=null)
    val txs by repo.observePersonTransactions(id).collectAsState(initial=emptyList())
    val accounts by repo.accounts.collectAsState(initial=emptyList())
    val commitments by repo.commitments.collectAsState(initial=emptyList())

    var showSettlement by rememberSaveable{id.let{mutableStateOf(false)}}
    var amountText by rememberSaveable{id.let{mutableStateOf("")}}
    var selectedAccountId by rememberSaveable{id.let{mutableStateOf<Long?>(null)}}
    var note by rememberSaveable{id.let{mutableStateOf("")}}
    var saving by remember{mutableStateOf(false)}
    var message by remember{mutableStateOf<String?>(null)}

    fun s(ar:String,en:String)=if(lang=="ar")ar else en
    fun dateText(value:Long):String=SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(Date(value))

    val p=person
    val matchingAccounts=if(p==null) emptyList() else accounts.filter{it.currency.equals(p.currency,ignoreCase=true)}
    val linkedCommitments=commitments.filter{it.personId==id}.sortedBy{it.dueAt}

    LaunchedEffect(p?.currency,matchingAccounts){
        if(selectedAccountId==null || matchingAccounts.none{it.id==selectedAccountId}) selectedAccountId=matchingAccounts.firstOrNull()?.id
    }

    FlosiPage(p?.name ?: localizedLegacyText("كشف الحساب"),s("الديوان • حساب الشخص","Diwan • Person ledger"),onBack){
        p?.let{personValue->
            val balance=personValue.currentBalance
            val balanceAbs=balance.absoluteValue
            CardBox{
                Metric(
                    if(balance>0)s("لك عنده","Owed to you") else if(balance<0)s("عليك له","You owe") else s("الحساب مصفّى","Settled"),
                    moneyText(balanceAbs,personValue.currency),
                    if(balance>0)FlosiGreen else if(balance<0)FlosiRed else FlosiMuted
                )
                if(personValue.phone.isNotBlank())Text(personValue.phone)
                Text(s("عملة حساب الشخص: ${personValue.currency}","Person balance currency: ${personValue.currency}"),color=FlosiMuted)
            }

            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(
                    onClick={
                        showSettlement=!showSettlement
                        message=null
                        if(showSettlement && amountText.isBlank() && balanceAbs>0) amountText=balanceAbs.toString()
                    },
                    enabled=balance!=0L,
                    modifier=Modifier.weight(1f)
                ){
                    Text(if(balance>0)s("استلم تسديد","Receive payment") else s("سدد ما عليك","Pay debt"))
                }
                OutlinedButton(onClick=onAddTx,modifier=Modifier.weight(1f)){Text("+ ${flosiText("activity")}")}
            }

            if(showSettlement && balance!=0L){
                CardBox{
                    Text(s("تسديد جزئي أو كامل","Partial or full settlement"),style=MaterialTheme.typography.titleMedium)
                    if(matchingAccounts.isEmpty()){
                        Text(s("لا يوجد حساب بنفس عملة ${personValue.currency}. أضف حساباً بهذه العملة أولاً.","No account uses ${personValue.currency}. Add one first."),color=FlosiOrange)
                    }else{
                        Text(s("الحساب","Account"),color=FlosiMuted)
                        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                            matchingAccounts.take(5).forEach{a->
                                FilterChip(selectedAccountId==a.id,{selectedAccountId=a.id;message=null},{Text(a.name)})
                            }
                        }
                    }
                    OutlinedTextField(
                        value=amountText,
                        onValueChange={amountText=it.filter(Char::isDigit);message=null},
                        modifier=Modifier.fillMaxWidth(),
                        label={Text(flosiText("amount"))},
                        supportingText={Text(s("الحد الأقصى ${moneyText(balanceAbs,personValue.currency)}","Maximum ${moneyText(balanceAbs,personValue.currency)}"))},
                        singleLine=true
                    )
                    OutlinedTextField(note,{note=it;message=null},Modifier.fillMaxWidth(),label={Text(s("ملاحظة — اختياري","Note — optional"))})
                    message?.let{Text(it,color=if(it.startsWith("✓"))FlosiGreen else FlosiRed)}
                    val amount=amountText.toLongOrNull()?:0L
                    Button(
                        onClick={
                            val accountId=selectedAccountId ?: return@Button
                            saving=true;message=null
                            scope.launch{
                                val result=runCatching{
                                    require(amount>0L){s("أدخل مبلغاً صحيحاً","Enter a valid amount")}
                                    require(amount<=balanceAbs){s("لا يمكن أن يتجاوز التسديد الرصيد المتبقي","Payment cannot exceed the remaining balance")}
                                    val kind=if(balance>0)"debt_received" else "debt_given"
                                    val title=if(balance>0)s("تسديد مستلم من ${personValue.name}","Payment received from ${personValue.name}") else s("تسديد إلى ${personValue.name}","Payment to ${personValue.name}")
                                    repo.addTransaction(TransactionEntity(kind=kind,amount=amount,title=title,note=note.trim(),accountId=accountId,personId=id))
                                }
                                saving=false
                                result.exceptionOrNull()?.let{message=it.message?:s("تعذر تسجيل التسديد","Could not record payment")} ?: run{
                                    message=s("✓ تم تسجيل التسديد","✓ Payment recorded")
                                    amountText="";note="";showSettlement=false
                                }
                            }
                        },
                        enabled=!saving&&matchingAccounts.isNotEmpty()&&selectedAccountId!=null&&amount>0L&&amount<=balanceAbs,
                        modifier=Modifier.fillMaxWidth()
                    ){
                        if(saving)CircularProgressIndicator(strokeWidth=2.dp) else Text(s("تسجيل التسديد","Record payment"))
                    }
                }
            }
        }

        if(linkedCommitments.isNotEmpty()){
            SectionTitle(s("الاستحقاقات والأقساط","Due items & installments"))
            CardBox{
                linkedCommitments.forEach{item->
                    ActionRow(
                        item.title,
                        s("استحقاق ${dateText(item.dueAt)}","Due ${dateText(item.dueAt)}"),
                        moneyText(item.amount,p?.currency?:"IQD"),
                        if(item.dueAt<System.currentTimeMillis())FlosiRed else FlosiOrange
                    )
                }
            }
        }

        SectionTitle(flosiText("activity"))
        CardBox{
            if(txs.isEmpty())Text(localizedLegacyText("ماكو حركات مرتبطة"),color=FlosiMuted)
            txs.forEach{t->
                val positive=t.kind in setOf("income","invoice_payment","debt_received")
                ActionRow(t.title,listOfNotNull(t.categoryName,t.accountName).joinToString(" • "),moneyText(t.amount,t.accountCurrency),if(positive)FlosiGreen else FlosiRed)
            }
        }
    }
}
