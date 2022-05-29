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

    fun sendFundsTo(recipient: PublicKey, amountToSend: Int): Transaction {
        if (amountToSend > balance) {
            throw IllegalArgumentException("Insufficient funds")
        }

        val tx = Transaction()
        tx.outputs.add(TransactionOutput(recipient = recipient, amount = amountToSend))

        var collectedAmount = 0

        for (myTx in getMyTransactions()) {
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

    private fun getMyTransactions(): Collection<TransactionOutput> {
        return blockChain.getTransactionsBy(publicKey)
    }

}