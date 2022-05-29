package com.vrlins.blockchain.app.data

import com.vrlins.blockchain.core.Wallet
import com.vrlins.blockchain.core.encodeToString

data class WalletBasicInfoResponse(
    val balance: Int,
    val publicKey: String
) {
    companion object {
        fun of(wallet: Wallet): WalletBasicInfoResponse {
            return WalletBasicInfoResponse(
                balance = wallet.balance,
                publicKey = wallet.publicKey.encodeToString(),
            )
        }
    }
}