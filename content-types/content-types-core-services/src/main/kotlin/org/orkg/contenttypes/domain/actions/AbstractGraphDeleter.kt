package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementSpec
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingSpec
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.output.StatementRepository
import java.util.LinkedList

/**
 * This helper class is used to delete abstract graph shapes, hence the name.
 * The class is not actually an abstract class.
 */
class AbstractGraphDeleter(
    private val statementRepository: StatementRepository,
    private val resourceUseCases: ResourceUseCases,
    private val predicateUseCases: PredicateUseCases,
) {
    internal fun delete(thingSpec: ThingSpec, root: Thing, statements: List<GeneralStatement>, contributorId: ContributorId): Boolean {
        val matchResult = matchGraph(thingSpec, root, statements)
        if (root.id !in matchResult.thingsToDelete) {
            return false
        }
        val thingIdToThing = mutableMapOf<ThingId, Thing>()
        statements.forEach { statement ->
            thingIdToThing[statement.subject.id] = statement.subject
            thingIdToThing[statement.predicate.id] = statement.predicate
            thingIdToThing[statement.`object`.id] = statement.`object`
        }
        // modifiability is not checked!
        statementRepository.deleteByStatementIds(matchResult.statementsToDelete)
        thingIdToThing.forEach { (id, thing) ->
            if (id in matchResult.thingsToDelete) {
                when (thing) {
                    is Resource -> {
                        // deleting resources individually is rather slow, we should think about bulk actions
                        try {
                            resourceUseCases.delete(id, contributorId)
                        } catch (_: Throwable) {
                            // ignore
                        }
                    }

                    is Predicate -> {
                        // deleting predicates individually is rather slow, we should think about bulk actions
                        try {
                            predicateUseCases.delete(id, contributorId)
                        } catch (_: Throwable) {
                            // ignore
                        }
                    }

                    is Class -> {
                        // classes cannot be deleted
                    }

                    is Literal -> {
                        // literals cannot be deleted directly, but will be removed together with statements
                    }
                }
            }
        }
        return true
    }

    internal fun matchGraph(thingSpec: ThingSpec, root: Thing, statements: List<GeneralStatement>): MatchResult {
        val statementsBySubject = statements.groupBy { it.subject.id }
        val statementsByObject = statements.groupBy { it.`object`.id }
        val matchedStatements = mutableSetOf<GeneralStatement>()
        val statementsToDelete = mutableSetOf<StatementId>()
        val sharedThings = mutableSetOf<ThingId>()
        val visited = mutableSetOf<ThingId>()
        matchGraph(thingSpec, root, statementsBySubject, statementsByObject, matchedStatements, statementsToDelete, sharedThings, visited)
        removeSharedSubgraphs(matchedStatements, statementsToDelete, sharedThings)
        removeExternallySharedSubgraphs(visited, matchedStatements, statementsToDelete, sharedThings)
        val thingsToDelete = visited - sharedThings
        return MatchResult(thingsToDelete, statementsToDelete)
    }

    data class MatchResult(
        val thingsToDelete: Set<ThingId>,
        val statementsToDelete: Set<StatementId>,
    )

    private fun removeExternallySharedSubgraphs(
        visited: MutableSet<ThingId>,
        matchedStatements: MutableSet<GeneralStatement>,
        statementsToDelete: MutableSet<StatementId>,
        sharedThings: MutableSet<ThingId>,
    ) {
        val statementsBySubject = matchedStatements.groupBy { it.subject.id }
        val additionalSharedThings = mutableSetOf<ThingId>()
        (visited - sharedThings).forEach { thingId ->
            val incomingStatements = statementRepository.findAll(objectId = thingId, pageable = PageRequests.ALL)
                .content.filter { it.subject.id !in visited }.map { it.id }
            val matchedIncomingStatements = statementsBySubject[thingId].orEmpty().toSet()
            if ((incomingStatements - matchedIncomingStatements).isNotEmpty()) {
                additionalSharedThings += thingId
            }
        }
        removeSharedSubgraphs(matchedStatements, statementsToDelete, additionalSharedThings)
        sharedThings += additionalSharedThings
    }

    private fun removeSharedSubgraphs(
        matchedStatements: MutableSet<GeneralStatement>,
        statementsToDelete: MutableSet<StatementId>,
        sharedThings: MutableSet<ThingId>,
    ) {
        val buffer = LinkedList(sharedThings)
        while (buffer.isNotEmpty()) {
            val thingId = buffer.pop()
            matchedStatements.removeIf { statement ->
                val result = statement.subject.id == thingId
                if (result) {
                    if (statement.`object`.id !in sharedThings) {
                        sharedThings += statement.`object`.id
                        buffer += statement.`object`.id
                    }
                    statementsToDelete -= statement.id
                }
                result
            }
        }
    }

    private fun matchGraph(
        thingSpec: ThingSpec,
        thing: Thing,
        statementsBySubject: Map<ThingId, List<GeneralStatement>>,
        statementsByObject: Map<ThingId, List<GeneralStatement>>,
        matchedStatements: MutableSet<GeneralStatement>,
        statementsToDelete: MutableSet<StatementId>,
        sharedThings: MutableSet<ThingId>,
        visited: MutableSet<ThingId>,
    ) {
        val matchedIncomingStatements = mutableMapOf<StatementSpec.Incoming, List<GeneralStatement>>()
        val matchedOutgoingStatements = mutableMapOf<StatementSpec.Outgoing, List<GeneralStatement>>()
        thingSpec.statements.forEach { statementSpec ->
            when (statementSpec) {
                is StatementSpec.Incoming -> {
                    matchedIncomingStatements[statementSpec] = statementsByObject[thing.id]
                        ?.filter { it.matches(statementSpec) }
                        .orEmpty()
                }

                is StatementSpec.Outgoing -> {
                    matchedOutgoingStatements[statementSpec] = statementsBySubject[thing.id]
                        ?.filter { it.matches(statementSpec) }
                        .orEmpty()
                }
            }
        }
        visited += thing.id
        if (!thingSpec.shared) {
            statementsToDelete += statementsBySubject[thing.id]?.map { it.id }.orEmpty()
            matchedOutgoingStatements.forEach { (spec, outgoingStatements) ->
                outgoingStatements.forEach { outgoingStatement ->
                    matchedStatements += outgoingStatement
                    if (outgoingStatement.`object`.id !in visited) {
                        matchGraph(
                            thingSpec = spec.`object`,
                            thing = outgoingStatement.`object`,
                            statementsBySubject = statementsBySubject,
                            statementsByObject = statementsByObject,
                            matchedStatements = matchedStatements,
                            statementsToDelete = statementsToDelete,
                            sharedThings = sharedThings,
                            visited = visited,
                        )
                    }
                }
            }
        } else {
            sharedThings += thing.id
        }
        matchedIncomingStatements.forEach { (spec, incomingStatements) ->
            incomingStatements.forEach { incomingStatement ->
                matchedStatements += incomingStatement
                if (!spec.subject.shared && incomingStatement.subject.id !in visited) {
                    matchGraph(
                        thingSpec = spec.subject,
                        thing = incomingStatement.subject,
                        statementsBySubject = statementsBySubject,
                        statementsByObject = statementsByObject,
                        matchedStatements = matchedStatements,
                        statementsToDelete = statementsToDelete,
                        sharedThings = sharedThings,
                        visited = visited,
                    )
                }
            }
        }
    }

    private fun Thing.matches(spec: ThingSpec): Boolean =
        when (this) {
            is Resource -> spec.classPredicate(classes)
            is Literal -> spec.classPredicate(setOf(Classes.literal))
            is Predicate -> spec.classPredicate(setOf(Classes.predicate))
            is Class -> spec.classPredicate(setOf(Classes.`class`))
        }

    private fun GeneralStatement.matches(spec: StatementSpec.Incoming): Boolean =
        subject.matches(spec.subject) && spec.predicatePredicate(predicate.id)

    private fun GeneralStatement.matches(spec: StatementSpec.Outgoing): Boolean =
        `object`.matches(spec.`object`) && spec.predicatePredicate(predicate.id)
}
