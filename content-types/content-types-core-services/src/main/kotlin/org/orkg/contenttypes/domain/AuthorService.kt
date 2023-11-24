package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.RetrieveAuthorUseCase
import org.orkg.graph.output.AuthorRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthorService(
    private val repository: AuthorRepository,
) : RetrieveAuthorUseCase {
    override fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor> =
        repository.findTopAuthorsOfComparison(id, pageable)

    override fun findAuthorsPerProblem(problemId: ThingId, pageable: Pageable): Page<PaperAuthor> =
        repository.findAuthorsLeaderboardPerProblem(problemId, pageable)
}
