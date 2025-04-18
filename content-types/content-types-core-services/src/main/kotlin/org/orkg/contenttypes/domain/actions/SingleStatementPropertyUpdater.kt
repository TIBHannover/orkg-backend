package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateLiteralUseCase

class SingleStatementPropertyUpdater(
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val statementService: StatementUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = SingleStatementPropertyCreator(
        unsafeLiteralUseCases,
        unsafeStatementUseCases
    ),
) {
    internal fun updateRequiredProperty(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String?,
        datatype: String = Literals.XSD.STRING.prefixedUri,
    ) {
        if (label != null) {
            val toRemove = statements.wherePredicate(predicateId)
                .filter { it.`object` is Literal }
                .toMutableSet()

            if (toRemove.isEmpty()) {
                singleStatementPropertyCreator.create(contributorId, subjectId, predicateId, label, datatype)
            } else {
                val statement = toRemove.first()
                unsafeLiteralUseCases.update(
                    UpdateLiteralUseCase.UpdateCommand(
                        id = (statement.`object` as Literal).id,
                        contributorId = contributorId,
                        label = label,
                        datatype = datatype
                    )
                )
                toRemove -= statement
            }

            if (toRemove.isNotEmpty()) {
                statementService.deleteAllById(toRemove.map { it.id }.toSet())
            }
        }
    }

    internal fun updateOptionalProperty(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String?,
        datatype: String = Literals.XSD.STRING.prefixedUri,
    ) {
        if (label == null) {
            val toRemove = statements.wherePredicate(predicateId)
                .filter { it.`object` is Literal }
                .map { it.id }
                .toSet()

            if (toRemove.isNotEmpty()) {
                statementService.deleteAllById(toRemove)
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
        objectId: ThingId,
    ) {
        val toRemove = statements.wherePredicate(predicateId).toMutableSet()
        if (toRemove.isNotEmpty()) {
            statementService.deleteAllById(toRemove.map { it.id }.toSet())
        }
        unsafeStatementUseCases.create(
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
        objectId: ThingId?,
    ) {
        val toRemove = statements.wherePredicate(predicateId).toMutableSet()
        if (toRemove.isNotEmpty()) {
            statementService.deleteAllById(toRemove.map { it.id }.toSet())
        }
        if (objectId != null) {
            unsafeStatementUseCases.create(
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
