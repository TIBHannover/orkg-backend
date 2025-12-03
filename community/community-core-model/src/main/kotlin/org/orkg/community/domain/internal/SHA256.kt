package org.orkg.community.domain.internal

import io.ipfs.multihash.Multihash
import io.ipfs.multihash.Multihash.Type
import org.orkg.common.sha256digest

object SHA256 {
    val ZERO: Multihash = Multihash(Type.sha2_256, ByteArray(32) { 0 })

    fun fromEmail(email: String): Multihash = Multihash(Type.sha2_256, email.trim().lowercase().sha256digest)
}
