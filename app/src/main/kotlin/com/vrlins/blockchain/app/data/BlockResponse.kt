package com.vrlins.blockchain.app.data

import com.vrlins.blockchain.core.Block

data class BlockResponse(
    val previousHash: String,
    val timestamp: Long,
    val nonce: Long,
    val transactions: List<TransactionResponse>,
    val hash: String,
) {
    companion object {
        fun of(block: Block): BlockResponse {
            return BlockResponse(
                previousHash = block.previousHash,
                timestamp = block.timestamp,
                nonce = block.nonce,
                transactions = TransactionResponse.of(block.transactions),
                hash = block.hash,
            )
        }

        fun of(blocks: List<Block>): List<BlockResponse> {
            return blocks.map { of(it) }
        }
    }
}