package eu.tib.orkg.prototype.core

/**
 * [Id]s are value types that can be used to identify an [Entity] by
 * means of wrapping a non-nullable type. They can be compared to each
 * other.
 */
interface Id<T : Any>

/**
 * Entities define objects that are identifiable by an [Id].
 *
 * Entities can identify themselves by returning their [Id] to the
 * caller. Implementing classes should overwrite the [Any.equals] and
 * [Any.hashCode] methods in a way so that only [Id]s are compared.
 */
interface Entity<T : Id<*>> {
    /**
     * Identify the object by returning it's [Id].
     *
     * @return the [Id] of the object.
     */
    fun identify(): T
}
