package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateState
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class RosettaStoneTemplateClosedCreator(
    private val literalService: LiteralUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) : CreateRosettaStoneTemplateAction {
    override fun invoke(
        command: CreateRosettaStoneTemplateCommand,
        state: CreateRosettaStoneTemplateState
    ): CreateRosettaStoneTemplateState =
        state.apply {
            val literalId = literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    label = "true",
                    datatype = Literals.XSD.BOOLEAN.prefixedUri
                )
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = rosettaStoneTemplateId!!,
                    predicateId = Predicates.shClosed,
                    objectId = literalId
                )
            )
        }
}
