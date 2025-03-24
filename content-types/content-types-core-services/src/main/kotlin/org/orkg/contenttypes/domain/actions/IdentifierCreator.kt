package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.Identifier
import org.orkg.contenttypes.domain.identifiers.parse
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class IdentifierCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) {
    internal fun create(
        contributorId: ContributorId,
        identifiers: Map<String, List<String>>,
        identifierDefinitions: Set<Identifier>,
        subjectId: ThingId,
    ) {
        val parsedIdentifiers = identifierDefinitions.parse(identifiers, validate = false)
        parsedIdentifiers.forEach { (identifier, values) ->
            values.forEach { value ->
                val identifierLiteralId = unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = value
                    )
                )
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = subjectId,
                        predicateId = identifier.predicateId,
                        objectId = identifierLiteralId
                    )
                )
            }
        }
    }
}
