package eu.tib.orkg.prototype.statements.domain.model

interface StatementRepository {

    fun findAll(): Iterable<Statement>

    fun add(statement: Statement)
}
