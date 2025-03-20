package org.orkg.world

/**
 * Simple abstraction over different parts of the outside world, which need to be controlled.
 */
// TODO: make constructor private?
class World(
    val environment: Environment,
    val randomness: Randomness,
) {
    companion object {
        @JvmStatic
        fun ofSystemState(): World = World(
            environment = SystemEnvironment(),
            randomness = SystemRandomness(),
        )

        @JvmStatic
        fun forTesting(): World = World(
            environment = MapBackedEnvironment(),
            randomness = ControlledRandomness(),
        )

        /**
         * Generates a world of controlled components with system fallbacks, but stable behavior.
         *
         * For example, the random number generator is deterministic due to the use of a seed,
         * but the seed can be changed through the environment during creation.
         * This setting is useful in cases where you need control,
         * but also need information passed from the environment,
         * for example, in integration or acceptance tests.
         */
        @JvmStatic
        fun controlledSystem(seedVariable: String = "RANDOM_SEED"): World {
            val environment = Environment.controlledWithSystemFallback()
            val seed = environment[seedVariable]?.toLong() ?: ControlledRandomness.DEFAULT_SEED
            return World(environment, ControlledRandomness(seed))
        }
    }
}
