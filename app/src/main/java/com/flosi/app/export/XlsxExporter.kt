package com.flosi.app.export

import android.content.Context
import android.net.Uri
import com.flosi.app.data.local.dao.TransactionWithNames
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object XlsxExporter {
    private fun xmlEscape(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;")
    private fun cell(ref:String,value:String)= """<c r="$ref" t="inlineStr"><is><t>${xmlEscape(value)}</t></is></c>"""

    fun exportTransactions(context:Context,uri:Uri,items:List<TransactionWithNames>) {
        context.contentResolver.openOutputStream(uri)?.use { raw ->
            ZipOutputStream(raw).use { zip ->
                fun entry(name:String,text:String){
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(text.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                entry("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>""")
                entry("_rels/.rels", """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""")
                entry("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>""")
                entry("xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Transactions" sheetId="1" r:id="rId1"/></sheets>
</workbook>""")
                val rows=buildString {
                    val headers=listOf("ID","Date","Kind","Title","Amount","Account","Person","Category","Note")
                    append("<row r=\"1\">")
                    headers.forEachIndexed{i,h->append(cell("${('A'.code+i).toChar()}1",h))}
                    append("</row>")
                    items.forEachIndexed{index,t->
                        val r=index+2
                        val vals=listOf(t.id.toString(),t.occurredAt.toString(),t.kind,t.title,t.amount.toString(),t.accountName,t.personName.orEmpty(),t.categoryName.orEmpty(),t.note)
                        append("<row r=\"$r\">")
                        vals.forEachIndexed{i,v->append(cell("${('A'.code+i).toChar()}$r",v))}
                        append("</row>")
                    }
                }
                entry("xl/worksheets/sheet1.xml", """<?xml version="1.0" encoding="UTF-8"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>$rows</sheetData></worksheet>""")
            }
        }
    }
}
