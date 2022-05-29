package com.vrlins.blockchain.app

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.vrlins.blockchain.app.data.*
import com.vrlins.blockchain.core.BlockChain
import com.vrlins.blockchain.core.Wallet
import com.vrlins.blockchain.core.toPublicKey
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.mainModule(blockChain: BlockChain, wallet: Wallet) {
    install(ContentNegotiation) {
        jackson {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }
    }

    routing {
        get("blockchain/blocks") {
            call.respond(
                HttpStatusCode.OK,
                BlockChainResponse.of(blockChain)
            )
        }
        get("blockchain/blocks/{index}") {
            val index = call.parameters["index"]?.toIntOrNull()

            if (index == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid block index"
                )
                return@get
            }

            if (index >= blockChain.blocks.size) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                BlockResponse.of(blockChain.blocks[index])
            )
        }
        post("blockchain/blocks") {
            val block = blockChain.mineBlock()
            call.respond(
                HttpStatusCode.Created,
                BlockResponse.of(block)
            )
        }
        get("blockchain/status") {
            call.respond(
                HttpStatusCode.OK,
                BlockChainStateResponse.of(blockChain)
            )
        }
        get("my_wallet") {
            call.respond(
                HttpStatusCode.OK,
                WalletBasicInfoResponse.of(wallet)
            )
        }
        post("my_wallet/transactions") {
            val values = call.receive<Map<String, Any>>()

            val required = arrayOf("recipient", "amount")
            if (!required.all { values.containsKey(it) }) {
                call.respond(HttpStatusCode.BadRequest, "Missing Values")
                return@post
            }

            val amount = values["amount"] as Int
            val recipient = values["recipient"] as String

            try {
                val tx = wallet.sendFundsTo(recipient = recipient.toPublicKey(), amountToSend = amount)
                val accepted = blockChain.addTransaction(tx)

                if (accepted) {
                    call.respond(
                        HttpStatusCode.Accepted,
                        TransactionResponse.of(tx)
                    )
                } else {
                    call.respond(HttpStatusCode.NotAcceptable)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        post("wallets") {
            val newWallet = Wallet.create(blockChain)
            call.respond(
                HttpStatusCode.Created,
                WalletCreatedResponse.of(newWallet)
            )
        }
    }
}