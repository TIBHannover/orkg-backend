package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class SingleStatementPropertyUpdater(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases,
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = SingleStatementPropertyCreator(literalService, statementService)
) {
    internal fun update(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) = update(
        statements = statementService.findAll(
            subjectId = subjectId,
            predicateId = predicateId,
            objectClasses = setOf(Classes.literal),
            pageable = PageRequests.SINGLE
        ).content,
        contributorId = contributorId,
        subjectId = subjectId,
        predicateId = predicateId,
        label = label,
        datatype = datatype
    )

    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String?,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) {
        val toRemove = statements.wherePredicate(predicateId)
            .filter { it.`object` is Literal }
            .toMutableSet()

        if (label != null) {
            if (toRemove.isEmpty()) {
                singleStatementPropertyCreator.create(contributorId, subjectId, predicateId, label)
            } else {
                val statement = toRemove.first()
                with(statement.`object` as Literal) {
                    literalService.update(copy(label = label, datatype = datatype))
                }
                toRemove -= statement
            }
        }

        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove.map { it.id }.toSet())
        }
    }

    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objectId: ThingId
    ) {
        val toRemove = statements.wherePredicate(predicateId).toMutableSet()
        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove.map { it.id }.toSet())
        }
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = predicateId,
            `object` = objectId
        )
    }
}
