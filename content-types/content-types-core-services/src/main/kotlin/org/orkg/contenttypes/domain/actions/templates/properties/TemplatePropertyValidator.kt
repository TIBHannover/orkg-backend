package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class TemplatePropertyValidator<T, S>(
    predicateRepository: PredicateRepository,
    classRepository: ClassRepository,
    private val valueSelector: (T) -> TemplatePropertyDefinition
) : AbstractTemplatePropertyValidator(predicateRepository, classRepository), Action<T, S> {
    override fun invoke(command: T, state: S): S =
        state.apply { validate(valueSelector(command)) }
}
