package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.RetrieveAuthorUseCase
import org.orkg.graph.output.AuthorRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j(readOnly = true)
class AuthorService(
    private val repository: AuthorRepository,
) : RetrieveAuthorUseCase {
    override fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor> =
        repository.findTopAuthorsOfComparison(id, pageable)

    override fun findAuthorsPerProblem(problemId: ThingId, pageable: Pageable): Page<PaperAuthor> =
        repository.findAuthorsLeaderboardPerProblem(problemId, pageable)
}
