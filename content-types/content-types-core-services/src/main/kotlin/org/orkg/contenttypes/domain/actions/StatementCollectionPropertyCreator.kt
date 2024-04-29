package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
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
                CreateCommand(
                    contributorId = contributorId,
                    label = label,
                    datatype = datatype
                )
            )
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = predicateId,
                `object` = literal
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
                userId = contributorId,
                subject = subjectId,
                predicate = predicateId,
                `object` = objectId
            )
        }
    }
}
