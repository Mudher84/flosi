package com.flosi.app.ui.screens.commitments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun CommitmentsScreen(onBack:()->Unit,onEdit:()->Unit){
    val vm:PlanningViewModel=flosiViewModel()
    val items by vm.commitments.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val lang=LocalFlosiLanguage.current
    var message by remember{mutableStateOf<String?>(null)}
    var messageSuccess by remember{mutableStateOf(false)}
    var payingId by remember{mutableStateOf<Long?>(null)}
    val base=CurrencyConverter.normalizeCode(prefs.currency)
    val accountMap=remember(accounts){accounts.associateBy{it.id}}
    val missing=linkedSetOf<String>()
    val total=items.sumOf{item->
        val source=item.accountId?.let(accountMap::get)?.currency?:base
        val converted=CurrencyConverter.convert(item.amount,source,base,prefs.exchangeRates)
        if(converted==null)missing+=CurrencyConverter.normalizeCode(source)
        converted?:0L
    }

    LaunchedEffect(items){if(payingId!=null&&items.none{it.id==payingId})payingId=null}

    FlosiPage(flosiText("commitments"),localizedLegacyText("القادم عليك"),onBack){
        CardBox{
            Metric(localizedLegacyText("إجمالي الالتزامات"),moneyText(total,base),FlosiRed)
            if(missing.isNotEmpty())Text(if(lang=="ar")"غير محسوب لعدم وجود سعر تحويل: ${missing.joinToString()}" else "Excluded because exchange rates are missing: ${missing.joinToString()}",color=FlosiOrange)
        }
        SectionTitle(localizedLegacyText("القادم"),if(lang=="ar")"+ التزام" else "+ Commitment",onEdit)
        CardBox{
            if(items.isEmpty())Text(localizedLegacyText("ماكو التزامات بعد"),color=FlosiMuted)
            items.forEach{item->
                val account=item.accountId?.let(accountMap::get)
                val accountMissing=item.accountId!=null&&account==null
                val insufficient=account!=null&&account.currentBalance<item.amount
                val busy=payingId!=null
                Column(Modifier.fillMaxWidth()){
                    ActionRow(item.title,listOfNotNull(item.repeatRule,account?.name).joinToString(" • "),moneyText(item.amount,account?.currency?:base),FlosiOrange)
                    if(accountMissing)Text(if(lang=="ar")"الحساب المرتبط غير موجود؛ لا يمكن تسجيل الدفع." else "The linked account no longer exists; payment cannot be recorded.",color=FlosiRed)
                    else if(insufficient)Text(if(lang=="ar")"رصيد ${account?.name} لا يكفي لهذا الالتزام." else "${account?.name} does not have enough balance for this commitment.",color=FlosiOrange)
                    TextButton(
                        onClick={
                            payingId=item.id;message=null
                            vm.payCommitment(item.id){error->
                                payingId=null
                                messageSuccess=error==null
                                message=error?:if(lang=="ar")"تم تسجيل الدفع وتحديث الموعد" else "Payment recorded and due date updated"
                            }
                        },
                        enabled=!busy&&account!=null&&!insufficient
                    ){
                        if(payingId==item.id) CircularProgressIndicator(strokeWidth=2f)
                        else Text(when{
                            account==null->if(lang=="ar")"اربط حساباً قبل الدفع" else "Link an account before paying"
                            insufficient->if(lang=="ar")"الرصيد غير كافٍ" else "Insufficient balance"
                            else->if(lang=="ar")"تسجيل كمدفوع" else "Mark as paid"
                        })
                    }
                }
            }
        }
        message?.let{Text(it,color=if(messageSuccess)FlosiGreen else FlosiRed)}
    }
}
