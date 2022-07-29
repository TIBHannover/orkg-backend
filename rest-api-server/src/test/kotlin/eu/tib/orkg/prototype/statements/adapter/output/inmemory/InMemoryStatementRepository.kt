package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryStatementRepository : StatementRepository {
    override fun findAll(): Sequence<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAll(depth: Int): Iterable<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun countStatementsAboutResource(id: ResourceId): Long {
        TODO("Not yet implemented")
    }

    override fun countStatementsAboutResources(resourceIds: Set<ResourceId>): Map<ResourceId, Long> {
        TODO("Not yet implemented")
    }

    override fun nextIdentity(): StatementId {
        TODO("Not yet implemented")
    }

    override fun save(statement: GeneralStatement) {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun delete(statement: GeneralStatement) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByPredicateId(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun countByIdRecursive(paperId: String): Int {
        TODO("Not yet implemented")
    }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllBySubjects(subjectIds: List<String>, pagination: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByObjects(subjectIds: List<String>, pagination: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun fetchAsBundle(id: String, configuration: Map<String, Any>): Iterable<GeneralStatement> {
        TODO("Not yet implemented")
    }
}
