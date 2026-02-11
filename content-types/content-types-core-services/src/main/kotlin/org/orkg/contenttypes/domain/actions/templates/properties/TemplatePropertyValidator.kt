package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.DuplicateTemplatePropertyPaths
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.input.TemplatePropertyCommand
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class TemplatePropertyValidator<T, S>(
    private val abstractTemplatePropertyValidator: AbstractTemplatePropertyValidator,
    private val existingTemplatePropertiesSelector: (S) -> List<TemplateProperty>,
    private val newValueSelector: (T) -> TemplatePropertyCommand,
    private val oldValueSelector: (S) -> TemplateProperty?,
) : Action<T, S> {
    constructor(
        predicateRepository: PredicateRepository,
        classRepository: ClassRepository,
        existingTemplatePropertiesSelector: (S) -> List<TemplateProperty>,
        newValueSelector: (T) -> TemplatePropertyCommand,
        oldValueSelector: (S) -> TemplateProperty? = { null },
    ) : this(
        AbstractTemplatePropertyValidator(predicateRepository, classRepository),
        existingTemplatePropertiesSelector,
        newValueSelector,
        oldValueSelector,
    )

    override fun invoke(command: T, state: S): S {
        val oldProperty = oldValueSelector(state)
        val newProperty = newValueSelector(command)
        if ((oldProperty == null || newProperty.path != oldProperty.path.id)) {
            val duplicateProperty = existingTemplatePropertiesSelector(state).find { it.path.id == newProperty.path }
            if (duplicateProperty != null) {
                throw DuplicateTemplatePropertyPaths(mapOf(duplicateProperty.path.id to 2))
            }
        }
        if (oldProperty == null || !newProperty.matchesProperty(oldProperty)) {
            abstractTemplatePropertyValidator.validate(newProperty)
        }
        return state
    }
}
