package com.flosi.app.ui.screens.activity

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.rememberFlosiRepository
import kotlinx.coroutines.launch

@Composable
fun TransactionDetailScreen(id:Long,onBack:()->Unit){
    val repo=rememberFlosiRepository()
    val item by repo.observeTransaction(id).collectAsState(initial=null)
    val lang=LocalFlosiLanguage.current
    val scope=rememberCoroutineScope()
    var showDelete by remember{mutableStateOf(false)}
    var deleting by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf<String?>(null)}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en
    fun kindLabel(kind:String)=if(lang=="ar")when(kind){
        "income"->"دخل";"expense"->"مصروف";"debt_given"->"سلفة ممنوحة";"debt_received"->"دين مستلم"
        "transfer_in"->"تحويل وارد";"transfer_out"->"تحويل صادر";"invoice_payment"->"دفعة فاتورة";"goal_saving"->"ادخار هدف";else->kind
    }else when(kind){
        "income"->"Income";"expense"->"Expense";"debt_given"->"Loan given";"debt_received"->"Debt received"
        "transfer_in"->"Transfer in";"transfer_out"->"Transfer out";"invoice_payment"->"Invoice payment";"goal_saving"->"Goal saving";else->kind
    }

    FlosiPage(localizedLegacyText("تفاصيل الحركة"),s("حركة محفوظة","Saved transaction"),onBack){
        val tx=item
        if(tx==null){
            CardBox{Text(s("الحركة غير موجودة","Transaction not found"))}
        }else{
            val positive=tx.kind in listOf("income","transfer_in","invoice_payment","debt_received")
            val neutral=tx.kind in listOf("transfer_in","transfer_out","goal_saving")
            val tone=when{neutral->FlosiPurple;positive->FlosiGreen;else->FlosiRed}
            CardBox{
                Metric(flosiText("amount"),moneyText(tx.amount,tx.accountCurrency),tone)
                Text(tx.title)
                Text(kindLabel(tx.kind),color=FlosiMuted)
            }
            CardBox{
                ActionRow(flosiText("account"),tx.accountName)
                ActionRow(s("الشخص المرتبط","Linked person"),tx.personName ?: s("بدون شخص","No person"))
                ActionRow(flosiText("category"),tx.categoryName ?: s("بدون تصنيف","No category"))
                if(tx.note.isNotBlank())ActionRow(s("ملاحظة","Note"),tx.note)
            }
            error?.let{Text(it,color=FlosiRed)}
            OutlinedButton(onClick={showDelete=true;error=null},enabled=!deleting){
                Text(s("حذف الحركة","Delete transaction"),color=FlosiRed)
            }
        }
    }

    if(showDelete){
        AlertDialog(
            onDismissRequest={if(!deleting)showDelete=false},
            title={Text(s("حذف الحركة؟","Delete transaction?"))},
            text={Text(s("سيتم عكس أثرها المحاسبي. التحويل يحذف طرفيه ورسومه معاً، أما دفعات الفواتير والالتزامات المرتبطة فلا يمكن حذفها منفردة.","Its accounting effect will be reversed. Transfers remove both sides and their fee together; linked invoice and commitment payments cannot be detached."))},
            confirmButton={
                Button(onClick={
                    deleting=true;error=null
                    scope.launch{
                        val failure=runCatching{repo.deleteTransaction(id)}.exceptionOrNull()
                        deleting=false
                        if(failure==null){showDelete=false;onBack()}else{showDelete=false;error=failure.message?:s("تعذر حذف الحركة","Could not delete transaction")}
                    }
                },enabled=!deleting){Text(s("حذف","Delete"))}
            },
            dismissButton={TextButton(onClick={showDelete=false},enabled=!deleting){Text(flosiText("cancel"))}}
        )
    }
}
