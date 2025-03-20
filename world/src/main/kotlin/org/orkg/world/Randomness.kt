package org.orkg.world

import kotlin.random.Random

interface Randomness {
    val rng: Random
}

class SystemRandomness : Randomness {
    override val rng: Random = Random.Default
}

class ControlledRandomness(seed: Long = DEFAULT_SEED) : Randomness {
    override val rng: Random = Random(seed)

    companion object {
        const val DEFAULT_SEED: Long = 0xc0ffee_ba771e
    }
}
