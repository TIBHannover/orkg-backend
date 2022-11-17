package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.statements.application.port.`in`.GetBulkStatementsQuery
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BulkStatementService(
    private val statementRepository: StatementRepository,
) :
    GetBulkStatementsQuery {
    override fun getBulkStatementsBySubjects(
        subjects: List<String>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>> {
        return statementRepository.findAllBySubjects(subjects, pageable)
            .content // FIXME: Not sure how page information can be passed in such call
            .groupBy { (it.subject as Resource).id!!.value }
    }

    override fun getBulkStatementsByObjects(
        objects: List<String>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>> {
        return statementRepository.findAllByObjects(objects, pageable)
            .content // FIXME: Not sure how page information can be passed in such call
            .groupBy { (it.`object` as Resource).id!!.value }
    }
}
