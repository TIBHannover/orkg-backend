package org.orkg.contenttypes.adapter.output.neo4j

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.contenttypes.adapter.output.neo4j.configuration.ContentTypesNeo4jConfiguration
import org.orkg.contenttypes.output.RankingService
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.contenttypes.output.testing.fixtures.rankingServiceContract
import org.orkg.graph.adapter.output.facade.ListAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jClassAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jLiteralAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jPredicateAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jResourceAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jStatementAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jThingAdapter
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.testing.Neo4jContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.cache.annotation.EnableCaching
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@EnableCaching
@ContextConfiguration(
    classes = [
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jThingAdapter::class,
        ListAdapter::class,
        SpringDataNeo4jRosettaStoneStatementAdapter::class,
        SpringDataNeo4jRankingServiceAdapter::class,
        GraphNeo4jConfiguration::class,
        ContentTypesNeo4jConfiguration::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
internal class SpringDataNeo4jRankingServiceAdapterContractTests(
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository,
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val listAdapter: ListRepository,
    @Autowired private val springDataNeo4jRosettaStoneStatementAdapter: RosettaStoneStatementRepository,
    @Autowired private val springDataNeo4jRankingServiceAdapter: RankingService
) : DescribeSpec({
    include(
        rankingServiceContract(
            springDataNeo4jStatementAdapter,
            springDataNeo4jClassAdapter,
            springDataNeo4jLiteralAdapter,
            springDataNeo4jResourceAdapter,
            springDataNeo4jPredicateAdapter,
            listAdapter,
            springDataNeo4jRosettaStoneStatementAdapter,
            springDataNeo4jRankingServiceAdapter
        )
    )
})
