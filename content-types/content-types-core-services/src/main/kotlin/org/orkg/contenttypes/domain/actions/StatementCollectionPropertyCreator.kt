package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class StatementCollectionPropertyCreator(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) {
    internal fun create(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        labels: Collection<String>,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) {
        labels.forEach { label ->
            val literal = literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = label,
                    datatype = datatype
                )
            )
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = predicateId,
                    objectId = literal
                )
            )
        }
    }

    internal fun create(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objects: List<ThingId>
    ) {
        objects.distinct().forEach { objectId ->
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = predicateId,
                    objectId = objectId
                )
            )
        }
    }
}
