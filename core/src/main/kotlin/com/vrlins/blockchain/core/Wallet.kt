package com.vrlins.blockchain.core

import java.security.PrivateKey
import java.security.PublicKey

data class Wallet(val publicKey: PublicKey, val privateKey: PrivateKey, val blockChain: BlockChain) {

    companion object {
        fun create(blockChain: BlockChain): Wallet {
            val keyPair = generateKeyPair()
            return Wallet(keyPair.public, keyPair.private, blockChain)
        }
    }

    val balance: Int
        get() = blockChain.balanceBy(publicKey)

    private val pendingAmountToSend: Int
        get() = blockChain.getPendingTransactionsBy(publicKey)
            .flatMap { it.outputs }
            .filter { !it.isMine(publicKey) }
            .sumOf { it.amount }

    fun sendFundsTo(recipient: PublicKey, amountToSend: Int): Transaction {
        if (amountToSend + pendingAmountToSend > balance) {
            throw IllegalArgumentException("Insufficient funds")
        }

        val unspentTransactionsOutputs = getUnspentTransactionsOutputs()
        val pendingTransactionsInputs = getPendingTransactions().flatMap { it.inputs }

        val availableUnspentTransactions = unspentTransactionsOutputs.filter { output ->
            pendingTransactionsInputs.none { input -> input.transactionOutput.hash == output.hash }
        }

        if (availableUnspentTransactions.isEmpty()) {
            throw IllegalArgumentException("Pending transactions need to be mined")
        }

        val tx = Transaction()
        tx.outputs.add(TransactionOutput(recipient = recipient, amount = amountToSend))

        var collectedAmount = 0

        for (myTx in availableUnspentTransactions) {
            collectedAmount += myTx.amount
            tx.inputs.add(TransactionInput(myTx, collectedAmount))

            if (collectedAmount > amountToSend) {
                val change = collectedAmount - amountToSend
                tx.outputs.add(
                    TransactionOutput(
                        recipient = publicKey,
                        amount = change,
                    )
                )
            }

            if (collectedAmount >= amountToSend) {
                break
            }
        }
        return tx.sign(privateKey)
    }

    private fun getUnspentTransactionsOutputs(): Collection<TransactionOutput> {
        return blockChain.getUnspentTransactionsOutputsBy(publicKey)
    }

    private fun getPendingTransactions(): Collection<Transaction> {
        return blockChain.getPendingTransactionsBy(publicKey)
    }
}