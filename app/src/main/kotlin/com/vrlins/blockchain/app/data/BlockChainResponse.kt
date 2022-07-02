package com.vrlins.blockchain.app.data

import com.vrlins.blockchain.core.BlockChain

data class BlockChainResponse(
    val chain: List<BlockResponse>,
    val blockHeight: Int,
    val difficulty: Int
) {

    companion object {
        fun of(blockChain: BlockChain): Any {
            return BlockChainResponse(
                BlockResponse.of(blockChain.blocks),
                blockChain.blocks.size,
                blockChain.difficulty
            )
        }
    }
}