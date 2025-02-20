package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.AbstractTemplatePropertiesUpdater
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.UpdateRosettaStoneTemplateAction.State
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class RosettaStoneTemplatePropertiesUpdater(
    private val abstractRosettaStoneTemplatePropertiesUpdater: AbstractTemplatePropertiesUpdater,
) : UpdateRosettaStoneTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        AbstractTemplatePropertiesUpdater(
            literalService,
            resourceService,
            unsafeResourceUseCases,
            statementService,
            unsafeStatementUseCases
        )
    )

    override fun invoke(command: UpdateRosettaStoneTemplateCommand, state: State): State {
        command.properties?.let { properties ->
            abstractRosettaStoneTemplatePropertiesUpdater.update(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                newProperties = properties,
                oldProperties = state.rosettaStoneTemplate!!.properties,
                statements = state.statements
            )
        }
        return state
    }
}
