package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.IndexInitializer
import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.configuration.Neo4jConfiguration
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.predicateRepositoryContract
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@EnableCaching
@ContextConfiguration(
    classes = [
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(value = [Neo4jConfiguration::class, IndexInitializer::class])
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jPredicateAdapterContractTests(
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository
) : DescribeSpec({
    include(
        predicateRepositoryContract(
            springDataNeo4jPredicateAdapter,
            springDataNeo4jStatementAdapter,
            springDataNeo4jLiteralAdapter
        )
    )
})
