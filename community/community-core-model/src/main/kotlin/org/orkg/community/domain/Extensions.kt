package org.orkg.community.domain

import io.ipfs.multibase.Base16
import io.ipfs.multihash.Multihash

fun Multihash.toDigestHex(): String = Base16.bytesToHex(hash)
