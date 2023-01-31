package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

interface StatementRepository : EntityRepository<GeneralStatement, StatementId> {
    fun countStatementsAboutResource(id: ResourceId): Long
    fun countStatementsAboutResources(resourceIds: Set<ResourceId>): Map<ResourceId, Long>
    // legacy methods:
    fun nextIdentity(): StatementId
    @Transactional
    fun save(statement: GeneralStatement)
    fun count(): Long
    fun delete(statement: GeneralStatement)
    fun deleteByStatementId(id: StatementId)
    fun deleteAll()
    fun findAll(depth: Int): Iterable<GeneralStatement>
    fun findByStatementId(id: StatementId): Optional<GeneralStatement>
    fun findAllBySubject(subjectId: String, pageable: Pageable): Page<GeneralStatement>
    fun findAllByPredicateId(predicateId: PredicateId, pageable: Pageable): Page<GeneralStatement>
    fun findAllByObject(objectId: String, pageable: Pageable): Page<GeneralStatement>
    fun countByIdRecursive(paperId: String): Int
    fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement>

    fun findAllBySubjects(subjectIds: List<String>, pageable: Pageable): Page<GeneralStatement>
    fun findAllByObjects(objectIds: List<String>, pageable: Pageable): Page<GeneralStatement>
    fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<GeneralStatement>
    fun findDOIByContributionId(id: ResourceId): Optional<Literal>
    fun countPredicateUsage(id: PredicateId): Long
}
