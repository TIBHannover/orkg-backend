package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository

class RosettaStoneStatementPropertyValueUpdateValidator(
    private val abstractRosettaStoneStatementPropertyValueValidator: AbstractRosettaStoneStatementPropertyValueValidator,
) : UpdateRosettaStoneStatementAction {
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

    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State =
        state.copy(
            validationCache = abstractRosettaStoneStatementPropertyValueValidator.validate(
                templateProperties = state.rosettaStoneTemplate!!.properties,
                thingCommands = command.all(),
                validationCacheIn = state.validationCache,
                templateId = state.rosettaStoneTemplate.id,
                subjects = command.subjects,
                objects = command.objects
            )
        )
}
