package org.orkg.contenttypes.domain.actions.papers

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.actions.Action

class PaperModifiableValidator<T, S>(
    private val modifiableSelector: (S) -> Boolean?,
    private val idSelector: (T) -> ThingId,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        if (modifiableSelector(state) == false) {
            throw PaperNotModifiable(idSelector(command))
        }
        return state
    }
}
