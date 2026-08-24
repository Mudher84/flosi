package com.flosi.app.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.flosi.app.data.local.dao.TransactionWithNames
import com.flosi.app.data.local.entity.InvoiceEntity
import com.flosi.app.data.local.entity.InvoiceItemEntity
import com.flosi.app.finance.CurrencyConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object PdfExporter {
    private const val PAGE_W = 595
    private const val PAGE_H = 842

    private fun currencyCode(raw:String):String {
        val code=CurrencyConverter.normalizeCode(raw)
        return if(CurrencyConverter.validCode(code)) code else ""
    }

    private fun money(value:Long,currency:String):String {
        val code=currencyCode(currency)
        return if(code.isBlank()) "%,d".format(Locale.US,abs(value)) else "%,d %s".format(Locale.US,abs(value),code)
    }

    private fun quantity(value:Double):String =
        if(value%1.0==0.0) String.format(Locale.US,"%.0f",value)
        else String.format(Locale.US,"%.3f",value).trimEnd('0').trimEnd('.')

    fun exportTransactions(
        context: Context,
        uri: Uri,
        items: List<TransactionWithNames>,
        title: String = "تقرير فلوسي"
    ) {
        val doc = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val reportDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val rowDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W,PAGE_H,pageNo).create())
        var canvas = page.canvas
        var y = 54f

        fun header() {
            paint.textSize=22f; paint.isFakeBoldText=true
            canvas.drawText("FLOSI",40f,y,paint); y+=28f
            paint.textSize=14f; paint.isFakeBoldText=false
            canvas.drawText(title,40f,y,paint); y+=20f
            paint.textSize=9f
            canvas.drawText(reportDate,40f,y,paint); y+=22f
            canvas.drawText("Date / Title / Amount / Currency / Account",40f,y,paint); y+=18f
        }
        fun newPage() {
            doc.finishPage(page)
            pageNo++
            page=doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W,PAGE_H,pageNo).create())
            canvas=page.canvas; y=54f; header()
        }
        header()
        paint.textSize=9f
        items.forEach { t ->
            if(y>780f) newPage()
            val currency=currencyCode(t.accountCurrency)
            val sign=when(t.kind){
                "income","invoice_payment","debt_received"->"+"
                "expense","debt_given"->"-"
                else->""
            }
            val line="${rowDate.format(Date(t.occurredAt))}  ${t.title.take(28)}  $sign${money(t.amount,currency)}  ${t.accountName.take(18)}"
            canvas.drawText(line,40f,y,paint); y+=16f
            val meta=listOfNotNull(t.categoryName,t.personName).joinToString(" • ")
            if(meta.isNotBlank()){
                paint.textSize=8f
                canvas.drawText(meta.take(75),55f,y,paint); y+=14f
                paint.textSize=9f
            }
        }
        doc.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use(doc::writeTo)
            ?: error("تعذر فتح ملف PDF للحفظ")
        doc.close()
    }

    fun exportInvoice(
        context: Context,
        uri: Uri,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ) {
        val doc=PdfDocument()
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        val currency=currencyCode(invoice.currency)
        var pageNo=1
        var page=doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W,PAGE_H,pageNo).create())
        var c=page.canvas
        var y=65f

        fun header(){
            p.textSize=24f;p.isFakeBoldText=true;c.drawText("FLOSI",40f,y,p);y+=35f
            p.textSize=18f;c.drawText("Invoice #${invoice.number}",40f,y,p);y+=26f
            p.textSize=10f;p.isFakeBoldText=false
            c.drawText("Currency: ${currency.ifBlank{"—"}}   Status: ${invoice.status}",40f,y,p);y+=24f
        }
        fun newPage(){
            doc.finishPage(page)
            pageNo++
            page=doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W,PAGE_H,pageNo).create())
            c=page.canvas;y=65f;header()
        }

        header()
        items.forEach { item ->
            if(y>720f)newPage()
            c.drawText(
                "${item.title.take(34)}   ${quantity(item.quantity)} x ${money(item.unitPrice,currency)} = ${money(item.lineTotal,currency)}",
                40f,y,p
            )
            y+=20f
        }

        if(y>650f)newPage()
        val taxable=(invoice.subtotal-invoice.discount).coerceAtLeast(0L)
        val tax=(invoice.total-taxable).coerceAtLeast(0L)
        val remaining=(invoice.total-invoice.paidAmount).coerceAtLeast(0L)
        y+=16f
        p.textSize=11f;p.isFakeBoldText=false
        c.drawText("Subtotal: ${money(invoice.subtotal,currency)}",40f,y,p);y+=18f
        if(invoice.discount>0L){c.drawText("Discount: ${money(invoice.discount,currency)}",40f,y,p);y+=18f}
        if(tax>0L){c.drawText("Tax: ${money(tax,currency)}",40f,y,p);y+=18f}
        c.drawText("Paid: ${money(invoice.paidAmount,currency)}",40f,y,p);y+=18f
        c.drawText("Remaining: ${money(remaining,currency)}",40f,y,p);y+=22f
        p.textSize=14f;p.isFakeBoldText=true
        c.drawText("Total: ${money(invoice.total,currency)}",40f,y,p)

        doc.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use(doc::writeTo)
            ?: error("تعذر فتح ملف PDF للحفظ")
        doc.close()
    }
}
