package org.orkg.graph.domain

import org.orkg.common.ThingId
import org.orkg.graph.input.BulkStatementUseCases
import org.orkg.graph.output.StatementRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import kotlin.collections.List

@Service
@TransactionalOnNeo4j
class BulkStatementService(
    private val statementRepository: StatementRepository,
) : BulkStatementUseCases {
    override fun findBulkStatementsBySubjects(
        subjects: List<ThingId>,
        pageable: Pageable,
    ): Map<String, Iterable<GeneralStatement>> = statementRepository.findAllBySubjects(subjects, pageable)
        .content // FIXME: Not sure how page information can be passed in such call
        .groupBy { (it.subject as Resource).id.value }

    override fun findBulkStatementsByObjects(
        objects: List<ThingId>,
        pageable: Pageable,
    ): Map<String, Iterable<GeneralStatement>> = statementRepository.findAllByObjects(objects, pageable)
        .content // FIXME: Not sure how page information can be passed in such call
        .groupBy { (it.`object` as Resource).id.value }
}
