package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

open class AuthorUpdater(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val statementService: StatementUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val listService: ListUseCases,
    private val listRepository: ListRepository,
    private val authorCreator: AuthorCreator = object : AuthorCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService) {},
) {
    // TODO: refactor method to use a provided list of statements and replace delete + create with update action
    internal fun update(contributorId: ContributorId, authors: List<Author>, subjectId: ThingId) {
        // Remove current authors list, literal authors will be fully deleted in the process
        statementService.findAll(
            subjectId = subjectId,
            predicateId = Predicates.hasAuthors,
            pageable = PageRequests.SINGLE
        )
            .filter { it.`object` is Resource && Classes.list in (it.`object` as Resource).classes }
            .singleOrNull()
            ?.let { listRepository.deleteById(it.`object`.id) }
        // Create new authors list
        authorCreator.create(contributorId, authors, subjectId)
    }
}
