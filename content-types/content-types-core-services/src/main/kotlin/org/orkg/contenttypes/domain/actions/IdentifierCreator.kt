package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.Identifier
import org.orkg.contenttypes.domain.identifiers.parse
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

abstract class IdentifierCreator(
    protected val statementService: StatementUseCases,
    protected val literalService: LiteralUseCases
) {
    internal fun create(
        contributorId: ContributorId,
        identifiers: Map<String, List<String>>,
        identifierDefinitions: Set<Identifier>,
        subjectId: ThingId
    ) {
        val parsedIdentifiers = identifierDefinitions.parse(identifiers, validate = false)
        parsedIdentifiers.forEach { (identifier, values) ->
            values.forEach { value ->
                statementService.create(
                    contributorId,
                    subjectId,
                    identifier.predicateId,
                    literalService.create(
                        CreateCommand(
                            contributorId = contributorId,
                            label = value
                        )
                    )
                )
            }
        }
    }
}
