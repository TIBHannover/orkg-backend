package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonAuthor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AuthorRepository {
    fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor>
}
