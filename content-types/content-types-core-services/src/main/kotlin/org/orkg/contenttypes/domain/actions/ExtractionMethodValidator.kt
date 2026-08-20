package org.orkg.contenttypes.domain.actions

import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidExtractionMethodChange

class ExtractionMethodValidator<T, S>(
    private val newValueSelector: (T) -> ExtractionMethod?,
    private val oldValueSelector: (S) -> ExtractionMethod,
    private val newLabelSelector: (T) -> String?,
    private val oldLabelSelector: (S) -> String,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newLabel = newLabelSelector(command)
        if (newLabel == null || oldLabelSelector(state) == newLabel) {
            val newValue = newValueSelector(command)
            if (newValue != null) {
                val oldValue = oldValueSelector(state)
                if (newValue != oldValue && !oldValue.canBeChangedTo(newValue)) {
                    throw InvalidExtractionMethodChange(oldValue, newValue)
                }
            }
        }
        return state
    }
}
