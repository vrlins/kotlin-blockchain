package com.vrlins.blockchain.core

import com.google.common.collect.ImmutableList
import java.security.PublicKey

class BlockChain(val difficulty: Int) {
    private val targetPrefix = "0".repeat(difficulty)
    private val unspentTransactionOutput: MutableMap<String, TransactionOutput> = mutableMapOf()

    private val _transactionPool = mutableListOf<Transaction>()
    val transactionPool: List<Transaction>
        get() = ImmutableList.copyOf(_transactionPool)

    private val _blocks = mutableListOf<Block>()
    val blocks: List<Block>
        get() = ImmutableList.copyOf(_blocks)

    fun addTransaction(transaction: Transaction): Boolean {
        if (transaction.isSignatureValid() && isUnspent(transaction) && !isDoubleTransaction(transaction)) {
            return _transactionPool.add(transaction)
        }

        return false
    }

    fun mineBlock(): Block =
        updateUnspentTransactionsOutputs(
            Block(previousHash = getPreviousBlockHash(), ImmutableList.copyOf(_transactionPool))
                .mine(targetPrefix)
        ).also {
            _transactionPool.clear()
            _blocks.add(it)
        }

    fun isValid(): Boolean {
        if (_blocks.size == 1) {
            return _blocks.first().isValid(targetPrefix)
        }

        for (i in 1 until _blocks.size) {
            val previousBlock = _blocks[i - 1]
            val currentBlock = _blocks[i]

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
        return getUnspentTransactionsOutputsBy(publicKey).sumOf { it.amount }
    }

    fun getUnspentTransactionsOutputsBy(publicKey: PublicKey): Collection<TransactionOutput> {
        return unspentTransactionOutput.filterValues { it.isMine(publicKey) }.values
    }

    fun getPendingTransactionsBy(publicKey: PublicKey): Collection<Transaction> {
        return transactionPool.filter { it.isMine(publicKey) }
    }

    private fun isDoubleTransaction(transaction: Transaction): Boolean {
        val outputsHashToBeSpent = transactionPool.flatMap { it.inputs }.map { it.transactionOutput.hash }
        val outputsHashToBeAdded = transaction.inputs.map { it.transactionOutput.hash }
        return outputsHashToBeSpent.intersect(outputsHashToBeAdded).isNotEmpty()
    }

    private fun isUnspent(transaction: Transaction) = blocks.isEmpty() ||
            transaction.inputs.map { it.transactionOutput }
                .intersect(unspentTransactionOutput.values).isNotEmpty()

    private fun updateUnspentTransactionsOutputs(block: Block): Block {
        block.transactions.flatMap { it.inputs }.map { it.transactionOutput.hash }
            .forEach { unspentTransactionOutput.remove(it) }
        unspentTransactionOutput.putAll(block.transactions.flatMap { it.outputs }.associateBy { it.hash })
        return block
    }

    private fun getPreviousBlockHash() = _blocks.lastOrNull()?.hash ?: PREVIOUS_HASH_FOR_GENESIS_BLOCK

    companion object {
        private const val PREVIOUS_HASH_FOR_GENESIS_BLOCK = "0"
        const val DEFAULT_DIFFICULTY = 4

        fun create(genesisData: GenesisConfiguration): Pair<BlockChain, Wallet> {
            val blockChain = BlockChain(genesisData.difficulty)

            val wallet = Wallet.create(blockChain)

            val tx = Transaction()
            tx.outputs.add(
                TransactionOutput(
                    recipient = wallet.publicKey,
                    amount = genesisData.initialAllocation,
                )
            )
            tx.sign(wallet.privateKey)

            blockChain.addTransaction(tx)
            blockChain.mineBlock()

            return Pair(blockChain, wallet)
        }
    }

}