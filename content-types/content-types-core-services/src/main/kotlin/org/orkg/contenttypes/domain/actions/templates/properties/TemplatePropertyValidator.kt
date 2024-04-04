package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class TemplatePropertyValidator<T, S>(
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator,
    private val valueSelector: (T) -> TemplatePropertyDefinition
) : Action<T, S> {
    constructor(
        predicateRepository: PredicateRepository,
        classRepository: ClassRepository,
        valueSelector: (T) -> TemplatePropertyDefinition
    ) : this(AbstractTemplatePropertyValidator(predicateRepository, classRepository), valueSelector)

    override fun invoke(command: T, state: S): S =
        state.apply { abstractTemplatePropertyValidator.validate(valueSelector(command)) }
}
