package org.orkg.graph.domain

import kotlin.collections.List
import org.orkg.common.ThingId
import org.orkg.graph.input.GetBulkStatementsQuery
import org.orkg.graph.output.StatementRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class BulkStatementService(
    private val statementRepository: StatementRepository,
) : GetBulkStatementsQuery {
    override fun findBulkStatementsBySubjects(
        subjects: List<ThingId>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>> {
        return statementRepository.findAllBySubjects(subjects, pageable)
            .content // FIXME: Not sure how page information can be passed in such call
            .groupBy { (it.subject as Resource).id.value }
    }

    override fun findBulkStatementsByObjects(
        objects: List<ThingId>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>> {
        return statementRepository.findAllByObjects(objects, pageable)
            .content // FIXME: Not sure how page information can be passed in such call
            .groupBy { (it.`object` as Resource).id.value }
    }
}
