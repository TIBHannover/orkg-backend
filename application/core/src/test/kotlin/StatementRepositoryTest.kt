package eu.tib.orkg.prototype.statements.ports

import org.junit.jupiter.api.DisplayName

@DisplayName("An in-memory StatementRepository")
class StatementRepositoryTest : StatementRepositoryContract {
    private val inMemoryStatementRepository = InMemoryStatementRepository()
    private val inMemoryResourceRepository = InMemoryResourceRepository()
    private val inMemoryPredicateRepository = InMemoryPredicateRepository()
    private val inMemoryLiteralRepository = InMemoryLiteralRepository()

    override val repositoryUnderTest: StatementRepository
        get() = inMemoryStatementRepository
    override val resourceRepository: ResourceRepository
        get() = inMemoryResourceRepository
    override val predicateRepository: PredicateRepository
        get() = inMemoryPredicateRepository
    override val literalRepository: LiteralRepository
        get() = inMemoryLiteralRepository
}
