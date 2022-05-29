package com.vrlins.blockchain.app.data

import com.vrlins.blockchain.core.TransactionInput
import com.vrlins.blockchain.core.encodeToString

data class TransactionInputResponse(
    val transactionOutputHash: String,
    val sender: String,
    val amount: Int
) {
    companion object {
        private fun of(input: TransactionInput): TransactionInputResponse {
            return TransactionInputResponse(
                transactionOutputHash = input.transactionOutput.hash,
                sender = input.transactionOutput.recipient.encodeToString(),
                amount = input.amount
            )
        }

        fun of(inputs: List<TransactionInput>): List<TransactionInputResponse> {
            return inputs.map { of(it) }
        }
    }
}
