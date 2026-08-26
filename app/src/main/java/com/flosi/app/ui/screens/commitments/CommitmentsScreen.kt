package com.flosi.app.ui.screens.commitments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.PlanningViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun CommitmentsScreen(onBack:()->Unit,onEdit:()->Unit){
    val vm:PlanningViewModel=flosiViewModel();val items by vm.commitments.collectAsState();val accounts by vm.accounts.collectAsState();val prefs by vm.preferences.collectAsState();val lang=LocalFlosiLanguage.current
    fun s(ar:String,en:String,tr:String,fr:String,de:String,es:String)=when(lang){"ar"->ar;"tr"->tr;"fr"->fr;"de"->de;"es"->es;else->en}
    var message by remember{mutableStateOf<String?>(null)};var messageSuccess by remember{mutableStateOf(false)};var payingId by remember{mutableStateOf<Long?>(null)}
    val base=CurrencyConverter.normalizeCode(prefs.currency);val accountMap=remember(accounts){accounts.associateBy{it.id}};val missing=linkedSetOf<String>();val total=items.sumOf{item->val source=item.accountId?.let(accountMap::get)?.currency?:base;val converted=CurrencyConverter.convert(item.amount,source,base,prefs.exchangeRates);if(converted==null)missing+=CurrencyConverter.normalizeCode(source);converted?:0L}
    LaunchedEffect(items){if(payingId!=null&&items.none{it.id==payingId})payingId=null}

    FlosiPage(flosiText("commitments"),localizedLegacyText("القادم عليك"),onBack){
        CardBox{Metric(localizedLegacyText("إجمالي الالتزامات"),moneyText(total,base),FlosiRed);if(missing.isNotEmpty())Text(s("غير محسوب لعدم وجود سعر تحويل: ${missing.joinToString()}","Excluded because exchange rates are missing: ${missing.joinToString()}","Kur eksik olduğu için hariç: ${missing.joinToString()}","Exclu faute de taux de change : ${missing.joinToString()}","Wegen fehlender Wechselkurse ausgeschlossen: ${missing.joinToString()}","Excluido por falta de tipos de cambio: ${missing.joinToString()}"),color=FlosiOrange)}
        SectionTitle(localizedLegacyText("القادم"),s("+ التزام","+ Commitment","+ Yükümlülük","+ Engagement","+ Verpflichtung","+ Compromiso"),onEdit)
        CardBox{
            if(items.isEmpty())Text(localizedLegacyText("ماكو التزامات بعد"),color=FlosiMuted)
            items.forEach{item->val account=item.accountId?.let(accountMap::get);val accountMissing=item.accountId!=null&&account==null;val insufficient=account!=null&&account.currentBalance<item.amount;val busy=payingId!=null
                Column(Modifier.fillMaxWidth()){
                    ActionRow(item.title,listOfNotNull(item.repeatRule,account?.name).joinToString(" • "),moneyText(item.amount,account?.currency?:base),FlosiOrange)
                    if(accountMissing)Text(s("الحساب المرتبط غير موجود؛ لا يمكن تسجيل الدفع.","The linked account no longer exists; payment cannot be recorded.","Bağlı hesap artık yok; ödeme kaydedilemez.","Le compte lié n’existe plus ; le paiement ne peut pas être enregistré.","Das verknüpfte Konto existiert nicht mehr; die Zahlung kann nicht erfasst werden.","La cuenta vinculada ya no existe; no se puede registrar el pago."),color=FlosiRed)
                    else if(insufficient)Text(s("رصيد ${account?.name} لا يكفي لهذا الالتزام.","${account?.name} does not have enough balance for this commitment.","${account?.name} bakiyesi bu yükümlülük için yetersiz.","Le solde de ${account?.name} est insuffisant pour cet engagement.","Das Guthaben von ${account?.name} reicht für diese Verpflichtung nicht aus.","El saldo de ${account?.name} no es suficiente para este compromiso."),color=FlosiOrange)
                    TextButton(onClick={payingId=item.id;message=null;vm.payCommitment(item.id){error->payingId=null;messageSuccess=error==null;message=error?:s("تم تسجيل الدفع وتحديث الموعد","Payment recorded and due date updated","Ödeme kaydedildi ve vade güncellendi","Paiement enregistré et échéance mise à jour","Zahlung erfasst und Fälligkeit aktualisiert","Pago registrado y vencimiento actualizado")}},enabled=!busy&&account!=null&&!insufficient){
                        if(payingId==item.id)CircularProgressIndicator(strokeWidth=2.dp) else Text(when{account==null->s("اربط حساباً قبل الدفع","Link an account before paying","Ödemeden önce hesap bağla","Liez un compte avant de payer","Vor der Zahlung ein Konto verknüpfen","Vincula una cuenta antes de pagar");insufficient->s("الرصيد غير كافٍ","Insufficient balance","Yetersiz bakiye","Solde insuffisant","Unzureichendes Guthaben","Saldo insuficiente");else->s("تسجيل كمدفوع","Mark as paid","Ödendi olarak işaretle","Marquer comme payé","Als bezahlt markieren","Marcar como pagado")})
                    }
                }
            }
        }
        message?.let{Text(it,color=if(messageSuccess)FlosiGreen else FlosiRed)}
    }
}
