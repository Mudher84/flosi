package com.flosi.app.export

import android.content.Context
import android.net.Uri
import com.flosi.app.data.local.dao.TransactionWithNames
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object XlsxExporter {
    private fun xmlEscape(s:String)=s
        .replace("&","&amp;")
        .replace("<","&lt;")
        .replace(">","&gt;")
        .replace("\"","&quot;")

    private fun textCell(ref:String,value:String)=
        """<c r="$ref" t="inlineStr"><is><t>${xmlEscape(value)}</t></is></c>"""

    private fun numberCell(ref:String,value:Number,style:Int?=null):String {
        val styleAttr=style?.let{" s=\"$it\""}.orEmpty()
        val numeric=when(value){
            is Float,is Double->String.format(Locale.US,"%.10f",value.toDouble()).trimEnd('0').trimEnd('.')
            else->value.toLong().toString()
        }
        return "<c r=\"$ref\"$styleAttr><v>$numeric</v></c>"
    }

    private fun excelSerial(epochMillis:Long):Double =
        epochMillis.toDouble()/86_400_000.0 + 25_569.0

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
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>""")

                entry("_rels/.rels", """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""")

                entry("xl/_rels/workbook.xml.rels", """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>""")

                entry("xl/workbook.xml", """<?xml version="1.0" encoding="UTF-8"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Transactions" sheetId="1" r:id="rId1"/></sheets>
</workbook>""")

                entry("xl/styles.xml", """<?xml version="1.0" encoding="UTF-8"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="1"><numFmt numFmtId="164" formatCode="yyyy-mm-dd hh:mm"/></numFmts>
<fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
<fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="2">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
</cellXfs>
<cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
</styleSheet>""")

                val rows=buildString {
                    val headers=listOf("ID","Date","Kind","Title","Amount","Currency","Account","Person","Category","Note")
                    append("<row r=\"1\">")
                    headers.forEachIndexed{i,h->append(textCell("${('A'.code+i).toChar()}1",h))}
                    append("</row>")

                    items.forEachIndexed{index,t->
                        val r=index+2
                        append("<row r=\"$r\">")
                        append(numberCell("A$r",t.id))
                        append(numberCell("B$r",excelSerial(t.occurredAt),1))
                        append(textCell("C$r",t.kind))
                        append(textCell("D$r",t.title))
                        append(numberCell("E$r",t.amount))
                        append(textCell("F$r",t.accountCurrency.trim().uppercase().ifBlank{"IQD"}))
                        append(textCell("G$r",t.accountName))
                        append(textCell("H$r",t.personName.orEmpty()))
                        append(textCell("I$r",t.categoryName.orEmpty()))
                        append(textCell("J$r",t.note))
                        append("</row>")
                    }
                }

                entry("xl/worksheets/sheet1.xml", """<?xml version="1.0" encoding="UTF-8"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<cols><col min="1" max="1" width="10" customWidth="1"/><col min="2" max="2" width="20" customWidth="1"/><col min="3" max="10" width="18" customWidth="1"/></cols>
<sheetData>$rows</sheetData>
</worksheet>""")
            }
        } ?: error("تعذر فتح ملف Excel للحفظ")
    }
}
