package org.orkg.contenttypes.domain

import org.springframework.stereotype.Component
import kotlin.random.Random

private const val ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"

@Component
class SnapshotIdGenerator(
    private val random: Random = Random.Default,
) {
    fun nextIdentity(): SnapshotId =
        SnapshotId(randomString(8))

    private fun randomString(length: Int): String =
        IntRange(1, length)
            .map { ALPHABET[random.nextInt(ALPHABET.length)] }
            .toCharArray()
            .concatToString()
}
