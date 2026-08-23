package com.flosi.app.export

import android.content.Context
import android.net.Uri
import com.flosi.app.data.local.dao.TransactionWithNames
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CsvExporter {
    internal fun safeSpreadsheetText(value:Any?):String {
        val raw=value?.toString().orEmpty()
        val trimmed=raw.trimStart()
        return if(trimmed.firstOrNull() in setOf('=','+','-','@','\t','\r')) "'$raw" else raw
    }

    private fun csv(value:Any?):String =
        "\"${safeSpreadsheetText(value).replace("\"","\"\"")}\""

    fun exportTransactions(context:Context,uri:Uri,items:List<TransactionWithNames>){
        val iso=SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
            timeZone=TimeZone.getDefault()
        }
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream,Charsets.UTF_8).use { w ->
                w.write('\uFEFF'.code)
                w.appendLine("id,date,kind,title,amount,currency,account,person,category,note")
                items.forEach { t ->
                    w.appendLine(
                        listOf(
                            csv(t.id),
                            csv(iso.format(Date(t.occurredAt))),
                            csv(t.kind),
                            csv(t.title),
                            t.amount.toString(),
                            csv(t.accountCurrency.trim().uppercase().ifBlank{"IQD"}),
                            csv(t.accountName),
                            csv(t.personName.orEmpty()),
                            csv(t.categoryName.orEmpty()),
                            csv(t.note)
                        ).joinToString(",")
                    )
                }
            }
        } ?: error("تعذر فتح ملف CSV للحفظ")
    }
}
