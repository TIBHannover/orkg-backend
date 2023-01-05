package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.configuration.CacheConfiguration
import eu.tib.orkg.prototype.configuration.Neo4jConfiguration
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepositoryContractTest
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.predicateRepositoryContract
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
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

@DataNeo4jTest
@ContextConfiguration(classes = [SpringDataNeo4jPredicateAdapter::class], initializers = [Neo4jContainerInitializer::class])
@Import(Neo4jConfiguration::class, CacheConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jPredicateAdapterContractTestsKotest(
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
) : DescribeSpec({
    include(predicateRepositoryContract(springDataNeo4jPredicateAdapter))
})
