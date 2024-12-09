package org.orkg.graph.adapter.output.neo4j

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.adapter.output.facade.ListAdapter
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.listRepositoryContract
import org.orkg.testing.Neo4jContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.cache.annotation.EnableCaching
import org.springframework.test.context.ContextConfiguration

@EnableAutoConfiguration
@DataNeo4jTest
@EnableCaching
@ContextConfiguration(
    classes = [
        ListAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jThingAdapter::class,
        GraphNeo4jConfiguration::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
internal class SpringDataNeo4jListAdapterContractTest(
    @Autowired private val listAdapter: ListRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository
) : DescribeSpec({
    include(
        listRepositoryContract(
            listAdapter,
            springDataNeo4jResourceAdapter,
            springDataNeo4jPredicateAdapter,
            springDataNeo4jStatementAdapter
        )
    )

    finalizeSpec {
        springDataNeo4jResourceAdapter.deleteAll()
        springDataNeo4jStatementAdapter.deleteAll()
        springDataNeo4jPredicateAdapter.deleteAll()
    }
})
