package com.vrlins.blockchain.core

import java.security.PrivateKey
import java.time.Instant

data class Transaction(
    val timestamp: Long = Instant.now().toEpochMilli(),
    val inputs: MutableList<TransactionInput> = mutableListOf(),
    val outputs: MutableList<TransactionOutput> = mutableListOf()
) {
    val hash: String
        get() = "${inputs.hash()}${outputs.hash()}$amount$timestamp".hash()

    val amount: Int
        get() = outputs.sumOf { it.amount }

    fun sign(privateKey: PrivateKey): Transaction {
        inputs.forEach {
            it.sign(privateKey)
        }
        return this
    }

    fun isSignatureValid(): Boolean {
        return inputs.all { it.isSignatureValid() }
    }
}

fun List<Transaction>.hash(): String {
    return joinToString { it.hash }.hash()
}