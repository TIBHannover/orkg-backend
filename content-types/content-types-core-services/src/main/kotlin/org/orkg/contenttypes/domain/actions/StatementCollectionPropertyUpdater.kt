package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class StatementCollectionPropertyUpdater(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) {
    internal fun update(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objects: Set<ThingId>
    ) = update(
        statements = statementService.findAll(
            subjectId = subjectId,
            predicateId = predicateId,
            pageable = PageRequests.ALL
        ).content,
        contributorId = contributorId,
        subjectId = subjectId,
        predicateId = predicateId,
        objects = objects
    )

    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objects: Set<ThingId>
    ) {
        // Find out what already exists and what needs to be created or removed
        val objectId2statementId = statements.wherePredicate(predicateId)
            .associate { it.`object`.id to it.id }
        val toRemove = objectId2statementId.keys - objects
        val toAdd = objects - objectId2statementId.keys

        // Remove unwanted object statements
        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove.map { objectId2statementId[it]!! }.toSet())
        }

        // Create new object statements
        toAdd.forEach { objectId ->
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = predicateId,
                `object` = objectId
            )
        }
    }

    internal fun update(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objects: List<ThingId>
    ) = update(
        statements = statementService.findAll(
            subjectId = subjectId,
            predicateId = predicateId,
            pageable = PageRequests.ALL
        ).content,
        contributorId = contributorId,
        subjectId = subjectId,
        predicateId = predicateId,
        objects = objects
    )

    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        objects: List<ThingId>
    ) {
        val statementsIterator = statements.wherePredicate(predicateId)
            .sortedBy { it.createdAt }
            .listIterator()
        val objectsIterator = objects.listIterator()
        val toRemove = mutableSetOf<StatementId>()

        while (objectsIterator.hasNext()) {
            val objectId = objectsIterator.next()
            var matchingStatement: GeneralStatement? = null
            while (statementsIterator.hasNext()) {
                val statement = statementsIterator.next()
                if (statement.`object`.id == objectId) {
                    matchingStatement = statement
                    break
                }
                toRemove += statement.id
            }
            if (matchingStatement == null) {
                statementService.add(
                    userId = contributorId,
                    subject = subjectId,
                    predicate = predicateId,
                    `object` = objectId
                )
            }
        }

        while (statementsIterator.hasNext()) {
            toRemove += statementsIterator.next().id
        }

        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove)
        }
    }

    internal fun update(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        literals: Set<String>,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) = update(
        statements = statementService.findAll(
            subjectId = subjectId,
            predicateId = predicateId,
            objectClasses = setOf(Classes.literal),
            pageable = PageRequests.ALL
        ).content,
        contributorId = contributorId,
        subjectId = subjectId,
        predicateId = predicateId,
        literals = literals,
        datatype = datatype
    )

    internal fun update(
        statements: List<GeneralStatement>,
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        literals: Set<String>,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) {
        // Find out what already exists and what needs to be created or removed
        val objectId2statementId = statements.wherePredicate(predicateId)
            .groupBy { it.`object`.label }
        val toRemove = objectId2statementId.keys - literals
        val toAdd = literals - objectId2statementId.keys

        // Remove unwanted object statements
        if (toRemove.isNotEmpty()) {
            statementService.delete(toRemove.map { objectId2statementId[it]!! }.flatten().map { it.id }.toSet())
        }

        // Create new object statements
        toAdd.forEach { literal ->
            val literalId = literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = literal,
                    datatype = datatype
                )
            )
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = predicateId,
                `object` = literalId
            )
        }
    }
}
