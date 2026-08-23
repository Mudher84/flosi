package com.flosi.app.finance

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Deterministic invoice calculations. Quantities may be fractional while money
 * remains stored as Long units, matching the rest of the Flosi ledger.
 */
object InvoiceMath {
    data class Totals(
        val subtotal: Long,
        val discount: Long,
        val taxable: Long,
        val taxAmount: Long,
        val total: Long,
        val paid: Long,
        val remaining: Long,
        val status: String
    )

    fun lineTotal(quantity: Double, unitPrice: Long): Long {
        require(quantity.isFinite() && quantity > 0.0) { "الكمية يجب أن تكون أكبر من صفر" }
        require(unitPrice >= 0L) { "سعر الوحدة لا يمكن أن يكون سالباً" }
        return BigDecimal.valueOf(quantity)
            .multiply(BigDecimal.valueOf(unitPrice))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }

    fun totals(
        lineTotals: Iterable<Long>,
        discount: Long = 0L,
        taxPercent: Double = 0.0,
        paid: Long = 0L
    ): Totals {
        require(discount >= 0L) { "الخصم لا يمكن أن يكون سالباً" }
        require(taxPercent.isFinite() && taxPercent >= 0.0) { "نسبة الضريبة غير صالحة" }
        require(paid >= 0L) { "المبلغ المدفوع لا يمكن أن يكون سالباً" }

        val subtotal = lineTotals.fold(0L) { acc, value ->
            require(value >= 0L) { "إجمالي البند لا يمكن أن يكون سالباً" }
            Math.addExact(acc, value)
        }
        require(discount <= subtotal) { "الخصم أكبر من المجموع الفرعي" }

        val taxable = subtotal - discount
        val taxAmount = BigDecimal.valueOf(taxable)
            .multiply(BigDecimal.valueOf(taxPercent))
            .divide(BigDecimal.valueOf(100L), 0, RoundingMode.HALF_UP)
            .longValueExact()
        val total = Math.addExact(taxable, taxAmount)
        require(paid <= total) { "المدفوع أكبر من إجمالي الفاتورة" }
        val remaining = total - paid
        val status = when {
            remaining == 0L -> "paid"
            paid > 0L -> "partial"
            else -> "unpaid"
        }
        return Totals(subtotal, discount, taxable, taxAmount, total, paid, remaining, status)
    }
}
