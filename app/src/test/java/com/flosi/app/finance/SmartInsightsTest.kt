package com.flosi.app.finance

import org.junit.Assert.assertTrue
import org.junit.Test

class SmartInsightsTest {
    @Test fun `warns when spending exceeds income`() {
        val result = SmartInsights.build(1000, 1200, 500, 25, 0, 0, 0, 0, "USD", 20)
        assertTrue(result.any { it.key == "overspending" && it.severity == InsightSeverity.CRITICAL })
    }

    @Test fun `flags overdue commitments first`() {
        val result = SmartInsights.build(2000, 500, 1000, 50, 2, 300, 0, 0, "USD", 20)
        assertTrue(result.first().key == "overdue")
    }

    @Test fun `detects pressure when upcoming dues exceed safe zone`() {
        val result = SmartInsights.build(2000, 700, 400, 20, 0, 0, 2, 600, "USD", 20)
        assertTrue(result.any { it.key == "upcoming_pressure" })
    }

    @Test fun `recognizes healthy spending pace`() {
        val result = SmartInsights.build(2000, 700, 1000, 50, 0, 0, 0, 0, "USD", 20)
        assertTrue(result.any { it.key == "healthy_pace" && it.severity == InsightSeverity.POSITIVE })
    }
}
