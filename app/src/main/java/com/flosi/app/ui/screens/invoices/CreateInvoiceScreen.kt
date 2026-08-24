package com.flosi.app.ui.screens.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.data.local.entity.InvoiceEntity
import com.flosi.app.data.local.entity.InvoiceItemEntity
import com.flosi.app.finance.CurrencyConverter
import com.flosi.app.finance.InvoiceMath
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.InvoicesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences

private data class DraftInvoiceLine(val id:Long,val title:String,val quantity:Double,val unitPrice:Long,val lineTotal:Long)
private fun decimalValue(raw:String):Double?=raw.trim().replace(',','.').toDoubleOrNull()
private fun decimalInput(raw:String):String{val out=StringBuilder();var separator=false;raw.forEach{ch->when{ch.isDigit()->out.append(ch);(ch=='.'||ch==',')&&!separator->{if(out.isEmpty())out.append('0');out.append('.');separator=true}}};return out.toString()}

@Composable
fun CreateInvoiceScreen(onBack:()->Unit){
    val vm:InvoicesViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState();val preferences=rememberFlosiPreferences();val prefs by preferences.state.collectAsState(initial=FlosiPreferencesState());val lang=LocalFlosiLanguage.current
    val currency=CurrencyConverter.normalizeCode(prefs.currency.ifBlank{"USD"});val lines=remember{mutableStateListOf<DraftInvoiceLine>()}
    var invoiceType by remember{mutableStateOf("sale")}
    var title by remember{mutableStateOf("")};var qty by remember{mutableStateOf("1")};var price by remember{mutableStateOf("")};var discount by remember{mutableStateOf("0")};var taxPercent by remember{mutableStateOf("0")};var paid by remember{mutableStateOf("0")};var paymentAccountId by remember{mutableStateOf<Long?>(null)};var accountMenu by remember{mutableStateOf(false)};var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    val quantity=decimalValue(qty);val unitPrice=price.toLongOrNull();val currentLineTotal=if(quantity!=null&&unitPrice!=null)runCatching{InvoiceMath.lineTotal(quantity,unitPrice)}.getOrNull()else null
    val totals=runCatching{InvoiceMath.totals(lines.map{it.lineTotal},discount.toLongOrNull()?:0L,decimalValue(taxPercent)?:0.0,paid.toLongOrNull()?:0L)}.getOrNull();val needsPaymentAccount=(totals?.paid?:0L)>0L
    val eligiblePaymentAccounts=accounts.filter{CurrencyConverter.normalizeCode(it.currency)==currency}
    val selectedAccount=eligiblePaymentAccounts.firstOrNull{it.id==paymentAccountId}
    LaunchedEffect(currency,accounts){if(paymentAccountId!=null&&eligiblePaymentAccounts.none{it.id==paymentAccountId})paymentAccountId=null}
    fun s(ar:String,en:String)=if(lang=="ar")ar else en

    FlosiPage(s("إنشاء فاتورة","Create invoice"),s("بيع أو شراء ببنود وحسابات دقيقة","Sale or purchase with precise item totals"),onBack){
        CardBox{
            Text(s("نوع الفاتورة","Invoice type"),color=FlosiText)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                FilterChip(invoiceType=="sale",{invoiceType="sale";paymentAccountId=null;error=null},{Text(s("بيع / أستلم","Sale / receive"))})
                FilterChip(invoiceType=="purchase",{invoiceType="purchase";paymentAccountId=null;error=null},{Text(s("شراء / أدفع","Purchase / pay"))})
            }
            Text(s("عملة الفاتورة: $currency","Invoice currency: $currency"),color=FlosiMuted)
            Text(s("تُحفظ العملة والضريبة مع الفاتورة نفسها حتى تبقى الأرقام ثابتة عند تغيير إعدادات التطبيق.","Currency and tax are stored with the invoice so totals remain stable when app settings change."),color=FlosiMuted)
        }
        CardBox{
            Text(s("إضافة بند","Add item"),color=FlosiText)
            OutlinedTextField(title,{title=it;error=null},Modifier.fillMaxWidth(),label={Text(s("البند","Item"))})
            OutlinedTextField(qty,{qty=decimalInput(it);error=null},Modifier.fillMaxWidth(),label={Text(s("الكمية","Quantity"))},supportingText={Text(s("تقبل الكسور مثل 1.5","Decimals such as 1.5 are supported"))})
            OutlinedTextField(price,{price=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(s("سعر الوحدة ($currency)","Unit price ($currency)"))})
            currentLineTotal?.let{Metric(s("إجمالي البند","Line total"),moneyText(it,currency),FlosiPurple)}
            OutlinedButton(onClick={val q=quantity;val p=unitPrice;val lineTotal=currentLineTotal;if(title.isBlank()||q==null||q<=0.0||p==null||p<0L||lineTotal==null)error=s("أكمل بيانات البند بصورة صحيحة","Complete the item correctly")else{lines+=DraftInvoiceLine(System.nanoTime(),title.trim(),q,p,lineTotal);title="";qty="1";price="";error=null}},enabled=title.isNotBlank()&&currentLineTotal!=null,modifier=Modifier.fillMaxWidth()){Text(s("+ إضافة البند للفاتورة","+ Add item to invoice"))}
        }
        if(lines.isNotEmpty())CardBox{SectionTitle(s("بنود الفاتورة","Invoice items"));lines.forEach{line->ActionRow(line.title,"${line.quantity} × ${moneyText(line.unitPrice,currency)}",moneyText(line.lineTotal,currency),FlosiPurple);TextButton(onClick={lines.remove(line)}){Text(s("حذف البند","Delete item"),color=FlosiRed)}}}
        CardBox{
            OutlinedTextField(discount,{discount=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(s("الخصم ($currency)","Discount ($currency)"))})
            OutlinedTextField(taxPercent,{taxPercent=decimalInput(it);error=null},Modifier.fillMaxWidth(),label={Text(s("الضريبة %","Tax %"))})
            OutlinedTextField(paid,{paid=it.filter(Char::isDigit);error=null},Modifier.fillMaxWidth(),label={Text(if(invoiceType=="sale")s("المقبوض الآن ($currency)","Received now ($currency)") else s("المدفوع الآن ($currency)","Paid now ($currency)"))})
            if(totals!=null){Metric(s("المجموع الفرعي","Subtotal"),moneyText(totals.subtotal,currency),FlosiText);if(totals.discount>0L)ActionRow(s("الخصم","Discount"),"",moneyText(totals.discount,currency),FlosiRed);if(totals.taxAmount>0L)ActionRow(s("الضريبة","Tax"),"${taxPercent}%",moneyText(totals.taxAmount,currency),FlosiOrange);Metric(s("الإجمالي النهائي","Grand total"),moneyText(totals.total,currency),FlosiPurple);ActionRow(if(invoiceType=="sale")s("المقبوض","Received") else s("المدفوع","Paid"),"",moneyText(totals.paid,currency),FlosiGreen);ActionRow(s("المتبقي","Remaining"),"",moneyText(totals.remaining,currency),if(totals.remaining>0L)FlosiOrange else FlosiGreen)}
            else if(lines.isNotEmpty())Text(s("راجع الخصم والضريبة والمدفوع: الخصم لا يتجاوز المجموع والمدفوع لا يتجاوز الإجمالي.","Review discount, tax and paid amount: discount cannot exceed subtotal and paid cannot exceed total."),color=FlosiRed)
        }
        if(needsPaymentAccount)CardBox{
            Text(if(invoiceType=="sale")s("حساب استلام الدفعة","Receiving account") else s("حساب دفع الفاتورة","Payment account"),color=FlosiText)
            Text(if(invoiceType=="sale")s("المقبوض الآن سيُسجّل كدخل فعلي ويزيد رصيد حساب بنفس عملة الفاتورة.","The received amount is recorded as real income in an account with the same invoice currency.") else s("المدفوع الآن سيُسجّل كمصروف فعلي من حساب بنفس عملة الفاتورة.","The paid amount is recorded as a real expense from an account with the same invoice currency."),color=FlosiMuted)
            if(eligiblePaymentAccounts.isEmpty()) Text(s("لا يوجد حساب بعملة $currency. أضف حساباً بهذه العملة أو اجعل المدفوع الآن صفراً.","No $currency account exists. Add one or set the immediate payment to zero."),color=FlosiRed)
            else Box{OutlinedButton(onClick={accountMenu=true},modifier=Modifier.fillMaxWidth()){Text(selectedAccount?.let{"${it.name} • ${it.currency}"}?:s("اختر الحساب","Choose account"))};DropdownMenu(accountMenu,{accountMenu=false}){eligiblePaymentAccounts.forEach{account->DropdownMenuItem(text={Text("${account.name} • ${account.currency}")},onClick={paymentAccountId=account.id;accountMenu=false;error=null})}}}
        }
        error?.let{Text(it,color=FlosiRed)}
        Button(onClick={
            val calculated=totals?:return@Button
            if(calculated.paid>0L&&selectedAccount==null){error=if(eligiblePaymentAccounts.isEmpty())s("أنشئ حساباً بعملة $currency أولاً","Create a $currency account first") else if(invoiceType=="sale")s("اختر حساب استلام الدفعة","Choose the receiving account") else s("اختر حساب دفع الفاتورة","Choose the payment account");return@Button}
            val number="${if(invoiceType=="sale")"S" else "P"}-${System.currentTimeMillis()}"
            val invoice=InvoiceEntity(number=number,type=invoiceType,currency=currency,subtotal=calculated.subtotal,discount=calculated.discount,taxPercent=decimalValue(taxPercent)?:0.0,taxAmount=calculated.taxAmount,total=calculated.total,paidAmount=calculated.paid,status=calculated.status)
            val items=lines.map{line->InvoiceItemEntity(invoiceId=0,title=line.title,quantity=line.quantity,unitPrice=line.unitPrice,lineTotal=line.lineTotal)}
            saving=true;error=null
            vm.create(invoice,items,selectedAccount?.id){id,message->saving=false;if(id!=null)onBack()else error=message?:s("تعذر حفظ الفاتورة","Could not save invoice")}
        },enabled=!saving&&lines.isNotEmpty()&&totals!=null&&(!needsPaymentAccount||selectedAccount!=null),modifier=Modifier.fillMaxWidth()){if(saving)CircularProgressIndicator(strokeWidth=2.dp)else Text(flosiText("save"))}
    }
}
