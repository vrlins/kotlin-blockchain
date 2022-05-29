package com.vrlins.blockchain.core

import java.security.PrivateKey

class TransactionInput(
    val transactionOutput: TransactionOutput,
    val amount: Int,
) {
    private var signature: ByteArray = ByteArray(0)

    val hash: String
        get() = "${transactionOutput.hash}${amount}".hash()

    fun sign(privateKey: PrivateKey): TransactionInput {
        signature = hash.sign(privateKey)
        return this
    }

    fun isSignatureValid(): Boolean {
        return hash.verifySignature(
            transactionOutput.recipient,
            signature
        )
    }
}

fun List<TransactionInput>.hash(): String {
    return joinToString { it.hash }
}