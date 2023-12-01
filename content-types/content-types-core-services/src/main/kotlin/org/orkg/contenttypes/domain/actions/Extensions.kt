package org.orkg.contenttypes.domain.actions

internal val String.isTempId: Boolean get() = startsWith('#') || startsWith('^')

internal fun <T, S> List<Action<T, S>>.execute(command: T, initialState: S) =
    fold(initialState) { state, executor -> executor(command, state) }