package com.vrlins.blockchain.app

import com.vrlins.blockchain.core.BlockChain
import com.vrlins.blockchain.core.GenesisConfiguration
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val (blockChain, wallet) =
        BlockChain.create(GenesisConfiguration(difficulty = BlockChain.DEFAULT_DIFFICULTY, initialAllocation = 1000))

    embeddedServer(
        Netty,
        port = 8080,
    ) {
        mainModule(blockChain, wallet)
    }.start(wait = true)
}