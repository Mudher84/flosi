package com.flosi.app.banking

/**
 * Contract for future official bank/wallet integrations.
 * Credentials must never be embedded in the Android app; production connectors
 * receive short-lived server-issued sessions/tokens from the Flosi backend.
 */
interface BankConnector {
    val id: String
    val displayName: String
    val kind: Kind

    enum class Kind { BANK, WALLET }
}

object ZainCashConnector : BankConnector {
    override val id: String = "zaincash"
    override val displayName: String = "ZainCash"
    override val kind: BankConnector.Kind = BankConnector.Kind.WALLET
}
