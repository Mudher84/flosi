package com.flosi.app.export

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class XlsxExporterTest {
    @Test fun excelSerialPreservesLocalWallClock(){
        val epoch=0L
        assertEquals(25_569.0,XlsxExporter.excelSerial(epoch,TimeZone.getTimeZone("UTC")),0.0000001)
        assertEquals(25_569.125,XlsxExporter.excelSerial(epoch,TimeZone.getTimeZone("GMT+03:00")),0.0000001)
    }
}
