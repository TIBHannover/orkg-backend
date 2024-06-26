package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementPropertyValueCreateValidator(
    private val abstractRosettaStoneStatementPropertyValueValidator: AbstractRosettaStoneStatementPropertyValueValidator
) : CreateRosettaStoneStatementAction {
    constructor(
        thingRepository: ThingRepository,
        statementRepository: StatementRepository
    ) : this(AbstractRosettaStoneStatementPropertyValueValidator(thingRepository, statementRepository))

    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
        val validatedIds = abstractRosettaStoneStatementPropertyValueValidator.validate(
            templateProperties = state.rosettaStoneTemplate!!.properties,
            thingDefinitions = command.all(),
            validatedIdsIn = state.validatedIds,
            tempIds = state.tempIds,
            templateId = command.templateId,
            subjects = command.subjects,
            objects = command.objects
        )
        return state.copy(validatedIds = validatedIds)
    }
}
