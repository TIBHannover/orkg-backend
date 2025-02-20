package org.orkg.contenttypes.domain.actions

import dev.forkhandles.values.ofOrNull
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label

class LabelCollectionValidator<T, S>(
    private val property: String,
    private val valueSelector: (T) -> Collection<String>?,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S =
        state.also { valueSelector(command)?.forEach { Label.ofOrNull(it) ?: throw InvalidLabel(property) } }
}
