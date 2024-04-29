package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.TempIdValidator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState

class TemplateInstanceTempIdValidator(
    private val tempIdValidator: TempIdValidator = TempIdValidator()
) : UpdateTemplateInstanceAction {
    override operator fun invoke(
        command: UpdateTemplateInstanceCommand,
        state: UpdateTemplateInstanceState
    ): UpdateTemplateInstanceState {
        val ids = tempIdValidator.run { command.tempIds() }
        if (ids.isNotEmpty()) {
            tempIdValidator.validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }
}
