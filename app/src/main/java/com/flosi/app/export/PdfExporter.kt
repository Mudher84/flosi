package com.flosi.app.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.flosi.app.data.local.dao.TransactionWithNames
import com.flosi.app.data.local.entity.InvoiceEntity
import com.flosi.app.data.local.entity.InvoiceItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    private const val PAGE_W = 595
    private const val PAGE_H = 842

    fun exportTransactions(
        context: Context,
        uri: Uri,
        items: List<TransactionWithNames>,
        title: String = "تقرير فلوسي"
    ) {
        val doc = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
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
            canvas.drawText(date,40f,y,paint); y+=24f
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
            if(y>790f) newPage()
            val line="${t.id}  ${t.title.take(38)}  ${t.amount} IQD  ${t.accountName}"
            canvas.drawText(line,40f,y,paint); y+=16f
            val meta=listOfNotNull(t.categoryName,t.personName).joinToString(" • ")
            if(meta.isNotBlank()){ paint.textSize=8f; canvas.drawText(meta.take(70),55f,y,paint); y+=14f; paint.textSize=9f }
        }
        doc.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use(doc::writeTo)
        doc.close()
    }

    fun exportInvoice(
        context: Context,
        uri: Uri,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ) {
        val doc=PdfDocument()
        val page=doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W,PAGE_H,1).create())
        val c=page.canvas
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        var y=65f
        p.textSize=24f;p.isFakeBoldText=true;c.drawText("FLOSI",40f,y,p);y+=35f
        p.textSize=18f;c.drawText("Invoice #${invoice.number}",40f,y,p);y+=28f
        p.textSize=10f;p.isFakeBoldText=false
        c.drawText("Status: ${invoice.status}",40f,y,p);y+=25f
        items.forEach {
            c.drawText("${it.title.take(35)}   ${it.quantity} x ${it.unitPrice} = ${it.lineTotal} IQD",40f,y,p)
            y+=20f
        }
        y+=15f;p.textSize=13f;p.isFakeBoldText=true
        c.drawText("Total: ${invoice.total} IQD",40f,y,p)
        doc.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use(doc::writeTo)
        doc.close()
    }
}
