package com.vrlins.blockchain.core

import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

const val DEFAULT_SIGNATURE_ALGORITHM = "SHA256withECDSA"
const val DEFAULT_HASH_ALGORITHM = "SHA-256"

const val ELLIPTIC_CURVE_KEY_GEN_ALGORITHM = "EC"
const val EC_STANDARD_CURVE_NAME = "secp256r1"

fun String.hash(algorithm: String = DEFAULT_HASH_ALGORITHM): String {
    val messageDigest = MessageDigest.getInstance(algorithm)
    messageDigest.update(toByteArray())
    return String.format("%064x", BigInteger(1, messageDigest.digest()))
}

fun generateKeyPair(keyAlgorithm: String = ELLIPTIC_CURVE_KEY_GEN_ALGORITHM): KeyPair {
    val keyGen = KeyPairGenerator.getInstance(keyAlgorithm)
    keyGen.initialize(ECGenParameterSpec(EC_STANDARD_CURVE_NAME), SecureRandom())
    return keyGen.generateKeyPair()
}

fun Key.encodeToString(): String {
    return Base64.getEncoder().encodeToString(encoded)
}

fun String.sign(privateKey: PrivateKey, signatureAlgorithm: String = DEFAULT_SIGNATURE_ALGORITHM): ByteArray {
    val rsa = Signature.getInstance(signatureAlgorithm)
    rsa.initSign(privateKey)
    rsa.update(toByteArray())
    return rsa.sign()
}

fun String.verifySignature(
    publicKey: PublicKey,
    signature: ByteArray,
    signatureAlgorithm: String = DEFAULT_SIGNATURE_ALGORITHM
): Boolean {
    val signatureInstance = Signature.getInstance(signatureAlgorithm)
    signatureInstance.initVerify(publicKey)
    signatureInstance.update(toByteArray())
    return signatureInstance.verify(signature)
}

fun String.toPublicKey(algorithm: String = ELLIPTIC_CURVE_KEY_GEN_ALGORITHM): PublicKey {
    val publicBytes: ByteArray = Base64.getDecoder().decode(this)
    val keySpec = X509EncodedKeySpec(publicBytes)
    val keyFactory = KeyFactory.getInstance(algorithm)

    return keyFactory.generatePublic(keySpec)
}