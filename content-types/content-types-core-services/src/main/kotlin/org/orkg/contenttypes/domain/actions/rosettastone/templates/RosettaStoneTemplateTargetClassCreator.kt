package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class RosettaStoneTemplateTargetClassCreator(
    private val classService: ClassUseCases,
    private val statementUseCases: StatementUseCases,
    private val literalService: LiteralUseCases
) : CreateRosettaStoneTemplateAction {
    override fun invoke(
        command: CreateRosettaStoneTemplateCommand,
        state: CreateRosettaStoneTemplateState
    ): CreateRosettaStoneTemplateState =
        state.apply {
            val classId = classService.create(
                CreateClassUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "${command.label} (class)",
                )
            )
            statementUseCases.add(
                userId = command.contributorId,
                subject = rosettaStoneTemplateId!!,
                predicate = Predicates.shTargetClass,
                `object` = classId
            )
            val exampleUsageId = literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = command.exampleUsage
                )
            )
            statementUseCases.add(
                userId = command.contributorId,
                subject = classId,
                predicate = Predicates.exampleOfUsage,
                `object` = exampleUsageId
            )
        }
}
