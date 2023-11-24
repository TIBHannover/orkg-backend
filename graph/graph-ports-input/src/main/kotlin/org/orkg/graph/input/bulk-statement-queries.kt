package org.orkg.graph.input

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement
import org.springframework.data.domain.Pageable

interface GetBulkStatementsQuery {
    fun getBulkStatementsBySubjects(
        subjects: List<ThingId>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>>
    fun getBulkStatementsByObjects(
        objects: List<ThingId>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>>
}
