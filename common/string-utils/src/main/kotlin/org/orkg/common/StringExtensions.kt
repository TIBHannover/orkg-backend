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

/**
 * Calculate the MD5 of a string.
 *
 * @return The MD5 in hexadecimal, zero-prefixed to 32 characters.
 */
val String.md5: String
    get() = BigInteger(1, MessageDigest.getInstance("MD5").digest(this.toByteArray()))
        .toString(16)
        .padStart(32, '0')
