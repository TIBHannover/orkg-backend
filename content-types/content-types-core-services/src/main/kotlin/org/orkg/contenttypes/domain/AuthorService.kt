package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.AuthorUseCases
import org.orkg.contenttypes.output.AuthorRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j(readOnly = true)
class AuthorService(
    private val repository: AuthorRepository,
) : AuthorUseCases {
    override fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor> =
        repository.findTopAuthorsOfComparison(id, pageable)

    override fun findAllByProblemId(problemId: ThingId, pageable: Pageable): Page<PaperAuthor> =
        repository.findAllByProblemId(problemId, pageable)
}
