package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.statements.api.RetrieveAuthorUseCase
import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthor
import eu.tib.orkg.prototype.statements.domain.model.PaperAuthor
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.AuthorRepository
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
