package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class TemplatePropertiesValidator<T, S>(
    predicateRepository: PredicateRepository,
    classRepository: ClassRepository,
    private val valueSelector: (T) -> List<TemplatePropertyDefinition>?
) : AbstractTemplatePropertyValidator(predicateRepository, classRepository), Action<T, S> {
    override fun invoke(command: T, state: S): S =
        state.apply { valueSelector(command)?.forEach(::validate) }
}
