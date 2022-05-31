package com.vrlins.blockchain.core

import java.security.PublicKey

data class TransactionOutput(
    val recipient: PublicKey,
    val amount: Int,
) {
    val hash: String
        get() = "${recipient.encodeToString()}$amount".hash()

    fun isMine(me: PublicKey): Boolean {
        return recipient == me
    }
}

fun List<TransactionOutput>.hash(): String {
    return joinToString { it.hash }.hash()
}