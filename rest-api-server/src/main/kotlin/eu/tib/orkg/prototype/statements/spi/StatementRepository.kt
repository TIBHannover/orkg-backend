package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

interface StatementRepository {
    fun findAll(): Sequence<GeneralStatement>
    // legacy methods:
    fun nextIdentity(): StatementId
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun save(statement: GeneralStatement)
    fun count(): Long
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun delete(statement: GeneralStatement)
    fun deleteAll()
    fun findAll(depth: Int): Iterable<GeneralStatement>
    fun findAll(pageable: Pageable): Page<GeneralStatement>
    fun findByStatementId(id: StatementId): Optional<GeneralStatement>
    fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement>
    fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement>
    fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement>
    fun countByIdRecursive(paperId: String): Int
    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement>

    fun findAllBySubjects(subjectIds: List<String>, pagination: Pageable): Page<GeneralStatement>
    fun findAllByObjects(subjectIds: List<String>, pagination: Pageable): Page<GeneralStatement>
    fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<GeneralStatement>
}
