package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonAuthor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AuthorUseCases : RetrieveAuthorUseCase

interface RetrieveAuthorUseCase {
    fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor>
}
