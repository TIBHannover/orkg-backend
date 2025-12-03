package org.orkg.common

import java.math.BigInteger
import java.security.MessageDigest

fun String.toSnakeCase(): String =
    if (isEmpty()) {
        this
    } else {
        buildString {
            this@toSnakeCase.forEachIndexed { index, c ->
                when {
                    c.isUpperCase() -> {
                        if (index > 0) {
                            append("_")
                        }
                        append(c.lowercase())
                    }
                    else -> append(c)
                }
            }
        }
    }

fun String.toCamelCase(): String =
    if (isEmpty()) {
        this
    } else {
        buildString {
            var nextUpper = false
            this@toCamelCase.forEach { c ->
                when {
                    c == '_' -> nextUpper = true
                    nextUpper -> {
                        append(c.uppercase())
                        nextUpper = false
                    }
                    else -> append(c)
                }
            }
        }
    }

/**
 * Calculate the MD5 of a string.
 *
 * @return The MD5 in hexadecimal, zero-prefixed to 32 characters.
 */
val String.md5: String
    get() = BigInteger(1, md5digest).toString(16).padStart(32, '0')

val String.md5digest: ByteArray
    get() = MessageDigest.getInstance("MD5").digest(toByteArray())

/**
 * Calculate the SHA256 of a string.
 *
 * @return The SHA256 in hexadecimal
 */
val String.sha256: String
    get() = sha256digest.fold(StringBuilder(), { acc, b -> acc.append("%02x".format(b)) }).toString()

val String.sha256digest: ByteArray
    get() = MessageDigest.getInstance("SHA-256").digest(toByteArray())
