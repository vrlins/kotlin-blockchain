package com.vrlins.blockchain.core

import java.time.Instant

data class Block(
    val previousHash: String,
    val transactions: List<Transaction> = listOf(),
    val timestamp: Long = Instant.now().toEpochMilli(),
    var nonce: Long = 0
) {
    val hash: String
        get() = "$previousHash${transactions.hash()}$timestamp$nonce".hash()

    fun isValid(targetPrefix: String): Boolean {
        return hash.startsWith(targetPrefix) && transactions.all { it.isSignatureValid() }
    }

    fun mine(targetPrefix: String): Block {
        var minedBlock = copy()
        while (!minedBlock.isValid(targetPrefix)) {
            minedBlock = minedBlock.copy(nonce = minedBlock.nonce + 1)
        }

        return minedBlock
    }
}