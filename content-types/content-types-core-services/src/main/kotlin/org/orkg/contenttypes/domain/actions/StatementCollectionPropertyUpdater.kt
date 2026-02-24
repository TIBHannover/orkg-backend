package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

data class StatementCollectionPropertyUpdater(
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val statementService: StatementUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) {
    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objects: Set<ThingId>,
    ) {
        // Find out what already exists and what needs to be created or removed
        val objectIdToStatementId = statements.wherePredicate(predicateId)
            .associate { it.`object`.id to it.id }
        val toRemove = objectIdToStatementId.keys - objects
        val toAdd = objects - objectIdToStatementId.keys

        // Remove unwanted object statements
        if (toRemove.isNotEmpty()) {
            statementService.deleteAllById(toRemove.map { objectIdToStatementId[it]!! }.toSet())
        }

        // Create new object statements
        toAdd.forEach { objectId ->
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

    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objects: List<ThingId>,
    ) = update(
        statements = statements,
        contributorId = contributorId,
        subjectId = subjectId,
        predicates = setOf(predicateId),
        objects = objects,
        predicateSelector = { predicateId },
        objectIdSelector = { it }
    )

    internal fun <T> update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicates: Set<ThingId>,
        objects: List<T>,
        predicateSelector: (T) -> ThingId,
        objectIdSelector: (T) -> ThingId,
    ) {
        val statementsIterator = statements.filter { it.predicate.id in predicates }
            .sortedBy { it.createdAt }
            .listIterator()
        val objectsIterator = objects.listIterator()
        val toRemove = mutableSetOf<StatementId>()

        while (objectsIterator.hasNext()) {
            val `object` = objectsIterator.next()
            val objectId = objectIdSelector(`object`)
            val predicateId = predicateSelector(`object`)
            var matchingStatement: GeneralStatement? = null
            while (statementsIterator.hasNext()) {
                val statement = statementsIterator.next()
                if (statement.`object`.id == objectId && statement.predicate.id == predicateId) {
                    matchingStatement = statement
                    break
                }
                toRemove += statement.id
            }
            if (matchingStatement == null) {
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

        while (statementsIterator.hasNext()) {
            toRemove += statementsIterator.next().id
        }

        if (toRemove.isNotEmpty()) {
            statementService.deleteAllById(toRemove)
        }
    }

    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        literals: Set<String>,
        datatype: String = Literals.XSD.STRING.prefixedUri,
    ) {
        // Find out what already exists and what needs to be created or removed
        val objectIdToStatementId = statements.wherePredicate(predicateId)
            .groupBy { it.`object`.label }
        val toRemove = objectIdToStatementId.keys - literals
        val toAdd = literals - objectIdToStatementId.keys

        // Remove unwanted object statements
        if (toRemove.isNotEmpty()) {
            statementService.deleteAllById(toRemove.map { objectIdToStatementId[it]!! }.flatten().map { it.id }.toSet())
        }

        // Create new object statements
        toAdd.forEach { literal ->
            val literalId = unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = literal,
                    datatype = datatype
                )
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = predicateId,
                    objectId = literalId
                )
            )
        }
    }

    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        literals: List<String>,
        datatype: String = Literals.XSD.STRING.prefixedUri,
    ) {
        val statementsIterator = statements.wherePredicate(predicateId)
            .sortedBy { it.createdAt }
            .listIterator()
        val literalIterator = literals.listIterator()
        val toRemove = mutableSetOf<StatementId>()

        while (literalIterator.hasNext()) {
            val literal = literalIterator.next()
            var matchingStatement: GeneralStatement? = null
            while (statementsIterator.hasNext()) {
                val statement = statementsIterator.next()
                if (statement.`object`.label == literal) {
                    matchingStatement = statement
                    break
                }
                toRemove += statement.id
            }
            if (matchingStatement == null) {
                val literalId = unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = literal,
                        datatype = datatype
                    )
                )
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = subjectId,
                        predicateId = predicateId,
                        objectId = literalId
                    )
                )
            }
        }

        while (statementsIterator.hasNext()) {
            toRemove += statementsIterator.next().id
        }

        if (toRemove.isNotEmpty()) {
            statementService.deleteAllById(toRemove)
        }
    }
}
