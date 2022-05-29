package com.vrlins.blockchain.app.data

import com.vrlins.blockchain.core.Transaction

data class TransactionResponse(
    val hash: String,
    val amount: Int,
    val timestamp: Long,
    val inputs: List<TransactionInputResponse>,
    val outputs: List<TransactionOutputResponse>
) {
    companion object {
        fun of(transaction: Transaction): TransactionResponse {
            return TransactionResponse(
                hash = transaction.hash,
                inputs = TransactionInputResponse.of(transaction.inputs),
                outputs = TransactionOutputResponse.of(transaction.outputs),
                amount = transaction.amount,
                timestamp = transaction.timestamp
            )
        }

        fun of(transactions: List<Transaction>): List<TransactionResponse> {
            return transactions.map { of(it) }
        }
    }
}