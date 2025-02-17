package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonAuthor
import org.orkg.contenttypes.domain.PaperAuthor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveAuthorUseCase {
    fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor>

    fun findAllByProblemId(problemId: ThingId, pageable: Pageable): Page<PaperAuthor>
}
