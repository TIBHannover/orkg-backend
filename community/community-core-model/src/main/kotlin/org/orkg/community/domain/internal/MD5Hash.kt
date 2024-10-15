package org.orkg.community.domain.internal

import org.orkg.common.isValidHexBinary
import org.orkg.common.md5

@JvmInline
value class MD5Hash(val value: String) {
    init {
        require(value.length == 32) { "Hash must have 32 characters." }
        require(value.isValidHexBinary()) { "Hash must be a hexadecimal string." }
    }

    companion object {
        val ZERO: MD5Hash = MD5Hash("0".repeat(32))

        fun fromEmail(email: String): MD5Hash = MD5Hash(email.trim().lowercase().md5)
    }
}
