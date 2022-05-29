package com.vrlins.blockchain.core

import java.security.PublicKey

class BlockChain(difficulty: Int) {
    private val targetPrefix = "0".repeat(difficulty)
    private val transactionPool = mutableListOf<Transaction>()
    private val unspentTransactionOutput: MutableMap<String, TransactionOutput> = mutableMapOf()

    private val _blocks = mutableListOf<Block>()
    val blocks: MutableList<Block>
        get() = _blocks

    fun addTransaction(transaction: Transaction): Boolean {
        if (transaction.isSignatureValid()) {
            return transactionPool.add(transaction)
        }

        return false
    }

    fun mineBlock(): Block =
        updateUnspentTransactionsOutputs(
            Block(previousHash = getPreviousBlockHash(), transactionPool.toMutableList())
                .mine(targetPrefix)
        ).also {
            transactionPool.clear()
            blocks.add(it)
        }

    fun isValid(): Boolean {
        if (blocks.size == 1) {
            return blocks.first().isValid(targetPrefix)
        }

        for (i in 1 until blocks.size) {
            val previousBlock = blocks[i - 1]
            val currentBlock = blocks[i]

            if (
                previousBlock.hash != currentBlock.previousHash ||
                !previousBlock.isValid(targetPrefix) ||
                !currentBlock.isValid(targetPrefix)
            ) {
                return false
            }
        }

        return true
    }

    fun balanceBy(publicKey: PublicKey): Int {
        return getTransactionsBy(publicKey).sumOf { it.amount }
    }

    fun getTransactionsBy(publicKey: PublicKey): Collection<TransactionOutput> {
        return unspentTransactionOutput.filterValues { it.isMine(publicKey) }.values
    }

    private fun updateUnspentTransactionsOutputs(block: Block): Block {
        block.transactions.flatMap { it.inputs }.map { it.transactionOutput.hash }
            .forEach { unspentTransactionOutput.remove(it) }
        unspentTransactionOutput.putAll(block.transactions.flatMap { it.outputs }.associateBy { it.hash })
        return block
    }

    private fun getPreviousBlockHash() = blocks.lastOrNull()?.hash ?: PREVIOUS_HASH_FOR_GENESIS_BLOCK

    companion object {
        private const val PREVIOUS_HASH_FOR_GENESIS_BLOCK = "0"
        const val DEFAULT_DIFFICULTY = 4

        fun create(genesisData: GenesisConfiguration): Pair<BlockChain, Wallet> {
            val blockChain = BlockChain(genesisData.difficulty)

            val wallet = Wallet.create(blockChain)

            val tx1 = Transaction()
            tx1.outputs.add(
                TransactionOutput(
                    recipient = wallet.publicKey,
                    amount = genesisData.initialAllocation,
                )
            )
            tx1.sign(wallet.privateKey)

            blockChain.addTransaction(tx1)
            blockChain.mineBlock()

            return Pair(blockChain, wallet)
        }
    }

}