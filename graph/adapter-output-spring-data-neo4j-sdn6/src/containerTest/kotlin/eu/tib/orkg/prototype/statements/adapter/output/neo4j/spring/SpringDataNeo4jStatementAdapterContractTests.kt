package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.statementRepositoryContract
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@Ignored("Workaround for Docker container issue active (\"all in one\"). Remove when solved.")
@DataNeo4jTest
@ContextConfiguration(
    classes = [
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jClassAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jStatementAdapterContractTests(
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository,
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository
) : DescribeSpec({
    include(
        statementRepositoryContract(
            springDataNeo4jStatementAdapter,
            springDataNeo4jClassAdapter,
            springDataNeo4jLiteralAdapter,
            springDataNeo4jResourceAdapter,
            springDataNeo4jPredicateAdapter
        )
    )
})
