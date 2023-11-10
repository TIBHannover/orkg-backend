package eu.tib.orkg.prototype.contenttypes.services.actions

interface Action<T, S> {
    operator fun invoke(command: T, state: S): S
}
