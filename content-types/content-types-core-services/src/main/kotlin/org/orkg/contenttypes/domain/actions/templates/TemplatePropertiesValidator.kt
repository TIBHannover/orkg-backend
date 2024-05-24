package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class TemplatePropertiesValidator<T, S>(
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator,
    private val newValueSelector: (T) -> List<TemplatePropertyDefinition>?,
    private val oldValueSelector: (S) -> List<TemplateProperty>
) : Action<T, S> {
    constructor(
        predicateRepository: PredicateRepository,
        classRepository: ClassRepository,
        newValueSelector: (T) -> List<TemplatePropertyDefinition>?,
        oldValueSelector: (S) -> List<TemplateProperty> = { emptyList() }
    ) : this(
        AbstractTemplatePropertyValidator(predicateRepository, classRepository),
        newValueSelector,
        oldValueSelector
    )

    override fun invoke(command: T, state: S): S {
        val newProperties = newValueSelector(command)
        if (newProperties != null) {
            val oldProperties = oldValueSelector(state)
            newProperties.filter { newProperty ->
                oldProperties.none { oldProperty ->
                    newProperty.matchesProperty(oldProperty)
                }
            }.forEach(abstractTemplatePropertyValidator::validate)
        }
        return state
    }
}
