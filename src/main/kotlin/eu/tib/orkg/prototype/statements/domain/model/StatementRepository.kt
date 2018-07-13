package eu.tib.orkg.prototype.statements.domain.model

interface StatementRepository {

    fun findAll(): Iterable<Statement>

    fun findById(statementId: Long): Statement

    fun findBySubject(resourceId: ResourceId): Iterable<Statement>

    fun findByPredicate(predicateId: PredicateId): Iterable<Statement>

    fun add(statement: Statement)

    fun nextIdentity(): Long
}
