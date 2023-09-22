package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthor
import eu.tib.orkg.prototype.statements.domain.model.PaperAuthor
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AuthorRepository {
    fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor>
    fun findAuthorsLeaderboardPerProblem(problemId: ThingId, pageable: Pageable): Page<PaperAuthor>
}
