package com.vrlins.blockchain.app.data

import com.vrlins.blockchain.core.BlockChain

data class BlockChainStateResponse(val isValid: Boolean, val blockHeight: Int) {
    companion object {
        fun of(blockChain: BlockChain): BlockChainStateResponse {
            return BlockChainStateResponse(blockChain.isValid(), blockChain.blocks.size)
        }
    }
}
