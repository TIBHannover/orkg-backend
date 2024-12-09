package org.orkg.contenttypes.adapter.output.neo4j

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import io.kotest.core.spec.style.DescribeSpec
import org.orkg.contenttypes.adapter.output.neo4j.configuration.ContentTypesNeo4jConfiguration
import org.orkg.contenttypes.output.LiteratureListRepository
import org.orkg.contenttypes.output.testing.fixtures.literatureListRepositoryContract
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jClassAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jLiteralAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jPredicateAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jResourceAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jStatementAdapter
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.testing.Neo4jContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.test.context.ContextConfiguration

@EnableAutoConfiguration
@DataNeo4jTest
@ContextConfiguration(
    classes = [
        SpringDataNeo4jLiteratureListAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        GraphNeo4jConfiguration::class,
        ContentTypesNeo4jConfiguration::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
internal class SpringDataNeo4jLiteratureListAdapterContractTest(
    @Autowired private val springDataNeo4jLiteratureListAdapter: LiteratureListRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository
) : DescribeSpec({
    include(
        literatureListRepositoryContract(
            springDataNeo4jLiteratureListAdapter,
            springDataNeo4jStatementAdapter,
            springDataNeo4jClassAdapter,
            springDataNeo4jLiteralAdapter,
            springDataNeo4jResourceAdapter,
            springDataNeo4jPredicateAdapter
        )
    )
})