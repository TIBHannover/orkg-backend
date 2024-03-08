package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.TemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.templates.properties.TemplatePropertyAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class TemplatePropertyValueValidator(
    predicateRepository: PredicateRepository,
    classRepository: ClassRepository
) : TemplatePropertyValidator(predicateRepository, classRepository), TemplatePropertyAction {
    override fun invoke(command: CreateTemplatePropertyCommand, state: State): State =
        state.apply { validate(command) }
}
