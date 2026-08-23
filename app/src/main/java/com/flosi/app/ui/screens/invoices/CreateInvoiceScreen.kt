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
private fun decimalInput(raw: String): String = raw.filter { it.isDigit() || it == '.' || it == ',' }

@Composable
fun CreateInvoiceScreen(onBack:()->Unit){
    val vm:InvoicesViewModel=flosiViewModel()
    val preferences=rememberFlosiPreferences()
    val prefs by preferences.state.collectAsState(initial=FlosiPreferencesState())
    val currency=prefs.currency

    val lines=remember{mutableStateListOf<DraftInvoiceLine>()}
    var title by remember{mutableStateOf("")}
    var qty by remember{mutableStateOf("1")}
    var price by remember{mutableStateOf("")}
    var discount by remember{mutableStateOf("0")}
    var taxPercent by remember{mutableStateOf("0")}
    var paid by remember{mutableStateOf("0")}
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

    FlosiPage("إنشاء فاتورة","بنود متعددة وحسابات دقيقة",onBack){
        CardBox{
            Text("إضافة بند",color=FlosiText)
            OutlinedTextField(
                value=title,
                onValueChange={title=it;error=null},
                modifier=Modifier.fillMaxWidth(),
                label={Text("البند")}
            )
            OutlinedTextField(
                value=qty,
                onValueChange={qty=decimalInput(it);error=null},
                modifier=Modifier.fillMaxWidth(),
                label={Text("الكمية")},
                supportingText={Text("تقبل الكسور مثل 1.5")}
            )
            OutlinedTextField(
                value=price,
                onValueChange={price=it.filter(Char::isDigit);error=null},
                modifier=Modifier.fillMaxWidth(),
                label={Text("سعر الوحدة ($currency)")}
            )
            currentLineTotal?.let{Metric("إجمالي البند",moneyText(it,currency),FlosiPurple)}
            OutlinedButton(
                onClick={
                    val q=quantity
                    val p=unitPrice
                    val lineTotal=currentLineTotal
                    if(title.isBlank()||q==null||q<=0.0||p==null||p<0L||lineTotal==null){
                        error="أكمل بيانات البند بصورة صحيحة"
                    }else{
                        lines += DraftInvoiceLine(System.nanoTime(),title.trim(),q,p,lineTotal)
                        title="";qty="1";price="";error=null
                    }
                },
                enabled=title.isNotBlank()&&currentLineTotal!=null,
                modifier=Modifier.fillMaxWidth()
            ){
                Text("+ إضافة البند للفاتورة")
            }
        }

        if(lines.isNotEmpty()){
            CardBox{
                SectionTitle("بنود الفاتورة")
                lines.forEach{line->
                    ActionRow(
                        title=line.title,
                        subtitle="${line.quantity} × ${moneyText(line.unitPrice,currency)}",
                        value=moneyText(line.lineTotal,currency),
                        accent=FlosiPurple
                    )
                    TextButton(onClick={lines.remove(line)}){Text("حذف البند",color=FlosiRed)}
                }
            }
        }

        CardBox{
            OutlinedTextField(
                value=discount,
                onValueChange={discount=it.filter(Char::isDigit);error=null},
                modifier=Modifier.fillMaxWidth(),
                label={Text("الخصم ($currency)")}
            )
            OutlinedTextField(
                value=taxPercent,
                onValueChange={taxPercent=decimalInput(it);error=null},
                modifier=Modifier.fillMaxWidth(),
                label={Text("الضريبة %")}
            )
            OutlinedTextField(
                value=paid,
                onValueChange={paid=it.filter(Char::isDigit);error=null},
                modifier=Modifier.fillMaxWidth(),
                label={Text("المدفوع الآن ($currency)")}
            )

            if(totals!=null){
                Metric("المجموع الفرعي",moneyText(totals.subtotal,currency),FlosiText)
                if(totals.discount>0L)ActionRow("الخصم","",moneyText(totals.discount,currency),FlosiRed)
                if(totals.taxAmount>0L)ActionRow("الضريبة","${taxPercent}%",moneyText(totals.taxAmount,currency),FlosiOrange)
                Metric("الإجمالي النهائي",moneyText(totals.total,currency),FlosiPurple)
                ActionRow("المدفوع","",moneyText(totals.paid,currency),FlosiGreen)
                ActionRow("المتبقي","",moneyText(totals.remaining,currency),if(totals.remaining>0L)FlosiOrange else FlosiGreen)
            }else if(lines.isNotEmpty()){
                Text("راجع الخصم والضريبة والمدفوع: الخصم لا يتجاوز المجموع والمدفوع لا يتجاوز الإجمالي.",color=FlosiRed)
            }
            error?.let{Text(it,color=FlosiRed)}
        }

        Button(
            onClick={
                val calculated=totals ?: return@Button
                val number="F-${System.currentTimeMillis().toString().takeLast(6)}"
                val invoice=InvoiceEntity(
                    number=number,
                    subtotal=calculated.subtotal,
                    discount=calculated.discount,
                    total=calculated.total,
                    paidAmount=calculated.paid,
                    status=calculated.status
                )
                val items=lines.map{line->
                    InvoiceItemEntity(
                        invoiceId=0,
                        title=line.title,
                        quantity=line.quantity,
                        unitPrice=line.unitPrice,
                        lineTotal=line.lineTotal
                    )
                }
                vm.create(invoice,items){onBack()}
            },
            enabled=lines.isNotEmpty()&&totals!=null,
            modifier=Modifier.fillMaxWidth()
        ){
            Text("حفظ الفاتورة")
        }
    }
}
