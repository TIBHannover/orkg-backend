package org.orkg.contenttypes.adapter.output.neo4j

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import io.kotest.core.spec.style.DescribeSpec
import org.orkg.contenttypes.adapter.output.neo4j.configuration.ContentTypesNeo4jConfiguration
import org.orkg.contenttypes.output.ResearchFieldHierarchyRepository
import org.orkg.contenttypes.output.testing.fixtures.researchFieldHierarchyRepositoryContract
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jPredicateAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jResourceAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jStatementAdapter
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.testing.Neo4jContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.cache.annotation.EnableCaching
import org.springframework.test.context.ContextConfiguration

@EnableAutoConfiguration
@DataNeo4jTest
@EnableCaching
@ContextConfiguration(
    classes = [
        SpringDataNeo4jResearchFieldHierarchyAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        GraphNeo4jConfiguration::class,
        ContentTypesNeo4jConfiguration::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
internal class SpringDataNeo4jResearchFieldHierarchyAdapterContractTest(
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
