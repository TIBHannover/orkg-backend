package eu.tib.orkg.prototype.profiling.spi

import kotlin.random.Random
import kotlin.reflect.KType

interface ValueGenerator<T : Any> {
    operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<T>
}
