package com.flosi.app.banking

import org.junit.Assert.assertEquals
import org.junit.Test

class BankConnectorTest {
    @Test
    fun zainCashIsWalletConnector() {
        assertEquals("zaincash", ZainCashConnector.id)
        assertEquals("ZainCash", ZainCashConnector.displayName)
        assertEquals(BankConnector.Kind.WALLET, ZainCashConnector.kind)
    }
}
