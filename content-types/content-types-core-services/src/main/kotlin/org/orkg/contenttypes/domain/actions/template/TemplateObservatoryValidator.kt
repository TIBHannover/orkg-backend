package org.orkg.contenttypes.domain.actions.template

import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State

class TemplateObservatoryValidator(
    observatoryRepository: ObservatoryRepository
) : ObservatoryValidator(observatoryRepository), TemplateAction {
    override operator fun invoke(command: CreateTemplateCommand, state: State): State =
        state.apply { validate(command.observatories) }
}
