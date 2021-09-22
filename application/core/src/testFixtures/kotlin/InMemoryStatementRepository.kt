package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

class InMemoryStatementRepository : StatementRepository {

    private val counter = AtomicLong()

    private val statements: MutableSet<GeneralStatement> = mutableSetOf()

    override fun nextIdentity(): StatementId = StatementId(counter.getAndIncrement())

    override fun save(statement: GeneralStatement) {
        statements += statement
    }

    override fun delete(statement: GeneralStatement) {
        TODO("Not yet implemented")
    }

    override fun count(): Long = statements.size.toLong()

    override fun countByIdRecursive(id: String): Long {
        TODO("Not yet implemented")
    }

    override fun fetchAsBundle(rootId: String, configuration: Map<String, Any>): List<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAll(): Iterable<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAll(pagination: Pageable): Iterable<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findById(statementId: StatementId): Optional<GeneralStatement> =
        Optional.of(statements.single { it.id == statementId })

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        TODO("Not yet implemented")
    }
}
