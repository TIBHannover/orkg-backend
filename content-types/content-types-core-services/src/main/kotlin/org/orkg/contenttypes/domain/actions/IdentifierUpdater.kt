package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.associateIdentifiers
import org.orkg.contenttypes.domain.identifiers.Identifier
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class IdentifierUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) {
    constructor(
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        StatementCollectionPropertyUpdater(
            unsafeLiteralUseCases,
            statementService,
            unsafeStatementUseCases
        )
    )

    internal fun update(
        statements: Map<ThingId, List<GeneralStatement>>,
        contributorId: ContributorId,
        newIdentifiers: Map<String, List<String>>,
        identifierDefinitions: Set<Identifier>,
        subjectId: ThingId,
    ) {
        val directStatements = statements[subjectId].orEmpty()
        val oldIdentifiers = directStatements.associateIdentifiers(identifierDefinitions)

        if (oldIdentifiers == newIdentifiers) {
            return
        }

        identifierDefinitions.forEach { identifier ->
            statementCollectionPropertyUpdater.update(
                statements = directStatements,
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = identifier.predicateId,
                literals = newIdentifiers[identifier.id].orEmpty().toSet()
            )
        }
    }
}
