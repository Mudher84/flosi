package com.flosi.app.export

import android.content.Context
import android.net.Uri
import com.flosi.app.data.local.dao.TransactionWithNames
import java.io.OutputStreamWriter

object CsvExporter {
    fun exportTransactions(context:Context,uri:Uri,items:List<TransactionWithNames>){
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream,Charsets.UTF_8).use { w ->
                w.appendLine("id,date,kind,title,amount,account,person,category,note")
                items.forEach { t ->
                    w.appendLine(
                        listOf(
                            t.id,t.occurredAt,t.kind,t.title,t.amount,t.accountName,
                            t.personName.orEmpty(),t.categoryName.orEmpty(),t.note
                        ).joinToString(","){ "\"${it.toString().replace("\"","\"\"")}\"" }
                    )
                }
            }
        }
    }
}
