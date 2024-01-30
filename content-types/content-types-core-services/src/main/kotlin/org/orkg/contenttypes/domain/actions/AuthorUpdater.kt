package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

open class AuthorUpdater(
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases,
    private val authorCreator: AuthorCreator = object : AuthorCreator(resourceService, statementService, literalService, listService) {}
) {
    internal fun update(contributorId: ContributorId, authors: List<Author>, subjectId: ThingId) {
        // Remove current authors list, literal authors will be fully deleted in the process
        statementService.findAllBySubjectAndPredicate(
            subjectId = subjectId,
            predicateId = Predicates.hasAuthors,
            pagination = PageRequests.SINGLE
        )
            .filter { it.`object` is Resource && Classes.list in (it.`object` as Resource).classes }
            .singleOrNull()
            ?.let { listService.delete(it.`object`.id) }
        // Create new authors list
        authorCreator.create(contributorId, authors, subjectId)
    }
}
