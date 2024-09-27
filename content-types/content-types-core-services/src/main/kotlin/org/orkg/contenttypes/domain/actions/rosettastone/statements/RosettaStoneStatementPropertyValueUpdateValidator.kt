package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementPropertyValueUpdateValidator(
    private val abstractRosettaStoneStatementPropertyValueValidator: AbstractRosettaStoneStatementPropertyValueValidator
) : UpdateRosettaStoneStatementAction {
    constructor(
        thingRepository: ThingRepository,
        statementRepository: StatementRepository,
        classHierarchyRepository: ClassHierarchyRepository,
        rosettaStoneStatementService: RosettaStoneStatementUseCases
    ) : this(
        AbstractRosettaStoneStatementPropertyValueValidator(
            thingRepository,
            statementRepository,
            rosettaStoneStatementService,
            classHierarchyRepository
        )
    )

    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State {
        val validatedIds = abstractRosettaStoneStatementPropertyValueValidator.validate(
            templateProperties = state.rosettaStoneTemplate!!.properties,
            thingDefinitions = command.all(),
            validatedIdsIn = state.validatedIds,
            tempIds = state.tempIds,
            templateId = state.rosettaStoneTemplate.id,
            subjects = command.subjects,
            objects = command.objects
        )
        return state.copy(validatedIds = validatedIds)
    }
}
