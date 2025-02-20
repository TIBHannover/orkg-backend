package org.orkg.graph.input

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement
import org.springframework.data.domain.Pageable

interface BulkStatementUseCases : RetrieveBulkStatementUseCase

interface RetrieveBulkStatementUseCase {
    fun findBulkStatementsBySubjects(
        subjects: List<ThingId>,
        pageable: Pageable,
    ): Map<String, Iterable<GeneralStatement>>

    fun findBulkStatementsByObjects(
        objects: List<ThingId>,
        pageable: Pageable,
    ): Map<String, Iterable<GeneralStatement>>
}
