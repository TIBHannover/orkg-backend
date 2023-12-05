package org.orkg.contenttypes.domain.actions.template

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.TemplatePropertyValidator
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository

class TemplatePropertiesValidator(
    predicateRepository: PredicateRepository,
    classRepository: ClassRepository
) : TemplatePropertyValidator(predicateRepository, classRepository), TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State =
        state.apply { command.properties.forEach(::validate) }
}
