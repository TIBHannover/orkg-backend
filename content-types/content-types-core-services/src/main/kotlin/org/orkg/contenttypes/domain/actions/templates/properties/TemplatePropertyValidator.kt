package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.input.TemplatePropertyCommand
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class TemplatePropertyValidator<T, S>(
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator,
    private val newValueSelector: (T) -> TemplatePropertyCommand,
    private val oldValueSelector: (S) -> TemplateProperty?,
) : Action<T, S> {
    constructor(
        predicateRepository: PredicateRepository,
        classRepository: ClassRepository,
        newValueSelector: (T) -> TemplatePropertyCommand,
        oldValueSelector: (S) -> TemplateProperty? = { null },
    ) : this(
        AbstractTemplatePropertyValidator(predicateRepository, classRepository),
        newValueSelector,
        oldValueSelector
    )

    override fun invoke(command: T, state: S): S {
        val oldProperty = oldValueSelector(state)
        val newProperty = newValueSelector(command)
        if (oldProperty == null || !newProperty.matchesProperty(oldProperty)) {
            abstractTemplatePropertyValidator.validate(newProperty)
        }
        return state
    }
}
