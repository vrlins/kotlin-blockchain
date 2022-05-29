package com.vrlins.blockchain.app.data

import com.vrlins.blockchain.core.Wallet
import com.vrlins.blockchain.core.encodeToString

data class WalletCreatedResponse(
    val publicKey: String,
    val privateKey: String
) {
    companion object {
        fun of(wallet: Wallet): WalletCreatedResponse {
            return WalletCreatedResponse(
                publicKey = wallet.publicKey.encodeToString(),
                privateKey = wallet.privateKey.encodeToString()
            )
        }
    }
}