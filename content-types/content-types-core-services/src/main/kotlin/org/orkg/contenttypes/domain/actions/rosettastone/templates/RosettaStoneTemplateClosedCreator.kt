package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class RosettaStoneTemplateClosedCreator(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) : CreateRosettaStoneTemplateAction {
    override fun invoke(
        command: CreateRosettaStoneTemplateCommand,
        state: CreateRosettaStoneTemplateState
    ): CreateRosettaStoneTemplateState =
        state.apply {
            val literalId = literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = "true",
                    datatype = Literals.XSD.BOOLEAN.prefixedUri
                )
            )
            statementService.add(
                userId = command.contributorId,
                subject = rosettaStoneTemplateId!!,
                predicate = Predicates.shClosed,
                `object` = literalId
            )
        }
}
