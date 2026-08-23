package com.flosi.app.finance

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Stores manual exchange rates as FROM|TO|RATE and converts monetary Long values
 * without silently treating different currencies as the same unit.
 */
object CurrencyConverter {
    private const val SEP = "|"
    private val CODE=Regex("[A-Z]{3}")

    data class Rate(
        val from: String,
        val to: String,
        val value: BigDecimal
    )

    fun normalizeCode(code: String): String = code.trim().uppercase()
    fun validCode(code:String):Boolean=normalizeCode(code).matches(CODE)

    fun encodeRate(from: String, to: String, rawRate: String): String? {
        val f = normalizeCode(from)
        val t = normalizeCode(to)
        if (!validCode(f) || !validCode(t) || f == t) return null
        val rate = rawRate.trim().replace(',', '.').toBigDecimalOrNull() ?: return null
        if (rate <= BigDecimal.ZERO || rate.precision()>30 || rate.scale()>18) return null
        return listOf(f, t, rate.stripTrailingZeros().toPlainString()).joinToString(SEP)
    }

    fun parseRate(entry: String): Rate? {
        val parts = entry.split(SEP)
        if (parts.size != 3) return null
        val from = normalizeCode(parts[0])
        val to = normalizeCode(parts[1])
        val value = parts[2].toBigDecimalOrNull() ?: return null
        if (!validCode(from) || !validCode(to) || from == to || value <= BigDecimal.ZERO || value.precision()>30 || value.scale()>18) return null
        return Rate(from, to, value)
    }

    fun rates(entries: Set<String>): List<Rate> = entries.mapNotNull(::parseRate).sortedWith(compareBy<Rate>{it.from}.thenBy{it.to}.thenBy{it.value})

    fun convert(amount: Long, from: String, to: String, entries: Set<String>): Long? {
        val source = normalizeCode(from)
        val target = normalizeCode(to)
        if(!validCode(source)||!validCode(target))return null
        if (source == target) return amount

        val available = rates(entries)
        val direct = findRate(source, target, available) ?: findTwoHopRate(source, target, available)
        return direct?.let { applyRate(amount, it) }
    }

    private fun findRate(from: String, to: String, rates: List<Rate>): BigDecimal? {
        rates.firstOrNull { it.from == from && it.to == to }?.let { return it.value }
        rates.firstOrNull { it.from == to && it.to == from }?.let {
            return runCatching{BigDecimal.ONE.divide(it.value, 18, RoundingMode.HALF_EVEN)}.getOrNull()
        }
        return null
    }

    private fun findTwoHopRate(from: String, to: String, rates: List<Rate>): BigDecimal? {
        val currencies = rates.asSequence().flatMap { sequenceOf(it.from, it.to) }.filter{it!=from&&it!=to}.toSortedSet()
        currencies.forEach { via ->
            val first = findRate(from, via, rates) ?: return@forEach
            val second = findRate(via, to, rates) ?: return@forEach
            return first.multiply(second)
        }
        return null
    }

    private fun applyRate(amount: Long, rate: BigDecimal): Long? = runCatching {
        BigDecimal.valueOf(amount)
            .multiply(rate)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrNull()
}
