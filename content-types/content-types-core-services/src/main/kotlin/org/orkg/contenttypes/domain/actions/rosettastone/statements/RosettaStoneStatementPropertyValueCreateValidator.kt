package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementPropertyValueCreateValidator(
    private val abstractRosettaStoneStatementPropertyValueValidator: AbstractRosettaStoneStatementPropertyValueValidator,
) : CreateRosettaStoneStatementAction {
    constructor(
        thingRepository: ThingRepository,
        statementRepository: StatementRepository,
        classHierarchyRepository: ClassHierarchyRepository,
        rosettaStoneStatementService: RosettaStoneStatementUseCases,
    ) : this(
        AbstractRosettaStoneStatementPropertyValueValidator(
            thingRepository,
            statementRepository,
            rosettaStoneStatementService,
            classHierarchyRepository
        )
    )

    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
        val validatedIds = abstractRosettaStoneStatementPropertyValueValidator.validate(
            templateProperties = state.rosettaStoneTemplate!!.properties,
            thingsCommand = command.all(),
            validatedIdsIn = state.validatedIds,
            tempIds = state.tempIds,
            templateId = command.templateId,
            subjects = command.subjects,
            objects = command.objects
        )
        return state.copy(validatedIds = validatedIds)
    }
}
