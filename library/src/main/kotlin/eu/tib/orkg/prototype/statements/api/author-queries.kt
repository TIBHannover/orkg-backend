package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthor
import eu.tib.orkg.prototype.statements.domain.model.PaperAuthor
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveAuthorUseCase {
    fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor>

    fun findAuthorsPerProblem(problemId: ThingId, pageable: Pageable): Page<PaperAuthor>
}
