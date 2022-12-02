package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepositoryContractTest
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class SpringDataNeo4jPredicateAdapterContractTests : Neo4jTestContainersBaseTest(), PredicateRepositoryContractTest {

    @Autowired
    private lateinit var adapter: SpringDataNeo4jPredicateAdapter

    @Autowired
    private lateinit var statementAdapter: SpringDataNeo4jStatementAdapter

    @Autowired
    private lateinit var resourceAdapter: SpringDataNeo4jResourceAdapter

    override val repository: PredicateRepository
        get() = adapter

    override val statementRepository: StatementRepository
        get() = statementAdapter

    override val resourceRepository: ResourceRepository
        get() = resourceAdapter

    override fun cleanUpAfterEach() {
        repository.deleteAll()
        statementRepository.deleteAll()
        resourceRepository.deleteAll()
    }
}
