package eu.tib.orkg.prototype.statements.infrastructure

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Statement
import eu.tib.orkg.prototype.statements.domain.model.StatementRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Primary
class InMemoryStatementRepository : StatementRepository {
    private val statements: MutableSet<Statement> =
        TreeSet()

    private var counter: Long = 0

    override fun findAll(): Iterable<Statement> {
        return statements.toSet()
    }

    override fun findById(statementId: Long): Statement =
        statements.first { it.statementId == statementId }

    override fun findBySubject(resourceId: ResourceId) =
        statements.filter { it.subject == resourceId }

    override fun findByPredicate(predicateId: PredicateId) =
        statements.filter { it.predicate == predicateId }

    override fun add(statement: Statement) {
        statements.add(statement)
    }

    override fun nextIdentity(): Long = ++counter
}
