package org.orkg.world

/**
 * Base class to provide access to environment variables.
 *
 * Implements the "Chain of Responsibility" pattern with downstream delegation,
 * meaning unknown variables will be delegated to the next environment in the chain.
 * Implementations are encouraged to not break the pattern by simply passing `null`.
 */
abstract class Environment(private val next: Environment?) {
    protected abstract fun getValue(variable: String): String?

    operator fun get(variable: String): String? = getValue(variable) ?: next?.get(variable)

    companion object {
        @JvmStatic
        fun controlledWithSystemFallback(initial: Map<String, String?> = emptyMap()): Environment = MapBackedEnvironment(
            initialMap = initial,
            next = SystemEnvironment()
        )
    }
}

/**
 * Simple delegation to the system's environment.
 */
class SystemEnvironment(next: Environment? = null) : Environment(next) {
    override fun getValue(variable: String): String? = System.getenv(variable)
}

/**
 * Access to environment variables backed up by a [Map].
 *
 * When no initial variables are provided, all lookups will fail unless delegated to another environment.
 */
class MapBackedEnvironment(
    initialMap: Map<String, String?> = emptyMap(),
    next: Environment? = null,
) : Environment(next) {
    private val variables: MutableMap<String, String?> = initialMap.toMutableMap()

    override fun getValue(variable: String): String? = variables[variable]
}
