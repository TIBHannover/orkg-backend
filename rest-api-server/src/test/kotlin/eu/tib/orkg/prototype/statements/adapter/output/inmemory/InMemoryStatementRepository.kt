package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

class InMemoryStatementRepository : StatementRepository {

    private val idCounter = AtomicLong(1)

    private val entities = mutableMapOf<StatementId, GeneralStatement>()

    override fun findAll(depth: Int): Iterable<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<GeneralStatement> {
        val paged = entities.values.drop(pageable.pageNumber * pageable.pageSize).take(pageable.pageSize)
        return PageImpl(paged, pageable, paged.size.toLong())
    }

    override fun countStatementsAboutResource(id: ResourceId): Long =
        entities.count { (id, entity) -> entity.subject.thingId == ThingId(id.value) }.toLong()

    override fun countStatementsAboutResources(resourceIds: Set<ResourceId>): Map<ResourceId, Long> =
        resourceIds.associateWith { countStatementsAboutResource(it) }

    override fun nextIdentity(): StatementId = StatementId(idCounter.getAndIncrement())

    override fun save(statement: GeneralStatement) {
        entities[statement.id!!] = statement.copy()
    }

    override fun count(): Long = entities.size.toLong()

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
