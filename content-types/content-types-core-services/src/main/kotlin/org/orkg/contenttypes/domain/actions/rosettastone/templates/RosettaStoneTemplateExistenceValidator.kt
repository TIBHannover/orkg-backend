package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.RosettaStoneTemplateService
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class RosettaStoneTemplateExistenceValidator(
    private val rosettaStoneTemplateService: RosettaStoneTemplateService,
    private val resourceRepository: ResourceRepository,
) : UpdateRosettaStoneTemplateAction {
    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        val resource = resourceRepository.findById(command.templateId)
            .filter { Classes.rosettaNodeShape in it.classes }
            .orElseThrow { RosettaStoneTemplateNotFound(command.templateId) }
        val subgraph = rosettaStoneTemplateService.findSubgraph(resource)
        val rosettaStoneTemplate = RosettaStoneTemplate.from(resource, subgraph.statements)
        return state.copy(rosettaStoneTemplate = rosettaStoneTemplate, statements = subgraph.statements)
    }
}
