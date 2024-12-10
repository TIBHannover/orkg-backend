package org.orkg.common

import java.math.BigInteger
import java.security.MessageDigest

fun String.toSnakeCase(): String =
    if (this.isEmpty()) this else StringBuilder().also {
        this.forEach { c ->
            when (c) {
                in 'A'..'Z' -> {
                    it.append("_")
                    it.append(c.lowercase())
                }

                else -> {
                    it.append(c)
                }
            }
        }
    }.toString()

/**
 * Calculate the MD5 of a string.
 *
 * @return The MD5 in hexadecimal, zero-prefixed to 32 characters.
 */
val String.md5: String
    get() = BigInteger(1, MessageDigest.getInstance("MD5").digest(this.toByteArray()))
        .toString(16)
        .padStart(32, '0')
