package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ListRepository

class AuthorUpdater(
    private val listRepository: ListRepository,
    private val authorCreator: AuthorCreator,
) {
    constructor(
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        listService: ListUseCases,
        listRepository: ListRepository,
    ) : this(
        listRepository,
        object : AuthorCreator(unsafeResourceUseCases, unsafeStatementUseCases, unsafeLiteralUseCases, listService) {}
    )

    internal fun update(
        statements: Map<ThingId, List<GeneralStatement>>,
        contributorId: ContributorId,
        authors: List<Author>,
        subjectId: ThingId,
    ) {
        // Remove current authors list, literal authors will be fully deleted in the process
        statements[subjectId].orEmpty()
            .singleOrNull { it.`object` is Resource && Classes.list in (it.`object` as Resource).classes }
            ?.let { listRepository.deleteById(it.`object`.id) }
        // Create new authors list
        authorCreator.create(contributorId, authors, subjectId)
    }
}
