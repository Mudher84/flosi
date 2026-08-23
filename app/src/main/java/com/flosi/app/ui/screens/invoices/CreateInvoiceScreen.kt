package com.flosi.app.ui.screens.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flosi.app.data.local.entity.InvoiceEntity
import com.flosi.app.data.local.entity.InvoiceItemEntity
import com.flosi.app.finance.InvoiceMath
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.InvoicesViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences

private data class DraftInvoiceLine(
    val id: Long,
    val title: String,
    val quantity: Double,
    val unitPrice: Long,
    val lineTotal: Long
)

private fun decimalValue(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()
private fun decimalInput(raw: String): String {
    val out=StringBuilder()
    var separator=false
    raw.forEach{ch->
        when{
            ch.isDigit()->out.append(ch)
            (ch=='.'||ch==',')&&!separator->{
                if(out.isEmpty())out.append('0')
                out.append('.')
                separator=true
            }
        }
    }
    return out.toString()
}

@Composable
fun CreateInvoiceScreen(onBack:()->Unit){
    val vm:InvoicesViewModel=flosiViewModel()
    val accounts by vm.accounts.collectAsState()
    val preferences=rememberFlosiPreferences()
    val prefs by preferences.state.collectAsState(initial=FlosiPreferencesState())
    val currency=prefs.currency.trim().uppercase().ifBlank{"IQD"}

    val lines=remember{mutableStateListOf<DraftInvoiceLine>()}
    var title by remember{mutableStateOf("")}
    var qty by remember{mutableStateOf("1")}
    var price by remember{mutableStateOf("")}
    var discount by remember{mutableStateOf("0")}
    var taxPercent by remember{mutableStateOf("0")}
    var paid by remember{mutableStateOf("0")}
    var paymentAccountId by remember{mutableStateOf<Long?>(null)}
    var accountMenu by remember{mutableStateOf(false)}
    var saving by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf<String?>(null)}

    val quantity=decimalValue(qty)
    val unitPrice=price.toLongOrNull()
    val currentLineTotal=if(quantity!=null&&unitPrice!=null) runCatching{
        InvoiceMath.lineTotal(quantity,unitPrice)
    }.getOrNull() else null

    val totals=runCatching{
        InvoiceMath.totals(
            lineTotals=lines.map{it.lineTotal},
            discount=discount.toLongOrNull()?:0L,
            taxPercent=decimalValue(taxPercent)?:0.0,
            paid=paid.toLongOrNull()?:0L
        )
    }.getOrNull()
    val needsPaymentAccount=(totals?.paid?:0L)>0L
    val selectedAccount=accounts.firstOrNull{it.id==paymentAccountId}

    FlosiPage("إنشاء فاتورة","بنود متعددة وحسابات دقيقة",onBack){
        CardBox{
            Text("عملة الفاتورة: $currency",color=FlosiMuted)
            Text("تُحفظ العملة والضريبة مع الفاتورة نفسها حتى تبقى الأرقام ثابتة عند تغيير إعدادات التطبيق.",color=FlosiMuted)
        }
        CardBox{
            Text("إضافة بند",color=FlosiText)
            OutlinedTextField(value=title,onValueChange={title=it;error=null},modifier=Modifier.fillMaxWidth(),label={Text("البند")})
            OutlinedTextField(value=qty,onValueChange={qty=decimalInput(it);error=null},modifier=Modifier.fillMaxWidth(),label={Text("الكمية")},supportingText={Text("تقبل الكسور مثل 1.5")})
            OutlinedTextField(value=price,onValueChange={price=it.filter(Char::isDigit);error=null},modifier=Modifier.fillMaxWidth(),label={Text("سعر الوحدة ($currency)")})
            currentLineTotal?.let{Metric("إجمالي البند",moneyText(it,currency),FlosiPurple)}
            OutlinedButton(
                onClick={
                    val q=quantity;val p=unitPrice;val lineTotal=currentLineTotal
                    if(title.isBlank()||q==null||q<=0.0||p==null||p<0L||lineTotal==null) error="أكمل بيانات البند بصورة صحيحة"
                    else { lines += DraftInvoiceLine(System.nanoTime(),title.trim(),q,p,lineTotal);title="";qty="1";price="";error=null }
                },
                enabled=title.isNotBlank()&&currentLineTotal!=null,
                modifier=Modifier.fillMaxWidth()
            ){Text("+ إضافة البند للفاتورة")}
        }

        if(lines.isNotEmpty()) CardBox{
            SectionTitle("بنود الفاتورة")
            lines.forEach{line->
                ActionRow(line.title,"${line.quantity} × ${moneyText(line.unitPrice,currency)}",moneyText(line.lineTotal,currency),FlosiPurple)
                TextButton(onClick={lines.remove(line)}){Text("حذف البند",color=FlosiRed)}
            }
        }

        CardBox{
            OutlinedTextField(value=discount,onValueChange={discount=it.filter(Char::isDigit);error=null},modifier=Modifier.fillMaxWidth(),label={Text("الخصم ($currency)")})
            OutlinedTextField(value=taxPercent,onValueChange={taxPercent=decimalInput(it);error=null},modifier=Modifier.fillMaxWidth(),label={Text("الضريبة %")})
            OutlinedTextField(value=paid,onValueChange={paid=it.filter(Char::isDigit);error=null},modifier=Modifier.fillMaxWidth(),label={Text("المدفوع الآن ($currency)")})

            if(totals!=null){
                Metric("المجموع الفرعي",moneyText(totals.subtotal,currency),FlosiText)
                if(totals.discount>0L)ActionRow("الخصم","",moneyText(totals.discount,currency),FlosiRed)
                if(totals.taxAmount>0L)ActionRow("الضريبة","${taxPercent}%",moneyText(totals.taxAmount,currency),FlosiOrange)
                Metric("الإجمالي النهائي",moneyText(totals.total,currency),FlosiPurple)
                ActionRow("المدفوع","",moneyText(totals.paid,currency),FlosiGreen)
                ActionRow("المتبقي","",moneyText(totals.remaining,currency),if(totals.remaining>0L)FlosiOrange else FlosiGreen)
            }else if(lines.isNotEmpty()) Text("راجع الخصم والضريبة والمدفوع: الخصم لا يتجاوز المجموع والمدفوع لا يتجاوز الإجمالي.",color=FlosiRed)
        }

        if(needsPaymentAccount) CardBox{
            Text("حساب استلام الدفعة",color=FlosiText)
            Text("المدفوع الآن سيُسجّل كحركة مالية فعلية ويزيد رصيد هذا الحساب.",color=FlosiMuted)
            Box{
                OutlinedButton(onClick={accountMenu=true},modifier=Modifier.fillMaxWidth()){
                    Text(selectedAccount?.let{"${it.name} • ${it.currency}"}?:"اختر الحساب")
                }
                DropdownMenu(expanded=accountMenu,onDismissRequest={accountMenu=false}){
                    accounts.forEach{account->
                        DropdownMenuItem(
                            text={Text("${account.name} • ${account.currency}")},
                            onClick={paymentAccountId=account.id;accountMenu=false;error=null}
                        )
                    }
                }
            }
        }

        error?.let{Text(it,color=FlosiRed)}
        Button(
            onClick={
                val calculated=totals ?: return@Button
                if(calculated.paid>0L&&paymentAccountId==null){error="اختر حساب استلام الدفعة";return@Button}
                val number="F-${System.currentTimeMillis().toString().takeLast(6)}"
                val invoice=InvoiceEntity(
                    number=number,
                    currency=currency,
                    subtotal=calculated.subtotal,
                    discount=calculated.discount,
                    taxPercent=decimalValue(taxPercent)?:0.0,
                    taxAmount=calculated.taxAmount,
                    total=calculated.total,
                    paidAmount=calculated.paid,
                    status=calculated.status
                )
                val items=lines.map{line->InvoiceItemEntity(invoiceId=0,title=line.title,quantity=line.quantity,unitPrice=line.unitPrice,lineTotal=line.lineTotal)}
                saving=true;error=null
                vm.create(invoice,items,paymentAccountId){id,message->
                    saving=false
                    if(id!=null)onBack() else error=message?:"تعذر حفظ الفاتورة"
                }
            },
            enabled=!saving&&lines.isNotEmpty()&&totals!=null&&(!needsPaymentAccount||paymentAccountId!=null),
            modifier=Modifier.fillMaxWidth()
        ){
            if(saving)CircularProgressIndicator(strokeWidth=2.dp) else Text("حفظ الفاتورة")
        }
    }
}
