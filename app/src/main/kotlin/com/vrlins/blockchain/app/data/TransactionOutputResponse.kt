package com.vrlins.blockchain.app.data

import com.vrlins.blockchain.core.TransactionOutput
import com.vrlins.blockchain.core.encodeToString

data class TransactionOutputResponse(
    val hash: String,
    val recipient: String,
    val amount: Int,
) {
    companion object {
        private fun of(transactionOutput: TransactionOutput): TransactionOutputResponse {
            return TransactionOutputResponse(
                hash = transactionOutput.hash,
                recipient = transactionOutput.recipient.encodeToString(),
                amount = transactionOutput.amount,
            )
        }

        fun of(outputs: List<TransactionOutput>): List<TransactionOutputResponse> {
            return outputs.map { of(it) }
        }
    }
}