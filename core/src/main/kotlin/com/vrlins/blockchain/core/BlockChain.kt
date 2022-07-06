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
        return _blocks.isValid(targetPrefix)
    }

    fun replaceChain(newBlocks: List<Block>): Boolean {
        if (newBlocks.isValid(targetPrefix) && newBlocks.size > blocks.size) {
            _blocks.clear()
            _blocks.addAll(newBlocks.toMutableList())

            unspentTransactionOutput.clear()
            unspentTransactionOutput.putAll(getAllUTXOs())

            _transactionPool.clear()

            return true
        }

        return false
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
        return outputsHashToBeSpent.intersect(outputsHashToBeAdded.toSet()).isNotEmpty()
    }

    private fun isUnspent(transaction: Transaction) = blocks.isEmpty() ||
            transaction.inputs.map { it.transactionOutput }
                .intersect(unspentTransactionOutput.values.toSet()).isNotEmpty()

    private fun updateUnspentTransactionsOutputs(block: Block): Block {
        block.transactions.flatMap { it.inputs }.map { it.transactionOutput.hash }
            .forEach { unspentTransactionOutput.remove(it) }
        unspentTransactionOutput.putAll(block.transactions.flatMap { it.outputs }.associateBy { it.hash })
        return block
    }

    private fun getPreviousBlockHash() = _blocks.lastOrNull()?.hash ?: PREVIOUS_HASH_FOR_GENESIS_BLOCK

    private fun getAllUTXOs(): MutableMap<String, TransactionOutput> {
        val allSpentTXOs: MutableMap<String, TransactionOutput> = this.getAllSpentTXOs()
        val allUTXOs: MutableMap<String, TransactionOutput> = mutableMapOf()

        for (block: Block in blocks) {
            for (transaction: Transaction in block.transactions) {
                for (txOutput: TransactionOutput in transaction.outputs) {
                    if (!allSpentTXOs.containsKey(txOutput.hash)) {
                        allUTXOs[txOutput.hash] = txOutput
                    }
                }
            }
        }
        return allUTXOs
    }

    private fun getAllSpentTXOs(): MutableMap<String, TransactionOutput> {
        val spentTXOs: MutableMap<String, TransactionOutput> = mutableMapOf()
        for (block: Block in blocks) {
            for (transaction: Transaction in block.transactions) {
                for (txInput: TransactionInput in transaction.inputs) {
                    spentTXOs[txInput.transactionOutput.hash] = txInput.transactionOutput
                }
            }
        }
        return spentTXOs
    }


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