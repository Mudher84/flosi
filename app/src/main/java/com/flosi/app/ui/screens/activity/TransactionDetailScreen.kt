package com.flosi.app.ui.screens.activity

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.FlosiTransactionDetailCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository
import kotlinx.coroutines.launch

@Composable
fun TransactionDetailScreen(id:Long,onBack:()->Unit){
    val repo=rememberFlosiRepository();val item by repo.observeTransaction(id).collectAsState(initial=null);val lang=LocalFlosiLanguage.current;val scope=rememberCoroutineScope();var showDelete by remember{mutableStateOf(false)};var deleting by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    fun c(key:String)=FlosiTransactionDetailCopy.text(lang,key)
    fun kindLabel(kind:String)=when(kind){"income"->c("income");"expense"->c("expense");"debt_given"->c("debt_given");"debt_received"->c("debt_received");"transfer_in"->c("transfer_in");"transfer_out"->c("transfer_out");"invoice_payment"->c("invoice_payment");"goal_saving"->c("goal_saving");else->kind}

    FlosiPage(localizedLegacyText("تفاصيل الحركة"),c("saved_transaction"),onBack){
        val tx=item
        if(tx==null) CardBox{Text(c("not_found"))}
        else{
            val positive=tx.kind in listOf("income","transfer_in","invoice_payment","debt_received");val neutral=tx.kind in listOf("transfer_in","transfer_out","goal_saving");val tone=when{neutral->FlosiPurple;positive->FlosiGreen;else->FlosiRed}
            CardBox{Metric(flosiText("amount"),moneyText(tx.amount,tx.accountCurrency),tone);Text(tx.title);Text(kindLabel(tx.kind),color=FlosiMuted)}
            CardBox{ActionRow(flosiText("account"),tx.accountName);ActionRow(c("linked_person"),tx.personName?:c("no_person"));ActionRow(flosiText("category"),tx.categoryName?:c("no_category"));if(tx.note.isNotBlank())ActionRow(c("note"),tx.note)}
            error?.let{Text(it,color=FlosiRed)}
            OutlinedButton(onClick={showDelete=true;error=null},enabled=!deleting){Text(c("delete_transaction"),color=FlosiRed)}
        }
    }

    if(showDelete) AlertDialog(onDismissRequest={if(!deleting)showDelete=false},title={Text(c("delete_question"))},text={Text(c("delete_explain"))},confirmButton={Button(onClick={deleting=true;error=null;scope.launch{val failure=runCatching{repo.deleteTransaction(id)}.exceptionOrNull();deleting=false;if(failure==null){showDelete=false;onBack()}else{showDelete=false;error=failure.message?:c("could_not_delete")}}},enabled=!deleting){Text(flosiText("delete"))}},dismissButton={TextButton(onClick={showDelete=false},enabled=!deleting){Text(flosiText("cancel"))}})
}
