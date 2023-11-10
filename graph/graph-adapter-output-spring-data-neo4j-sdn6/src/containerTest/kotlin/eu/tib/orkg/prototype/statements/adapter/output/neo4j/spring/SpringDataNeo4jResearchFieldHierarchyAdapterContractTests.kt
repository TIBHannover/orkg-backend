package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import eu.tib.orkg.prototype.testing.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResearchFieldHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.researchFieldHierarchyRepositoryContract
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@EnableCaching
@ContextConfiguration(
    classes = [
        SpringDataNeo4jResearchFieldHierarchyAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(value = [Neo4jConfiguration::class])
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jResearchFieldHierarchyAdapterContractTests(
    @Autowired private val springDataNeo4jResearchFieldHierarchyAdapter: ResearchFieldHierarchyRepository,
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository
) : DescribeSpec({
    include(
        researchFieldHierarchyRepositoryContract(
            springDataNeo4jResearchFieldHierarchyAdapter,
            springDataNeo4jStatementAdapter,
            springDataNeo4jResourceAdapter,
            springDataNeo4jPredicateAdapter
        )
    )

    finalizeSpec {
        springDataNeo4jStatementAdapter.deleteAll()
        springDataNeo4jResourceAdapter.deleteAll()
        springDataNeo4jPredicateAdapter.deleteAll()
    }
})
