package org.orkg.contenttypes.domain.actions

interface Action<T, S> {
    operator fun invoke(command: T, state: S): S
}
