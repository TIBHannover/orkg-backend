package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label

class LabelValidator<T, S>(
    private val valueSelector: (T) -> String
) : Action<T, S> {
    override fun invoke(command: T, state: S): S =
        state.also { Label.ofOrNull(valueSelector(command)) ?: throw InvalidLabel() }
}
