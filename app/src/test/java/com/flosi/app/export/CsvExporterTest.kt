package com.flosi.app.export

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvExporterTest {
    @Test fun dangerousSpreadsheetPrefixesAreNeutralized(){
        assertEquals("'=1+1",CsvExporter.safeSpreadsheetText("=1+1"))
        assertEquals("'+SUM(A1:A2)",CsvExporter.safeSpreadsheetText("+SUM(A1:A2)"))
        assertEquals("'-10",CsvExporter.safeSpreadsheetText("-10"))
        assertEquals("'@cmd",CsvExporter.safeSpreadsheetText("@cmd"))
        assertEquals("normal",CsvExporter.safeSpreadsheetText("normal"))
    }
}
