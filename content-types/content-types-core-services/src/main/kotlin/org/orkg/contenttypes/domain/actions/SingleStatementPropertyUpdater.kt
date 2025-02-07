package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class SingleStatementPropertyUpdater(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases,
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = SingleStatementPropertyCreator(literalService, statementService)
) {
    internal fun updateRequiredProperty(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) = updateRequiredProperty(
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

    internal fun updateRequiredProperty(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String?,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) {
        if (label != null) {
            val toRemove = statements.wherePredicate(predicateId)
                .filter { it.`object` is Literal }
                .toMutableSet()

            if (toRemove.isEmpty()) {
                singleStatementPropertyCreator.create(contributorId, subjectId, predicateId, label, datatype)
            } else {
                val statement = toRemove.first()
                with(statement.`object` as Literal) {
                    literalService.update(copy(label = label, datatype = datatype))
                }
                toRemove -= statement
            }

            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove.map { it.id }.toSet())
            }
        }
    }

    internal fun updateOptionalProperty(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String?,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) = updateOptionalProperty(
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

    internal fun updateOptionalProperty(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String?,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) {
        if (label == null) {
            val toRemove = statements.wherePredicate(predicateId)
                .filter { it.`object` is Literal }
                .map { it.id }
                .toSet()

            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove)
            }
        } else {
            updateRequiredProperty(statements, contributorId, subjectId, predicateId, label, datatype)
        }
    }

    internal fun updateRequiredProperty(
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
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = predicateId,
                objectId = objectId
            )
        )
    }

    internal fun updateOptionalProperty(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objectId: ThingId?
    ) {
        val toRemove = statements.wherePredicate(predicateId).toMutableSet()
        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove.map { it.id }.toSet())
        }
        if (objectId != null) {
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
