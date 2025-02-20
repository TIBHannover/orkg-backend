package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.contenttypes.output.RosettaStoneStatementRepository

class RosettaStoneTemplateModifiableValidator(
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository,
) : UpdateRosettaStoneTemplateAction {
    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        if (!state.rosettaStoneTemplate!!.modifiable) {
            throw RosettaStoneTemplateNotModifiable(command.templateId)
        }
        val statements = rosettaStoneStatementRepository.findAll(
            templateId = command.templateId,
            pageable = PageRequests.SINGLE
        )
        return state.copy(isUsedInRosettaStoneStatement = statements.totalElements > 0)
    }
}
