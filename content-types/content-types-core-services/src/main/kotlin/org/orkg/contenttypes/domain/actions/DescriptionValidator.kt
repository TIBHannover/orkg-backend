package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.graph.domain.Description
import org.orkg.graph.domain.InvalidDescription

class DescriptionValidator<T, S>(
    private val property: String = "description",
    private val valueSelector: (T) -> String?,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        valueSelector(command)?.also {
            Description.ofOrNull(it) ?: throw InvalidDescription(property)
        }
        return state
    }
}
