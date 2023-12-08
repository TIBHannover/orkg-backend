package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.Identifier
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

abstract class IdentifierUpdater(
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val identifierCreator: IdentifierCreator = object : IdentifierCreator(statementService, literalService) {}
) {
    internal fun update(
        contributorId: ContributorId,
        oldIdentifiers: Map<String, String>,
        newIdentifiers: Map<String, String>,
        identifierDefinitions: Set<Identifier>,
        subjectId: ThingId
    ) {
        val key2Identifier = identifierDefinitions.associateBy { it.id }

        // Find out what already exists and what needs to be created or removed
        val toRemove = oldIdentifiers.filter { (key, value) -> key !in newIdentifiers || value != newIdentifiers[key] }
        val toAdd = newIdentifiers.filter { (key, value) -> key !in oldIdentifiers || value != oldIdentifiers[key] }

        // Remove unwanted identifiers
        if (toRemove.isNotEmpty()) {
            toRemove.forEach { (key, _) ->
                statementService.findAllBySubjectAndPredicate(
                    subjectId = subjectId,
                    predicateId = key2Identifier[key]!!.predicateId,
                    pagination = PageRequests.ALL
                ).forEach { statementService.delete(it.id!!) }
            }
        }

        // Create new identifiers
        if (toAdd.isNotEmpty()) {
            identifierCreator.create(contributorId, toAdd, identifierDefinitions, subjectId)
        }
    }
}
