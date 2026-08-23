package com.flosi.app.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class InvoiceMathTest {
    @Test
    fun fractionalQuantityRoundsToMoneyUnit() {
        assertEquals(1500L, InvoiceMath.lineTotal(1.5, 1000L))
        assertEquals(833L, InvoiceMath.lineTotal(2.5, 333L))
    }

    @Test
    fun discountTaxPaidAndRemainingAreConsistent() {
        val result=InvoiceMath.totals(
            lineTotals=listOf(2000L,1500L),
            discount=500L,
            taxPercent=10.0,
            paid=1000L
        )
        assertEquals(3500L,result.subtotal)
        assertEquals(3000L,result.taxable)
        assertEquals(300L,result.taxAmount)
        assertEquals(3300L,result.total)
        assertEquals(1000L,result.paid)
        assertEquals(2300L,result.remaining)
        assertEquals("partial",result.status)
    }

    @Test
    fun paidInvoiceHasZeroRemaining() {
        val result=InvoiceMath.totals(listOf(1000L),paid=1000L)
        assertEquals(0L,result.remaining)
        assertEquals("paid",result.status)
    }

    @Test
    fun invalidDiscountAndOverpaymentAreRejected() {
        assertThrows(IllegalArgumentException::class.java){
            InvoiceMath.totals(listOf(1000L),discount=1001L)
        }
        assertThrows(IllegalArgumentException::class.java){
            InvoiceMath.totals(listOf(1000L),paid=1001L)
        }
    }
}
